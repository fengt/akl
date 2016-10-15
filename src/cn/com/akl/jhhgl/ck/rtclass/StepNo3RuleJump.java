package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo3RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第一节点跳转系统规则");
	}

	/*		系统规则：			
	 * 		如果【是否预约=“是”】，则路由至002；
			如果【是否预约=“否”】，则路由至003节点。
			人工规则：人工审核菜单【第三方物流】；
			系统规则：
			如果【是否预约=“是”】，则路由至002；
			如果【是否预约=“否”】，则路由至004节点。
	 */
	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		
		Connection conn = null;
		try{
			conn =  DBSql.open();
			boolean zy = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "自提");
			String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			if(zy)
				return 9999;
			// 2.装箱 3.出库 4.预约 5.调度 6.生成第三方运单
			
			// 0: 否  1:是
			/*switch(sfyy){
				case 0: return zy?5:6;
				case 1: return 4;
			}*/
			if("025001".equals(sfyy)){
				return zy?5:6;
			}else if("025000".equals(sfyy)){
				return 4;
			}else {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "是否预收的取值不是 '是'、'否'这两个值，请修正！");
				return -1;
			}
		} catch(Exception e){
			e.printStackTrace();
			DBSql.close(conn, null, null);
		}
		return 0;
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}

}
