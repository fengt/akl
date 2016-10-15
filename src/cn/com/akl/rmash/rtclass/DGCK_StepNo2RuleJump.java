package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class DGCK_StepNo2RuleJump extends WorkFlowStepJumpRuleRTClassA{
	public DGCK_StepNo2RuleJump(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		setVersion("代管出库流程v1.0");
		setProvider("刘松");
		setDescription("用于处理第二节点跳转规则");
	}
	/**
	 * 人工规则：人工审核菜单【自运】；
		系统规则：
		如果【是否预约=“是”】，则路由至003；
		如果【是否预约=“否”】，则路由至004节点。
		人工规则：人工审核菜单【第三方物流】；
		系统规则：
		如果【是否预约=“是”】，则路由至003；
		如果【是否预约=“否”】，则路由至005节点。
		*/

	@Override
	public int getNextNodeNo() {
		// TODO Auto-generated method stub
		String sql = null;
		String sfyy = null;
		//获取任务实例ID
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		//获取流程实例ID
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Connection conn = DBSql.open();
		try {
			boolean zy = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "自运");
			boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
			if(th){
				return WorkFlowUtil.getPreviousStepNo(conn, bindid, 3);
			}
			boolean zjck = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "直接出库");
			if(zjck){
				return 9999;
			}
			sql = "select SFYY from BO_BO_AKL_DGCK_P where bindid = "+bindid;
			sfyy = DBSql.getString(conn, sql, "SFYY")==null?"":DBSql.getString(conn, sql, "SFYY");
			if(sfyy.equals("否")){
				return zy?5:6;
			}
			else{
				return 4;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			DBSql.close(conn, null, null);
			e.printStackTrace();
			return 0;
		}
		finally{
			DBSql.close(conn, null, null);
		}
		
	}

	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		return null;
	}

}
