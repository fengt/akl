package cn.com.akl.dgkgl.xsck.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

public class DGOutBiz {
	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_BODY = "SELECT XSSL, WLBH, YCKSL, KHCGDH FROM BO_AKL_DGXS_S where DDID=?";
	/**
	 * 查询出库数量是否超出销售订单中的数量
	 */
	private static final String queryKCKSLNoEnoughWLBH = "SELECT s1.WLBH+'数量应该在'+CONVERT(VARCHAR(16), SUM(ISNULL(s2.XSSL, 0))-SUM(ISNULL(s2.YCKSL, 0))) FROM  BO_BO_AKL_DGCK_S s1, (SELECT p.XSDDID as DDID,s.WLBH,s.XSSL,s.YCKSL,s.JLDW FROM BO_AKL_DGXS_P p, BO_AKL_DGXS_S s WHERE p.BINDID=s.BINDID AND p.BINDID=?) as s2 WHERE s1.DDH = s2.DDID AND s1.WLBH=s2.WLBH AND s1.DW=s2.JLDW AND s1.DDH=? GROUP BY s1.WLBH HAVING SUM(ISNULL(s1.SFSL, 0))>SUM(ISNULL(s2.XSSL, 0))-SUM(ISNULL(s2.YCKSL, 0))";
	/**
	 * 查询代管销售的物料数量.
	 */
	private static final String QUERY_DGXS_WLSL = "SELECT SUM(ISNULL(s.XSSL, 0)) FROM BO_AKL_DGXS_P p, BO_AKL_DGXS_S s WHERE p.bindid=s.bindid AND p.XSDDID=?";
	/**
	 * 查询代管出库的物料数量.
	 */
	private static final String QUERY_DGCK_WLSL = "SELECT SUM(ISNULL(SFSL, 0)) FROM BO_BO_AKL_DGCK_S WHERE BINDID=?";
	/**
	 * 查询销售订单.
	 */
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * 查询单身的物料信息 +
	 */
	private static final String queryWLXX = "SELECT s.WLBH,s.PCH,sum(s.SFSL) sfsl,s.HWDM,s.SX,p.XSDH, s.KHCGDH FROM BO_BO_AKL_DGCK_S s left join BO_BO_AKL_DGCK_P p on s.bindid = p.bindid WHERE s.BINDID=? group by s.WLBH,s.PCH,s.HWDM,s.SX, p.XSDH,s.KHCGDH";
	/**
	 * 查询代管库存明细中此货位、此批次物料数量不足的 +
	 */
	private static final String queryWLSLNoEnoughMXHW = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCMX_S s WHERE s.HWDM=? AND s.WLBH=? AND s.PCH=? AND ISNULL(s.KWSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = s.WLBH and HWDM = s.HWDM AND PCH = s.PCH AND (XSDH<>? or ISNULL(KHCGDH, '')<>?)), s.KWSL)>=? AND SX=?";
	/**
	 * 查询代管库存汇总中此批次物料数量不足的 +
	 */
	private static final String queryWLSLNoEnoughHZPC = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCHZ_P p WHERE p.WLBH=? AND p.PCH=? AND ISNULL(p.PCSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = p.WLBH AND PCH = p.PCH AND (XSDH<>? or ISNULL(KHCGDH, '')<>?)), p.PCSL)>=? AND ZT='042022'";
	/**
	 * 其他出库+
	 */
	private static final String queryWLSLNoEnoughMXHWQT = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCMX_S s WHERE s.HWDM=? AND s.WLBH=? AND s.PCH=? AND ISNULL(s.KWSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = s.WLBH AND HWDM = s.HWDM AND PCH = s.PCH AND XSDH=? AND ISNULL(KHCGDH, '')<>?), s.KWSL)>=? AND SX=?";
	/**
	 * 其他出库+
	 */
	private static final String queryWLSLNoEnoughHZPCQT = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCHZ_P p WHERE p.WLBH=? AND p.PCH=? AND ISNULL(p.PCSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = p.WLBH AND PCH = p.PCH AND XSDH=? AND ISNULL(KHCGDH, '')<>?), p.PCSL)>=? AND ZT='042022'";
	/**
	 * 出库类型.
	 */
	private static final String QUERY_CKD_CKLX = "SELECT CKLX FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * 查询出库单是否已出
	 */
	private static final String queryXSDDCKSL = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a join BO_AKL_DGXS_S b on a.BINDID=b.BINDID WHERE a.XSDDID=? GROUP BY a.XSDDID HAVING SUM(ISNULL(YCKSL, 0))<>SUM(ISNULL(XSSL, 0))";
	/**
	 * 查询出库单是否已办理
	 */
	private static final String queryYBXSDH = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a left join BO_BO_AKL_DGCK_P b on a.XSDDID = b.XSDH where a.ZT <> '部分出库' and b.XSDH=? and b.bindid<>? and b.ZT = '未出库'";

	/**
	 * 验证销售订单与销售出库数量是否一致.
	 * 
	 * @throws SQLException
	 */
	public void validateSalesAndOutNumIsEquals(Connection conn, int bindid, String uid) throws SQLException {
		String xsdh = DAOUtil.getString(conn, queryXSDH, bindid);

		// 出库数量提示.
		Integer xsWlsl = DAOUtil.getIntOrNull(conn, QUERY_DGXS_WLSL, xsdh);
		Integer ckWlsl = DAOUtil.getIntOrNull(conn, QUERY_DGCK_WLSL, bindid);
		if (xsWlsl == null) {
			if (!ckWlsl.equals(xsWlsl)) {
				MessageQueue.getInstance().putMessage(uid, "销售数量与出库数量不符，请注意！");
			}
		} else {
			if (!xsWlsl.equals(ckWlsl)) {
				MessageQueue.getInstance().putMessage(uid, "销售数量与出库数量不符，请注意！");
			}
		}
	}

	/**
	 * 验证物料可用数量.
	 * 
	 * @throws SQLException
	 */
	public void validateMaterialAvailableAmount(Connection conn, int bindid, final String uid) throws SQLException {
		/** 出库类型 */
		final String cklx = DAOUtil.getString(conn, QUERY_CKD_CKLX, bindid);

		// 验证每个库位的货物是否充足
		DAOUtil.executeQueryForParser(conn, queryWLXX, new DAOUtil.ResultPaser() {
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				if (!cklx.equals("054143") && !cklx.equals("054144")) {
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughMXHW, reset.getString("HWDM"), reset.getString("WLBH"), reset.getString("PCH"),
							reset.getString("XSDH"), reset.getString("KHCGDH"), reset.getInt("SFSL"), reset.getString("SX"))) {
						MessageQueue.getInstance().putMessage(
								uid,
								"批次号：" + reset.getString("PCH") + " 物料号:" + reset.getString("WLBH") + " 在货位代码：" + reset.getString("HWDM") + "可用数量不足"
										+ reset.getInt("SFSL") + "!", true);
						throw new RuntimeException("批次号：" + reset.getString("PCH") + " 物料号:" + reset.getString("WLBH") + " 在货位代码："
								+ reset.getString("HWDM") + "可用数量不足" + reset.getInt("SFSL") + "!");
					}
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughHZPC, reset.getString("WLBH"), reset.getString("PCH"), reset.getString("XSDH"),
							reset.getString("KHCGDH"), reset.getInt("SFSL"))) {
						MessageQueue.getInstance().putMessage(uid,
								"物料号:" + reset.getString("WLBH") + " 这批" + reset.getString("PCH") + "物料的出库数量已经达到上限，请检查库存汇总表", true);
						throw new RuntimeException("物料号:" + reset.getString("WLBH") + " 这批" + reset.getString("PCH") + "物料的出库数量已经达到上限，请检查库存汇总表");
					}
				} else {
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughMXHWQT, reset.getString("HWDM"), reset.getString("WLBH"), reset.getString("PCH"),
							reset.getString("XSDH"), reset.getString("KHCGDH"), reset.getInt("SFSL"), reset.getString("SX"))) {
						MessageQueue.getInstance().putMessage(
								uid,
								"批次号：" + reset.getString("PCH") + " 物料号:" + reset.getString("WLBH") + " 在货位代码：" + reset.getString("HWDM") + "可用数量不足"
										+ reset.getInt("SFSL") + "!", true);
						throw new RuntimeException("批次号：" + reset.getString("PCH") + " 物料号:" + reset.getString("WLBH") + " 在货位代码："
								+ reset.getString("HWDM") + "库位数量不足" + reset.getInt("SFSL") + "!");
					}
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughHZPCQT, reset.getString("WLBH"), reset.getString("PCH"), reset.getString("XSDH"),
							reset.getString("KHCGDH"), reset.getInt("SFSL"))) {
						MessageQueue.getInstance().putMessage(uid,
								"物料号:" + reset.getString("WLBH") + " 这批" + reset.getString("PCH") + "物料的出库数量已经达到上限，请检查库存汇总表", true);
						throw new RuntimeException("物料号:" + reset.getString("WLBH") + " 这批" + reset.getString("PCH") + "物料的出库数量已经达到上限，请检查库存汇总表");
					}
				}
				return true;
			}
		}, bindid);
	}

	/**
	 * 验证此出库单是否能够出<br/>
	 * 1、验证销售单是否出完了.<br/>
	 * 2、验证销售单的状态.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param xsdh
	 * @throws SQLException
	 */
	public void validateIsCanOut(Connection conn, int bindid, String uid, String xsdh) throws SQLException {
		/** 1.验证销售订单状态 */
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(QUERY_XSDD_BODY);
			rs = DAOUtil.executeFillArgsAndQuery(conn, stat, xsdh);
			while (rs.next()) {
				// 查询单身改后各物料应发数量之和
				String WLBH = rs.getString(2);
				String KHCGDH = PrintUtil.parseNull(rs.getString(4));
				int YFSL = DAOUtil.getIntOrNull(conn,
						"SELECT sum(YFSL) YFSL FROM BO_BO_AKL_DGCK_S where bindid=? and WLBH=? AND ISNULL(KHCGDH, '')=?", bindid, WLBH, KHCGDH);
				int SYXSSL = rs.getInt(1) - rs.getInt(3);
				if (YFSL != SYXSSL) {
					throw new RuntimeException("客户采购单号为：" + KHCGDH + "物料编号为：" + WLBH + "的改后实发数量总计：" + YFSL + "与该物料应发数量总计：" + SYXSSL + "不符");
				}
			}
		} finally {
			DBSql.close(stat, rs);
		}

		/** 2.验证销售订单已出库数量 */
		String YB = DAOUtil.getStringOrNull(conn, queryYBXSDH, xsdh, bindid);
		if (YB != null && Integer.parseInt(YB) > 0) {
			throw new RuntimeException("此销售订单已办理！");
		}
		String YC = DAOUtil.getStringOrNull(conn, queryXSDDCKSL, xsdh);
		if (YC == null || "0".equals(YC)) {
			throw new RuntimeException("此销售订单已办理！");
		}
	}

	/**
	 * 验证出库单的出库数量是否超过销售订单的数量。
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateSalesOutNum(Connection conn, final int bindid, String xsdh) throws SQLException {
		Integer xsddbindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_DGXS_P WHERE XSDDID=?", xsdh);
		DAOUtil.executeQueryForParser(conn,
				"SELECT WLBH, XH, SUM(ISNULL(XSSL, 0)) XSSL, SUM(ISNULL(YCKSL, 0)) YCKSL FROM BO_AKL_DGXS_S WHERE BINDID=? GROUP BY WLBH, XH",
				new DAOUtil.ResultPaser() {
					@Override
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						Integer sfsl = DAOUtil.getIntOrNull(conn, "SELECT SUM(ISNULL(SFSL, 0)) SFSL FROM BO_BO_AKL_DGCK_S WHERE BINDID=? AND WLBH=?",
								bindid, reset.getString("WLBH"));
						int xssl = reset.getInt("XSSL");
						int ycksl = reset.getInt("YCKSL");
						int kysl = xssl - ycksl;

						if (sfsl != kysl) {
							throw new RuntimeException("物料型号：" + reset.getString("XH") + "的出库数量 不等于 销售数量：" + kysl + "！");
						}

						return true;
					}
				}, xsddbindid);

	}

}
