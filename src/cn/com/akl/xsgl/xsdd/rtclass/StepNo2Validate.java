package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;
import cn.com.akl.xsgl.xsdd.biz.ProcessPOSBiz;
import cn.com.akl.xsgl.xsdd.biz.ProcessRebateBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Validate extends WorkFlowStepRTClassA {
	/**
	 * ��ѯ��ָ���ͻ�֮�������ͻ����������Ͽ�������
	 */
	private static final String QUERY_KYWLXX = "SELECT COUNT (*) FROM BO_AKL_WXB_XSDD_BODY s LEFT JOIN BO_AKL_WXB_XSDD_HEAD p ON s.bindid = p.bindid WHERE s.KYSL < s.DDSL AND p.KHID NOT IN (SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE DLBM='055') AND p.bindid = ?";
	/**
	 * ��ѯ���۶�����.
	 */
	private static final String QUERY_XSDD_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ���۶�������.
	 */
	private static final String QUERY_XSDD_DS = "SELECT WLBH, SUM(DDSL) as DDSL, PCH FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? GROUP BY PCH, WLBH";
	
	ProcessPOSBiz posbiz = new ProcessPOSBiz();
	ProcessRebateBiz flbiz = new ProcessRebateBiz();
	ProcessMaterialBiz pmBiz = new ProcessMaterialBiz();

	public StepNo2Validate() {
		super();
	}

	public StepNo2Validate(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("�ڶ����ڵ����̽ڵ����ǰУ���¼�: ����POS�Ƿ����");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// ͬ����
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�ύ����");
		if (!tyFlag) {
			return true;
		}

		// У��POS�Ľ�ֹ����>=��ǰ���ڻ�POS����Ϊ�ѽ����ģ���ʾ����POS����XXX�ѹ��ڡ����̲����°���
		// 1.���������ӵ�е�����POS����
		// 2. ����Ƿ��й��ڵ�
		// 3. ����Ƿ��н�����
		Connection conn = null;
		try {
			conn = DBSql.open();

			posbiz.validatePOSFAEqualsFA(conn, bindid);
			posbiz.validatePOSFAEqualsZJC(conn, bindid);
			posbiz.validatePOSTimeOut(conn, bindid);

			flbiz.validateFLTimeOut(conn, bindid);
			flbiz.validateFLSL(conn, bindid);

			String ddid = DAOUtil.getStringOrNull(conn, QUERY_XSDD_DDID, bindid);

			// У���������������Ƿ����.
			DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, pmBiz.getValidateRepository(ddid), bindid);

			int count = DAOUtil.getInt(conn, QUERY_KYWLXX, bindid);
			if (count > 0) {
				MessageQueue.getInstance().putMessage(uid, "�����۶������ж����������ڿ����������ݴ��ڣ����飡", true);
				return false;
			}
			// �ύʱУ����д�ķ���֧�ֽ�������ڷ����������еķ���֧�ֽ����ֹ�ύ������ʾ�����������еķ���֧�ֽ��ֵ

			return true;
		} catch (RuntimeException e) {
			MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			MessageQueue.getInstance().putMessage(uid, "��̨�����쳣���������̨", true);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
