package cn.com.akl.shgl.fpkp.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

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
		setDescription("У���ӱ��еķ�Ʊ�Ƿ�������Ʊ���̰����ˣ����鿪Ʊ״̬�Ƿ����.");
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
	 * У�鷢Ʊ��¼�Ƿ����.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validate(Connection conn, int bindid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(FpkpConstant.QUERY_FPKP_FORM_BODY);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String fpsqdh = reset.getString("FPSQDH");

				// 1�� У�鷢Ʊ��״̬.
				String fpzt = DAOUtil.getStringOrNull(conn, FpkpConstant.QUERY_FPSQ_FPZT, fpsqdh);
				if (!FpkpConstant.FPZT_WKP.equals(fpzt)) {
					throw new RuntimeException("��ǰ��Ʊ���ܱ�����! �˷�Ʊ״̬Ϊ" + fpzt);
				}

				// 2��У�鷢Ʊ�Ƿ��������̰�����.
				Integer otherBindid = DAOUtil.getIntOrNull(conn, FpkpConstant.QUERY_FPKP_FORM_BODY_FPSQDH, fpsqdh, bindid);
				if (otherBindid != null && otherBindid > 0) {
					Integer otherStepNo = WorkFlowUtil.getProcessInstanceStepNo(conn, otherBindid);
					if (otherStepNo > 1) {
						// 2.1 ��ѯ��һ�������Ƿ������˵�һ�ڵ�.
						throw new RuntimeException("��ǰ��Ʊ���ܱ�����! �˷�Ʊ״̬Ϊ" + fpzt);
					}
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}
}
