package cn.com.akl.cwgl.gysfyzc.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepN2Validate extends WorkFlowStepRTClassA{
	
	public StepN2Validate() {
		super();
	}

	public StepN2Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��Ӧ�̷���֧���������̣��ڶ����ڵ�Ľڵ��У���¼������ڼ���Ƿ���д�����ϵ�TPM��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = this.getParameter(PARAMETER_TASK_ID).toInt();
		
		// ��ȡ��˲˵����
		boolean flag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "����ͨ��");
		
		if(flag){
				Connection conn = null;
				try{
					conn = DBSql.open();
					
					int count = DAOUtil.getInt(conn, "select count(*) c from BO_AKL_WXB_XS_POS_BODY  where Bindid=? and (TPM is null or TPM='')", bindid);
					if(count >0){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��TPM��Ϊ�գ�����дTPM�ţ�");
						return false;
					}
					
					count = DAOUtil.getInt(conn, "select count(*) c from BO_AKL_WXB_XS_POS_HEAD  where Bindid=? and (TPM is null or TPM='')", bindid);
					if(count > 0){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��TPM��Ϊ�գ�����дTPM�ţ�");
						return false;
					}
					return true;
				} catch(Exception e){
					e.printStackTrace();
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
					return true;
				} finally {
					DBSql.close(conn, null, null);
				}
		}
		
		return true;
	}

}
