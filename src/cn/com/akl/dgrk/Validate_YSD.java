package cn.com.akl.dgrk;

import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Validate_YSD extends WorkFlowStepRTClassA {

	public Validate_YSD() {
	}

	public Validate_YSD(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验运输单是否正确填写");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		//读取运输单头信息
//		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_YD_P", bindid);
//		String ddh = rkdtData.get("DDH") == null ?"":rkdtData.get("DDH").toString();//订单号
//		String cys = rkdtData.get("CYS") == null ?"":rkdtData.get("CYS").toString();//承运商
//		if("".equals(ddh) || "".equals(cys)){
//			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "运输单头信息不全！！！");
//			return false;
//		}
		//读取运输单身信息
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "调度")){
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_YD_S", bindid);
			if(vc == null){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "运输单身信息为空！！！");
				return false;
			}
		}
		return true;
	}
}
