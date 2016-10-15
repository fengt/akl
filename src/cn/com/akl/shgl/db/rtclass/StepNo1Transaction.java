package cn.com.akl.shgl.db.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.db.biz.DBBiz;
import cn.com.akl.shgl.db.biz.DBConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	private DBBiz dbBiz = new DBBiz();

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("插入锁库");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// 若路由走出库，则不需要装箱.
		boolean ckFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "出库");
		if (ckFlag) {
			return true;
		}

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			service(conn, bindid);
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 将汇总插入明细并锁库.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
		String uid = getUserContext().getUID();
		String xmlb = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_XMLX, bindid);
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DB_S", bindid);
		dbBiz.removeLock(conn, bindid);
		dbBiz.insertHzToMx(conn, bindid, uid, xmlb);
	}

	public String convertNull(String str) {
		return str == null ? "" : str;
	}

}
