package cn.com.akl.ccgl.qssl;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo1RuleJump extends WorkFlowStepJumpRuleRTClassA{

	public StepNo1RuleJump(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("����ǩ������¼�����̣���ת�¼�����ѡ��ֱ�ӳ�������һ�ڵ�������޲���");
	}
	@Override
	public int getNextNodeNo() {
		// TODO Auto-generated method stub
		//		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		boolean qtck = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "��������");

		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
			if(qtck){
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "0", bindid);
				int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_DGCK_QSSL_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);

				int counts = DAOUtil.getInt(conn, "SELECT COUNT(*) SL FROM (SELECT CKDH FROM BO_AKL_DGCK_QSSL_S WHERE BINDID = ? GROUP BY CKDH) A", bindid);

				//����ʵ�ʳ�������-ǩ�ճ��������������0���޸ĳ��ⵥ�Ķ���״̬Ϊǩ�ղ���
				if(count!=0&&counts==1){
					// ���³��ⵥ״̬ -> ����״̬  �������״̬���������۶�������Ҫ�޸�����
					// ���µ�ǰ���ⵥ״̬Ϊ����״̬
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "1", bindid);
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "ǩ�����������������������ǩ�ղ��������֣�", true);
				} else {
					// ���µ�ǰ���ⵥ״̬Ϊ����״̬
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "0", bindid);
				}
				return 9999;
			}
			else{
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "0", bindid);
			}
			conn.commit();
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return 0;
		} finally {
			DBSql.close(conn, null, null);
		}
		return 2;

	}
	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		return null;
	}

}

