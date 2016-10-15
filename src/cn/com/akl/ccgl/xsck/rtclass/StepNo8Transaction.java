package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo8Transaction extends WorkFlowStepRTClassA {

	public StepNo8Transaction() {
		super();
	}

	public StepNo8Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���̽���������ʵ�ʳ�������-ǩ�ճ��������������0���޸ĳ��ⵥ�Ķ���״̬Ϊǩ�ղ����ϴ���������������ֶ����ƣ�ǩ�յ���");
	}

	@Override
	public boolean execute() {
		// �ϴ���������������ֶ����ƣ�ǩ�յ���
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		
		Connection conn = null;
		
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");
		if (backFlag) {
			return true;
		}

		try {
			conn = DAOUtil.openConnectionTransaction();
			int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_QSD_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
			// ����ʵ�ʳ�������-ǩ�ճ��������������0���޸ĳ��ⵥ�Ķ���״̬Ϊǩ�ղ���
			if (count != 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "ǩ�����������������һ�£���ǩ�ղ��������֣����������Ϻ󽫻������������̣�", true);
			}
			
			// �ϴ���������������ֶ����ƣ�ǩ�յ���
			int qsdIsNull = DAOUtil.getInt(conn, "SELECT count(*) FROM BO_AKL_CKD_HEAD WHERE BINDID=? AND QSD is null", bindid);
			if (qsdIsNull == 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ϴ�ǩ�յ�����", true);
				return false;
			}
			
			// �������۶���Ϊȷ��ǩ��
			String xsddh = DAOUtil.getString(conn,"SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_QRQS, xsddh);

			conn.commit();
			return true;
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
