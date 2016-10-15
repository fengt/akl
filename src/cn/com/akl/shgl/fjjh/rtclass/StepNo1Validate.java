package cn.com.akl.shgl.fjjh.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo1Validate() {
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("");
	}

	@Override
	public boolean execute() {
		
		return false;
	}

}
