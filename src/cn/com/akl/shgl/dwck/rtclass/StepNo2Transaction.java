package cn.com.akl.shgl.dwck.rtclass;

import cn.com.akl.shgl.dwck.biz.DWCKBiz;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

    public StepNo2Transaction() {
        super();
    }

    public StepNo2Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("填充装箱单");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        // 若路由走出库，则不需要装箱.
        boolean isGo = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "出库");
        if (isGo) {
            return true;
        }

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (isBack) {
            BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_SH_KCSK", bindid);
            return true;
        }

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            DWCKBiz dwckBiz = new DWCKBiz();
            dwckBiz.fillTable(conn, bindid, uid);
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

}
