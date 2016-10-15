package cn.com.akl.rmash.rtclass;

import java.sql.Connection;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo5Transaction extends WorkFlowStepRTClassA {

	public StepNo5Transaction(UserContext uc) {
		super(uc);
		setVersion("1.0.0");
		setDescription("若果选择退回,清除写入库存的数据");
	}

	@Override
	public boolean execute() {
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
		if (th) {
			Connection conn = null;
			try {
				conn = DBSql.open();
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_RMA_KCMX", bindid);
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}

}
