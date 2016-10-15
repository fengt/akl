package cn.com.akl.dgkgl.xsdd.biz;

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

public class DGSalesFillBiz {

	/**
	 * 查询单身所有物料信息.
	 */
	private static final String QUERY_BODY_MATERIAL = "SELECT b.WLBH,b.WLMC,b.GG,b.XH,b.JLDW,b.XSSL,b.KCSL,b.YCKSL,b.KHCGDH  FROM BO_AKL_DGXS_S b WHERE BINDID=?";
	/**
	 * 查询仓库的可用物料信息.
	 */
	private static final String QUERY_REPOSITORY_MATERIAL = "SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.HWDM, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? ORDER BY s.PCH, s.KWSL";
	/**
	 * 查询物料的锁定数量.
	 */
	private static final String QUERY_MATERIAL_LOCKNUM = "SELECT SUM(ISNULL(XSSL, 0)) SDSL FROM BO_AKL_DGCKSK WHERE WLBH=? AND PCH=? AND HWDM=?";

	/**
	 * 查询单身的所有物料，并且从仓库抓取物料
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fetchCanUseMaterial(Connection conn, int bindid, String uid, String xsddh, String cklx) throws SQLException, AWSSDKException {
		PreparedStatement ps = conn.prepareStatement(QUERY_BODY_MATERIAL);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
		try {
			while (reset.next()) {
				// 查询库位，根据时间排序
				String wlbh = PrintUtil.parseNull(reset.getString("WLBH"));
				String xh = PrintUtil.parseNull(reset.getString("XH"));
				String khcgdh = PrintUtil.parseNull(reset.getString("KHCGDH"));
				int xssl = reset.getInt("XSSL");
				int ycksl = reset.getInt("YCKSL");

				// 销售数量需要减去已出库数量.
				int sl = xssl - ycksl;

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

}
