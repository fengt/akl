package cn.com.akl.zsj.wlxx.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Transaction extends WorkFlowStepRTClassA {
	
	private static final String QUERY_WLXX_WLBH_REPEATCOUNT = "SELECT XH FROM BO_AKL_WLXX a WHERE BINDID=? AND XH in (SELECT XH FROM BO_AKL_WLXX WHERE a.ID<>ID AND HZBM=a.HZBM)";

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setDescription("����У��");
		setVersion("1.0.0");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			String xh = DAOUtil.getStringOrNull(conn, QUERY_WLXX_WLBH_REPEATCOUNT, bindid);
			if(xh != null){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͺţ�"+xh+"�� ��ϵͳ���Ѵ��ڣ�", true);
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա!", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
