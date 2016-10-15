package cn.com.akl.shgl.dwrk.biz;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.dfh.biz.DfhBiz;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.kc.biz.RepositoryConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

public class DWRKBiz {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    public void wlToGo(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable) throws SQLException, AWSSDKException {

        String xmlx = hashtable.get("XMLX");
        String shckbm = hashtable.get("SHKFCKBM");
        String shckmc = hashtable.get("SHKFCKMC");

        /** 对单身进行操作，转换物料库存位置. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DWRKConstant.QUERY_DWRK_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                int id = reset.getInt("ID");
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String wlmc = reset.getString("WLMC");
                String gg = reset.getString("GG");
                String cpsx = reset.getString("CPSX");
                String dj = reset.getString("JG");
                int rksl = reset.getInt("RKSL");
                String pch = reset.getString("PCH");

                if (pch == null || pch.equals("")) {
                    throw new RuntimeException("请检查是否有批次号未填的记录!");
                }

                /** 获取映射的货位代码，并更新到单身中. */
                String rkhwdm = repositoryBiz.mapPoistion(conn, xmlx, shckbm, wlbh, cpsx);
                int updateCount = DAOUtil.executeUpdate(conn, DWRKConstant.UPDATE_DB_FORM_SHCKBM_AND_RKSL, rkhwdm, shckbm, shckmc, pch, rksl, rksl,
                        id);
                if (updateCount != 1) {
                    throw new RuntimeException("Update error!");
                }

                /** 更新在途物料的库存数量，若没有在途物料数据那么就插入. */
                int queryCount = repositoryBiz.queryRecordCount(conn, xmlx, wlbh, pch, shckbm, cpsx, RepositoryConstant.WL_ZT_ZT);
                if (queryCount == 0) {
                    Hashtable<String, String> materialInfo = new Hashtable<String, String>();
                    mapMaterialInfo(reset, xmlx, materialInfo);
                    materialInfo.put("CKDM", PrintUtil.parseNull(shckbm));
                    materialInfo.put("CKMC", PrintUtil.parseNull(shckmc));
                    materialInfo.put("HWDM", PrintUtil.parseNull(rkhwdm));
                    materialInfo.put("PCH", PrintUtil.parseNull(pch));
                    materialInfo.put("KWSL", String.valueOf(rksl));
                    materialInfo.put("DJ", dj);
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", materialInfo, bindid, uid);
                } else {
                    int mxUpdateCount = repositoryBiz.updateMaterialInfo(conn, xmlx, wlbh, pch, shckbm, cpsx, RepositoryConstant.WL_ZT_ZT, rksl);
                    if (mxUpdateCount != 1) {
                        throw new RuntimeException("明细数据更新失败!");
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
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
    public void deductInventory(Connection conn, int bindid, String uid) throws AWSSDKException, SQLException {
        String xmlx = DAOUtil.getStringOrNull(conn, DWRKConstant.QUERY_DWRK_XMLX, bindid);

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DWRKConstant.QUERY_DWRK_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String rkhwdm = reset.getString("RKHWDM");
                int rksl = reset.getInt("RKSL");
                int sjrksl = reset.getInt("SJRKSL");
                String pch = reset.getString("PCH");
                String jg = reset.getString("JG");
                String cpsx = reset.getString("CPSX");
                String xh = reset.getString("XH");
                String wlmc = reset.getString("WLMC");
                String gg = reset.getString("GG");

                Integer hzCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SHKC_P WHERE XMLB=? AND WLBH=? AND PCH=?", xmlx, wlbh, pch);
                if (hzCount == null || hzCount == 0) {
                    Hashtable<String, String> hzHashtable = new Hashtable<String, String>();
                    hzHashtable.put("XMLB", PrintUtil.parseNull(xmlx));
                    hzHashtable.put("WLBH", PrintUtil.parseNull(wlbh));
                    hzHashtable.put("WLMC", PrintUtil.parseNull(wlmc));
                    hzHashtable.put("GG", PrintUtil.parseNull(gg));
                    hzHashtable.put("XH", PrintUtil.parseNull(xh));
                    hzHashtable.put("PCH", PrintUtil.parseNull(pch));
                    hzHashtable.put("RKSL", String.valueOf(rksl));
                    hzHashtable.put("CKSL", "0");
                    hzHashtable.put("PCSL", String.valueOf(rksl));
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_P", hzHashtable, bindid, uid);
                } else {
                    repositoryBiz.updateMaterialInfoHz(conn, xmlx, wlbh, pch, rksl);
                }


                /** 将在途物料转换成库存物料. */
                /** 先将在途物料数量减掉 */
                int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlx, wlbh, pch, rkhwdm, cpsx, RepositoryConstant.WL_ZT_ZT, -rksl);
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
                        materialInfo.put("DJ", jg);
                        materialInfo.put("HWDM", rkhwdm);
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
        hashtable.put("CKDM", PrintUtil.parseNull(reset.getString("RKCKDM")));
        hashtable.put("CKMC", "");
        hashtable.put("QDM", "");
        hashtable.put("DDM", "");
        hashtable.put("KWDM", "");
        hashtable.put("HWDM", PrintUtil.parseNull(reset.getString("RKHWDM")));
        hashtable.put("KWSL", "");
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
     * 插入物流数据.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param main
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertWLDate(Connection conn, int bindid, String uid, Hashtable<String, String> main) throws SQLException, AWSSDKException {
        /** 向待发货物流表中插入数据 */
        /** 单头. */
        Hashtable<String, String> dfhInfo = new Hashtable<String, String>();
        DfhBiz.convertCustomerServiceAddressInfoToConsignee(conn, main.get("SHKFCKBM"), dfhInfo);
        DfhBiz.convertCustomerAddressInfoToConsignor(conn, main.get("FHKHBM"), dfhInfo);

        dfhInfo.put("FHR", PrintUtil.parseNull(main.get("FHR")));
        dfhInfo.put("FHRDH", PrintUtil.parseNull(main.get("FHRDH")));
        dfhInfo.put("FHRYX", PrintUtil.parseNull(main.get("FHRYX")));
        dfhInfo.put("FHDZ", PrintUtil.parseNull(main.get("FHDZ")));
        dfhInfo.put("DJLB", PrintUtil.parseNull(DfhConstant.DJLB_DWRK));
        dfhInfo.put("SHDZ", PrintUtil.parseNull(main.get("SHDZ")));
        dfhInfo.put("SHR", PrintUtil.parseNull(main.get("SHR")));
        dfhInfo.put("SHRDH", PrintUtil.parseNull(main.get("SHRDH")));

        dfhInfo.put("SHRDHQH", PrintUtil.parseNull(main.get("SHRDHQH")));
        dfhInfo.put("FHRSJ", PrintUtil.parseNull(main.get("FHRSJ")));

        dfhInfo.put("SHRYX", PrintUtil.parseNull(main.get("SHRYX")));
        dfhInfo.put("FHKFCKBM", PrintUtil.parseNull(main.get("FHKFCKBM")));
        dfhInfo.put("FHKFCKMC", PrintUtil.parseNull(main.get("FHKFCKMC")));
        dfhInfo.put("FHF", PrintUtil.parseNull(main.get("FHF")));
        dfhInfo.put("FHFLX", PrintUtil.parseNull(DfhConstant.SFHFLX_KH));
        dfhInfo.put("SHF", PrintUtil.parseNull(main.get("SHF")));
        dfhInfo.put("SHFLX", PrintUtil.parseNull(DfhConstant.SFHFLX_KFCK));
        dfhInfo.put("SHS", PrintUtil.parseNull(main.get("SHS")));
        dfhInfo.put("SHSHI", PrintUtil.parseNull(main.get("SHSHI")));
        dfhInfo.put("SHQX", PrintUtil.parseNull(main.get("SHQX")));
        dfhInfo.put("FHS", PrintUtil.parseNull(main.get("FHS")));
        dfhInfo.put("FHSHI", PrintUtil.parseNull(main.get("FHSHI")));
        dfhInfo.put("FHQX", PrintUtil.parseNull(main.get("FHQX")));
        dfhInfo.put("XMLB", PrintUtil.parseNull(main.get("XMLX")));
        dfhInfo.put("DH", PrintUtil.parseNull(main.get("DWRKDH")));
        dfhInfo.put("WLZT", DfhConstant.WLZT_DCL);
        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", dfhInfo, bindid, uid);

        /** 单身. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DWRKConstant.QUERY_DWRK_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String wlmc = reset.getString("WLMC");
                String rkhwdm = reset.getString("RKHWDM");
                String rkckdm = reset.getString("RKCKDM");
                int sjcksl = reset.getInt("SJRKSL");
                String pch = reset.getString("PCH");
                String cpsx = reset.getString("CPSX");

                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                hashtable.put("WLBH", PrintUtil.parseNull(wlbh));
                hashtable.put("XH", PrintUtil.parseNull(xh));
                hashtable.put("WLMC", PrintUtil.parseNull(wlmc));
                hashtable.put("SL", String.valueOf(sjcksl));
                hashtable.put("SX", PrintUtil.parseNull(cpsx));
                hashtable.put("QSSL", String.valueOf(sjcksl));
                hashtable.put("PCH", PrintUtil.parseNull(pch));
                hashtable.put("HWDM", PrintUtil.parseNull(rkhwdm));
                hashtable.put("CKDM", PrintUtil.parseNull(rkckdm));
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", hashtable, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);

        }

    }
}