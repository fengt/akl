package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMA_SH_SPBM_EX extends ExcelDownFilterRTClassA {

	public RMA_SH_SPBM_EX(UserContext arg0) {
		super(arg0);
		setVersion("RMA�ջ�����v1.0");
		setProvider("����");
		setDescription("ģ���ϴ�����������SKUƥ��ϵͳ���ϣ���������Ӧ��������Ϣ��");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook hs) {
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		try {
			conn = DBSql.open();
			service(conn, bindid, uid);
			conn.commit();
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
		} finally {
			DBSql.close(conn, null, null);
		}

		return null;
	}

	/**
	 * ����������Ϣ.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void service(Connection conn, int bindid, String uid) throws SQLException {
		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);

		if (v == null || v.size() == 0) {
			throw new RuntimeException("��������Ϊ�գ�����");
		}

		conn.setAutoCommit(false);
		for (Hashtable<String, String> h : v) {
			// ͨ���ͻ���Ʒ����Ϳͻ���Ų�ѯ������Ʒ���.
			updateMaterialInfo(conn, bindid, h.get("XH"), ha.get("DJBH"), h.get("BJTM"), h.get("KHSPBH"), ha.get("KHBH"));
		}
	}

	/**
	 * ���Ҳ��������ϵ����Ӧ��������Ϣ.
	 * 
	 * @param conn
	 * @param bindid
	 * @param djbh
	 * @param bjtm
	 * @param khspbh
	 * @param khbh
	 * @throws SQLException
	 */
	public void updateMaterialInfo(Connection conn, int bindid, String xh, String djbh, String bjtm, String khspbh, String khbh) throws SQLException {
		PreparedStatement state = null;
		ResultSet rs = null;

		try {
			/**
			 * ƥ�䷽����<br/>
			 * 1������ͻ���Ʒ���Ϊnull��������ͺŶ�Ӧ���ϱ��. <br/>
			 * 2������ͻ���Ʒ��Ų�Ϊnull������ÿͻ���Ʒ��Ŷ�Ӧ���ϱ��.<br/>
			 * 3�����µ���������Ϣʱ,����ͻ���Ʒ��Ų�Ϊnull��ΪXHδ֪����δ�ϴ�,��Ҫ����XH�ֶ�.qjc.2015-02-16 
			 */
			String wlbh = null;
			if (khspbh == null || khspbh.equals("")) {
				wlbh = DAOUtil.getStringOrNull(conn, "SELECT WLBH FROM BO_AKL_WLXX WHERE XH=? AND HZBM=?", xh, "01065");
				if (wlbh == null) {
					throw new RuntimeException("�������룺" + bjtm + " �ͺţ�" + xh + " ��Ӧ���������ϱ�Ų����ڣ����飡");
				}
			} else {
				wlbh = DAOUtil.getStringOrNull(conn, "SELECT YKSPSKU FROM BO_AKL_KHSPBMGL where KHSPSKU =? AND KHBM =?", khspbh, khbh);
				if (wlbh == null) {
					throw new RuntimeException("�������룺" + bjtm + " �ͻ���ţ�" + khbh + " ��Ӧ���������ϱ�Ų����ڣ����飡");
				}
			}

			state = conn.prepareStatement("SELECT WLBH, WLMC, XH FROM BO_AKL_WLXX WHERE WLBH=?");
			rs = DAOUtil.executeFillArgsAndQuery(conn, state, wlbh);
			if (rs.next()) {
				// ���µ���������Ϣ.
				DAOUtil.executeUpdate(conn,
						"UPDATE BO_AKL_WXB_XS_RMASH_S SET DJBH = ?, YKSKU = ?, SHID = ?, SPMC = ?,XH=? WHERE BJTM = ? AND bindid =? AND XH=?", djbh,
						rs.getString("WLBH"), khbh, rs.getString("WLMC"),rs.getString("XH"), bjtm, bindid, xh);
			} else {
				throw new RuntimeException("�������룺" + bjtm + " ���ϱ�ţ�" + wlbh + " ��Ӧ������������Ϣ�����ڣ����飡");
			}
		} finally {
			DBSql.close(state, rs);
		}
	}
}
