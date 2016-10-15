package cn.com.akl.ccgl.wply.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	public StepNo2Transaction() {
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
		setDescription("审核不同意时，删除锁库信息。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// 获取退回审核菜单.
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "不同意");
		
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			if(backFlag){
				//1、删除锁库
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindid);
			}
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
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
