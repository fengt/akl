package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo6Transaction extends WorkFlowStepRTClassA {

	public StepNo6Transaction() {
		super();
	}

	public StepNo6Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第六节点流程流转事件：改变销售订单状态");
	}
	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			final String xsdh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGXS_P SET ZT=? WHERE XSDDID=?", "已签收", xsdh);
			conn.commit();
			return true;
		} catch (SQLException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
