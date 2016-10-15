package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class DGCK_StepNo2RuleJump extends WorkFlowStepJumpRuleRTClassA{
	public DGCK_StepNo2RuleJump(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		setVersion("���ܳ�������v1.0");
		setProvider("����");
		setDescription("���ڴ���ڶ��ڵ���ת����");
	}
	/**
	 * �˹������˹���˲˵������ˡ���
		ϵͳ����
		������Ƿ�ԤԼ=���ǡ�������·����003��
		������Ƿ�ԤԼ=���񡱡�����·����004�ڵ㡣
		�˹������˹���˲˵�����������������
		ϵͳ����
		������Ƿ�ԤԼ=���ǡ�������·����003��
		������Ƿ�ԤԼ=���񡱡�����·����005�ڵ㡣
		*/

	@Override
	public int getNextNodeNo() {
		// TODO Auto-generated method stub
		String sql = null;
		String sfyy = null;
		//��ȡ����ʵ��ID
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		//��ȡ����ʵ��ID
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Connection conn = DBSql.open();
		try {
			boolean zy = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "����");
			boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");
			if(th){
				return WorkFlowUtil.getPreviousStepNo(conn, bindid, 3);
			}
			boolean zjck = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ֱ�ӳ���");
			if(zjck){
				return 9999;
			}
			sql = "select SFYY from BO_BO_AKL_DGCK_P where bindid = "+bindid;
			sfyy = DBSql.getString(conn, sql, "SFYY")==null?"":DBSql.getString(conn, sql, "SFYY");
			if(sfyy.equals("��")){
				return zy?5:6;
			}
			else{
				return 4;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			DBSql.close(conn, null, null);
			e.printStackTrace();
			return 0;
		}
		finally{
			DBSql.close(conn, null, null);
		}
		
	}

	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		return null;
	}

}
