package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo9Transaction extends WorkFlowStepRTClassA {

	public StepNo9Transaction() {
		super();
	}

	public StepNo9Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�ھŽڵ�����¼�������ǩ�ղ����״̬�����������������̡�");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// ��˲˵��ж�
		boolean tgFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "ͬ��");

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();

			// ����˲˵�Ϊ��ͬ���ʱ�򣬽����ⵥ�Ĳ���״̬��Ϊ�޲��죬��ֹ�������������̡�
			if (!tgFlag) {
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 0, bindid);
			} else {
				int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_QSD_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
				// ���в��������˲˵�Ϊ��ͬ���ʱ�򣬽����ⵥ�Ĳ���״̬��Ϊ
				if (count != 0) {
					// ���µ�ǰ���ⵥ״̬Ϊ����״̬
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 1, bindid);
				} else {
					// ���µ�ǰ���ⵥ״̬Ϊ����״̬
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 0, bindid);
				}
			}

			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}

	}

}
