package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1ExcelModelUpAfter extends ExcelDownFilterRTClassA {

	public StepNo1ExcelModelUpAfter(UserContext us) {
		super(us);
		setVersion("1.0.0");
		setDescription("Excel模板导入后自动去除无效行,并更新返新数据");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;
		Hashtable<String, String> hft = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
		try {
			conn = DAOUtil.openConnectionTransaction();
			stat = conn.createStatement();
			String sql = "select * from BO_AKL_WXB_RMAFX_S where bindid = " + bindid;
			rs = stat.executeQuery(sql);
			while (rs.next()) {
				String thxh = rs.getString("THXH") == null ? "" : rs.getString("THXH");
				if (thxh.trim().equals("")) {
					BOInstanceAPI.getInstance().removeBOData("BO_AKL_WXB_RMAFX_S", rs.getInt("ID"));
				}
			}
			if (v != null) {
				Iterator<Hashtable<String, String>> it = v.iterator();
				String FXXH = null;
				while (it.hasNext()) {
					Hashtable<String, String> h = it.next();
					String TKJE = h.get("TKJE") == null ? "0" : h.get("TKJE").toString();

					FXXH = h.get("FXXH").toString();
					sql = "SELECT * FROM ( SELECT h.KHSPSKU, a.HZBM, a.WLBH, a.XH, ISNULL(b.PCSL, 0) PCSL, ISNULL(g.XSGHJ, 0) XSGHJ, ISNULL(c.ZDCB, 0) ZDCB FROM BO_AKL_WLXX a LEFT JOIN ( SELECT s.WLBH, SUM (ISNULL(s.KWSL, 0)) - ISNULL(( SELECT SUM (SDSL) FROM BO_AKL_KC_SPPCSK WHERE WLBH = s.WLBH AND CKDM = s.CKDM ), 0 ) AS PCSL FROM BO_AKL_KC_KCMX_S s WHERE s.CKDM = '"
							+ hft.get("CHCKDM")
							+ "' AND s.SX IN ('049088', '049090') GROUP BY s.WLBH, s.CKDM ) b ON a.WLBH = b.WLBH LEFT JOIN BO_AKL_JGGL c ON a.WLBH = c.WLBH AND c.ZXRQ = ( SELECT MAX (f.ZXRQ) ZXRQ FROM BO_AKL_JGGL f WHERE f.WLBH = a.WLBH HAVING MAX (f.ZXRQ) <= GETDATE()) LEFT JOIN ( SELECT s.XSGHJ, s.WLBH, p.KHBH FROM BO_AKL_KH_JGGL_P p, BO_AKL_KH_JGGL_S s WHERE s.bindid = p.bindid AND s.ID = ( SELECT MAX (x.ID) FROM BO_AKL_KH_JGGL_S x, BO_AKL_KH_JGGL_P y WHERE x.WLBH = s.WLBH AND x.BINDID = y.BINDID AND y.KHBH = p.KHBH )) g ON g.KHBH = '"
							+ hft.get("KHBH")
							+ "' AND g.WLBH = a.WLBH LEFT JOIN BO_AKL_KHSPBMGL h ON a.WLBH = h.YKSPSKU AND h.KHBM = g.KHBH ) A WHERE HZBM = '01065' AND PCSL > 0 AND XH = '"
							+ FXXH + "'";

					if (Double.parseDouble(TKJE) > 0) {
						FXXH = h.get("THXH").toString();
						sql = "SELECT * FROM ( SELECT h.KHSPSKU, a.HZBM, a.WLBH, a.XH, ISNULL(b.PCSL, 0) PCSL, ISNULL(g.XSGHJ, 0) XSGHJ, ISNULL(c.ZDCB, 0) ZDCB FROM BO_AKL_WLXX a LEFT JOIN ( SELECT s.WLBH, SUM (ISNULL(s.KWSL, 0)) - ISNULL(( SELECT SUM (SDSL) FROM BO_AKL_KC_SPPCSK WHERE WLBH = s.WLBH AND CKDM = s.CKDM ), 0 ) AS PCSL FROM BO_AKL_KC_KCMX_S s WHERE s.CKDM = '"
								+ hft.get("CHCKDM")
								+ "' AND s.SX IN ('049088', '049090') GROUP BY s.WLBH, s.CKDM ) b ON a.WLBH = b.WLBH LEFT JOIN BO_AKL_JGGL c ON a.WLBH = c.WLBH AND c.ZXRQ = ( SELECT MAX (f.ZXRQ) ZXRQ FROM BO_AKL_JGGL f WHERE f.WLBH = a.WLBH HAVING MAX (f.ZXRQ) <= GETDATE()) LEFT JOIN ( SELECT s.XSGHJ, s.WLBH, p.KHBH FROM BO_AKL_KH_JGGL_P p, BO_AKL_KH_JGGL_S s WHERE s.bindid = p.bindid AND s.ID = ( SELECT MAX (x.ID) FROM BO_AKL_KH_JGGL_S x, BO_AKL_KH_JGGL_P y WHERE x.WLBH = s.WLBH AND x.BINDID = y.BINDID AND y.KHBH = p.KHBH )) g ON g.KHBH = '"
								+ hft.get("KHBH")
								+ "' AND g.WLBH = a.WLBH LEFT JOIN BO_AKL_KHSPBMGL h ON a.WLBH = h.YKSPSKU AND h.KHBM = g.KHBH ) A WHERE HZBM = '01065' AND XH = '"
								+ FXXH + "'";
					}

					rs = stat.executeQuery(sql);
					if (rs.next()) {
						if (Double.parseDouble(TKJE) <= 0) {
							DAOUtil.executeUpdate(
									conn,
									"update BO_AKL_WXB_RMAFX_S set FXWLBH=?, KCSL=?, XSDJ=?, ZDCBWS=?, FXKHSPBM=? where bindid=? AND KHDH=? AND THJBM=?",
									rs.getString("WLBH"), rs.getInt("PCSL"), rs.getDouble("XSGHJ"), rs.getDouble("ZDCB"), rs.getString("KHSPSKU"),
									bindid, h.get("KHDH"), h.get("THJBM"));
						} else {
							DAOUtil.executeUpdate(conn,
									"update BO_AKL_WXB_RMAFX_S set KCSL=?, XSDJ=?, ZDCBWS=? where bindid=? AND KHDH=? AND THJBM=?",
									rs.getInt("PCSL"), rs.getDouble("XSGHJ"), rs.getDouble("ZDCB"), bindid, h.get("KHDH"), h.get("THJBM"));
						}
					}

					String id = h.get("ID");
					String khbh = hft.get("KHBH");
					String khthspbm = h.get("KHTHSPBM");
					String ckdm = hft.get("CHCKDM");
					String lx = h.get("LX");
					String fxkhspbm = h.get("FXKHSPBM");
					if ("坏品返新".equals(lx) && ("".equals(fxkhspbm) && fxkhspbm==null)) {
						insertTHWL(conn, khbh, khthspbm, ckdm, id);
					}
				}
			}

			conn.commit();
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "刷新单身数据失败", true);
			return null;
		} finally {
			DBSql.close(conn, stat, rs);
		}

		return arg0;
	}

	/**
	 * 更新替换规则物料.
	 * 
	 * @throws SQLException
	 */
	public void insertTHWL(Connection conn, String khbh, String khthspbm, String ckdm, String id) throws SQLException {
		String sql = "SELECT XLMC, kcmx.WLBH, kcmx.XH, kcmx.WLMC, wlxx.HZ, SUM (kcmx.KWSL) PCSL, kchz.DJ AS ZDCB, 1 AS SL, ( SELECT TOP 1 jggls.XSGHJ FROM BO_AKL_KH_JGGL_P jgglp "
				+ "LEFT JOIN BO_AKL_KH_JGGL_S jggls ON jgglp.bindid = jggls.bindid WHERE jgglp.KHBH = ? AND jggls.WLBH = kcmx.WLBH ORDER BY jggls.ID DESC ) AS XSGHJ FROM BO_AKL_RMA_THGZ "
				+ "thgz INNER JOIN BO_AKL_RMA_THGZ thwl ON thgz.THGZ = thwl.THGZ AND thgz.THYXJ <= thwl.THYXJ LEFT JOIN BO_AKL_KC_KCMX_S kcmx ON thwl.WLBH = kcmx.WLBH LEFT JOIN BO_AKL_KC_"
				+ "KCHZ_P kchz ON kcmx.WLBH = kchz.WLBH AND kcmx.PCH = kchz.PCH LEFT JOIN BO_AKL_WLXX wlxx ON kcmx.WLBH = wlxx.WLBH LEFT JOIN BO_AKL_DATA_DICT_S dict ON dict.XLBM = wlxx.LBID "
				+ "WHERE thgz.WLBH = ( SELECT YKSPSKU FROM BO_AKL_KHSPBMGL WHERE KHSPSKU = ? ) AND HZBM = '01065' AND kcmx.CKDM = ? GROUP BY kcmx.WLBH, kcmx.XH, kchz.DJ, kcmx.CKDM, thwl.THYXJ, "
				+ "XLMC, kcmx.WLMC, HZ HAVING SUM (kcmx.KWSL) > 0 ORDER BY thwl.THYXJ";

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(sql);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, khbh, khthspbm, ckdm);
			if (reset.next()) {
				String xh = reset.getString("XH");
				String pcsl = reset.getString("PCSL");
				String xsghj = reset.getString("XSGHJ");
				String zdcb = reset.getString("ZDCB");
				String sl = reset.getString("SL");
				String wlbh = reset.getString("WLBH");
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_RMAFX_S SET FXXH=?, KCSL=?, XSDJ=?, ZDCBWS=?, FXSL=?, FXWLBH=? WHERE ID=?", xh, pcsl,
						xsghj, zdcb, sl, wlbh, id);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

}
