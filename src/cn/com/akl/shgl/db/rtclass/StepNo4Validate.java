package cn.com.akl.shgl.db.rtclass;

import cn.com.akl.shgl.db.biz.DBConstant;
import cn.com.akl.shgl.db.biz.DBValidater;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;
import java.sql.SQLException;

public class StepNo4Validate extends WorkFlowStepRTClassA {

    private DBValidater validater = new DBValidater();

    public StepNo4Validate() {
        super();
    }

    public StepNo4Validate(UserContext arg0) {
        super(arg0);
        setDescription("装箱总数验证.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskId = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
        if (isBack) {
            return true;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();
            return validater.validateZXDAndCKD(conn, bindid, uid);
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

}
