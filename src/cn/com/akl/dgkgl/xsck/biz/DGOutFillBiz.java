package cn.com.akl.dgkgl.xsck.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGOutFillBiz {
	/**
	 * 查询销售订单的BINDID.
	 */
	private static final String QUERY_SALES_BINDID= "SELECT BINDID FROM BO_AKL_DGXS_P WHERE XSDDID=?";
	/**
	 * 查询货主编号.
	 */
	private static final String QUERY_HZBH = "SELECT HZBH FROM BO_AKL_DGXS_P WHERE XSDDID=?";
	/**
	 * 查询锁库表中此销售订单的数量.
	 */
	private static final String QUERY_LOCK_MATERIAL_NUM = "SELECT a.HWKYSL, a.WLBH, a.XSDH, a.XSSL, a.PCH, a.HWDM, b.GG, b.XH, b.DW, b.WLMC, a.KHCGDH  FROM BO_AKL_DGCKSK a left join BO_AKL_WLXX b on a.WLBH = b.WLBH WHERE a.XSDH=? AND b.HZBM=? AND b.WLZT in (0, 1, 4) order by a.KHCGDH, b.XH";
	/**
	 * 查询仓库的可用物料信息.
	 */
	private static final String QUERY_CANUSE_MATERIAL = "SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? and s.PCH=? and s.HWDM=? and a.HZBM=? AND s.SX in ('049088', '049090') AND a.WLZT in (0, 1, 4)";
	/**
	 * 查询单身所有物料信息.
	 */
	private static final String QUERY_SALES_BODY_MATERIAL = "SELECT b.WLBH,b.WLMC,b.GG,b.XH,b.JLDW,b.XSSL,b.KCSL,b.YCKSL,b.KHCGDH  FROM BO_AKL_DGXS_S b WHERE BINDID=?";
	/**
	 * 查询仓库的可用物料信息.
	 */
	private static final String QUERY_REPOSITORY_MATERIAL = "SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.HWDM, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? ORDER BY s.PCH, s.KWSL";
	/**
	 * 查询物料的锁定数量.
	 */
	private static final String QUERY_MATERIAL_LOCKNUM = "SELECT SUM(ISNULL(XSSL, 0)) SDSL FROM BO_AKL_DGCKSK WHERE WLBH=? AND PCH=? AND HWDM=?";
	/**
	 * 查询此销售订单物料的锁定数量.
	 */
	private static final String QUERY_SALES_MATERIAL_LOCKNUM = "SELECT SUM(ISNULL(XSSL, 0)) SDSL FROM BO_AKL_DGCKSK WHERE WLBH=? AND XSDH=?";
	
	/**
	 * 查询单身的所有物料，并且从仓库抓取物料.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fetchLockMaterial(Connection conn, int bindid, String uid, String xsddh) throws SQLException, AWSSDKException {
		String hzbh = DAOUtil.getStringOrNull(conn, QUERY_HZBH, xsddh);
		PreparedStatement ps = conn.prepareStatement(QUERY_LOCK_MATERIAL_NUM);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddh, hzbh);
		try {
			while (reset.next()) {
				// 查询库位，根据时间排序
				int hwkysl = reset.getInt("HWKYSL");
				String wlbh = PrintUtil.parseNull(reset.getString("WLBH"));
				String khcgdh = PrintUtil.parseNull(reset.getString("KHCGDH"));
				String wlmc = PrintUtil.parseNull(reset.getString("WLMC"));
				String wlgg = PrintUtil.parseNull(reset.getString("GG"));
				String xh = PrintUtil.parseNull(reset.getString("XH"));
				String jldw = PrintUtil.parseNull(reset.getString("DW"));
				int xssl = reset.getInt("XSSL");
				String pch = PrintUtil.parseNull(reset.getString("PCH"));
				String hwdm = PrintUtil.parseNull(reset.getString("HWDM"));
				// 查询销售单身摘要
				String zy = DAOUtil.getStringOrNull(conn, "SELECT zy from BO_AKL_DGXS_S where DDID=? and WLBH=?", xsddh, wlbh);
				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset = null;
				try {
					kywlxxPs = conn.prepareStatement(QUERY_CANUSE_MATERIAL);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh, pch, hwdm, hzbh);
					while (kywlxxReset.next()) {
						int kwsl = kywlxxReset.getInt("KWSL");
						int haveSl = hwkysl;
						String qdm = PrintUtil.parseNull(kywlxxReset.getString("QDM"));
						String ddm = PrintUtil.parseNull(kywlxxReset.getString("DDM"));
						String kwdm = PrintUtil.parseNull(kywlxxReset.getString("KWDM"));
						String ckdm = PrintUtil.parseNull(kywlxxReset.getString("CKDM"));
						String ckmc = PrintUtil.parseNull(kywlxxReset.getString("CKMC"));
						String sx = PrintUtil.parseNull(kywlxxReset.getString("SX"));
						int TJ = kywlxxReset.getInt("TJ");
						int ZL = kywlxxReset.getInt("ZL");
						if (haveSl >= xssl) {
							// 预备转存入库单身中
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("CKDM", ckdm);
							hashtable.put("KHCGDH", khcgdh);
							hashtable.put("CKMC", ckmc);
							hashtable.put("QDM", qdm);
							hashtable.put("DDM", ddm);
							hashtable.put("KWDM", kwdm);
							hashtable.put("HWDM", hwdm);
							hashtable.put("DDH", xsddh);
							hashtable.put("PCH", pch);
							hashtable.put("WLBH", wlbh);
							hashtable.put("XH", xh);
							hashtable.put("GG", wlgg);
							hashtable.put("WLMC", wlmc);
							hashtable.put("DW", jldw);
							hashtable.put("KCSL", String.valueOf(kwsl));
							hashtable.put("SX", sx);
							hashtable.put("TJ", String.valueOf(TJ));
							hashtable.put("ZL", String.valueOf(ZL));
							hashtable.put("BZ", zy);
							hashtable.put("SFSL", String.valueOf(xssl));
							hashtable.put("YFSL", String.valueOf(xssl));
							hashtable.put("HWKYSL", String.valueOf(hwkysl));

							BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", hashtable, bindid, uid);
						} else {
							throw new RuntimeException("销售订单：" + xsddh + "中物料编号为" + wlbh + "型号为" + xh + "计量单位为" + jldw + "的物料可用数量不足。");
						}
					}
				} finally {
					DBSql.close(kywlxxPs, kywlxxReset);
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 从销售订单中抓取物料.<br/>
	 * 依据销售订单剩余未出库的物料进行重新锁库.<br/>
	 * 销售订单销售数量-已出库数量-本次出库数量 = 剩下的数量 
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fetchCanUseMaterial(Connection conn, int bindid, String uid, String xsddh) throws SQLException, AWSSDKException {
		String xsddbindid = DAOUtil.getStringOrNull(conn, QUERY_SALES_BINDID, xsddh);
		PreparedStatement ps = conn.prepareStatement(QUERY_SALES_BODY_MATERIAL);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddbindid);
		try {
			while (reset.next()) {
				// 查询库位，根据时间排序
				String wlbh = PrintUtil.parseNull(reset.getString("WLBH"));
				String xh = PrintUtil.parseNull(reset.getString("XH"));
				String khcgdh = PrintUtil.parseNull(reset.getString("KHCGDH"));
				int xssl = reset.getInt("XSSL");
				int ycksl = reset.getInt("YCKSL");

				// 销售数量需要减去已出库数量.
				int ysksl = DAOUtil.getInt(conn, QUERY_SALES_MATERIAL_LOCKNUM, wlbh, xsddh);
				int sl = xssl - ycksl - ysksl;

				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset = null;
				try {
					kywlxxPs = conn.prepareStatement(QUERY_REPOSITORY_MATERIAL);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh);

					// 分货结束标记 false为已结束
					boolean overFlag = true;
					while (overFlag && kywlxxReset.next()) {
						int kwsl = kywlxxReset.getInt("KWSL");
						String hwdm = PrintUtil.parseNull(kywlxxReset.getString("HWDM"));
						String pch = PrintUtil.parseNull(kywlxxReset.getString("PCH"));

						int kysl;

						// 查询此物料的锁定情况.
						Integer sdsl = DAOUtil.getIntOrNull(conn, QUERY_MATERIAL_LOCKNUM, wlbh, pch, hwdm);
						if (sdsl == null || sdsl == 0) {
							kysl = kwsl;
						} else {
							kysl = kwsl - sdsl;
						}
						int haveSl = kysl;

						// 物料数量足够，则分配.
						if (haveSl > 0) {
							sl -= haveSl;

							// 预备转存入库单身中
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("HWDM", hwdm);
							hashtable.put("XSDH", xsddh);
							hashtable.put("PCH", pch);
							hashtable.put("KHCGDH", khcgdh);
							hashtable.put("WLBH", wlbh);
							hashtable.put("HWKYSL", String.valueOf(kysl));
							if (sl <= 0) {
								hashtable.put("XSSL", String.valueOf(haveSl + sl));
								// 分货结束
								overFlag = false;
							} else {
								hashtable.put("XSSL", String.valueOf(haveSl));
							}
							// 插入数据
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCKSK", hashtable, bindid, uid);
						}
					}
					if (overFlag == true && sl > 0) {
						throw new RuntimeException("销售订单：" + xsddh + "中物料编号为" + wlbh + "型号为" + xh + "的物料可用数量不足。");
					}
				} finally {
					DBSql.close(kywlxxPs, kywlxxReset);
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}
	
	/**
	 * 移除锁库.
	 * @param conn
	 * @param xsdh
	 * @throws SQLException
	 */
	public void removeLockMaterial(Connection conn, String xsdh) throws SQLException{
		DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_DGCKSK WHERE XSDH=?", xsdh);
	}

	/**
	 * 将出库单的单身重新进行锁库.
	 * 
	 * @param conn
	 * @param parentBindid
	 * @param bindid
	 * @param uid
	 * @param xsdh
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertLockFromBody(Connection conn, int bindid, String uid, String xsdh) throws SQLException, AWSSDKException {
		PreparedStatement ps = null;
		ResultSet result = null;
		try {
			int xsddbindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_DGXS_P WHERE XSDDID=?", xsdh);

			ps = conn.prepareStatement("SELECT WLBH,HWDM,PCH,HWKYSL,SFSL,KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID=?");
			result = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			// 遍历所有的单身物料信息
			while (result.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("HWDM", parseNullStr(result.getString("HWDM")));
				hashtable.put("KHCGDH", parseNullStr(result.getString("KHCGDH")));
				hashtable.put("PCH", parseNullStr(result.getString("PCH")));
				hashtable.put("HWKYSL", String.valueOf(result.getInt("HWKYSL")));
				hashtable.put("XSDH", xsdh);
				hashtable.put("WLBH", parseNullStr(result.getString("WLBH")));
				hashtable.put("XSSL", String.valueOf(result.getInt("SFSL")));
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCKSK", hashtable, xsddbindid, uid);
			}
		} finally {
			DBSql.close(ps, result);
		}
	}
	
	public String parseNullStr(String str){
		return str==null?"":str;
	}


}
