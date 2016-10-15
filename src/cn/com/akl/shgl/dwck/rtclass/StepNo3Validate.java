package cn.com.akl.shgl.dwck.rtclass;

import cn.com.akl.shgl.dwck.biz.DWCKValidater;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;

public class StepNo3Validate extends WorkFlowStepRTClassA {

    private DWCKValidater validater = new DWCKValidater();

    public StepNo3Validate() {
        super();
    }

    public StepNo3Validate(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("校验装箱单数量是否一致.");
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

}
