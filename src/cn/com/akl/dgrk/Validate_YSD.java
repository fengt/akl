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
		setDescription("У�����䵥�Ƿ���ȷ��д");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		//��ȡ���䵥ͷ��Ϣ
//		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_YD_P", bindid);
//		String ddh = rkdtData.get("DDH") == null ?"":rkdtData.get("DDH").toString();//������
//		String cys = rkdtData.get("CYS") == null ?"":rkdtData.get("CYS").toString();//������
//		if("".equals(ddh) || "".equals(cys)){
//			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "���䵥ͷ��Ϣ��ȫ������");
//			return false;
//		}
		//��ȡ���䵥����Ϣ
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "����")){
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_YD_S", bindid);
			if(vc == null){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "���䵥����ϢΪ�գ�����");
				return false;
			}
		}
		return true;
	}
}
