package cn.com.akl.shgl.dwck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.dwck.biz.DWCKBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	private DWCKBiz dwckBiz = new DWCKBiz();

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("根据汇总插入明细.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

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
	 * 
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
		Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_SH_DWCK_P", bindid);
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_DWCK_S", bindid);

		dwckBiz.removeLock(conn, bindid);
		dwckBiz.insertHzToMx(conn, bindid, getUserContext().getUID(), boData);
	}

}
