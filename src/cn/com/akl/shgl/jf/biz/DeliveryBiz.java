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
     * ��ȡ�����к�.
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
     * ��ȡ���ϼ۸�.
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
     * ���뽻����¼.
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

        // ���Ѿ������˴����޼�¼�Ľ�����¼����ô�Ͳ������µĽ�����¼��.
        ArrayList<String> yjfhhList = DAOUtil.getStringCollection(conn, "SELECT DISTINCT YJFHH FROM BO_AKL_WXJF_S WHERE SXCPHID=? AND BINDID<>?", sxwljlid, bindid);
        // ��ѯ���ܻ���һ�����������������������к�.
        if (yjfhhList.size() > 0) {
            for (String yjfhh : yjfhhList) {
                dealOldDeliveryRecord(conn, bindid, uid, jfdh, hh, sxwljlid, yjfhh);
            }
        } else {

            String hwdm = sxHashtable.get("HWDM");
            String pch = sxHashtable.get("PCH");

            // ���в������.
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

            // �жϴ���ʽ
            String clfs = jfHashtable.get("CLFS");
            if (DeliveryConstant.CLFS_HX.equals(clfs)) {
                if (DeliveryConstant.YWLX_XS.equals(ywlx) || DeliveryConstant.YWLX_ZS.equals(ywlx)) {
                    // ����
                    replaceNew2(conn, bindid, uid, jfHashtable);
                } else {
                    // ����
                    replaceNew(conn, bindid, uid, jfHashtable);
                }
            } else if (DeliveryConstant.CLFS_TH.equals(clfs)) {
                // �˻�
                rollback(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_BNWX.equals(clfs)) {
                // ά��
                maintain(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_BWWX.equals(clfs)) {
                // ά��
                maintain2(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_DSH.equals(clfs)) {
                // ���ջ�
                MessageQueue.getInstance().putMessage(uid, "���ջ�����Ҫ���н���!");
                return;
            } else if (DeliveryConstant.CLFS_WSWGH.equals(clfs)) {
                // ��ʵ�ﻻ�£��뻻��һ������.
                replaceNew(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_XS.equals(clfs)) {
                // ���ۺ����͵Ĵ���ʽ.
                replaceNew2(conn, bindid, uid, jfHashtable);
            } else if (DeliveryConstant.CLFS_ZS.equals(clfs)) {
                // ���ۺ����͵Ĵ���ʽ.
                replaceNew2(conn, bindid, uid, jfHashtable);
            } else {
                throw new RuntimeException("�������޲�Ʒ��Ϣ���Ƿ���δ��д�������͵ļ�¼!");
            }

            String wlbh = jfHashtable.get("WLBH");
            String toSx = jfHashtable.get("SX");
            String tockdm = jfHashtable.get("CKDM");

            // ��ȡ�����Ϣ.
            // ��ȡ���.
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
     * ����ԭ������¼.
     * ��Ҫ�ǻ�ȡ���޼�¼δ����������ϣ����뵽��������¼��.
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
        // �Ѿ���������.
        if (count != null && count > 0) {
            MessageQueue.getInstance().putMessage(uid, "�����кţ�" + hh + "' ��Ӧ�Ľ����кţ�" + yjfhh + " �Ѿ�������������������!");
        } else {
            // ��ѯ��һ��������¼����ʲô״̬.
            String zdhh = DAOUtil.getStringOrNull(conn, "SELECT MAX(HH) FROM BO_AKL_WXJF_S WHERE YJFHH=? AND BINDID<>?", yjfhh, bindid);
            String zt = DAOUtil.getStringOrNull(conn, "SELECT ZT FROM BO_AKL_WXJF_S WHERE HH=?", zdhh);
            String sfjf = DAOUtil.getStringOrNull(conn, "SELECT SFJF FROM BO_AKL_WXJF_S WHERE HH=?", zdhh);
            // ֻ�д���ȱ����Ĳ��ܽ��д���.
            if (DeliveryConstant.JF_JLZT_YTZ.equals(zt) || DeliveryConstant.JF_JLZT_YJF.equals(zt)) {
                // ֻ���Ƿ񽻸�Ϊ��Ŀ��Դ���.
                if (XSDDConstant.NO.equals(sfjf)) {
                    // ��֤�ӱ��Ƿ��Ѿ�������ԭ�кš�
                    count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE YJFHH=? AND BINDID=?", yjfhh, bindid);
                    if (count == null || count == 0) {
                        // ͨ���кŻ�ȡ��¼�������뵱ǰ��������.
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

                        // ��ʼ���к���Ϣ.
                        String rowNum = getJFRowNum(conn, bindid, jfdh);
                        hashtable.put("HH", rowNum);
                        hashtable.put("YJFHH", yjfhh);
                        hashtable.put("SCJFHH", zdhh);
                        hashtable.put("SFJF", XSDDConstant.YES);
                        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_WXJF_S", hashtable, bindid, uid);
                        MessageQueue.getInstance().putMessage(uid, "�����кţ�'" + hh + " ��Ӧ�Ľ����кţ�" + yjfhh + " �Ѿ���ԭ���������������!");
                    }
                } else {
                    // �ѽ�����������.
                    MessageQueue.getInstance().putMessage(uid, "�����кţ�'" + hh + " ��Ӧ�Ľ����кţ�" + yjfhh + " �ѽ��������½����к�Ϊ��" + zdhh + "��");
                }
            } else {
                // ���ڱ���������������, ��������.
                MessageQueue.getInstance().putMessage(uid, "�����кţ�'" + hh + " ��Ӧ�Ľ����кţ�" + yjfhh + " ���ڱ��������ݴ������½����к�Ϊ��" + zdhh + "!");
            }

        }
    }

    /**
     * ���´���. �ҳ����϶�Ӧ�滻���ϣ����п��ķ���ȥ����û�п��Ĭ���滻�Լ�.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param hashtable �ӱ�����
     * @return
     * @throws SQLException
     * @throws AWSSDKException
     */
    public Hashtable<String, String> replaceNew(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException,
            AWSSDKException {

        // �������ͺ����ۣ�ֱ���г���ǰ���ϵ��滻����.

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
            // ƴ�Ӵ���������.
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
            throw new RuntimeException("�޷�ƥ�䵽��Ӧ����!");
        }
        hashtable.put("SX", sx);

        // �ҵ��п�������.
        for (String reWlbh : replaceWlbhList) {
            if (repositoryBiz.autoFetchNoLock(conn, bindid, uid, xmlb, reWlbh, xh, ckdm, sx, 1, hashtable)) {
                return hashtable;
            }
        }

        // ��ʾ��Ϣ�������������.
        MessageQueue.getInstance().putMessage(uid, "���滻������û�п��!");
        // ����Ĭ������.
        repositoryBiz.fillDeliveryMaterialInfo(conn, hashtable);
        // �����滻���ϴ�����У�������Ĭ������.
        return hashtable;
    }

    /**
     * ����������ʱ����Ļ��·�ʽ.
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
            throw new RuntimeException("�޷�ƥ�䵽��Ӧ����!");
        }
        hashtable.put("SX", sx);
        return hashtable;
    }

    /**
     * ��ȡ�����Ϣ.
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
                    MessageQueue.getInstance().putMessage(uid, "�����" + pjCpmc + "��������!");
                }
                vector.add(hashtable);
            }

            return vector;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * ����ʽΪ���˻� ʱ�Ĵ���.
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
     * ����ά�޴���.
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
            throw new RuntimeException("�޷�ƥ�䵽��Ӧ����!");
        }
        hashtable.put("SX", sx);
        hashtable.put("JFSN", PrintUtil.parseNull(hashtable.get("SN")));
        // ά����������� StepNo1BeforeSave�н��д�����Ϊÿһ�α��涼��Ҫ���¼���һ���������.
        return hashtable;
    }

    /**
     * ����ά�޴���.
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
            throw new RuntimeException("�޷�ƥ�䵽��Ӧ����!");
        }
        hashtable.put("SX", sx);
        hashtable.put("JFSN", PrintUtil.parseNull(hashtable.get("SN")));
        // ά����������� StepNo1BeforeSave�н��д�����Ϊÿһ�α��涼��Ҫ���¼���һ���������.
        return hashtable;
    }

    /**
     * ��ȡ��ǰӦ��������.
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
     * ��ȡ��������.
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
     * ����ȱ����¼<br/>
     * 1�������ֶ� BO_AKL_SX_P,BO_AKL_WXJF_S -> BO_AKL_QHJL
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
            // ��֮ǰû��ȱ����¼���������ȱ����¼����.
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
     * 1�������������Ͻ����������.<br/>
     * 2�����˻غ�ά�޵����Ͻ����������.<br/>
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
        // ���µĲ�Ʒ��������.
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
                throw new RuntimeException("�ͺţ�" + xh + " �ڻ�λ��" + hwdm + "���������㣡");
            }
        }
    }

    /**
     * �����ֶ� BO_AKL_WXJF_SX_S -> BO_AKL_WXJF_S
     *
     * @param hashtable
     * @param copyHashtable
     */
    public void copyFieldSXToJF(Hashtable<String, String> hashtable, Hashtable<String, String> copyHashtable) {
        // ����Ĭ�����սر�����
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
     * �����ֶ�.
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
