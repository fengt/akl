package cn.com.akl.rmash.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class RMA_FXTH_ZX extends WorkFlowStepRTClassA{
	public RMA_FXTH_ZX(UserContext uc){
		super(uc);
		setVersion("RMA返新退货v1.0");
		setProvider("刘松");
		setDescription("自动装箱");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}

}
