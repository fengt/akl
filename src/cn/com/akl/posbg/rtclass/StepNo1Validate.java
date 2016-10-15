package cn.com.akl.posbg.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.posbg.biz.POSModifyBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private POSModifyBiz biz = new POSModifyBiz();

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("У��POS�޸ĵ��������Ƿ�С��POS��ʹ������");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			biz.validatePOS(conn, bindid);
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
