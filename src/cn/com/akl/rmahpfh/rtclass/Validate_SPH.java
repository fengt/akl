package cn.com.akl.rmahpfh.rtclass;

import java.util.Hashtable;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Validate_SPH extends WorkFlowStepRTClassA {

	public Validate_SPH() {
	}

	public Validate_SPH(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验RMA审批号是否为空");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
		String rmasph = rkdtData.get("RMASPH") == null ?"":rkdtData.get("RMASPH").toString();//RMA审批号
		if("".equals(rmasph)){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "RMA审批号为空，请检查!");
			return false;
		}
		return true;
	}
}
