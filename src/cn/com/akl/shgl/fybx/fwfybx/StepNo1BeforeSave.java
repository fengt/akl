package cn.com.akl.shgl.fybx.fwfybx;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.fybx.cnt.FYBXCnt;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by huangming on 2015/4/29.
 */
public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    public StepNo1BeforeSave() {
        super();
    }

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setDescription("插入费用报销汇总信息.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

        if (!"BO_AKL_FWZXBX_P".equals(tableName)) {
            return true;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();
            service(conn, bindid);
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

    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String djlx = hashtable.get("DJLX");

        if ("0".equals(djlx) || "单据引发".equals(djlx)) {
            DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_FPMX_S SET ZE=? WHERE BINDID=?", 0, bindid);
            sumKdToMx(conn, bindid);
            DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_FPMX_S WHERE ZE=0 AND BINDID=?", bindid);
        }

        sumMxToHz(conn, bindid);
    }

    /**
     * 快递汇总到明细.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    private void sumKdToMx(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT XMLB, BXXM, SUM(YF) ZE, '发票' PZLX, '0' AS ZS FROM BO_AKL_KDFY_S WHERE BINDID=? GROUP BY XMLB, BXXM");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String bxxm = reset.getString("BXXM");
                String pzlx = reset.getString("PZLX");
                String xmlb = reset.getString("XMLB");
                String zs = reset.getString("ZS");
                String ze = reset.getString("ZE");

                int updateCount = DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_FPMX_S SET ZE=? WHERE BXXM=? AND XMLB=? AND BINDID=?", ze, bxxm, xmlb, bindid);
                if (updateCount == 0) {
                    // 更新汇总数据.
                    Hashtable<String, String> hashtable = new Hashtable<String, String>();
                    hashtable.put("BXXM", PrintUtil.parseNull(bxxm));
                    hashtable.put("XMLB", PrintUtil.parseNull(xmlb));
                    hashtable.put("PZLX", PrintUtil.parseNull(pzlx));
                    hashtable.put("ZS", PrintUtil.parseNull(zs));
                    hashtable.put("ZE", PrintUtil.parseNull(ze));
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FPMX_S", hashtable, bindid, uid);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

    }

    /**
     * 明细到汇总.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    private void sumMxToHz(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        int count = 0;

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(FYBXCnt.QUERY_KF_FPHZ);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String bxxm = reset.getString("BXXM");
                String xmlb = reset.getString("XMLB");
                String pzlx = reset.getString("PZLX");
                String zs = reset.getString("ZS");
                String ze = reset.getString("ZE");
                String fyfssj = reset.getString("FYFSSJ");
                String fyjssj = reset.getString("FYJSSJ");

                // 更新汇总数据.
                int updateCount = DAOUtil.executeUpdate(conn, FYBXCnt.UPDATE_KF_FPHZ, zs, ze, fyfssj, fyjssj, bxxm, pzlx, bindid, xmlb);
                if (updateCount == 0) {
                    Hashtable<String, String> hashtable = new Hashtable<String, String>();
                    hashtable.put("BXXM", PrintUtil.parseNull(bxxm));
                    hashtable.put("XMLB", PrintUtil.parseNull(xmlb));
                    hashtable.put("PZLX", PrintUtil.parseNull(pzlx));
                    hashtable.put("ZS", PrintUtil.parseNull(zs));
                    hashtable.put("ZE", PrintUtil.parseNull(ze));
                    hashtable.put("FYFSSJ", PrintUtil.parseNull(fyfssj));
                    hashtable.put("FYJSSJ", PrintUtil.parseNull(fyjssj));
                    hashtable.put("FYCDBM", getUserContext().getDepartmentModel().getDepartmentName());
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FWZXBX_FPHZ_S", hashtable, bindid, uid);
                }

                count++;
            }
        } finally {
            DBSql.close(ps, reset);
        }

        if (count == 0) {
            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_FWZXBX_FPHZ_S", bindid);
        }
    }

}


