package cn.com.akl.ccgl.qtck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo6AfterSave extends WorkFlowStepRTClassA {

	public StepNo6AfterSave() {
		super();
	}

	public StepNo6AfterSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第六节点:填充签收单");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
		String uid = getUserContext().getUID();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;

		if (!"BO_AKL_YD_P".equals(tableName)) {
			return false;
		}

		try {
			conn = DAOUtil.openConnectionTransaction();
			FillBiz fillbiz = new FillBiz();
			fillbiz.fillQSDHead(conn, bindid, uid);
			fillbiz.fillQSDBody(conn, bindid, uid);
			conn.commit();
			return true;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}
}
