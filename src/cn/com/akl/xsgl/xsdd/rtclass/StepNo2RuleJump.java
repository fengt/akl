package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo2RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�ڵ����������ת�¼���ϵͳ�����޷�ʵ�ֵļ������ܣ� ����δ�����ë�������۶����µ��۸�������ָ���۲�����POS�ʽ��ѡ�񡢳����Ŷ�ȵ�������Զ���ת���������ڲ���ˡ����̽ڵ㣬ϵͳ·����003�ڵ�");
	}

	@Override
	public int getNextNodeNo() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�ύ����");
		SalesOrderBiz xsddbiz = new SalesOrderBiz();

		if (tyFlag) {
			Connection conn = null;
			try {
				conn = DBSql.open();
				conn.setAutoCommit(true);
				xsddbiz.otherCaseValidate(conn, bindid);
				xsddbiz.validateSalesOrderFormBodyGrossMarginRate(conn, bindid);
				return 9999;
			} catch (RuntimeException e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return 0;
			} catch (Exception e) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "����RuleJump�¼���������!");
				e.printStackTrace();
				return this.getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();
			} finally {
				DBSql.close(conn, null, null);
			}
		} else {
			return 0;
		}
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}

}
