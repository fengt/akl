package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {
	/**
	 * ��ѯ�ͻ�ID.
	 */
	private static final String QUERY_KHID = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ���۶�������.
	 */
	private static final String QUERY_XSDD_DS = "SELECT FLFAH, FLFS, JJZE, ID, FLZCJ, WLBH, POSID, POSFALX, POSJE, POSZCSL, FLSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";

	public StepNo4Transaction() {
		super();
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���ĸ��ڵ��������¼�: ���� 1������POS�����˷����� 2�����̽�����������");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();
		SalesOrderBiz xsddbiz = new SalesOrderBiz();
		// ͬ����
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "ͬ��");
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			if (tyFlag) {
				String khid = DAOUtil.getStringOrNull(conn, QUERY_KHID, bindid);
				// ���º���.
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, xsddbiz.getUpdateHFLResultPaser(bindid, uid, khid), bindid);
			} else {
				// ���˷�����POS
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, xsddbiz.getRollbackFLAndPOSPaser(), bindid);
			}
			conn.commit();
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}

		if (tyFlag) {
			Connection conn2 = null;
			try {
				conn2 = DBSql.open();
				xsddbiz.startCKDProcess(conn2, bindid, uid);
			} catch (RuntimeException e) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "����������ʧ�ܣ����ֶ���������!", true);
			} finally {
				DBSql.close(conn2, null, null);
			}
		}

		return true;
	}

}
