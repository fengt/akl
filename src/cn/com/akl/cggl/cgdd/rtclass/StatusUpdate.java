package cn.com.akl.cggl.cgdd.rtclass;

import cn.com.akl.cggl.cgdd.constant.CgddConstant;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StatusUpdate extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StatusUpdate() {
	}

	public StatusUpdate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("���²ɹ����붩��״̬��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int tastid = this.getParameter(PARAMETER_TASK_ID).toInt();
		int currentStepID = this.getParameter(PARAMETER_WORKFLOW_STEP_ID).toInt();
		
		int currentStepNO = DBSql.getInt("SELECT * FROM SYSFLOWSTEP WHERE ID="+currentStepID, "STEPNO");//��ȡ��ǰ�ڵ���
		boolean agree = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, "ͬ��");//��ȡ��˲˵�
		
		if(agree){//ͬ��
			if(currentStepNO == 3){
				String update_head_zt = "UPDATE " +CgddConstant.tableName0+ " SET DZT='"+CgddConstant.dzt1+"',DDZT='"+CgddConstant.zt+"' WHERE BINDID="+bindid;
				String update_body_zt = "UPDATE " +CgddConstant.tableName1+ " SET ZT='"+CgddConstant.zt+"' WHERE BINDID="+bindid;
				DBSql.executeUpdate(update_head_zt);
				DBSql.executeUpdate(update_body_zt);
			}
		}else{//��ͬ��
			String update_head_zt = "UPDATE " +CgddConstant.tableName0+ " SET DZT='"+CgddConstant.dzt2+"',DDZT='"+CgddConstant.dzt2+"',JSHJ="+CgddConstant.jshj+" WHERE BINDID="+bindid;
			String update_body_zt = "UPDATE " +CgddConstant.tableName1+ " SET ZT='"+CgddConstant.dzt2+"' WHERE BINDID="+bindid;
			DBSql.executeUpdate(update_head_zt);
			DBSql.executeUpdate(update_body_zt);
		}
		return false;
	}

}
