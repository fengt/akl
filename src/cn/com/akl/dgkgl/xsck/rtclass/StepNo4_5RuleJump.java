package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4_5RuleJump extends WorkFlowStepJumpRuleRTClassA{
	public StepNo4_5RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("������ڵ���תϵͳ����");
	}
	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�˻�");
		Connection conn = null;
		try{
			conn =  DBSql.open();
			
			String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
			
			// 2.װ�� 3.���� 4.ԤԼ 5.���� 6.���ɵ������˵�
			// 0: ��  1:��
			if(th){
				if(sfyy.equals("��"))
					return 4;
				else
					return 3;
			}
			else
				return 7;
		} catch(Exception e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return 0;
		} finally{
			DBSql.close(conn, null, null);
		}
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}
}
