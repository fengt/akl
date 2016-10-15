package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ������.
	 */
	private static final String QUERY_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * �ֿ����.
	 */
	private static final String QUERY_CKDM = "SELECT CKDM FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * �ֿ�����.
	 */
	private static final String QUERY_CKMC = "SELECT CKMC FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯѯ������.
	 */
	private static final String QUERY_WLXX = "SELECT WLBH, WLMC, WLGG, XH, ZL, TJ, KHSPBH, XSZDJ, WOS, SDZT, JLDW, DFSL, XSDJ, POSFALX, POSID, POSMC, POSZCDJ, POSJE, POSZCSL, FLFS, FLFAH, FLFAMC, FLZCJ, FLZCD, FLSL FROM BO_AKL_WXB_XSDD_XDWL WHERE BINDID=?";

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��һ���ڵ��������¼�: ���� 1��������Ʒ 2���ı䶩��״̬Ϊѯ������Ч 3������ʱ����״̬ѯ��δ��Ч");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();
		ProcessMaterialBiz pmBiz = new ProcessMaterialBiz();

		// ���ϱ��
		boolean zfFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "����");
		Connection conn = null;
		PreparedStatement state = null;
		ResultSet reset = null;

		try {
			conn = DAOUtil.openConnectionTransaction();
			if (zfFlag) {
				// ���ϴ���
				// ����״̬Ϊ�ѽ�����״̬Ϊѯ������
				// DAOUtil.executeUpdate(conn,
				// "update BO_AKL_WXB_XSDD_HEAD set DDZT=?, ZT=? WHERE BINDID=?", 1, 1, bindId);
			} else {
				// ��������
				// 2���������ϲ�ֻ��
				String ddid = DAOUtil.getString(conn, QUERY_DDID, bindid);
				String ckdm = DAOUtil.getString(conn, QUERY_CKDM, bindid);
				String ckmc = DAOUtil.getString(conn, QUERY_CKMC, bindid);
				DAOUtil.executeQueryForParser(conn, QUERY_WLXX, pmBiz.getAllocationResultPaser(bindid, uid, ddid, ckdm, ckmc), bindid);
			}
			conn.commit();
			return true;
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
			DBSql.close(conn, state, reset);
		}
	}
}
