package cn.com.akl.dgkgl.xsck.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3RuleJumpAndFX extends WorkFlowStepJumpRuleRTClassA{

	public StepNo3RuleJumpAndFX(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		setVersion("1.0.0");
		setDescription("根据选择填充运单还是反更新库存");
	}

	@Override
	public int getNextNodeNo() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		
		boolean zy = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "自运");
		
		return 0;
	}

	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		return null;
	}

}
