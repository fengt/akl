package cn.com.akl.shgl.db.rtclass;

import cn.com.akl.shgl.db.biz.DBValidater;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

import java.sql.Connection;

public class StepNo8Validate extends WorkFlowStepRTClassA {

	private DBValidater validater = new DBValidater();

	public StepNo8Validate() {
		super();
	}

	public StepNo8Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("У��������Ϣ�Ƿ���д");
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
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
