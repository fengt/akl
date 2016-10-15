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

		// �������
		boolean noAgreeFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "��ͬ��");
		if (noAgreeFlag) {
			// ɾ����ֵĻ���
			BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_JGSQ_WLMX_S", bindid);
			// ɾ������
			BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_KC_SPPCSK", bindid);
		} else {

		}

		return true;
	}

}
