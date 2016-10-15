/**
 * 
 */
package cn.com.akl.ccgl.cgrk.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * @author wjj
 *
 */
public class StepNo2RuleJump extends WorkFlowStepJumpRuleRTClassA {

	/**
	 * @param arg0
	 */
	public StepNo2RuleJump(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA#getNextNodeNo()
	 */
	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = DBSql.getInt("SELECT TOP 1 ID FROM WF_TASK_LOG WHERE BIND_ID=" + bindid + " ORDER BY ENDTIME DESC", "ID");

		boolean isZt = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "自提");
		boolean isWw = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "委外");
		boolean isCj = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "厂家送货");
		if(isZt){
			return 3;
		}
		if(isWw){
			return 4;
		}
		if(isCj){
			return 5;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA#getNextTaskUser()
	 */
	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
