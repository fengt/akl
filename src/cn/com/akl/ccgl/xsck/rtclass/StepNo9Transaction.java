package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo9Transaction extends WorkFlowStepRTClassA {

	public StepNo9Transaction() {
		super();
	}

	public StepNo9Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第九节点办理事件：更新签收差异的状态，并控制启动子流程。");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// 审核菜单判断
		boolean tgFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "同意");

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();

			// 当审核菜单为不同意的时候，将出库单的差异状态改为无差异，防止意外启动子流程。
			if (!tgFlag) {
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 0, bindid);
			} else {
				int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_QSD_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
				// 当有差异或者审核菜单为不同意的时候，将出库单的差异状态变为
				if (count != 0) {
					// 更新当前出库单状态为差异状态
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 1, bindid);
				} else {
					// 更新当前出库单状态为正常状态
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 0, bindid);
				}
			}

			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}

	}

}
