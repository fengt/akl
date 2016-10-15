package cn.com.akl.shgl.jf.biz;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DeliveryBiz {

    RepositoryBiz repositoryBiz = new RepositoryBiz();

    /**
     * 获取交付行号.
     *
     * @param conn
     * @param bindid
     * @return
     * @throws SQLException
     */
    public static String getJFRowNum(Connection conn, int bindid, String jfdh) throws SQLException {
        Integer rowNum = DAOUtil.getIntOrNull(conn, "SELECT ISNULL(MAX(CONVERT(INT, SUBSTRING(HH,16,19))),0)+1 FROM BO_AKL_WXJF_S WHERE BINDID=?", bindid);
        StringBuilder jfrow = new StringBuilder(20);
        if (rowNum == null) {
            return jfrow.append(jfdh).append("-").append(1).toString();
        } else {
            return jfrow.append(jfdh).append("-").append(rowNum).toString();
        }
    }

    /**
     * 获取物料价格.
     *
     * @param conn
     * @param xmlb
     * @param wlbh
     * @return
     * @throws SQLException
     */
    public static BigDecimal getMatiralPrice(Connection conn, String xmlb, String wlbh) throws SQLException {
        return DAOUtil.getBigDecimalOrNull(conn, DeliveryConstant.QUERY_WL_JG, xmlb, wlbh);
    }

    /**
     * 插入交付记录.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param sxHashtable
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertDeliveryRecord(Connection conn, int bindid, String uid, Hashtable<String, String> sxHashtable) throws SQLException,
            AWSSDKException {
        String jfdh = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_JFDH, bindid);
        String ckdm = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_BDCKDM, bindid);
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String ywlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YWLX, bindid);

        String hh = sxHashtable.get("HH");
        String sxwljlid = sxHashtable.get("SXWLJLID");
        String sxwlbh = sxHashtable.get("WLBH");
        String sxpn = sxHashtable.get("XH");
        String sx = sxHashtable.get("SX");
        String sxwlmc = sxHashtable.get("WLMC");

        // 若已经出现了此送修记录的交付记录，那么就不生成新的交付记录了.
        ArrayList<String> yjfhhList = DAOUtil.getStringCollection(conn, "SELECT DISTINCT YJFHH FROM BO_AKL_WXJF_S WHERE SXCPHID=? AND BINDID<>?", sxwljlid, bindid);
        // 查询可能会有一换多的情况，会产生多个交付行号.
        if (yjfhhList.size() > 0) {
            for (String yjfhh : yjfhhList) {
                dealOldDeliveryRecord(conn, bindid, uid, jfdh, hh, sxwljlid, yjfhh);
            }
        } else {

            String hwdm = sxHashtable.get("HWDM");
            String pch = sxHashtable.get("PCH");

            // 进行插入操作.
            Hashtable<String, String> jfHashtable = new Hashtable<String, String>();
            // init defualt
            jfHashtable.put("SXCPHH", hh);
            jfHashtable.put("SXCPHID", sxwljlid);
            jfHashtable.put("SL", "1");
            jfHashtable.put("SFSJ", XSDDConstant.NO);
            jfHashtable.put("SFQHSQ", XSDDConstant.NO);
            jfHashtable.put("CKDM", ckdm);
            jfHashtable.put("SXWLBH", PrintUtil.parseNull(sxwlbh));
            jfHashtable.put("SXWLPN", PrintUtil.parseNull(sxpn));
            jfHashtable.put("SXWLMC", PrintUtil.parseNull(sxwlmc));
            jfHashtable.put("SFJF", XSDDConstant.YES);
            jfHashtable.put("CLYJ", XSDDConstant.NO);
            jfHashtable.put("HWDM", PrintUtil.parseNull(hwdm));
            jfHashtable.put("PCH", PrintUtil.parseNull(pch));
            jfHashtable.put("SX", PrintUtil.parseNull(sx));

            String rowNum = getJFRowNum(conn, bindid, jfdh);
            jfHashtable.put("HH", rowNum);
            jfHashtable.put("YJFHH", rowNum);

            copyFieldSXToJF(sxHashtable, jfHashtable);

            // 判断处理方式
            String clfs = jfHashtable.get("CLFS");
            if (DeliveryConstant.CLFS_HX.equals(clfs)) {
                if (DeliveryConstant.YWLX_XS.equals(ywlx) || DeliveryConstant.YWLX_ZS.equals(ywlx)) {
                    // 换新
                    replaceNew2(conn, bindid, uid, jfHashtable);
                } else {
                    // 换新
                    replaceNew(conn, bindid, uid, jfHashtable);
                }
            } else if (DeliveryConstant.CLFS_TH.equals(clfs)) {
                // 退回
                rollback(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_BNWX.equals(clfs)) {
                // 维修
                maintain(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_BWWX.equals(clfs)) {
                // 维修
                maintain2(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_DSH.equals(clfs)) {
                // 代收货
                MessageQueue.getInstance().putMessage(uid, "代收货不需要进行交付!");
                return;
            } else if (DeliveryConstant.CLFS_WSWGH.equals(clfs)) {
                // 无实物换新，与换新一样处理.
                replaceNew(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_XS.equals(clfs)) {
                // 销售和赠送的处理方式.
                replaceNew2(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_ZS.equals(clfs)) {
                // 销售和赠送的处理方式.
                replaceNew2(conn, bindid, uid, jfHashtable);
            } else {
                throw new RuntimeException("请检查送修产品信息中是否有未填写处理类型的记录!");
            }

            String wlbh = jfHashtable.get("WLBH");
            String toSx = jfHashtable.get("SX");
            String tockdm = jfHashtable.get("CKDM");

            // 获取库存信息.
            // 获取库存.
            int ckkysl = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, tockdm, toSx);
            int zbkysl = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, DfhConstant.ZBKFBM, toSx);
            Integer qhsl = DAOUtil.getIntOrNull(conn, "SELECT SUM (SL) QHSQSL FROM BO_AKL_QHJL WHERE ZT <> '076278' AND ZT <> '076349' AND WLBH=? AND SX=? AND XMLB=?", wlbh, sx, xmlb);
            if (qhsl == null) {
                qhsl = 0;
            }
            jfHashtable.put("BDKFCKKC", String.valueOf(ckkysl));
            jfHashtable.put("ZBCKKC", String.valueOf(zbkysl - qhsl));

            BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_WXJF_S", jfHashtable, bindid, uid);
        }
    }

    /**
     * 处理原交付记录.
     * 主要是获取送修记录未交付完的物料，带入到本交付记录中.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param jfdh
     * @param hh
     * @param sxwljlid
     * @param yjfhh
     * @throws SQLException
     * @throws AWSSDKException
     */
    private void dealOldDeliveryRecord(Connection conn, int bindid, String uid, String jfdh, String hh, String sxwljlid, String yjfhh) throws SQLException, AWSSDKException {
        Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE YJFHH=? AND SFJF=? AND ZT IN (?,?)", yjfhh, XSDDConstant.YES, DeliveryConstant.JF_JLZT_YTZ, DeliveryConstant.JF_JLZT_YJF);
        // 已经被交付了.
        if (count != null && count > 0) {
            MessageQueue.getInstance().putMessage(uid, "送修行号：" + hh + "' 对应的交付行号：" + yjfhh + " 已经被其他交付单交付了!");
        } else {
            // 查询上一个交付记录处于什么状态.
            String zdhh = DAOUtil.getStringOrNull(conn, "SELECT MAX(HH) FROM BO_AKL_WXJF_S WHERE YJFHH=? AND BINDID<>?", yjfhh, bindid);
            String zt = DAOUtil.getStringOrNull(conn, "SELECT ZT FROM BO_AKL_WXJF_S WHERE HH=?", zdhh);
            String sfjf = DAOUtil.getStringOrNull(conn, "SELECT SFJF FROM BO_AKL_WXJF_S WHERE HH=?", zdhh);
            // 只有处于缺货后的才能进行处理.
            if (DeliveryConstant.JF_JLZT_YTZ.equals(zt) || DeliveryConstant.JF_JLZT_YJF.equals(zt)) {
                // 只有是否交付为否的可以处理.
                if (XSDDConstant.NO.equals(sfjf)) {
                    // 验证子表是否已经出现了原行号。
                    count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE YJFHH=? AND BINDID=?", yjfhh, bindid);
                    if (count == null || count == 0) {
                        // 通过行号获取记录，并存入当前交付表中.
                        Map<String, Object> rowRecord = DAOUtil.getRowRecord(conn, "SELECT * FROM BO_AKL_WXJF_S WHERE HH=?", zdhh);
                        if (rowRecord == null) {
                            throw new RuntimeException("");
                        }
                        Hashtable hashtable = new Hashtable();
                        Set<Map.Entry<String, Object>> entries = rowRecord.entrySet();
                        for (Map.Entry<String, Object> entry : entries) {
                            Object val = entry.getValue();
                            if (val == null) {
                                val = "";
                            }
                            hashtable.put(entry.getKey(), val);
                        }

                        // 初始化行号信息.
                        String rowNum = getJFRowNum(conn, bindid, jfdh);
                        hashtable.put("HH", rowNum);
                        hashtable.put("YJFHH", yjfhh);
                        hashtable.put("SCJFHH", zdhh);
                        hashtable.put("SFJF", XSDDConstant.YES);
                        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_WXJF_S", hashtable, bindid, uid);
                        MessageQueue.getInstance().putMessage(uid, "送修行号：'" + hh + " 对应的交付行号：" + yjfhh + " 已经由原交付单引入过来了!");
                    }
                } else {
                    // 已交付，不可用.
                    MessageQueue.getInstance().putMessage(uid, "送修行号：'" + hh + " 对应的交付行号：" + yjfhh + " 已交付，最新交付行号为：" + zdhh + "！");
                }
            } else {
                // 正在被其他交付单处理, 不可引用.
                MessageQueue.getInstance().putMessage(uid, "送修行号：'" + hh + " 对应的交付行号：" + yjfhh + " 正在被其他单据处理，最新交付行号为：" + zdhh + "!");
            }

        }
    }

    /**
     * 换新处理. 找出物料对应替换物料，将有库存的放上去，若没有库存默认替换自己.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param hashtable 子表数据
     * @return
     * @throws SQLException
     * @throws AWSSDKException
     */
    public Hashtable<String, String> replaceNew(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException,
            AWSSDKException {

        // 若是赠送和销售，直接列出当前物料的替换方法.

        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String khlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_KHLX, bindid);
        String ywlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YWLX, bindid);
        String sx = getOutMaterialAttribute(conn, xmlb, DeliveryConstant.CLFS_HX, khlx, ywlx);

        String wlbh = hashtable.get("WLBH");
        String ckdm = hashtable.get("CKDM");
        String xh = hashtable.get("XH");

        ReplacementRuleBiz ruleBiz = new ReplacementRuleBiz();
        List<String> replaceWlbhList = ruleBiz.replaceMaterial(conn, xmlb, wlbh, sx);

        if (replaceWlbhList.size() != 0) {
            // 拼接待处理物料.
            StringBuilder replaceWlbhSb = new StringBuilder(50);
            replaceWlbhSb.append(replaceWlbhList.get(0));
            for (int i = 1; i < replaceWlbhList.size(); i++) {
                replaceWlbhSb.append(",");
                replaceWlbhSb.append(replaceWlbhList.get(i));
            }
            hashtable.put("KTHWLBH", replaceWlbhSb.toString());
        } else {
            hashtable.put("KTHWLBH", wlbh);
        }

        if (sx == null) {
            throw new RuntimeException("无法匹配到对应属性!");
        }
        hashtable.put("SX", sx);

        // 找到有库存的物料.
        for (String reWlbh : replaceWlbhList) {
            if (repositoryBiz.autoFetchNoLock(conn, bindid, uid, xmlb, reWlbh, xh, ckdm, sx, 1, hashtable)) {
                return hashtable;
            }
        }

        // 提示消息，库存数量不足.
        MessageQueue.getInstance().putMessage(uid, "可替换的物料没有库存!");
        // 设置默认物料.
        repositoryBiz.fillDeliveryMaterialInfo(conn, hashtable);
        // 将可替换物料存入表单中，并设置默认物料.
        return hashtable;
    }

    /**
     * 赠送与销售时特殊的换新方式.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param hashtable
     * @return
     * @throws SQLException
     */
    public Hashtable<String, String> replaceNew2(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String ywlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YWLX, bindid);
        String sx = getOutMaterialAttribute(conn, xmlb, DeliveryConstant.CLFS_HX, "", ywlx);
        String wlbh = hashtable.get("WLBH");
        hashtable.put("KTHWLBH", wlbh);
        if (sx == null) {
            throw new RuntimeException("无法匹配到对应属性!");
        }
        hashtable.put("SX", sx);
        return hashtable;
    }

    /**
     * 获取配件信息.
     *
     * @param conn
     * @param wlbh
     * @param wxbw
     * @return
     * @throws SQLException
     * @throws AWSSDKException
     */
    public Vector<Hashtable<String, String>> getPartMaterial(Connection conn, int bindid, String uid, String xmlb, String wlbh, String ckdm,
                                                             String wxbw) throws SQLException, AWSSDKException {
        Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_PJXX);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, wlbh, wxbw);
            while (reset.next()) {
                String pjWlbh = reset.getString("PJCPBH");
                String pjCpxh = reset.getString("PJCPXH");
                String pjCpmc = reset.getString("PJCPMC");
                int pjXhsl = reset.getInt("PJXHSL");

                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                hashtable.put("WLBH", PrintUtil.parseNull(pjWlbh));
                hashtable.put("XH", PrintUtil.parseNull(pjCpxh));
                hashtable.put("MC", PrintUtil.parseNull(pjCpmc));
                hashtable.put("SL", String.valueOf(pjXhsl));
                hashtable.put("SHSL", String.valueOf(pjXhsl));
                hashtable.put("SX", PrintUtil.parseNull(DeliveryConstant.WLSX_XP));
                hashtable.put("CKDM", PrintUtil.parseNull(ckdm));

                if (!repositoryBiz.autoFetchNoLock(conn, bindid, uid, xmlb, pjWlbh, pjCpxh, ckdm, DeliveryConstant.WLSX_XP, pjXhsl, hashtable)) {
                    hashtable.put("HWDM", "");
                    hashtable.put("PCH", "");
                    MessageQueue.getInstance().putMessage(uid, "配件：" + pjCpmc + "数量不足!");
                }
                vector.add(hashtable);
            }

            return vector;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 处理方式为：退回 时的处理.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param hashtable
     * @return
     * @throws SQLException
     */
    public Hashtable<String, String> rollback(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException {
        String wlbh = hashtable.get("WLBH");
        hashtable.put("KTHWLBH", wlbh);
        //String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        //String ywlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YWLX, bindid);
        //String sx = getOutMaterialAttribute(conn, xmlb, DeliveryConstant.CLFS_TH, khlx);
        //hashtable.put("SX", sx);
        hashtable.put("JFSN", PrintUtil.parseNull(hashtable.get("SN")));
        repositoryBiz.fillDeliveryMaterialInfo(conn, hashtable);
        return hashtable;
    }

    /**
     * 保内维修处理.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param hashtable
     * @return
     * @throws SQLException
     */
    public Hashtable<String, String> maintain(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String ywlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YWLX, bindid);
        String sx = getOutMaterialAttribute(conn, xmlb, DeliveryConstant.CLFS_BNWX, "", ywlx);
        String wlbh = hashtable.get("WLBH");
        hashtable.put("KTHWLBH", wlbh);
        if (sx == null) {
            throw new RuntimeException("无法匹配到对应属性!");
        }
        hashtable.put("SX", sx);
        hashtable.put("JFSN", PrintUtil.parseNull(hashtable.get("SN")));
        // 维修配件插入在 StepNo1BeforeSave中进行处理，因为每一次保存都需要重新计算一次所有配件.
        return hashtable;
    }

    /**
     * 保外维修处理.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param hashtable
     * @return
     * @throws SQLException
     */
    public Hashtable<String, String> maintain2(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String ywlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YWLX, bindid);
        String sx = getOutMaterialAttribute(conn, xmlb, DeliveryConstant.CLFS_BWWX, "", ywlx);
        String wlbh = hashtable.get("WLBH");
        hashtable.put("KTHWLBH", wlbh);
        if (sx == null) {
            throw new RuntimeException("无法匹配到对应属性!");
        }
        hashtable.put("SX", sx);
        hashtable.put("JFSN", PrintUtil.parseNull(hashtable.get("SN")));
        // 维修配件插入在 StepNo1BeforeSave中进行处理，因为每一次保存都需要重新计算一次所有配件.
        return hashtable;
    }

    /**
     * 获取当前应换出属性.
     *
     * @param conn
     * @param xmlx
     * @param clfs
     * @param khlx
     * @return
     * @throws SQLException
     */
    public static String getOutMaterialAttribute(Connection conn, String xmlx, String clfs, String khlx, String ywlx) throws SQLException {
        return DAOUtil.getStringOrNull(conn, "SELECT HCSX FROM BO_AKL_SH_YWSXGX WHERE XMLB=? AND CLFS=? AND YWLX=?", xmlx, clfs, ywlx);
    }

    /**
     * 获取换入属性.
     *
     * @param conn
     * @param xmlx
     * @param clfs
     * @param khlx
     * @return
     * @throws SQLException
     */
    public static String getInMaterialAttribute(Connection conn, String xmlx, String clfs, String khlx, String ywlx) throws SQLException {
        return DAOUtil.getStringOrNull(conn, "SELECT SRSX FROM BO_AKL_SH_YWSXGX WHERE XMLB=? AND CLFS=? AND YWLX=?", xmlx, clfs, ywlx);
    }

    /**
     * 插入缺货记录<br/>
     * 1、复制字段 BO_AKL_SX_P,BO_AKL_WXJF_S -> BO_AKL_QHJL
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertShortageOfMaterials(Connection conn, ResultSet reset, int bindid, String uid, Hashtable<String, String> jf) throws SQLException, AWSSDKException {
        String sfqhsq = reset.getString("SFQHSQ");
        if (XSDDConstant.YES.equals(sfqhsq)) {
            Integer qhjlCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_QHJL WHERE JFCPHH=?", jf.get("YJFHH"));
            // 若之前没有缺货记录申请则进行缺货记录插入.
            if (qhjlCount == null || qhjlCount == 0) {
                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                hashtable.put("SXDH", PrintUtil.parseNull(jf.get("SXDH")));
                hashtable.put("XMLB", PrintUtil.parseNull(jf.get("XMLB")));
                hashtable.put("KHLX", PrintUtil.parseNull(jf.get("KHLX")));
                hashtable.put("DH", PrintUtil.parseNull(jf.get("DH")));
                hashtable.put("JFKFBM", PrintUtil.parseNull(jf.get("YDJFKFBM")));
                hashtable.put("JFKFMC", PrintUtil.parseNull(jf.get("YDJFKF")));
                hashtable.put("SL", PrintUtil.parseNull(reset.getString("SL")));
                hashtable.put("SX", PrintUtil.parseNull(reset.getString("SX")));
                hashtable.put("JFCPHH", PrintUtil.parseNull(reset.getString("HH")));
                hashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
                hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
                hashtable.put("PN", PrintUtil.parseNull(reset.getString("XH")));
                hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
                hashtable.put("SXCPHH", PrintUtil.parseNull(reset.getString("SXCPHH")));
                hashtable.put("ZT", QHSQCnt.zt3);
                hashtable.put("YXJ", "0");
                hashtable.put("QHFS", QHSQCnt.bhlx0);
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHJL", hashtable, bindid, uid);
            }
        }
    }

    /**
     * 1、将交付的物料进行锁库插入.<br/>
     * 2、将退回和维修的物料进行锁库插入.<br/>
     *
     * @param conn
     * @param reset
     * @param bindid
     * @param uid
     * @param xmlb
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void fetchAndLockMaterialFQHSQ(Connection conn, ResultSet reset, int bindid, String uid, String xmlb) throws SQLException, AWSSDKException {
        // 换新的产品插入锁库.
        String wlbh = reset.getString("WLBH");
        String xh = reset.getString("XH");
        String sx = reset.getString("SX");
        String ckdm = reset.getString("CKDM");
        String pch = reset.getString("PCH");
        String hwdm = reset.getString("HWDM");
        int sl = reset.getInt("SL");
        int id = reset.getInt("ID");
        String sfqhsq = reset.getString("SFQHSQ");
        String clfs = reset.getString("CLFS");
        if (XSDDConstant.NO.equals(sfqhsq)) {
            boolean isSuccess = repositoryBiz.autoFetch1(conn, bindid, uid, xmlb, wlbh, xh, ckdm, sx, 1, "UPDATE BO_AKL_WXJF_S SET PCH=?,HWDM=? WHERE ID=?", id);
            if (!isSuccess) {
                throw new RuntimeException("型号：" + xh + " 在货位：" + hwdm + "上数量不足！");
            }
        }
    }

    /**
     * 复制字段 BO_AKL_WXJF_SX_S -> BO_AKL_WXJF_S
     *
     * @param hashtable
     * @param copyHashtable
     */
    public void copyFieldSXToJF(Hashtable<String, String> hashtable, Hashtable<String, String> copyHashtable) {
        // 设置默认最终截保日期
        String zbjzrq = hashtable.get("ZBJZRQ");
        if (zbjzrq == null || "".equals(zbjzrq)) {
            copyField(hashtable, copyHashtable, "ZZJBRQ", "ZZJBRQ");
        } else {
            copyField(hashtable, copyHashtable, "ZZJBRQ", "ZBJZRQ");
        }

        // copy
        copyField(hashtable, copyHashtable, "WLBH", "WLBH");
        copyField(hashtable, copyHashtable, "WLMC", "WLMC");
        copyField(hashtable, copyHashtable, "XH", "XH");
        copyField(hashtable, copyHashtable, "JG", "JG");
        copyField(hashtable, copyHashtable, "ZT", "ZT");
        copyField(hashtable, copyHashtable, "GZTM", "GZTM");
        copyField(hashtable, copyHashtable, "YKTMH", "YKTMH");
        copyField(hashtable, copyHashtable, "GZYY", "GZYY");
        copyField(hashtable, copyHashtable, "GZMS", "GZMS");
        // copyField(hashtable, copyHashtable, "SFSJ", "SFSJ");
        // copyField(hashtable, copyHashtable, "SJLX", "SJLX");
        // copyField(hashtable, copyHashtable, "SJMS", "SJMS");
        // copyField(hashtable, copyHashtable, "PFJG", "PFJG");
        // copyField(hashtable, copyHashtable, "SFTP", "SFTP");
        // copyField(hashtable, copyHashtable, "TPH", "TPH");
        // copyField(hashtable, copyHashtable, "PFMS", "PFMS");
        // copyField(hashtable, copyHashtable, "CLYJ", "CLYJ");
        copyField(hashtable, copyHashtable, "SN", "SN");
        copyField(hashtable, copyHashtable, "ZBJZRQ", "ZBJZRQ");
        copyField(hashtable, copyHashtable, "ZBYY", "ZBYY");
        copyField(hashtable, copyHashtable, "CLFS", "CLFS");
        copyField(hashtable, copyHashtable, "HWDM", "HWDM");
        // copyField(hashtable, copyHashtable, "FJ", "FJ");
    }

    /**
     * 复制字段.
     *
     * @param hashtable
     * @param copyHashtable
     * @param field
     * @param copyField
     */
    public void copyField(Hashtable<String, String> hashtable, Hashtable<String, String> copyHashtable, String field, String copyField) {
        String value = hashtable.get(field);
        copyHashtable.put(copyField, value == null ? "" : value);
    }

}
