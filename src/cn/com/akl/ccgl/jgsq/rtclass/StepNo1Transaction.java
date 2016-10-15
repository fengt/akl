package cn.com.akl.ccgl.jgsq.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.ccgl.jgsq.biz.JGSQBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		JGSQBiz jgsqBiz = new JGSQBiz();

		// 锁定库存
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			jgsqBiz.splitWL(conn, bindid, uid);

			// 计算总代成本
			BigDecimal zdcb = DAOUtil.getBigDecimalOrNull(conn, "SELECT SUM(ZDCB*JGSL) FROM BO_AKL_JGSQ_WLMX_S WHERE BINDID=?", bindid);
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_JGSQ_CP_S SET ZDCB=?/SL WHERE BINDID=?", zdcb, bindid);

			conn.commit();
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
			DAOUtil.connectRollBack(conn);
			return false;
		} catch (SQLException e) {
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现问题，请联系管理员!");
			e.printStackTrace();
			DAOUtil.connectRollBack(conn);
			return false;
		} catch (AWSSDKException e) {
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现问题，请联系管理员!");
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
