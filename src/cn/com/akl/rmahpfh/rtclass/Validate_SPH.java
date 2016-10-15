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
		setDescription("У��RMA�������Ƿ�Ϊ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
		String rmasph = rkdtData.get("RMASPH") == null ?"":rkdtData.get("RMASPH").toString();//RMA������
		if("".equals(rmasph)){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "RMA������Ϊ�գ�����!");
			return false;
		}
		return true;
	}
}
