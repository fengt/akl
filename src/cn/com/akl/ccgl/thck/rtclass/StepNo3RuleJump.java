package cn.com.akl.ccgl.thck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo3RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("½áÊøÌø×ª");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		int stepNo = getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "ÍË»Ø");
		if (backFlag) {
			Connection conn = null;
			try {
				DBSql.open();
				return WorkFlowUtil.getPreviousStepNo(conn, bindid, stepNo);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				DBSql.close(conn, null, null);
			}
		} else {
			return 9999;
		}
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}

}
