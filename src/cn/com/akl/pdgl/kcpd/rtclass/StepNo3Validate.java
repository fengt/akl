package cn.com.akl.pdgl.kcpd.rtclass;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Validate extends WorkFlowStepRTClassA {

	public StepNo3Validate() {
		// TODO Auto-generated constructor stub
	}

	public StepNo3Validate(UserContext arg0) {
		super(arg0);
		setDescription("У�鵥���ӱ�ʵ��������ֵ");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int tastid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean audit_disagree = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, "��ͬ��");//��ȡ��ǰ��˲˵�
		
		String str0 = "select PKSJSL from BO_AKL_KCPD_S where bindid = "+bindid;
		String str1 = "update BO_AKL_KCPD_S set PKSJSL=0, CYSL=0, CYYY='' where bindid="+bindid;
		
		//1���̿�ʵ����������Ϊ��
		/*int pksjsl = DBSql.getInt(str0, "PKSJSL");
		if(pksjsl == 0){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ʵ������������Ϊ��!", true);
			return false;
		}*/
		
		//2�����Ϊ��ͬ��
		if(audit_disagree){
			 DBSql.executeUpdate(str1);
		}
		
		
		return true;
	}

}
