package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo3RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��һ�ڵ���תϵͳ����");
	}

	/*
	 * ϵͳ���� ������Ƿ�ԤԼ=���ǡ�������·����002�� ������Ƿ�ԤԼ=���񡱡�����·����003�ڵ㡣 �˹������˹���˲˵����������������� ϵͳ����
	 * ������Ƿ�ԤԼ=���ǡ�������·����002�� ������Ƿ�ԤԼ=���񡱡�����·����004�ڵ㡣
	 */
	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		Connection conn = null;
		try {
			conn = DBSql.open();
			boolean zt = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "����");
			if (zt) {
				return 9999;
			}

			boolean zy = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "����");
			String sfyy = DAOUtil.getStringOrNull(conn, "SELECT SFYY FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);

			// 2.װ�� 3.���� 4.ԤԼ 5.���� 6.���ɵ������˵�
			boolean flag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�˻�");
			if (flag == true) {
				String fszx = DAOUtil.getStringOrNull(conn, "SELECT SFZX FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
				if ("��".equals(fszx) || XSDDConstant.NO.equals(fszx)) {
					return 1;
				} else {
					return 2;
				}
			}

			// 0: �� 1:��
			if ("��".equals(sfyy) || XSDDConstant.NO.equals(sfyy)) {
				return zy ? 5 : 6;
			} else if ("��".equals(sfyy) || XSDDConstant.YES.equals(sfyy)) {
				return 4;
			} else {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�Ƿ�Ԥ�յ�ȡֵ���� '��'��'��'������ֵ����������");
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return -1;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}

}
