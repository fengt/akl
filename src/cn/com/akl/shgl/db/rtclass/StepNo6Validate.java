package cn.com.akl.shgl.db.rtclass;

import java.sql.Connection;

import cn.com.akl.shgl.db.biz.DBValidater;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo6Validate extends WorkFlowStepRTClassA {

	private DBValidater validater = new DBValidater();

	public StepNo6Validate() {
		super();
	}

	public StepNo6Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("校验物流信息是否填写");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		try {
			conn = DBSql.open();
			return validater.validateWLZT(conn, bindid, uid);
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

}
