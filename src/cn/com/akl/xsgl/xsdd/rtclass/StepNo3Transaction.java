package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_DS = "SELECT WLBH, POSID, POSFALX, POSJE, POSZCSL, FLFAH, FLSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第三个节点办理完后事件: 处理 1、回退POS、回退返利");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// 同意标记
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "同意");
		SalesOrderBiz xsddbiz = new SalesOrderBiz();

		Connection conn = null;

		try {
			conn = DAOUtil.openConnectionTransaction();

			if (!tyFlag) {
				// 回退返利和POS
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, xsddbiz.getRollbackFLAndPOSPaser(), bindid);
			}
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
