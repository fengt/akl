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
		setDescription("节点二的流程跳转事件：系统规则无法实现的几个功能： 逾期未付款、负毛利、销售订单下单价格与销售指导价不符、POS资金池选择、超授信额度的情况，自动跳转至“网销内部审核”流程节点，系统路由至003节点");
	}

	@Override
	public int getNextNodeNo() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "提交订单");
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
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "流程RuleJump事件出现问题!");
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
