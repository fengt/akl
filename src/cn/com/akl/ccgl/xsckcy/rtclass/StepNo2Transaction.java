package cn.com.akl.ccgl.xsckcy.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA{

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("���̰����¼�:ͬ�⣬д���������ⵥ���������ϵͳ���ͳ��ⵥ");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ͬ��");
		if(tyFlag){
			// д���������ⵥ���������ϵͳ���ͳ��ⵥ
		}
		
		return true;
	}

}
