package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

	public StepNo4Transaction() {
		super();
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("替换方案升级处理、缺货信息插入.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		boolean passFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "通过");
		if (!passFlag)
			return true;

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			// service(conn, bindid);
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
	 * 1、将替换的物料更新上去.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
		RepositoryBiz repositoryBiz = new RepositoryBiz();

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				int id = reset.getInt("ID");
				String thfacp = reset.getString("THFACP");
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("WLBH", thfacp);
				repositoryBiz.fillDeliveryMaterialInfo(conn, hashtable);
				BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_WXJF_S", hashtable, id);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

}
