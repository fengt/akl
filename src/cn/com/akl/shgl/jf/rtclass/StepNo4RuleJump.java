package cn.com.akl.shgl.jf.rtclass;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;

public class StepNo4RuleJump extends WorkFlowStepJumpRuleRTClassA {

    public StepNo4RuleJump(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("若有缺货就走缺货.");
    }

    @Override
    public int getNextNodeNo() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (isBack) {
            return 0;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();

            String sfqhsq = DAOUtil.getStringOrNull(conn, "SELECT SFQHSQ FROM BO_AKL_WXJF_S WHERE BINDID=? AND SFQHSQ=?", bindid, XSDDConstant.YES);
            if (sfqhsq == null || sfqhsq.equals("") || sfqhsq.equals(XSDDConstant.NO)) {
                // 无缺货申请记录.
                return DeliveryConstant.STEP_SJCLKFQR;
            } else {
                // 有缺货申请记录.
                return DeliveryConstant.STEP_QHSQ;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return this.getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();
        } catch (Exception e) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "流程RuleJump事件出现问题!");
            e.printStackTrace();
            return this.getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    @Override
    public String getNextTaskUser() {
        return null;
    }

}
