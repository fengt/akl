package cn.com.akl.shgl.dwrk.rtclass;

import cn.com.akl.shgl.dwrk.biz.DWRKBiz;
import cn.com.akl.shgl.dwrk.biz.DWRKValidater;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;

import java.sql.Connection;
import java.sql.SQLException;

public class StepNo3Validate extends WorkFlowStepRTClassA {

	private DWRKBiz dwrkBiz = new DWRKBiz();

	public StepNo3Validate() {
		super();
	}

	public StepNo3Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("验证物流单是否填写");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		try {
			conn = DBSql.open();
			return validate(conn, bindid);
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
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
	 */
	public boolean validate(Connection conn, int bindid) throws SQLException {
		DWRKValidater validate = new DWRKValidater();
		return validate.validateWLZT(conn, bindid, getUserContext().getUID());
	}

}
