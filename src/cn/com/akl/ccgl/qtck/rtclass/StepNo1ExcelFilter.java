package cn.com.akl.ccgl.qtck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1ExcelFilter extends ExcelDownFilterRTClassA {

	private static final String QUERY_KYPC = "SELECT a.WLBH, a.PCH, RKSL, ISNULL(CKSL, 0) as CKSL, ISNULL(b.SDSL, 0) AS SDSL FROM BO_AKL_KC_KCHZ_P a LEFT JOIN (SELECT WLBH, PCH, SUM(ISNULL(SDSL, 0)) as SDSL FROM BO_AKL_KC_SPPCSK GROUP BY WLBH, PCH) b ON a.PCH=b.PCH AND a.WLBH=b.WLBH WHERE RKSL-ISNULL(CKSL, 0)-ISNULL(b.SDSL, 0)>0 AND a.XH=? ORDER BY a.PCH";
	/**
	 * 查询可用的物料.
	 */
	private static final String QUERY_KCMX = "SELECT a.WLBH, a.WLMC, a.PCH, a.SX, RKRQ, b.DJ, a.KWDM, a.HWDM, a.DDM, a.QDM, a.CKDM, a.CKMC, SUM ( CASE WHEN ZT = '042023' THEN ISNULL(a.KWSL, 0) ELSE 0 END ) AS ZTSL, SUM ( CASE WHEN ZT = '042022' THEN ISNULL(a.KWSL, 0) ELSE 0 END ) AS ZCSL, a.JLDW FROM BO_AKL_KC_KCMX_S a RIGHT JOIN BO_AKL_KC_KCHZ_P b ON a.WLBH = b.WLBH AND a.PCH = b.PCH WHERE a.XH = ? AND a.PCH=? AND ( a.SX = '049088' OR a.SX = '049090' ) GROUP BY a.WLBH, a.WLMC, a.JLDW, a.HWDM, a.PCH, b.RKRQ, b.DJ, a.SX, a.KWDM, a.DDM, a.QDM, a.CKDM, a.CKMC HAVING SUM ( CASE WHEN ZT = '042022' THEN ISNULL(a.KWSL, 0) ELSE 0 END ) + SUM ( CASE WHEN ZT = '042023' THEN ISNULL(a.KWSL, 0) ELSE 0 END ) > 0 ORDER BY SUM (a.KWSL)";

	public StepNo1ExcelFilter(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("根据型号找到对应物料.");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {

		final int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;

		try {
			conn = DAOUtil.openConnectionTransaction();

			// 1、查出单身数据.
			DAOUtil.executeQueryForParser(conn, "SELECT ID, XH, SJSL FROM BO_AKL_CKD_BODY WHERE BINDID=?", new DAOUtil.ResultPaser() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					String xh = reset.getString("XH");
					int sl = reset.getInt("SJSL");
					int id = reset.getInt("ID");
					int fetchCount = 0;

					PreparedStatement pchPstat = null;
					ResultSet pchReset = null;

					try {
						pchPstat = conn.prepareStatement(QUERY_KYPC);
						pchReset = DAOUtil.executeFillArgsAndQuery(conn, pchPstat, xh);

						while (pchReset.next() && sl > 0) {
							// 2、根据已存在的库存，逐渐分解
							String pch = pchReset.getString("PCH");
							Integer cksl = pchReset.getInt("CKSL");
							int sdsl = pchReset.getInt("SDSL");
							int rksl = pchReset.getInt("RKSL");
							int kysl = rksl - cksl - sdsl;

							if (kysl <= 0) {
								continue;
							}

							if (sl - kysl <= 0) {
								kysl = sl;
								sl = 0;
							} else {
								sl = sl - kysl;
							}

							PreparedStatement kcmxPstat = null;
							ResultSet kcmxReset = null;

							try {
								kcmxPstat = conn.prepareStatement(QUERY_KCMX);
								kcmxReset = DAOUtil.executeFillArgsAndQuery(conn, kcmxPstat, xh, pch);

								while (kcmxReset.next() && kysl > 0) {
									Hashtable<String, String> hashtable = new Hashtable<String, String>();

									int kcsl = kcmxReset.getInt("ZCSL");
									int ckkysl = kcsl;

									if (ckkysl <= 0) {
										continue;
									}

									if (kysl - ckkysl <= 0) {
										ckkysl = kysl;
										kysl = 0;
									} else {
										kysl = kysl - ckkysl;
									}

									hashtable.put("PC", kcmxReset.getString("PCH"));
									hashtable.put("WLH", kcmxReset.getString("WLBH"));
									hashtable.put("WLMC", kcmxReset.getString("WLMC"));
									hashtable.put("FHKFBH", kcmxReset.getString("CKDM"));
									hashtable.put("FHKFMC", kcmxReset.getString("CKMC"));
									hashtable.put("KWBH", kcmxReset.getString("HWDM"));
									hashtable.put("XH", xh);
									hashtable.put("JLDW", kcmxReset.getString("JLDW"));
									hashtable.put("KCSL", kcmxReset.getString("ZCSL"));
									hashtable.put("SX", kcmxReset.getString("SX"));
									hashtable.put("SJSL", String.valueOf(ckkysl));

									if (fetchCount == 0) {
										BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_CKD_BODY", hashtable, id);
									} else {
										BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hashtable, bindid,
												getUserContext().getUID());
									}

									fetchCount++;
								}

								if (kysl > 0) {
									sl = sl + kysl;
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								DBSql.close(kcmxPstat, kcmxReset);
							}
						}

						if (sl > 0) {
							MessageQueue.getInstance().putMessage(getUserContext().getUID(), "物料数量不足以出库，型号为:" + xh + " ，缺少数量:" + sl);
							return false;
						} else {
							return true;
						}
					} finally {
						DBSql.close(pchPstat, pchReset);
					}
				}
			}, bindid);

			conn.commit();
			return arg0;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return arg0;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
}
