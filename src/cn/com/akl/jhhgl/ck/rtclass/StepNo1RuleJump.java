/**
 * 
 */
package cn.com.akl.jhhgl.ck.rtclass;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * @author hzy
 *
 */
public class StepNo1RuleJump extends WorkFlowStepJumpRuleRTClassA {

	/**
	 * @param arg0
	 */
	public StepNo1RuleJump(UserContext arg0) {
		super(arg0);
		setDescription("入库录入节点跳转.");
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA#getNextNodeNo()
	 */
	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String db = DBSql.getString("select * from BO_AKL_CCB_RKD_HEAD where bindid="+bindid, "rkdb");
		if(HHDJConstant.rkdb2.equals(db)){
			return 2;
		} else {
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
