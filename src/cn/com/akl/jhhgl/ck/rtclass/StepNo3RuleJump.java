package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo3RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��һ�ڵ���תϵͳ����");
	}

	/*		ϵͳ����			
	 * 		������Ƿ�ԤԼ=���ǡ�������·����002��
			������Ƿ�ԤԼ=���񡱡�����·����003�ڵ㡣
			�˹������˹���˲˵�����������������
			ϵͳ����
			������Ƿ�ԤԼ=���ǡ�������·����002��
			������Ƿ�ԤԼ=���񡱡�����·����004�ڵ㡣
	 */
	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		
		Connection conn = null;
		try{
			conn =  DBSql.open();
			boolean zy = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "����");
			String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			if(zy)
				return 9999;
			// 2.װ�� 3.���� 4.ԤԼ 5.���� 6.���ɵ������˵�
			
			// 0: ��  1:��
			/*switch(sfyy){
				case 0: return zy?5:6;
				case 1: return 4;
			}*/
			if("025001".equals(sfyy)){
				return zy?5:6;
			}else if("025000".equals(sfyy)){
				return 4;
			}else {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�Ƿ�Ԥ�յ�ȡֵ���� '��'��'��'������ֵ����������");
				return -1;
			}
		} catch(Exception e){
			e.printStackTrace();
			DBSql.close(conn, null, null);
		}
		return 0;
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}

}
