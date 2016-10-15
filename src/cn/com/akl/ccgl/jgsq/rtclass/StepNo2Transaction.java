package cn.com.akl.ccgl.jgsq.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
	}

	@Override
	public boolean execute() {

 		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// 拆分物料
		boolean noAgreeFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "不同意");
		if (noAgreeFlag) {
			// 删除拆分的货物
			BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_JGSQ_WLMX_S", bindid);
			// 删除锁库
			BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_KC_SPPCSK", bindid);
		} else {

		}

		return true;
	}

}
