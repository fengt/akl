package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.ccgl.xsck.biz.FillBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo5RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo5RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第五节点回退事件");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		int stepNo = getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
		if (backFlag) {
			Connection conn = null;
			try {
				conn = DBSql.open();
				FillBiz fillbiz = new FillBiz();
				return fillbiz.getPreviousStepNo(conn, bindid, stepNo);
			} catch (Exception e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
				return stepNo;
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
