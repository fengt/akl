package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {
	/**
	 * 查询客户ID.
	 */
	private static final String QUERY_KHID = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_DS = "SELECT FLFAH, FLFS, JJZE, ID, FLZCJ, WLBH, POSID, POSFALX, POSJE, POSZCSL, FLSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";

	public StepNo4Transaction() {
		super();
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第四个节点办理完后事件: 处理 1、回退POS、回退返利。 2、流程结束返利处理");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();
		SalesOrderBiz xsddbiz = new SalesOrderBiz();
		// 同意标记
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "同意");
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			if (tyFlag) {
				String khid = DAOUtil.getStringOrNull(conn, QUERY_KHID, bindid);
				// 更新后返利.
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, xsddbiz.getUpdateHFLResultPaser(bindid, uid, khid), bindid);
			} else {
				// 回退返利和POS
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, xsddbiz.getRollbackFLAndPOSPaser(), bindid);
			}
			conn.commit();
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

		if (tyFlag) {
			Connection conn2 = null;
			try {
				conn2 = DBSql.open();
				xsddbiz.startCKDProcess(conn2, bindid, uid);
			} catch (RuntimeException e) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "子流程启动失败，请手动启动流程!", true);
			} finally {
				DBSql.close(conn2, null, null);
			}
		}

		return true;
	}

}
