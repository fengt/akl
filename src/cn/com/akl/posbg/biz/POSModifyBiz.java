package cn.com.akl.posbg.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;

public class POSModifyBiz {

	/**
	 * 查询POS的BINDID.
	 */
	private static final String QUERY_POS_BINDID = "SELECT BINDID FROM BO_AKL_WXB_XS_POS_HEAD WHERE POSBH=?";
	/**
	 * 更新pos申请单头.
	 */
	private static final String UPDATE_POS_DT = "update BO_AKL_WXB_XS_POS_HEAD set KSSJ=?, JSSJ=?, POSMC=? where POSBH=?";
	/**
	 * 更新销售订单中的POS以及其他的计算..
	 */
	private static final String UPDATE_XSDD_POS = "UPDATE BO_AKL_WXB_XSDD_BODY SET POSZCDJ =?, POSJE =?*POSZCSL, CBZE = ( PCCBJ * DDSL - ISNULL(POSZCSL, 0) * ? ) * ( CASE WHEN SL IS NULL THEN 1.17 ELSE 1 + (SL / 100) END ), JJMLL = ( 1 - ( PCCBJ * DDSL - ISNULL(POSZCSL, 0) * ? ) * ( CASE WHEN SL IS NULL THEN 1.17 ELSE 1 + (SL / 100) END ) / JJZE ) * 100 WHERE WLBH=? AND POSID=?";
	/**
	 * 更新POS单身数据.
	 */
	private static final String UPDATE_POS_DS = "UPDATE BO_AKL_WXB_XS_POS_BODY SET POSDJ=?, POSSL=? WHERE BINDID=? AND WLBH=?";

	/**
	 * 更新销售订单中该POS的单价，支持单价，净价毛利率.
	 * 
	 * @return
	 */
	public ResultPaserAbs getUpdateSalerOrder() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String posbh = reset.getString("POSBH");
				String wlbh = reset.getString("WLBH");
				Double posdj = reset.getDouble("POSDJ");
				DAOUtil.executeUpdate(conn, UPDATE_XSDD_POS, posdj, posdj, posdj, posdj, wlbh, posbh);
				return true;
			}
		};
	}

	/**
	 * 更新POS中的记录信息.
	 * 
	 * @param posid
	 * @return
	 */
	public ResultPaserAbs getUpdatePOSBody(final String posid) {
		return new ResultPaserAbs() {
			private int bindid;

			@Override
			public void init(Connection conn) throws SQLException {
				bindid = DAOUtil.getIntOrNull(conn, QUERY_POS_BINDID, posid);
			}

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");
				Double posdj = reset.getDouble("POSDJ");
				int possl = reset.getInt("POSSL");
				DAOUtil.executeUpdate(conn, UPDATE_POS_DS, posdj, possl, bindid, wlbh);
				return true;
			}
		};
	}

	/**
	 * 更新POS单头的信息.
	 * 
	 * @return
	 */
	public ResultPaserAbs getUpdatePOSHead() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				DAOUtil.executeUpdate(conn, UPDATE_POS_DT, reset.getTimestamp("KSSJ"), reset.getTimestamp("JSSJ"), reset.getString("POSMC"),
						reset.getString("POSBH"));
				return false;
			}
		};
	}

	/**
	 * 验证POS修改后的数量和POS数量.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOS(Connection conn, int bindid) throws SQLException {
		String message = DAOUtil.getStringOrNull(conn, "SELECT WLBH FROM BO_AKL_POS_BG_S WHERE POSSL<YSYSL AND BINDID=?", bindid);
		if (message != null) {
			throw new RuntimeException("物料编号：" + message + "，当前的数量超过已使用数量，应该小于已使用数量！");
		}
	}

}
