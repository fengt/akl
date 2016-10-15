package cn.com.akl.ccgl.xsck.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * ���ۿ�������.
 * 
 * @author huangming
 *
 */
public class KCBiz {

	/**
	 * ���۳��⣬����ϸ..
	 */
	private static final String UPDATE_OUT_MX = "update BO_AKL_KC_KCMX_S set KWSL=ISNULL(KWSL, 0)-? where HWDM=? AND WLBH=? AND PCH=? AND KWSL>=?";
	/**
	 * ���۳��⣬������.
	 */
	private static final String UPDATE_OUT_HZ = "update BO_AKL_KC_KCHZ_P set CKSL=ISNULL(CKSL, 0)+? where WLBH=? AND PCH=? AND ?<=PCSL AND ZT='"+XSCKConstant.PC_ZT_ZC+"'";
	/**
	 * ������»��ܱ����������.
	 */
	private static final String UPDATE_OUT_HZ_PCSL = "update BO_AKL_KC_KCHZ_P set PCSL=ISNULL(RKSL, 0)-ISNULL(CKSL, 0) " + "where WLBH=? AND PCH=?";
	/**
	 * ��ѯ��Ҫ�����������Ϣ.
	 */
	private static final String QUERY_LOCK_WLXX = "SELECT RMAFXDH, WLH, PC, FHKFBH, XSDDH, SUM(ISNULL(SJSL, 0)) SJSL, SUM(ISNULL(SL, 0)) as SL "
			+ "FROM BO_AKL_CKD_BODY WHERE BINDID=? GROUP BY WLH, PC, FHKFBH, XSDDH, RMAFXDH";

	/**
	 * ���۳��⣬������.
	 * 
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param kwbh
	 * @param sl
	 * @throws SQLException
	 */
	public void outOfWarehouseHZ(Connection conn, String wlbh, String pch, String kwbh, Integer sl) throws SQLException {
		int count = DAOUtil.executeUpdate(conn, UPDATE_OUT_MX, sl, kwbh, wlbh, pch, sl);
		if (0 == count) {
			throw new RuntimeException("���Ϻ�:" + wlbh + " �ڻ�λ���룺" + kwbh + "��治��" + sl + "!");
		}
	}

	/**
	 * ���۳��⣬����ϸ.
	 * 
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param sl
	 * @throws SQLException
	 */
	public void outOfWarehouseMX(Connection conn, String wlbh, String pch, Integer sl) throws SQLException {
		int count = DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ, sl, wlbh, pch, sl);
		if (0 == count) {
			throw new RuntimeException("���Ϻ�:" + wlbh + " ����" + pch + "���ϵĳ��������Ѿ��ﵽ���ޣ���������ܱ�");
		}
		DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ_PCSL, wlbh, pch);
	}

	/**
	 * ��⣬����.
	 * 
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param kwbh
	 * @param sl
	 * @throws SQLException
	 */
	public void enterWarehouseHZ(Connection conn, String wlbh, String pch, String kwbh, Integer sl) throws SQLException {
		if (0 == DAOUtil.executeUpdate(conn, UPDATE_OUT_MX, -sl, kwbh, wlbh, pch, -sl)) {
			throw new RuntimeException("���Ϻ�:" + wlbh + " �ڻ�λ���룺" + kwbh + "���˿��δ�ɹ�������Ϊ:" + sl + "!");
		}
	}

	/**
	 * ��⣬��ϸ.
	 * 
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param sl
	 * @throws SQLException
	 */
	public void enterWarehouseMX(Connection conn, String wlbh, String pch, Integer sl) throws SQLException {
		if (0 == DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ, -sl, wlbh, pch, -sl)) {
			throw new RuntimeException("���Ϻ�:" + wlbh + " ����" + pch + "���˿��δ�ɹ�������Ϊ:" + sl);
		}

		DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ_PCSL, wlbh, pch);
	}

	/**
	 * ��������
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertLockBase(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			ps = conn.prepareStatement(QUERY_LOCK_WLXX);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = reset.getString("WLH");
				String pch = reset.getString("PC");
				String fhkfbh = reset.getString("FHKFBH");
				String xsddh = reset.getString("XSDDH");
				if (xsddh == null || xsddh.trim().equals("")) {
					xsddh = reset.getString("RMAFXDH");
				}
				String sjsl = reset.getString("SJSL");

				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("WLBH", wlbh);
				hashtable.put("PCH", pch);
				hashtable.put("CKDM", fhkfbh);
				hashtable.put("DDH", xsddh);
				hashtable.put("SDSL", sjsl);
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", hashtable, uid);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ɾ������.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void deleteLockBase(Connection conn, int bindid, String uid) throws SQLException {
		String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
		DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_KC_SPPCSK WHERE DDH=?", xsddh);
	}
}
