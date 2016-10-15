package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo8Transaction extends WorkFlowStepRTClassA {

	public StepNo8Transaction() {
		super();
	}

	public StepNo8Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("流程结束，计算实际出库数量-签收出量，如果不等于0，修改出库单的订单状态为签收差异上传附件，必填，附件字段名称：签收单。");
	}

	@Override
	public boolean execute() {
		// 上传附件，必填，附件字段名称：签收单。
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		
		Connection conn = null;
		
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
		if (backFlag) {
			return true;
		}

		try {
			conn = DAOUtil.openConnectionTransaction();
			int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_QSD_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
			// 计算实际出库数量-签收出量，如果不等于0，修改出库单的订单状态为签收差异
			if (count != 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "签收数量与出库数量不一致，在签收差异中体现，财务审核完毕后将会启动差异流程！", true);
			}
			
			// 上传附件，必填，附件字段名称：签收单。
			int qsdIsNull = DAOUtil.getInt(conn, "SELECT count(*) FROM BO_AKL_CKD_HEAD WHERE BINDID=? AND QSD is null", bindid);
			if (qsdIsNull == 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请上传签收单附件", true);
				return false;
			}
			
			// 更新销售订单为确认签收
			String xsddh = DAOUtil.getString(conn,"SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_QRQS, xsddh);

			conn.commit();
			return true;
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
