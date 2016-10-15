package cn.com.akl.shgl.qscy.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.kc.biz.RepositoryConstant;
import cn.com.akl.shgl.qscy.biz.QSCYConstants;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    public StepNo4Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("签收差异回库");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskId = getParameter(PARAMETER_TASK_ID).toInt();

        boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
        if (th) {
            return true;
        }

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台!", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
