package cn.com.akl.shgl.dfh.biz;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.util.DBSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

public class DfhBiz {

    /**
     * 查询客服信息.
     */
    public static final String QUERY_CUSTOMERSERVICE = "SELECT * FROM BO_AKL_KFCK WHERE KFCKBM=?";
    /**
     * 查询客户信息.
     */
    public static final String QUERY_CUSTOMER = "SELECT * FROM BO_AKL_SH_KH WHERE KHBH=?";

    /**
     * 封装客服地址信息到Hashtable中，存入收货人字段.
     *
     * @param conn
     * @param kfckbm
     * @param map
     * @throws SQLException
     */
    public static void convertCustomerServiceAddressInfoToConsignee(Connection conn, String kfckbm, Hashtable<String, String> map)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(QUERY_CUSTOMERSERVICE);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, kfckbm);
            while (reset.next()) {
                map.put("SHKFCKBM", PrintUtil.parseNull(reset.getString("KFCKBM")));
                map.put("SHDQBM", PrintUtil.parseNull(reset.getString("DQBM")));
                map.put("SHDQMC", PrintUtil.parseNull(reset.getString("DQMC")));
                map.put("SHSSX", PrintUtil.parseNull(reset.getString("SSX")));
                map.put("SHS", PrintUtil.parseNull(reset.getString("S")));
                map.put("SHSHI", PrintUtil.parseNull(reset.getString("SHI")));
                map.put("SHQX", PrintUtil.parseNull(reset.getString("QX")));
                map.put("SHKFFZR", PrintUtil.parseNull(reset.getString("KFFZR")));
                map.put("SHEMAIL", PrintUtil.parseNull(reset.getString("EMAIL")));
                map.put("SHLXR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("SHDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("SHSJH", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("SHDZ", PrintUtil.parseNull(reset.getString("DZ")));
                map.put("SHYB", PrintUtil.parseNull(reset.getString("YB")));
                map.put("SHKFCKMC", PrintUtil.parseNull(reset.getString("KFCKMC")));

                map.put("SHR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("SHF", PrintUtil.parseNull(reset.getString("KFCKMC")));
                map.put("SHFLX", DfhConstant.SFHFLX_KFCK);
                map.put("SHRDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("SHRYX", PrintUtil.parseNull(reset.getString("EMAIL")));

                map.put("SHRSJ", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("SHRDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("SHRDHQH", PrintUtil.parseNull(reset.getString("DHQH")));
                map.put("SHGJ", PrintUtil.parseNull(reset.getString("GJ")));
                map.put("SHYB", PrintUtil.parseNull(reset.getString("YB")));
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 封装客服地址信息到Hashtable中，存入发货人字段.
     *
     * @param conn
     * @param kfckbm
     * @throws SQLException
     */
    public static void convertCustomerServiceAddressInfoToConsignor(Connection conn, String kfckbm, Hashtable<String, String> map)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(QUERY_CUSTOMERSERVICE);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, kfckbm);
            while (reset.next()) {
                map.put("FHKFCKBM", PrintUtil.parseNull(reset.getString("KFCKBM")));
                map.put("FHDQBM", PrintUtil.parseNull(reset.getString("DQBM")));
                map.put("FHDQMC", PrintUtil.parseNull(reset.getString("DQMC")));
                map.put("FHSSX", PrintUtil.parseNull(reset.getString("SSX")));
                map.put("FHS", PrintUtil.parseNull(reset.getString("S")));
                map.put("FHSHI", PrintUtil.parseNull(reset.getString("SHI")));
                map.put("FHQX", PrintUtil.parseNull(reset.getString("QX")));
                map.put("FHKFFZR", PrintUtil.parseNull(reset.getString("KFFZR")));
                map.put("FHEMAIL", PrintUtil.parseNull(reset.getString("EMAIL")));
                map.put("FHLXR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("FHDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("FHSJH", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("FHSJ", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("FHDZ", PrintUtil.parseNull(reset.getString("DZ")));
                map.put("FHYB", PrintUtil.parseNull(reset.getString("YB")));
                map.put("FHKFCKMC", PrintUtil.parseNull(reset.getString("KFCKMC")));

                map.put("FHFLX", DfhConstant.SFHFLX_KFCK);
                map.put("FHF", PrintUtil.parseNull(reset.getString("KFCKMC")));
                map.put("FHR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("FHRYX", PrintUtil.parseNull(reset.getString("EMAIL")));

                map.put("FHRSJ", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("FHRDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("FHRDHQH", PrintUtil.parseNull(reset.getString("DHQH")));
                map.put("FHGJ", PrintUtil.parseNull(reset.getString("GJ")));
                map.put("FHYB", PrintUtil.parseNull(reset.getString("YB")));
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 封装客户地址信息到Hashtable中，存入收货人字段.
     *
     * @param conn
     * @param khbh
     * @param map
     * @throws SQLException
     */
    public static void convertCustomerAddressInfoToConsignee(Connection conn, String khbh, Hashtable<String, String> map) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(QUERY_CUSTOMER);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, khbh);
            while (reset.next()) {
                // map.put("SHKFCKBM", PrintUtil.parseNull(reset.getString("KFCKBM")));
                // map.put("SHDQBM", PrintUtil.parseNull(reset.getString("DQBM")));
                // map.put("SHDQMC", PrintUtil.parseNull(reset.getString("DQMC")));
                // map.put("SHSSX", PrintUtil.parseNull(reset.getString("SSX")));
                // map.put("SHKFCKMC", PrintUtil.parseNull(reset.getString("KFCKMC")));
                map.put("SHS", PrintUtil.parseNull(reset.getString("S")));
                map.put("SHSHI", PrintUtil.parseNull(reset.getString("SHI")));
                map.put("SHQX", PrintUtil.parseNull(reset.getString("QX")));
                map.put("SHKFFZR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("SHEMAIL", PrintUtil.parseNull(reset.getString("EMAIL")));
                map.put("SHLXR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("SHDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("SHSJH", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("SHSJ", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("SHDZ", PrintUtil.parseNull(reset.getString("DZ")));
                map.put("SHYB", PrintUtil.parseNull(reset.getString("YB")));

                map.put("SHR", PrintUtil.parseNull(reset.getString("LXR")));
                // map.put("SHF", PrintUtil.parseNull(reset.getString("KFCKMC")));
                map.put("SHFLX", DfhConstant.SFHFLX_KH);

                map.put("SHF", PrintUtil.parseNull(reset.getString("KHMC")));
                map.put("SHGJ", PrintUtil.parseNull(reset.getString("GJ")));

                map.put("SHRDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("SHRYX", PrintUtil.parseNull(reset.getString("EMAIL")));
                map.put("SHRSJ", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("SHR", PrintUtil.parseNull(reset.getString("KHMC")));
                map.put("SHRDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("SHRDHQH", PrintUtil.parseNull(reset.getString("QH")));
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 封装客户地址到发货人.
     *
     * @param conn
     * @param khbh
     * @param map
     * @throws SQLException
     */
    public static void convertCustomerAddressInfoToConsignor(Connection conn, String khbh, Hashtable<String, String> map) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(QUERY_CUSTOMER);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, khbh);
            while (reset.next()) {
                // map.put("FHKFCKBM", PrintUtil.parseNull(reset.getString("KFCKBM")));
                // map.put("FHDQBM", PrintUtil.parseNull(reset.getString("DQBM")));
                // map.put("FHDQMC", PrintUtil.parseNull(reset.getString("DQMC")));
                // map.put("FHSSX", PrintUtil.parseNull(reset.getString("SSX")));
                map.put("FHS", PrintUtil.parseNull(reset.getString("S")));
                map.put("FHSHI", PrintUtil.parseNull(reset.getString("SHI")));
                map.put("FHQX", PrintUtil.parseNull(reset.getString("QX")));
                map.put("FHKFFZR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("FHEMAIL", PrintUtil.parseNull(reset.getString("EMAIL")));
                map.put("FHLXR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("FHDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("FHSJH", PrintUtil.parseNull(reset.getString("SJH")));
                map.put("FHDZ", PrintUtil.parseNull(reset.getString("DZ")));
                map.put("FHYB", PrintUtil.parseNull(reset.getString("YB")));
                // map.put("FHKFCKMC", PrintUtil.parseNull(reset.getString("KFCKMC")));

                map.put("FHFLX", DfhConstant.SFHFLX_KH);
                // map.put("FHF", PrintUtil.parseNull(reset.getString("KFCKMC")));
                map.put("FHR", PrintUtil.parseNull(reset.getString("LXR")));
                map.put("FHRDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("FHRYX", PrintUtil.parseNull(reset.getString("EMAIL")));

                map.put("FHF", PrintUtil.parseNull(reset.getString("KHMC")));
                map.put("FHGJ", PrintUtil.parseNull(reset.getString("GJ")));
                map.put("FHRYX", PrintUtil.parseNull(reset.getString("EMAIL")));
                map.put("FHR", PrintUtil.parseNull(reset.getString("KHMC")));
                map.put("FHRDH", PrintUtil.parseNull(reset.getString("DH")));
                map.put("FHRDHQH", PrintUtil.parseNull(reset.getString("QH")));
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
