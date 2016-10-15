package cn.com.akl.shgl.zsjgl.cpxx.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ���ϱ���Ƿ����ظ�.
	 */
	private String QUERY_REPEAT_WLBH = "SELECT WLBH FROM BO_AKL_CPXX wlxx WHERE BINDID=? AND (SELECT COUNT(*) FROM BO_AKL_CPXX wlxx2 WHERE wlxx.ID<>wlxx2.ID AND wlxx.WLBH=wlxx2.WLBH)";

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�����ύУ�飬���ϱ���Ƿ��ظ�.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			String wlbh = DAOUtil.getStringOrNull(conn, QUERY_REPEAT_WLBH, bindid);
			if (wlbh == null) {
				return true;
			} else {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ϱ�ţ�" + wlbh + "�������ظ������飡");
				return false;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
