package cn.com.akl.shgl.dwck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.shgl.dwck.biz.DWCKConstnat;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
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
		setDescription("��֤����Ƿ����.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		try {
			conn = DBSql.open();
			validate(conn, bindid);
			return true;
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
	 * ��֤����Ƿ���㣬������������Ϊ0.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validate(Connection conn, int bindid) throws SQLException {
		RepositoryBiz repositoryBiz = new RepositoryBiz();

		String xmlb = DAOUtil.getStringOrNull(conn, DWCKConstnat.QUERY_DWCK_XMLX, bindid);

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(DWCKConstnat.QUERY_DWCK_HZ_BODY);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String cpsx = reset.getString("CPSX");
				String ckckdm = reset.getString("CKCKDM");
				String ckckmc = reset.getString("CKCKMC");
				int cksl = reset.getInt("CKSL");

				if (cksl == 0) {
					throw new RuntimeException("PN��" + xh + "�ĳ�����������Ϊ0��");
				}

				int haveSl = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, ckckdm, cpsx);
				if (haveSl < cksl) {
					throw new RuntimeException(ckckmc + " �ֿ�:"+ckckmc+"��PN��" + xh + "�����ϲ��㣡 �ֿ�ʣ��������" + haveSl + "������������" + cksl);
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}
}
