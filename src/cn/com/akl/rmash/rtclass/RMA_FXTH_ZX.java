package cn.com.akl.rmash.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class RMA_FXTH_ZX extends WorkFlowStepRTClassA{
	public RMA_FXTH_ZX(UserContext uc){
		super(uc);
		setVersion("RMA�����˻�v1.0");
		setProvider("����");
		setDescription("�Զ�װ��");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}

}
