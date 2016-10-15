package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {
	/**
	 * ��ѯ�ͻ�ID.
	 */
	private static final String QUERY_KHID = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ���۶�������.
	 */
	private static final String QUERY_XSDD_DS = "SELECT FLFAH, FLFS, JJZE, ID, FLZCJ, WLBH, POSID, POSFALX, POSJE, POSZCSL, FLSL, PCH, CKID, DDSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	/**
	 * �������۶�������״̬.
	 */
	private static final String UPDATE_XSDD_DDZT = "update BO_AKL_WXB_XSDD_HEAD set ZT=?, DDZT=? where bindid=?";
	/**
	 * ��ѯ������.
	 */
	private static final String QUERY_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * �����һ���ڵ�.
	 */
	private int nextStepNo;

	private SalesOrderBiz xsddbiz = new SalesOrderBiz();

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("������ת�¼�����POS��������");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// ��ȡ�¸��ڵ�
		try {
			nextStepNo = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, bindid, taskId);
		} catch (AWSSDKException e) {
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			e.printStackTrace();
			return false;
		}

		// ͬ����
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�ύ����");

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			if (tyFlag) {
				auditMenuIsYes(conn, bindid, uid);
			} else {
				auditMenuIsNo(conn, bindid);
			}
			conn.commit();
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}

		if (tyFlag && nextStepNo == -1) {
			Connection conn2 = null;
			try {
				// ����������
				conn2 = DBSql.open();
				xsddbiz.startCKDProcess(conn2, bindid, uid);
			} catch (RuntimeException e) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				e.printStackTrace();
			} catch (Exception e) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "����������ʧ�ܣ����ֶ���������!", true);
				e.printStackTrace();
			} finally {
				DBSql.close(conn2, null, null);
			}
		}

		return true;
	}

	/**
	 * ��˲˵�ѡ��ͬ��.
	 * 
	 * @param conn
	 * @param bindId
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void auditMenuIsNo(Connection conn, int bindId) throws SQLException, AWSSDKException {
		// ��˲˵�ѡ��ͬ��ʱ���������۶�������ɾ����������ļ�¼��
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindId);
		// ɾ���ֽ����������
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_WXB_XSDD_BODY", bindId);
	}

	/**
	 * ��˲˵�ͨ��.
	 * 
	 * @param conn
	 * @param bindid
	 * @param taskId
	 * @throws Exception
	 */
	public void auditMenuIsYes(Connection conn, int bindid, String uid) throws Exception {
		// ɾ��֮ǰ����ģ��ٲ���
		ProcessMaterialBiz skbiz = new ProcessMaterialBiz();
		skbiz.deleteSK(conn, bindid);

		// ���̽����󣬶�����Ч
		if (nextStepNo == -1) {
			// ��ѯ�ͻ�ID
			String khid = DAOUtil.getStringOrNull(conn, QUERY_KHID, bindid);
			// ����POS�ͷ�������ʹ����������
			DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, new DAOUtil.ResultPaser[] {
					// ����POS�ͷ���
					xsddbiz.getUpdateFLAndPOSPaser(),
					// ���º���
					xsddbiz.getUpdateHFLResultPaser(bindid, uid, khid),
					// ��������
					skbiz.getInsertLockRepositoryPaser(bindid, uid, DAOUtil.getStringOrNull(conn, QUERY_DDID, bindid)) }, bindid);
			// ���Ķ���״̬
			DAOUtil.executeUpdate(conn, UPDATE_XSDD_DDZT, XSDDConstant.ZT_JS, 3, bindid);
		} else {
			// ���·�����POS
			DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, new DAOUtil.ResultPaser[] { 
					// ���·�����POS.
					xsddbiz.getUpdateFLAndPOSPaser(), 
					// ��������
					skbiz.getInsertLockRepositoryPaser(bindid, uid, DAOUtil.getStringOrNull(conn, QUERY_DDID, bindid)) 
			}, bindid);
			// ���¶���״̬
			DAOUtil.executeUpdate(conn, UPDATE_XSDD_DDZT, XSDDConstant.ZT_JS, 2, bindid);
		}
	}

}
