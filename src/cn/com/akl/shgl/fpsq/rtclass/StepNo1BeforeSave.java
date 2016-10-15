package cn.com.akl.shgl.fpsq.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * Created by huangming on 2015/6/9.
 */
public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("自动带出交付产品信息.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

        Connection conn = null;
        try {
            conn = DBSql.open();

            if ("BO_AKL_FPSQ".equals(tableName)) {
                String yjfdh = DAOUtil.getStringOrNull(conn, "SELECT JFDH FROM BO_AKL_FPSQ WHERE BINDID=?", bindid);
                Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
                String jfdh = hashtable.get("JFDH");
                if (jfdh == null || !jfdh.equals(yjfdh)) {
                    BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_FPSQ_S", bindid);
                }
                dealHead(conn, bindid);
            }

            if ("".equals(tableName)) {
                dealBody(conn, bindid);
            }

            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 处理单身.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void dealBody(Connection conn, int bindid) throws SQLException, AWSSDKException {

    }

    /**
     * 处理单头.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void dealHead(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        Hashtable<String, String> main = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String jfdh = main.get("JFDH");

        Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_FPSQ_S WHERE BINDID=?", bindid);
        if (count != null && count > 0) {
            return;
        }

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT jfs.ID, jfp.KHMC, jfs.HH, jfp.SXR, jfp.SXDH, jfp.JFDH, jfs.XH, jfs.WLMC, jfs.CLFS, jfs.SX, jfs.SL, jfs.JG, sx.XLMC SXN, clfs.XLMC CLFSN FROM BO_AKL_WXJF_P jfp LEFT JOIN BO_AKL_WXJF_S jfs ON jfp.BINDID = jfs.BINDID LEFT JOIN BO_AKL_DATA_DICT_S clfs ON jfs.CLFS = clfs.XLBM LEFT JOIN BO_AKL_DATA_DICT_S sx ON jfs.SX = sx.XLBM WHERE jfp.JFDH = ? AND jfs.SFJF=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, jfdh, XSDDConstant.YES);
            while (reset.next()) {
                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                hashtable.put("CLFS", PrintUtil.parseNull(reset.getString("CLFS")));
                hashtable.put("JFHH", PrintUtil.parseNull(reset.getString("HH")));
                hashtable.put("JFDH", PrintUtil.parseNull(reset.getString("JFDH")));
                hashtable.put("JE", PrintUtil.parseNull(reset.getString("JG")));
                hashtable.put("KHXX", PrintUtil.parseNull(reset.getString("KHMC")));
                hashtable.put("SL", PrintUtil.parseNull(reset.getString("SL")));
                hashtable.put("SX", PrintUtil.parseNull(reset.getString("SX")));
                hashtable.put("SXDH", PrintUtil.parseNull(reset.getString("SXDH")));
                hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
                hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FPSQ_S", hashtable, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}

