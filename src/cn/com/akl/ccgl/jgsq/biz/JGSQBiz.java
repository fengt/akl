package cn.com.akl.ccgl.jgsq.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class JGSQBiz {

	/**
	 * 物料信息缓存.
	 */
	private Map<String, Hashtable<String, String>> wlxxCacheMap = new HashMap<String, Hashtable<String, String>>();
	/**
	 * 总代成本缓存.
	 */
	private Map<String, BigDecimal> zdcbCacheMap = new HashMap<String, BigDecimal>();
	/**
	 * 加工申请原料及数量查询.
	 */
	private static final String QUERY_JGSQ_YL = "SELECT WLBH, XH, GG, WLMC, DW, ZDCB, SUM(ISNULL(JGSL, 0)) JGSL FROM BO_AKL_JGSQ_YL_S "
			+ "WHERE BINDID=? GROUP BY WLBH, XH, GG, WLMC, ZDCB, DW";
	/**
	 * 查询可用批次.
	 */
	private static final String QUERY_KYPC = "SELECT a.PCH, RKSL, ISNULL(CKSL, 0) as CKSL, ISNULL(b.SDSL, 0) AS SDSL "
			+ "FROM BO_AKL_KC_KCHZ_P a LEFT JOIN (SELECT WLBH, PCH, SUM(ISNULL(SDSL, 0)) as SDSL "
			+ "FROM BO_AKL_KC_SPPCSK GROUP BY WLBH, PCH) b ON a.PCH=b.PCH AND a.WLBH=b.WLBH "
			+ "WHERE RKSL-ISNULL(CKSL, 0)-ISNULL(b.SDSL, 0)>=0 AND a.WLBH=? AND a.PCH=? ORDER BY a.PCH";
	/**
	 * 查询可用的物料.
	 */
	private static final String QUERY_KCMX = "SELECT a.WLBH, a.CKDM, a.CKMC, a.PCH, RKRQ, b.DJ, a.HWDM,"
			+ "SUM(CASE WHEN ZT='042023' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as  ZTSL,"
			+ "SUM(CASE WHEN ZT='042022' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as ZCSL, "
			+ "a.JLDW FROM BO_AKL_KC_KCMX_S a RIGHT JOIN BO_AKL_KC_KCHZ_P b ON a.WLBH=b.WLBH AND a.PCH=b.PCH "
			+ "WHERE a.WLBH=? AND a.PCH=? GROUP BY a.WLBH, a.JLDW, a.CKDM, a.CKMC,a.HWDM, a.PCH, b.RKRQ, DJ ORDER BY SUM(a.KWSL)";
	/**
	 * 查询加工申请物料.
	 */
	private static final String QUERY_WLXX = "SELECT WLBH, WLMC, XH, GG, ZDCB, JGSL, PCH, DW FROM BO_AKL_JGSQ_YL_S WHERE BINDID=?";
	/**
	 * 查询规则编号.
	 */
	private static final String QUERY_JGSQ_GZBH = "SELECT GZBH FROM BO_AKL_JG_JGSQ_P WHERE BINDID=?";
	/**
	 * 加工申请，加工单号.
	 */
	private static final String QUERY_JGSQ_JGDH = "SELECT JGDH FROM BO_AKL_JG_JGSQ_P WHERE BINDID=?";

	/**
	 * 获取加工申请原料的物料编号以及数量.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Integer> getProcessingMaterial(Connection conn, int bindid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		Map<String, Integer> jgMap = new HashMap<String, Integer>();
		try {
			ps = conn.prepareStatement(QUERY_JGSQ_YL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				jgMap.put(reset.getString("WLBH"), reset.getInt("JGSL"));
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("WLBH", reset.getString("WLBH"));
				hashtable.put("WLMC", reset.getString("WLMC"));
				hashtable.put("XH", reset.getString("XH"));
				hashtable.put("GG", reset.getString("GG"));
				hashtable.put("DW", reset.getString("DW"));
				zdcbCacheMap.put(reset.getString("WLBH"), reset.getBigDecimal("ZDCB"));
				wlxxCacheMap.put(reset.getString("WLBH"), hashtable);
			}
			conn.commit();
			return jgMap;
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 获取物料信息.
	 * 
	 * @param wlbh
	 * @return
	 */
	public Hashtable<String, String> getProductInfo(String wlbh) {
		return wlxxCacheMap.get(wlbh);
	}

	/**
	 * 获取物料的总代成本.
	 * 
	 * @param wlbh
	 * @return
	 */
	public BigDecimal getProductCost(String wlbh) {
		return zdcbCacheMap.get(wlbh);
	}

	/**
	 * 获取规则编号.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public String getRuleNo(Connection conn, int bindid) throws SQLException {
		return DAOUtil.getString(conn, QUERY_JGSQ_GZBH, bindid);
	}
	
	/**
	 * 加工申请单号.
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public String getJGSQNo(Connection conn, int bindid) throws SQLException{
		return DAOUtil.getString(conn, QUERY_JGSQ_JGDH, bindid);
	}

	/**
	 * 根据询单物料信息表拆分物料，并锁库.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void splitWL(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
		// 拆分物料

		// 1、了解需要拆分的货物，数量
		PreparedStatement pstat = null;
		ResultSet reset = null;

		try {
			String ddid = getJGSQNo(conn, bindid);

			pstat = conn.prepareStatement(QUERY_WLXX);
			reset = DAOUtil.executeFillArgsAndQuery(conn, pstat, bindid);
			int i = 1;
			while (reset.next()) {
				String wlbh = reset.getString("WLBH");
				String wlmc = reset.getString("WLMC");
				String wlgg = reset.getString("GG");
				String xh = reset.getString("XH");
				int dfsl = reset.getInt("JGSL");
				String pch = reset.getString("PCH");

				// 1、查询此物料的所有批次，对照锁库表，找到可出的批次
				int sl = dfsl;

				if (dfsl == 0) {
					continue;
				}

				PreparedStatement pchPstat = null;
				ResultSet pchReset = null;

				try {
					pchPstat = conn.prepareStatement(QUERY_KYPC);
					pchReset = DAOUtil.executeFillArgsAndQuery(conn, pchPstat, wlbh, pch);

					while (pchReset.next() && sl > 0) {
						// 2、根据已存在的库存，逐渐分解
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
							Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
							Vector<Hashtable<String, String>> skVector = new Vector<Hashtable<String, String>>();

							kcmxPstat = conn.prepareStatement(QUERY_KCMX);
							kcmxReset = DAOUtil.executeFillArgsAndQuery(conn, kcmxPstat, wlbh, pch);

							while (kcmxReset.next() && kysl > 0) {
								Hashtable<String, String> hashtable = new Hashtable<String, String>();
								Hashtable<String, String> skhashtable = new Hashtable<String, String>();

								BigDecimal pccbj = kcmxReset.getBigDecimal("DJ");
								String ckdm = kcmxReset.getString("CKDM");
								String ckmc = kcmxReset.getString("CKMC");
								String hwdm = kcmxReset.getString("HWDM");
								int ztsl = kcmxReset.getInt("ZTSL");
								int kcsl = kcmxReset.getInt("ZCSL");
								int ckkysl = ztsl + kcsl;

								if (ckkysl <= 0) {
									continue;
								}

								if (kysl - ckkysl <= 0) {
									ckkysl = kysl;
									kysl = 0;
								} else {
									kysl = kysl - ckkysl;
								}

								hashtable.put("DDID", ddid);
								hashtable.put("DH", String.valueOf(i++));
								hashtable.put("WLBH", wlbh);
								hashtable.put("PCH", pch);
								hashtable.put("ZDCB", pccbj.toString());
								hashtable.put("CKID", ckdm);
								hashtable.put("CKMC", ckmc);
								hashtable.put("KYSL", String.valueOf(kcsl - sdsl));
								hashtable.put("ZTSL", String.valueOf(ztsl));
								hashtable.put("KCSL", String.valueOf(kcsl));
								hashtable.put("WLMC", wlmc);
								hashtable.put("XH", xh);
								hashtable.put("GG", wlgg);
								hashtable.put("KWSL", String.valueOf(ztsl + kcsl));
								hashtable.put("JGSL", String.valueOf(ckkysl));
								hashtable.put("HWDM", hwdm);

								skhashtable.put("PCH", pch);
								skhashtable.put("DDH", ddid);
								skhashtable.put("WLBH", wlbh);
								skhashtable.put("CKDM", ckdm);
								skhashtable.put("SDSL", String.valueOf(ckkysl));

								vector.add(hashtable);
								skVector.add(skhashtable);
							}
							// 3、分解结束，保存
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_JGSQ_WLMX_S", vector, bindid, uid);
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", skVector, bindid, uid);
						} finally {
							DBSql.close(kcmxPstat, kcmxReset);
						}
					}
				} finally {
					DBSql.close(pchPstat, pchReset);
				}

				if (sl > 0) {
					throw new RuntimeException("物料数量不足出库，型号为:" + xh + " ，缺少数量:" + sl);
				}
			}
		} finally {
			DBSql.close(pstat, reset);
		}
	}

}
