package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ���۶�������.
	 */
	private static final String QUERY_XSDD_DS = "SELECT WLBH, POSID, POSFALX, POSJE, POSZCSL, FLFAH, FLSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�������ڵ��������¼�: ���� 1������POS�����˷���");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// ͬ����
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "ͬ��");
		SalesOrderBiz xsddbiz = new SalesOrderBiz();

		Connection conn = null;

		try {
			conn = DAOUtil.openConnectionTransaction();

			if (!tyFlag) {
				// ���˷�����POS
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, xsddbiz.getRollbackFLAndPOSPaser(), bindid);
			}
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
