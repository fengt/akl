package cn.com.akl.ccgl.xsck.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo4RuleJump(UserContext arg0) {
		super(arg0);
		setDescription("第四节点回退处理");
		setVersion("1.0.0");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
		if (backFlag) {
			return 3;
		} else {
			return 0;
		}
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}

}
