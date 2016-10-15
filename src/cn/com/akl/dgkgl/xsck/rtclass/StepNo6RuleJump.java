package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo6RuleJump extends WorkFlowStepJumpRuleRTClassA{

	public StepNo6RuleJump(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		setVersion("1.0.0");
		setDescription("根据选择审核菜单：退回到上一节点");
	}

	@Override
	public int getNextNodeNo() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
//		this.getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();

		boolean TH = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			if(TH){
				return WorkFlowUtil.getPreviousStepNo(conn, bindid, 7);
			}
			else{
				return 9999;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return 0;
		}finally {
			DBSql.close(conn, null, null);
		}

	}

	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		return null;
	}

}
