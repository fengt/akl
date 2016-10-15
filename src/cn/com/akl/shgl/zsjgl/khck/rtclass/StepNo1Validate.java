package cn.com.akl.shgl.zsjgl.khck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setDescription("��֤�ͻ���Ϣά��ʱ���Ƿ�����ظ���¼.");
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
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
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
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM BO_AKL_KFCK WHERE BINDID=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String khmc = reset.getString("SJH");
				String khlx = reset.getString("DH");
				String kfckmc = reset.getString("KFCKMC");

				if ((khmc == null || "".equals(khmc.trim())) && (khlx == null || "".equals(khlx.trim()))) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�����¼���ͷ��ֿ⣺" + kfckmc + " ���绰���ֻ�������дһ����");
					return false;
				}
			}
			return true;
		} finally {
			DBSql.close(ps, reset);
		}
	}

}
