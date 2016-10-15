package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 锁库操作.
 * 
 * @author huangming
 *
 */
public class ProcessMaterialBiz {

	/**
	 * 查询可用批次.
	 */
	private static final String QUERY_KYPC = "SELECT a.PCH, RKSL, ISNULL(CKSL, 0) as CKSL, ISNULL(b.SDSL, 0) AS SDSL FROM BO_AKL_KC_KCHZ_P a LEFT JOIN (SELECT WLBH, PCH, SUM(ISNULL(SDSL, 0)) as SDSL FROM BO_AKL_KC_SPPCSK GROUP BY WLBH, PCH) b ON a.PCH=b.PCH AND a.WLBH=b.WLBH WHERE RKSL-ISNULL(CKSL, 0)-ISNULL(b.SDSL, 0)>0 AND a.WLBH=? ORDER BY a.PCH";
	/**
	 * 查询可用的物料.
	 */
	private static final String QUERY_KCMX = "SELECT a.WLBH, a.PCH, RKRQ, b.DJ, SUM(CASE WHEN ZT='042023' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as  ZTSL,SUM(CASE WHEN ZT='042022' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as ZCSL, a.JLDW FROM BO_AKL_KC_KCMX_S a RIGHT JOIN BO_AKL_KC_KCHZ_P b ON a.WLBH=b.WLBH AND a.PCH=b.PCH WHERE a.WLBH=? AND a.PCH=? AND a.CKDM=? AND (a.SX='049088' OR a.SX='049090') GROUP BY a.WLBH, a.JLDW, a.PCH, b.RKRQ, DJ HAVING SUM(CASE WHEN ZT='042022' THEN ISNULL(a.KWSL, 0) ELSE 0 END)+SUM(CASE WHEN ZT='042023' THEN ISNULL(a.KWSL, 0) ELSE 0 END)>0 ORDER BY SUM(a.KWSL)";
	/**
	 * 查询毛利率.
	 */
	private static final String QUERY_MLL = "SELECT MLL FROM BO_AKL_WLXX WHERE WLBH=?";

	/**
	 * 插入锁库记录.
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ddh
	 * @param pch
	 * @param wlbh
	 * @param ckdm
	 * @param ddsl
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertSK(Connection conn, int bindid, String uid, String ddh, String pch, String wlbh, String ckdm, int ddsl) throws SQLException,
			AWSSDKException {
		// 插入记录
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		hashtable.put("PCH", pch);
		hashtable.put("DDH", ddh);
		hashtable.put("SDSL", String.valueOf(ddsl));
		hashtable.put("WLBH", wlbh);
		hashtable.put("CKDM", ckdm);
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", hashtable, bindid, uid);
	}

	/**
	 * 删除锁库记录.
	 * @param conn
	 * @param bindId
	 * @throws SQLException
	 */
	public void deleteSK(Connection conn, int bindId) throws SQLException {
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindId);
	}

	
	/**
	 * 分配物料.
	 * @param bindid
	 * @param uid
	 * @param ddid
	 * @return
	 */
	public ResultPaserAbs getAllocationResultPaser(final int bindid, final String uid, final String ddid, final String ckdm, final String ckmc) {
		return new ResultPaserAbs() {
			private int row = 0;
			private ComputeBiz computeBiz = new ComputeBiz();
			
			private Vector<Hashtable<String, String>> insertVector = new Vector<Hashtable<String, String>>();

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");
				// 此处占用资源.
				BigDecimal mll = DAOUtil.getBigDecimalOrNull(conn, QUERY_MLL, wlbh);
				String wlmc = reset.getString("WLMC");
				String wlgg = reset.getString("WLGG");
				String xh = reset.getString("XH");
				String zj = reset.getString("ZL");
				String tj = reset.getString("TJ");
				String khspbh = reset.getString("KHSPBH");
				BigDecimal xszdj = reset.getBigDecimal("XSZDJ");
				BigDecimal xsdj = reset.getBigDecimal("XSDJ");
				String wos = reset.getString("WOS");
				String sdzt = reset.getString("SDZT");
				String jldw = reset.getString("JLDW");
				int dfsl = reset.getInt("DFSL");

				if (dfsl == 0) {
					return true;
				}

				// 1、查询此物料的所有批次，对照锁库表，找到可出的批次
				int sl = dfsl;

				PreparedStatement pchPstat = null;
				ResultSet pchReset = null;

				try {
					pchPstat = conn.prepareStatement(QUERY_KYPC);
					pchReset = DAOUtil.executeFillArgsAndQuery(conn, pchPstat, wlbh);

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
							kcmxReset = DAOUtil.executeFillArgsAndQuery(conn, kcmxPstat, wlbh, pch, ckdm);

							while (kcmxReset.next() && kysl > 0) {
								Hashtable<String, String> hashtable = new Hashtable<String, String>();
								Hashtable<String, String> skhashtable = new Hashtable<String, String>();

								BigDecimal pccbj = kcmxReset.getBigDecimal("DJ");
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
								hashtable.put("DH", String.valueOf(row++));
								hashtable.put("WLBH", wlbh);
								hashtable.put("PCH", pch);
								hashtable.put("PCCBJ", pccbj.toString());
								hashtable.put("CKID", ckdm);
								hashtable.put("CKMC", ckmc);
								hashtable.put("KYSL", String.valueOf(kcsl - sdsl));
								hashtable.put("ZTSL", String.valueOf(ztsl));
								hashtable.put("KC", String.valueOf(kcsl));
								hashtable.put("WLMC", wlmc);
								hashtable.put("XH", xh);
								hashtable.put("WLGG", wlgg);
								hashtable.put("ZL", zj);
								hashtable.put("TJ", tj);
								hashtable.put("KHSPBH", khspbh);
								hashtable.put("XSZDJ", xszdj.toString());
								hashtable.put("XSDJ", xsdj == null || xsdj.doubleValue() == 0 ? xszdj.toString() : xsdj.toString());
								hashtable.put("WOS", wos);
								hashtable.put("SDZT", sdzt);
								hashtable.put("JLDW", jldw);
								hashtable.put("WLGDMLL", mll == null ? "" : mll.toString());
								hashtable.put("ZT", "0");// 未结束
								hashtable.put("KWSL", String.valueOf(ztsl + kcsl));
								hashtable.put("XSSL", String.valueOf(ckkysl));
								hashtable.put("DDSL", String.valueOf(ckkysl));
								hashtable.put("SL", "0.17");

								// 获取POS信息
								String posfalx = reset.getString("POSFALX");
								String posid = reset.getString("POSID");
								String posmc = reset.getString("POSMC");
								BigDecimal poszcdj = reset.getBigDecimal("POSZCDJ");
								BigDecimal posje = reset.getBigDecimal("POSJE");
								int poszcsl = reset.getInt("POSZCSL");

								// 获取返利信息
								String flfs = reset.getString("FLFS");
								String flfah = reset.getString("FLFAH");
								String flfamc = reset.getString("FLFAMC");
								// BigDecimal flzcj = reset.getBigDecimal("FLZCJ");
								BigDecimal flzcd = reset.getBigDecimal("FLZCD");
								int flsl = reset.getInt("FLSL");

								// 存入POS信息
								hashtable.put("POSFALX", parseNull(posfalx));
								hashtable.put("POSID", parseNull(posid));
								hashtable.put("POSMC", parseNull(posmc));

								// 存入返利信息
								hashtable.put("FLFS", parseNull(flfs));
								hashtable.put("FLFAH", parseNull(flfah));
								hashtable.put("FLFAMC", parseNull(flfamc));

								// 存入物料基础信息
								skhashtable.put("PCH", parseNull(pch));
								skhashtable.put("DDH", parseNull(ddid));
								skhashtable.put("WLBH", parseNull(wlbh));
								skhashtable.put("CKDM", parseNull(ckdm));
								skhashtable.put("SDSL", String.valueOf(ckkysl));

								if (posfalx.equals(XSDDConstant.POS_FALX_FA) && poszcsl > 0) {
									if (poszcsl - ckkysl >= 0 && poszcsl > 0) {
										poszcsl = poszcsl - ckkysl;
										hashtable.put("POSZCSL", String.valueOf(ckkysl));
									} else {
										hashtable.put("POSZCSL", String.valueOf(poszcsl));
										poszcsl = 0;
									}
									hashtable.put("POSZCDJ", poszcdj.toString());
								} else if (posfalx.equals(XSDDConstant.POS_FALX_ZJC) && posje.doubleValue() > 0) {
									hashtable.put("POSZCSL", String.valueOf(ckkysl));
									hashtable.put("POSJE", posje.toString());
									posje = new BigDecimal(0);
								}

								if (flfah != null && !flfah.trim().equals("")) {
									if (flsl - ckkysl >= 0 && flsl > 0) {
										flsl = flsl - ckkysl;
										hashtable.put("FLSL", String.valueOf(ckkysl));
									} else {
										hashtable.put("FLSL", String.valueOf(flsl));
										flsl = 0;
									}
									hashtable.put("FLZCD", flzcd.toString());
								}
								computeBiz.computeChengben(hashtable);
								computeBiz.computePOS(hashtable);
								computeBiz.computeFL(hashtable);

								insertVector.add(hashtable);

								if ("是".equals(sdzt) || XSDDConstant.YES.equals(sdzt)) {
									BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", skhashtable, bindid, uid);
								}
							}
							
							if(kysl > 0){
								sl = sl + kysl;
							}
							
						} catch (AWSSDKException e) {
							throw new RuntimeException(e);
						} finally {
							DBSql.close(kcmxPstat, kcmxReset);
						}
					}

					if (sl > 0) {
						throw new RuntimeException("物料数量不足，型号为:" + xh + " ，缺少数量:" + sl);
					}

					return true;
				} finally {
					DBSql.close(pchPstat, pchReset);
				}
			}

			@Override
			public void destory(Connection conn) throws SQLException {
				try {
					// 3、插入分配完的物料.
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_WXB_XSDD_BODY", insertVector, bindid, uid);
				} catch (AWSSDKException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	
	/**
	 * 插入锁库
	 * @param bindid
	 * @param uid
	 * @return
	 */
	public ResultPaserAbs getInsertLockRepositoryPaser(final int bindid, final String uid, final String ddh){
		return new ResultPaserAbs(){
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				try {
					insertSK(conn, bindid, uid, ddh, reset.getString("PCH"), reset.getString("WLBH"), reset.getString("CKID"), reset.getInt("DDSl"));
					return true;
				} catch (AWSSDKException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	
	/**
	 * 校验订单中的库存数量.
	 * @return
	 */
	public ResultPaserAbs getValidateRepository(final String ddid){
		return new ResultPaserAbs(){
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				int pcsl = DAOUtil.getInt(conn, "SELECT SUM(KWSL) FROM BO_AKL_KC_KCHZ_P p, BO_AKL_KC_KCMX_S s WHERE p.WLBH=s.WLBH AND p.PCH=s.PCH AND p.PCH=? AND p.WLBH=?", reset.getString("PCH"), reset.getString("WLBH"));
				int sdsl = DAOUtil.getInt(conn, "SELECT SUM(SDSL) FROM BO_AKL_KC_SPPCSK WHERE PCH=? AND WLBH=? AND DDH<>?", reset.getString("PCH"), reset.getString("WLBH"), ddid);
				int ddsl = reset.getInt("DDSL");
				if(ddsl > pcsl-sdsl){
					throw new RuntimeException("物料编号："+reset.getString("WLBH") + " 的订单数量超过了可用数量，可用数量目前只剩下："+(pcsl-sdsl));
				}
				return true;
			}
		};
	}

	/**
	 * 转换NULL.
	 * 
	 * @param str
	 * @return
	 */
	public String parseNull(String str) {
		return str == null ? "" : str;
	}

}
