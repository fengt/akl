package cn.com.akl.shgl.dwck.biz;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

public class DWCKBiz {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    public void removeLock(Connection conn, int bindid) {
        repositoryBiz.removeLock(conn, bindid);
    }

    /**
     * 将汇总插入明细.
     *
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertHzToMx(Connection conn, int bindid, String uid, Hashtable<String, String> boData) throws SQLException, AWSSDKException {
        String xmlb = boData.get("XMLX");
        String fhkfckbm = boData.get("FHKFCKBM");
        String fhkfckmc = boData.get("FHKFCKMC");

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DWCKConstnat.QUERY_DWCK_HZ_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                int cksl = reset.getInt("CKSL");
                String cpsx = reset.getString("CPSX");

                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                copyFieldHzToMx(hashtable, reset);
                hashtable.put("CKCKDM", fhkfckbm);
                hashtable.put("CKCKMC", fhkfckmc);

                Vector<Hashtable<String, String>> wlVector = repositoryBiz.autoFetchs(conn, bindid, uid, xmlb, wlbh, xh, fhkfckbm, cpsx, cksl,
                        hashtable);
                // 字段转换.
                for (Hashtable<String, String> hashtable2 : wlVector) {
                    String sl = hashtable2.get("SL");
                    String hwdm = hashtable2.get("HWDM");
                    hashtable2.put("CKHWDM", hwdm);
                    hashtable2.put("CKSL", sl);
                    hashtable2.put("SJCKSL", sl);
                }
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_DWCK_S", wlVector, bindid, uid);

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
        hashtable.put("WLBH", reset.getString("WLBH"));
        hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
        hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
        hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
        hashtable.put("SJLH", PrintUtil.parseNull(reset.getString("SJLH")));
        hashtable.put("KCSL", PrintUtil.parseNull(reset.getString("KCSL")));
        hashtable.put("CPLX", PrintUtil.parseNull(reset.getString("CPLX")));
        hashtable.put("L9", PrintUtil.parseNull(reset.getString("L9")));
        hashtable.put("WLSL", PrintUtil.parseNull(reset.getString("WLSL")));
        hashtable.put("CKLX", PrintUtil.parseNull(reset.getString("CKLX")));
        hashtable.put("CPSX", PrintUtil.parseNull(reset.getString("CPSX")));
        hashtable.put("JG", PrintUtil.parseNull(reset.getString("JG")));
        hashtable.put("MS", PrintUtil.parseNull(reset.getString("MS")));
        hashtable.put("CKSL", PrintUtil.parseNull(reset.getString("CKSL")));
        hashtable.put("SJCKSL", PrintUtil.parseNull(reset.getString("CKSL")));
        hashtable.put("CKCKDM", PrintUtil.parseNull(reset.getString("CKCKDM")));
        hashtable.put("CKCKMC", PrintUtil.parseNull(reset.getString("CKCKMC")));
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
        String xmlx = DAOUtil.getStringOrNull(conn, DWCKConstnat.QUERY_DWCK_XMLX, bindid);

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DWCKConstnat.QUERY_DWCK_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String ckhwdm = reset.getString("CKHWDM");
                int sjcksl = reset.getInt("SJCKSL");
                String pch = reset.getString("PCH");
                String cpsx = reset.getString("CPSX");

                /** 将在途物料转换成库存物料. */
                /** 先将在途物料数量减掉 */
                int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlx, wlbh, pch, ckhwdm, cpsx, RepositoryConstant.WL_ZT_ZK, -sjcksl);
                if (updateCount != 1) {
                    throw new RuntimeException("库存信息更新失败!");
                }

                repositoryBiz.updateMaterialInfoHz(conn, xmlx, wlbh, pch, -sjcksl);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 插入物流数据. 向待发货物流表中插入数据
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void insertWLDate(Connection conn, int bindid, String uid, Hashtable<String, String> main) throws SQLException, AWSSDKException {
        /** 单头. */
        Hashtable<String, String> dfhInfo = new Hashtable<String, String>();
        DfhBiz.convertCustomerServiceAddressInfoToConsignor(conn, main.get("FHKFCKBM"), dfhInfo);
        DfhBiz.convertCustomerAddressInfoToConsignee(conn, main.get("SHKHBM"), dfhInfo);
        dfhInfo.put("FHR", PrintUtil.parseNull(main.get("FHR")));
        dfhInfo.put("FHRDH", PrintUtil.parseNull(main.get("FHRDH")));
        dfhInfo.put("FHRYX", PrintUtil.parseNull(main.get("FHRYX")));
        dfhInfo.put("FHDZ", PrintUtil.parseNull(main.get("FHDZ")));
        dfhInfo.put("DJLB", PrintUtil.parseNull(DfhConstant.DJLB_DWCK));
        dfhInfo.put("SHDZ", PrintUtil.parseNull(main.get("SHDZ")));
        dfhInfo.put("SHR", PrintUtil.parseNull(main.get("SHR")));
        dfhInfo.put("SHRDH", PrintUtil.parseNull(main.get("SHRDH")));
        dfhInfo.put("SHRSJ", PrintUtil.parseNull(main.get("SHRSJ")));
        dfhInfo.put("SHYB", PrintUtil.parseNull(main.get("SHYB")));
        dfhInfo.put("FHRDHQH", PrintUtil.parseNull(main.get("FHRDHQH")));
        dfhInfo.put("SHRYX", PrintUtil.parseNull(main.get("SHRYX")));
        dfhInfo.put("FHKFCKBM", PrintUtil.parseNull(main.get("FHKFCKBM")));
        dfhInfo.put("FHKFCKMC", PrintUtil.parseNull(main.get("FHKFCKMC")));
        dfhInfo.put("FHF", PrintUtil.parseNull(main.get("FHF")));
        dfhInfo.put("FHFLX", PrintUtil.parseNull(DfhConstant.SFHFLX_KFCK));
        dfhInfo.put("SHF", PrintUtil.parseNull(main.get("SHF")));
        dfhInfo.put("SHFLX", PrintUtil.parseNull(DfhConstant.SFHFLX_KH));
        dfhInfo.put("SHS", PrintUtil.parseNull(main.get("SHS")));
        dfhInfo.put("SHSHI", PrintUtil.parseNull(main.get("SHSHI")));
        dfhInfo.put("SHQX", PrintUtil.parseNull(main.get("SHQX")));
        dfhInfo.put("FHS", PrintUtil.parseNull(main.get("FHS")));
        dfhInfo.put("FHSHI", PrintUtil.parseNull(main.get("FHSHI")));
        dfhInfo.put("FHQX", PrintUtil.parseNull(main.get("FHQX")));
        dfhInfo.put("XMLB", PrintUtil.parseNull(main.get("XMLX")));
        dfhInfo.put("DH", PrintUtil.parseNull(main.get("DWCKDH")));
        dfhInfo.put("WLZT", DfhConstant.WLZT_DCL);
        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", dfhInfo, bindid, uid);

        insertWLDateBody(conn, bindid, uid);
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
    public void insertWLDateBody(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
        /** 单身. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DWCKConstnat.QUERY_DWCK_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String wlmc = reset.getString("WLMC");
                String ckhwdm = reset.getString("CKHWDM");
                String ckckdm = reset.getString("CKCKDM");
                int sjcksl = reset.getInt("SJCKSL");
                int qssl = reset.getInt("QSSL");
                String pch = reset.getString("PCH");
                String cpsx = reset.getString("CPSX");

                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                hashtable.put("WLBH", PrintUtil.parseNull(wlbh));
                hashtable.put("XH", PrintUtil.parseNull(xh));
                hashtable.put("WLMC", PrintUtil.parseNull(wlmc));
                hashtable.put("SL", String.valueOf(sjcksl));
                hashtable.put("SX", PrintUtil.parseNull(cpsx));
                hashtable.put("QSSL", String.valueOf(qssl));
                hashtable.put("PCH", PrintUtil.parseNull(pch));
                hashtable.put("HWDM", PrintUtil.parseNull(ckhwdm));
                hashtable.put("CKDM", PrintUtil.parseNull(ckckdm));
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", hashtable, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 清空装箱单. 填充装箱单.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void fillTable(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
        Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_SH_DWCK_P", bindid);

        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("FHR", PrintUtil.parseNull(boData.get("FHR")));
        hashtable.put("FHGS", PrintUtil.parseNull(boData.get("FHF")));
        hashtable.put("FHRDH", PrintUtil.parseNull(boData.get("FHRDH")));
        hashtable.put("FHDZ", PrintUtil.parseNull(boData.get("FHDZ")));
        hashtable.put("BZ", PrintUtil.parseNull(boData.get("FHBZ")));
        hashtable.put("SHR", PrintUtil.parseNull(boData.get("SHR")));
        hashtable.put("SHGS", PrintUtil.parseNull(boData.get("SHF")));
        hashtable.put("SHRDZ", PrintUtil.parseNull(boData.get("SHDZ")));
        hashtable.put("WLDH", PrintUtil.parseNull(boData.get("DWCKDH")));
        hashtable.put("SHRDH", PrintUtil.parseNull(boData.get("SHRDH")));
        hashtable.put("FHRSJ", PrintUtil.parseNull(boData.get("FHRSJ")));
        hashtable.put("FHRDHQH", PrintUtil.parseNull(boData.get("SHRDHQH")));
        hashtable.put("FHRSJ", PrintUtil.parseNull(boData.get("SHRSJ")));
        hashtable.put("FHRDHQH", PrintUtil.parseNull(boData.get("FHRDHQH")));

        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_ZXD_P", bindid);
        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_ZXD_S", bindid);

        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_ZXD_P", hashtable, bindid, uid);

        fillSubTable(conn, bindid, uid);
    }

    public void fillSubTable(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        int row = 1;

        String sfgjpchzx = DAOUtil.getStringOrNull(conn, DWCKConstnat.QUERY_DWCK_SFGJPCHZX, bindid);
        String sql = null;
        if (sfgjpchzx != null && sfgjpchzx.equals(XSDDConstant.YES)) {
            sql = DWCKConstnat.QUERY_DWCK_BODY_GROUP_ZXD_PCH;
        } else {
            sql = DWCKConstnat.QUERY_DWCK_BODY_GROUP_ZXD;
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
                    hashtable.put("PCH", PrintUtil.parseNull(reset.getString("PCH")));
                    hashtable.put("ZXSL", PrintUtil.parseNull(reset.getString("ZXSL")));
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
