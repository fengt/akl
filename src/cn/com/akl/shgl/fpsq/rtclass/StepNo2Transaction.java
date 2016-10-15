package cn.com.akl.shgl.fpsq.rtclass;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.fpsq.constant.FpsqConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by huangming on 2015/4/29.
 */
public class StepNo2Transaction extends WorkFlowStepRTClassA {

    public StepNo2Transaction() {
        super();
    }

    public StepNo2Transaction(UserContext arg0) {
        super(arg0);
        setDescription("如果不同意，更新回交付记录的开票状态.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        boolean isTy = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
        if (isTy) {
            return true;
        }

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            //service(conn, bindid);
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

    public void service(Connection conn, int bindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(FpsqConstant.QUEYR_FPSQ_DH);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String fpsqdh = reset.getString("JFHH");
                // 更新交付产品记录为已开票.
                DAOUtil.executeUpdate(conn, FpsqConstant.UPDATE_JFCP_KPXX, XSDDConstant.NO, fpsqdh, XSDDConstant.YES);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}


