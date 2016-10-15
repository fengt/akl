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

public class StepNo7RuleJump extends WorkFlowStepJumpRuleRTClassA {

    public StepNo7RuleJump(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("保证流程正常结束.");
    }

    @Override
    public int getNextNodeNo() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (isBack) {
            return 0;
        } else {
            return 9999;
        }

    }

    @Override
    public String getNextTaskUser() {
        return null;
    }

}
