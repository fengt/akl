package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	/**
	 * 查询订单号.
	 */
	private static final String QUERY_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 仓库代码.
	 */
	private static final String QUERY_CKDM = "SELECT CKDM FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 仓库名称.
	 */
	private static final String QUERY_CKMC = "SELECT CKMC FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询询单物料.
	 */
	private static final String QUERY_WLXX = "SELECT WLBH, WLMC, WLGG, XH, ZL, TJ, KHSPBH, XSZDJ, WOS, SDZT, JLDW, DFSL, XSDJ, POSFALX, POSID, POSMC, POSZCDJ, POSJE, POSZCSL, FLFS, FLFAH, FLFAMC, FLZCJ, FLZCD, FLSL FROM BO_AKL_WXB_XSDD_XDWL WHERE BINDID=?";

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第一个节点办理完后事件: 处理 1、锁定物品 2、改变订单状态为询单已生效 3、作废时更新状态询单未生效");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();
		ProcessMaterialBiz pmBiz = new ProcessMaterialBiz();

		// 作废标记
		boolean zfFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "作废");
		Connection conn = null;
		PreparedStatement state = null;
		ResultSet reset = null;

		try {
			conn = DAOUtil.openConnectionTransaction();
			if (zfFlag) {
				// 作废处理
				// 订单状态为已结束、状态为询单作废
				// DAOUtil.executeUpdate(conn,
				// "update BO_AKL_WXB_XSDD_HEAD set DDZT=?, ZT=? WHERE BINDID=?", 1, 1, bindId);
			} else {
				// 正常处理
				// 2、根据物料拆分货物。
				String ddid = DAOUtil.getString(conn, QUERY_DDID, bindid);
				String ckdm = DAOUtil.getString(conn, QUERY_CKDM, bindid);
				String ckmc = DAOUtil.getString(conn, QUERY_CKMC, bindid);
				DAOUtil.executeQueryForParser(conn, QUERY_WLXX, pmBiz.getAllocationResultPaser(bindid, uid, ddid, ckdm, ckmc), bindid);
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
			DBSql.close(conn, state, reset);
		}
	}
}
