package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4_5RuleJump extends WorkFlowStepJumpRuleRTClassA{
	public StepNo4_5RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第四五节点跳转系统规则");
	}
	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		Connection conn = null;
		try{
			conn =  DBSql.open();
			
			String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
			
			// 2.装箱 3.出库 4.预约 5.调度 6.生成第三方运单
			// 0: 否  1:是
			if(th){
				if(sfyy.equals("是"))
					return 4;
				else
					return 3;
			}
			else
				return 7;
		} catch(Exception e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return 0;
		} finally{
			DBSql.close(conn, null, null);
		}
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}
}
