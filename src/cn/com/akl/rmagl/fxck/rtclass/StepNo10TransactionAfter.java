package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;


import cn.com.akl.u8.senddata.SendStoreOutDate;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo10TransactionAfter extends WorkFlowStepRTClassA {

	private Connection conn = null;
	@SuppressWarnings("unused")
	private UserContext uc;

	public StepNo10TransactionAfter(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("wjj");
		setDescription("RMA返新出库接口测试");

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		// 审核菜单判断
		boolean tgFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "不同意");
		if (tgFlag)
			return true;

		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData(
				"BO_AKL_CKD_HEAD", bindid);
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas(
				"BO_AKL_CKD_BODY", bindid);
		try {

				SendStoreOutDate ssod = new SendStoreOutDate();
				ssod.sendData(head, body);
			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(conn, null, null);
		}
		return false;
	}

}
