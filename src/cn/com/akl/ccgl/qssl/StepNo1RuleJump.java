package cn.com.akl.ccgl.qssl;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo1RuleJump extends WorkFlowStepJumpRuleRTClassA{

	public StepNo1RuleJump(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("网销签收数量录入流程：跳转事件，若选择直接出库且下一节点更新有无差异");
	}
	@Override
	public int getNextNodeNo() {
		// TODO Auto-generated method stub
		//		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		boolean qtck = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "其他出库");

		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
			if(qtck){
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "0", bindid);
				int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_DGCK_QSSL_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);

				int counts = DAOUtil.getInt(conn, "SELECT COUNT(*) SL FROM (SELECT CKDH FROM BO_AKL_DGCK_QSSL_S WHERE BINDID = ? GROUP BY CKDH) A", bindid);

				//计算实际出库数量-签收出量，如果不等于0，修改出库单的订单状态为签收差异
				if(count!=0&&counts==1){
					// 更新出库单状态 -> 差异状态  如果订单状态不是在销售订单中需要修改这里
					// 更新当前出库单状态为差异状态
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "1", bindid);
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "签收数量与出库数量不付，在签收差异中体现！", true);
				} else {
					// 更新当前出库单状态为正常状态
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "0", bindid);
				}
				return 9999;
			}
			else{
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGCK_QSSL_P SET SFCY=? WHERE BINDID=?", "0", bindid);
			}
			conn.commit();
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return 0;
		} finally {
			DBSql.close(conn, null, null);
		}
		return 2;

	}
	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		return null;
	}

}

