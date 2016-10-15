package cn.com.akl.shgl.db.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.db.biz.DBBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo5Transaction extends WorkFlowStepRTClassA {

	private DBBiz dbBiz = new DBBiz();

	public StepNo5Transaction() {
		super();
	}

	public StepNo5Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("出库， 自动更新收货货位代码默认值");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		boolean ckFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "出库");
		if (!ckFlag) {
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
	 * 扣库存，并添加库存，状态为在途. 自动更新收货货位代码默认值.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
		String uid = getUserContext().getUID();

		Hashtable<String, String> pData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DB_P", bindid);
		String shckbm = pData.get("SHKFCKBM");

		dbBiz.removeLock(conn, bindid);
		dbBiz.insertWLDate(conn, bindid, uid, pData);
		dbBiz.wlToGo(conn, bindid, uid, pData);
		dbBiz.outerXLH(conn, bindid, uid, shckbm);
	}

}
