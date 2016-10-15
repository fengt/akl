package cn.com.akl.rmahpfh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Update_RMAKC extends WorkFlowStepRTClassA {

	/**
	 * ���¿������.
	 */
	private static final String UPDATE_CANUSE_MATRIAL = "UPDATE BO_AKL_RMA_KCMX SET KWSL = KWSL-? FROM BO_AKL_RMA_KCMX a, BO_AKL_WXB_XS_RMASH_P b WHERE a.LX IN ( '��Ʒ����', '�����Ѵ���' ) AND a.DDH = b.DJBH AND b.ISEND = 1 AND a.ID=? AND a.XH = ? AND b.PP = ? AND b.UPDATEDATE >= ? AND b.UPDATEDATE < ?";
	/**
	 * ��ѯ��������.
	 */
	private static final String QUERY_CANUSE_MATERIAL = "SELECT a.ID, KWSL FROM BO_AKL_RMA_KCMX a, BO_AKL_WXB_XS_RMASH_P b WHERE a.LX IN ( '��Ʒ����', '�����Ѵ���' ) AND a.DDH = b.DJBH AND b.ISEND = 1 AND a.XH = ? AND b.PP = ? AND b.UPDATEDATE >= ? AND b.UPDATEDATE < ?";
	/**
	 * ��ѯ������.
	 */
	private static final String QUERY_FORM_BODY = "SELECT XH, SL, XDSL FROM BO_AKL_WXB_RMAHPFH_BODY WHERE BINDID=?";

	public Update_RMAKC() {
	}

	public Update_RMAKC(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("����RMA�������");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable recordData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
		String pp = recordData.get("PP") == null ? "" : recordData.get("PP").toString();// Ʒ��
		String qsrq = recordData.get("QSRQ") == null ? "" : recordData.get("QSRQ").toString();// ��ʼ����
		String jsrq = recordData.get("JSRQ") == null ? "" : recordData.get("JSRQ").toString();// ��������

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			queryFormBody(conn, bindid, pp, qsrq, jsrq);
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ��ѯ��������.
	 * 
	 * @param conn
	 * @param bindid
	 * @param pp
	 * @param qsrq
	 * @param jsrq
	 * @throws SQLException
	 */
	public void queryFormBody(Connection conn, int bindid, String pp, String qsrq, String jsrq) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			ps = conn.prepareStatement(QUERY_FORM_BODY);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String xh = reset.getString("XH");
				int sl = reset.getInt("SL");
				int xdsl = reset.getInt("XDSL");
				int sysl = updateMaterial(conn, bindid, pp, qsrq, jsrq, xh, sl, xdsl);
				if (sysl > 0) {
					throw new RuntimeException("�����������!");
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * �������Ͽ��.
	 * 
	 * @param conn
	 * @param bindid
	 * @param pp
	 * @param qsrq
	 * @param jsrq
	 * @param xh
	 * @param sl
	 * @param xdsl
	 * @return
	 * @throws SQLException
	 */
	public int updateMaterial(Connection conn, int bindid, String pp, String qsrq, String jsrq, String xh, int sl, int xdsl) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_CANUSE_MATERIAL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xh, pp, qsrq, jsrq);
			while (reset.next()) {
				int id = reset.getInt("ID");
				int kwsl = reset.getInt("KWSL");
				int uusl = 0;

				if (sl - kwsl > 0) {
					uusl = kwsl;
					sl = sl - kwsl;
				} else {
					uusl = sl;
					sl = 0;
				}

				int updateCount = DAOUtil.executeUpdate(conn, UPDATE_CANUSE_MATRIAL, uusl, id, xh, pp, qsrq, jsrq);
				if (updateCount != 1) {
					throw new RuntimeException("���¿��ʧ��!");
				}
			}
			return sl;
		} finally {
			DBSql.close(ps, reset);
		}
	}
}
