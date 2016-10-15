package cn.com.akl.shgl.yzsq.rtclass;

import cn.com.akl.shgl.dfh.biz.DfhBiz;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.yzsq.biz.YZSQBiz;
import cn.com.akl.shgl.yzsq.biz.YZSQConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;


/**
 * Created by huangming on 2015/4/28.
 */
public class StepNo7Transaction extends WorkFlowStepRTClassA {

    private YZSQBiz yzsqBiz = new YZSQBiz();

    public StepNo7Transaction() {
        super();
    }

    public StepNo7Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("升级后用章留在客服.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();

        boolean isReturnZB = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
        if (!isReturnZB) {
            return true;
        }

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, bindid);
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        Hashtable<String, String> bo_data = BOInstanceAPI.getInstance().getBOData("BO_AKL_YZSQ_P", bindid);
        serviceInsertDfhs(conn, bindid, bo_data);
    }

    /**
     * 插入待发货单身信息。
     *
     * @param conn
     * @param bindid
     */
    public void serviceInsertDfhs(Connection conn, int bindid, Hashtable<String, String> boData) throws SQLException, AWSSDKException {
        // 插入待发货记录.
        String uid = getUserContext().getUID();

        PreparedStatement stat = null;
        ResultSet reset = null;
        try {
            stat = conn.prepareStatement(YZSQConstant.QUERY_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, stat, bindid);
            while (reset.next()) {
                String zz = reset.getString("ZZ");
                String zzlx = reset.getString("ZZLX");
                String fzjgmc = reset.getString("FZJGMC");
                int zzid = reset.getInt("ZZID");
                int sl = 1;

                // 因流程结束，更新所在地.
                yzsqBiz.updateALLPalce(conn, zz, zzid, boData.get("KFZXBM"));
            }
        } finally {
            DBSql.close(stat, reset);
        }
    }

}
