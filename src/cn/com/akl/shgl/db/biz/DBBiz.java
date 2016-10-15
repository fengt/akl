package cn.com.akl.shgl.db.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.dfh.biz.DfhBiz;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.kc.biz.RepositoryConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DBBiz {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    /**
     * 删除锁库.
     *
     * @param conn
     * @param bindid
     */
    public void removeLock(Connection conn, int bindid) {
        repositoryBiz.removeLock(conn, bindid);
    }

    /**
     * 将汇总插入明细.
     *
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertHzToMx(Connection conn, int bindid, String uid, String xmlb) throws SQLException, AWSSDKException {

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_HZ_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String ckckdm = reset.getString("CKCKDM");
                int cksl = reset.getInt("CKSL");
                String cpsx = reset.getString("CPSX");

                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                copyFieldHzToMx(hashtable, reset);

                Vector<Hashtable<String, String>> wlVector = repositoryBiz.autoFetchs(conn, bindid, uid, xmlb, wlbh, xh, ckckdm, cpsx, cksl,
                        hashtable);
                for (Hashtable<String, String> hashtable2 : wlVector) {
                    String sl = hashtable2.get("SL");
                    String ckhwdm = hashtable2.get("HWDM");
                    hashtable2.put("CKHWDM", ckhwdm);
                    hashtable2.put("CKSL", sl);
                    hashtable2.put("SJCKSL", sl);
                }

                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_S", wlVector, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 将明细插入锁库.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertMXToLock(Connection conn, int bindid, String uid, String xmlx) throws SQLException, AWSSDKException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String wlmc = reset.getString("WLMC");
                String xh = reset.getString("XH");
                String ckhwdm = reset.getString("CKHWDM");
                String ckckdm = reset.getString("CKCKDM");
                String pch = reset.getString("PCH");
                int cksl = reset.getInt("CKSL");
                String cpsx = reset.getString("CPSX");
                String gztm = reset.getString("GZTM");

                /** 验证库存数量 */
                int haveSl = repositoryBiz.queryMaterialCanUse(conn, xmlx, wlbh, pch, ckhwdm, cpsx);
                if (cksl > haveSl) {
                    throw new RuntimeException("物料：" + wlmc + " 库存数量不足！");
                }

                /** 验证序列号，通过位置 */
                if (gztm != null && !"".equals(gztm.trim())) {
                    int haveXlh = repositoryBiz.queryXLHCount(conn, xmlx, wlbh, pch, ckhwdm, cpsx, gztm);
                    if (haveXlh == 0) {
                        throw new RuntimeException("序列号：" + gztm + " 不存在此货位上：" + ckhwdm + "！");
                    }
                }

                repositoryBiz.insertLock(conn, bindid, uid, xmlx, wlbh, xh, pch, ckckdm, ckhwdm, cpsx, cksl);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 复制字段从汇总到明细.
     *
     * @param hashtable
     * @param reset
     * @throws SQLException
     */
    public void copyFieldHzToMx(Hashtable<String, String> hashtable, ResultSet reset) throws SQLException {
        hashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
        hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
        hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
        hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
        hashtable.put("SJLH", PrintUtil.parseNull(reset.getString("SJLH")));
        hashtable.put("KCSL", PrintUtil.parseNull(reset.getString("KCSL")));
        hashtable.put("CPLX", PrintUtil.parseNull(reset.getString("CPLX")));
        hashtable.put("L9", PrintUtil.parseNull(reset.getString("L9")));
        hashtable.put("SJFHSL", PrintUtil.parseNull(reset.getString("SJFHSL")));
        hashtable.put("BFHYY", PrintUtil.parseNull(reset.getString("BFHYY")));
        hashtable.put("RKSL", PrintUtil.parseNull(reset.getString("RKSL")));
        hashtable.put("WLSL", PrintUtil.parseNull(reset.getString("WLSL")));
        hashtable.put("CKLX", PrintUtil.parseNull(reset.getString("CKLX")));
        hashtable.put("CPSX", PrintUtil.parseNull(reset.getString("CPSX")));
        hashtable.put("JG", PrintUtil.parseNull(reset.getString("JG")));
        hashtable.put("MS", PrintUtil.parseNull(reset.getString("MS")));
        hashtable.put("CKSL", PrintUtil.parseNull(reset.getString("CKSL")));
        hashtable.put("CKCKDM", PrintUtil.parseNull(reset.getString("CKCKDM")));
        hashtable.put("RKCKDM", PrintUtil.parseNull(reset.getString("RKCKDM")));
        hashtable.put("CKCKMC", PrintUtil.parseNull(reset.getString("CKCKMC")));
        hashtable.put("RKCKMC", PrintUtil.parseNull(reset.getString("RKCKMC")));
    }

    /**
     * 填充物料信息.
     *
     * @param reset
     * @param hashtable
     * @throws SQLException
     */
    public void mapMaterialInfo(ResultSet reset, String xmlx, Hashtable<String, String> hashtable) throws SQLException {
        hashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
        hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
        hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
        hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
        hashtable.put("PCH", PrintUtil.parseNull(reset.getString("PCH")));
        hashtable.put("CKDM", "");
        hashtable.put("CKMC", "");
        hashtable.put("QDM", "");
        hashtable.put("DDM", "");
        hashtable.put("KWDM", "");
        hashtable.put("HWDM", PrintUtil.parseNull(reset.getString("RKHWDM")));
        hashtable.put("KWSL", PrintUtil.parseNull(reset.getString("CKSL")));
        hashtable.put("ZJM", "");
        hashtable.put("BZQ", "");
        hashtable.put("FZSX", "");
        hashtable.put("SCRQ", "");
        hashtable.put("JLDW", "");
        hashtable.put("SX", PrintUtil.parseNull(reset.getString("CPSX")));
        hashtable.put("XMLB", xmlx);
        hashtable.put("ZT", RepositoryConstant.WL_ZT_ZT);
    }

    /**
     * 扣减库存.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws AWSSDKException
     * @throws SQLException
     */
    public void deductInventory(Connection conn, int bindid, String uid, String xmlx) throws AWSSDKException, SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String rkhwdm = reset.getString("RKHWDM");
                int rksl = reset.getInt("RKSL");
                int sjrksl = reset.getInt("SJRKSL");
                int sjcksl = reset.getInt("SJCKSL");
                String pch = reset.getString("PCH");
                String cpsx = reset.getString("CPSX");

                /** 将在途物料转换成库存物料. */
                /** 先将在途物料数量减掉 */
                int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlx, wlbh, pch, rkhwdm, cpsx, RepositoryConstant.WL_ZT_ZT, -sjcksl);
                if (updateCount != 1) {
                    throw new RuntimeException("库存信息更新失败!");
                }

                /** 插入或更新在库物料 */
                int queryCount = repositoryBiz.queryRecordCount(conn, xmlx, wlbh, pch, rkhwdm, cpsx, RepositoryConstant.WL_ZT_ZK);
                if (queryCount == 0) {
                    Hashtable<String, String> materialInfo = repositoryBiz.queryRecordHashtable(conn, xmlx, wlbh, pch, rkhwdm, cpsx,
                            RepositoryConstant.WL_ZT_ZT);
                    if (materialInfo == null) {
                        throw new RuntimeException("在途物料转为库存物料失败!");
                    } else {
                        materialInfo.put("KWDM", rkhwdm);
                        materialInfo.put("KWSL", String.valueOf(sjrksl));
                        materialInfo.put("ZT", RepositoryConstant.WL_ZT_ZK);
                        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", materialInfo, bindid, uid);
                    }
                } else {
                    updateCount = repositoryBiz.updateMaterialInfo(conn, xmlx, wlbh, pch, rkhwdm, cpsx, RepositoryConstant.WL_ZT_ZK, sjrksl);
                    if (updateCount != 1) {
                        throw new RuntimeException("库存信息更新失败!");
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 序列号出库.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     */
    public void outerXLH(Connection conn, int bindid, String uid, String shckbm) throws SQLException {
        updateXLH(conn, bindid, uid, shckbm, RepositoryConstant.GZTM_ZT_ZT);
    }

    /**
     * 序列号入库.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     */
    public void enterXLH(Connection conn, int bindid, String uid, String shckbm) throws SQLException {
        updateXLH(conn, bindid, uid, shckbm, RepositoryConstant.GZTM_ZT_ZK);
    }

    /**
     * 更新序列号状态
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     */
    private void updateXLH(Connection conn, int bindid, String uid, String shckbm, String zt) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_XLH);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String gztm = reset.getString("XLH");
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SHKC_XLH_S SET HWDM=?,CKDM=?,ZT=? WHERE GZTM=?", shckbm, shckbm, zt, gztm);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 将物料出库，并向入库仓库插入在途物料.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void wlToGo(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException, AWSSDKException {
        String xmlx = hashtable.get("XMLX");
        String shckbm = hashtable.get("SHKFCKBM");
        String shckmc = hashtable.get("SHKFCKMC");

        /** 对单身进行操作，转换物料库存位置. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                int id = reset.getInt("ID");
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String ckhwdm = reset.getString("CKHWDM");
                int cksl = reset.getInt("CKSL");
                int sjcksl = reset.getInt("SJCKSL");
                String cpsx = reset.getString("CPSX");
                String pch = reset.getString("PCH");
                String dj = reset.getString("JG");
                int rksl = reset.getInt("RKSL");

                /** 获取映射的货位代码，并更新到单身中. */
                String rkhwdm = repositoryBiz.mapPoistion(conn, xmlx, shckbm, wlbh, cpsx);
                DAOUtil.executeUpdate(conn, DBConstant.UPDATE_DB_FORM_SHCKBM_AND_RKSL, shckbm, shckmc, rkhwdm, sjcksl, sjcksl, id);

                /** 更新库存物料的库存数量. */
                int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlx, wlbh, pch, ckhwdm, cpsx, RepositoryConstant.WL_ZT_ZK, -sjcksl);
                if (updateCount != 1) {
                    throw new RuntimeException("型号：" + xh + " 的库存更新失败!");
                }

                /** 更新在途物料的库存数量，若没有在途物料数据那么就插入. */
                int queryCount = repositoryBiz.queryRecordCount(conn, xmlx, wlbh, pch, shckbm, cpsx, RepositoryConstant.WL_ZT_ZT);
                if (queryCount == 0) {
                    Hashtable<String, String> materialInfo = new Hashtable<String, String>();
                    mapMaterialInfo(reset, xmlx, materialInfo);
                    materialInfo.put("PCH", pch);
                    materialInfo.put("CKDM", shckbm);
                    materialInfo.put("CKMC", shckmc);
                    materialInfo.put("HWDM", rkhwdm);
                    materialInfo.put("KWSL", String.valueOf(sjcksl));
                    materialInfo.put("DJ", dj);
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", materialInfo, bindid, uid);
                } else {
                    repositoryBiz.updateMaterialInfo(conn, xmlx, wlbh, pch, shckbm, cpsx, RepositoryConstant.WL_ZT_ZT, sjcksl);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 插入物流数据.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertWLDate(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException, AWSSDKException {
        String xmlx = hashtable.get("XMLX");
        String fhckbm = hashtable.get("FHKFCKBM");
        String shckbm = hashtable.get("SHKFCKBM");
        String dh = hashtable.get("DBDH");
        String fhdz = hashtable.get("FHDZ");
        String shdz = hashtable.get("SHDZ");

        /** 向待发货物流表中插入数据 */
        Hashtable<String, String> dfhInfo = new Hashtable<String, String>();
        dfhInfo.put("DJLB", DfhConstant.DJLB_DB);
        dfhInfo.put("DH", dh);
        dfhInfo.put("XMLB", xmlx);
        dfhInfo.put("CLZT", DfhConstant.WLZT_DCL);
        dfhInfo.put("WLZT", DfhConstant.WLZT_DCL);
        DfhBiz.convertCustomerServiceAddressInfoToConsignor(conn, fhckbm, dfhInfo);
        DfhBiz.convertCustomerServiceAddressInfoToConsignee(conn, shckbm, dfhInfo);
        dfhInfo.put("SHDZ", shdz);
        dfhInfo.put("FHDZ", fhdz);
        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", dfhInfo, bindid, uid);

        insertWLDataBody(conn, bindid, uid);
    }

    /**
     * 插入物流单身数据.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertWLDataBody(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
        /** 单身. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String wlmc = reset.getString("WLMC");
                String ckhwdm = reset.getString("CKHWDM");
                String ckckdm = reset.getString("CKCKDM");
                int sjcksl = reset.getInt("SJCKSL");
                int sjrksl = reset.getInt("SJRKSL");
                String pch = reset.getString("PCH");
                String cpsx = reset.getString("CPSX");

                Hashtable<String, String> dfhHashtable = new Hashtable<String, String>();
                dfhHashtable.put("WLBH", PrintUtil.parseNull(wlbh));
                dfhHashtable.put("XH", PrintUtil.parseNull(xh));
                dfhHashtable.put("WLMC", PrintUtil.parseNull(wlmc));
                dfhHashtable.put("SL", String.valueOf(sjcksl));
                dfhHashtable.put("SX", PrintUtil.parseNull(cpsx));
                dfhHashtable.put("QSSL", String.valueOf(sjrksl));
                dfhHashtable.put("PCH", PrintUtil.parseNull(pch));
                dfhHashtable.put("HWDM", PrintUtil.parseNull(ckhwdm));
                dfhHashtable.put("CKDM", PrintUtil.parseNull(ckckdm));
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", dfhHashtable, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    public void fillSubTable(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        int row = 1;

        String sql = null;
        String sfgjpchzx = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_SFGJPCHZX, bindid);
        if (sfgjpchzx != null && sfgjpchzx.equals(XSDDConstant.YES)) {
            sql = DBConstant.QUERY_DB_FORM_BODY_GROUP_ZXD_PCH;
        } else {
            sql = DBConstant.QUERY_DB_FORM_BODY_GROUP_ZXD;
        }

        Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
        try {
            ps = conn.prepareStatement(sql);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                if (reset.getInt("SL") > 0) {
                    Hashtable<String, String> hashtable = new Hashtable<String, String>();
                    hashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
                    hashtable.put("CPMC", PrintUtil.parseNull(reset.getString("CPMC")));
                    hashtable.put("CPSX", PrintUtil.parseNull(reset.getString("CPSX")));
                    hashtable.put("SL", PrintUtil.parseNull(reset.getString("SL")));
                    hashtable.put("CPLX", PrintUtil.parseNull(reset.getString("CPLX")));
                    hashtable.put("ZXSL", PrintUtil.parseNull(reset.getString("ZXSL")));
                    hashtable.put("PCH", PrintUtil.parseNull(reset.getString("PCH")));
                    hashtable.put("ZXXH", String.valueOf(row));
                    hashtable.put("BH", String.valueOf(row));
                    vector.add(hashtable);
                    row++;
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_ZXD_S", vector, bindid, uid);
    }

}
