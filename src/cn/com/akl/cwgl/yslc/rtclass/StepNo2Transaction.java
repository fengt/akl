package cn.com.akl.cwgl.yslc.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("�ڵ�����¼�:���ⵥ�����촦����ɺ������ϵͳ�в�������������");
	}

	@Override
	public boolean execute() {
		//���ⵥ�����촦����ɺ������ϵͳ�в������������ݣ�
		
		return true;
	}

}
