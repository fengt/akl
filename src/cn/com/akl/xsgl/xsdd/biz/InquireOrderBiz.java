package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

public class InquireOrderBiz {
	/**
	 * 更新销售指导价.
	 */
	private static final String UPDATE_WLXX_XSDZJ = "UPDATE BO_AKL_WXB_XSDD_XDWL SET XSZDJ=? WHERE BINDID=? AND WLBH=?";
	/**
	 * 更新系统价税合计.
	 */
	private static final String UPDATE_XSDD_XTJSHJ = "UPDATE BO_AKL_WXB_XSDD_HEAD SET ZDJSHJ=? WHERE BINDID=?";

	/**
	 * 更新价格处理.
	 * 
	 * @param bindid
	 * @return
	 */
	public ResultPaserAbs getUpdateMaterialPricePaser(final int bindid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				BigDecimal xszdj = reset.getBigDecimal("JG");
				String wlbh = reset.getString("WLBH");
				DAOUtil.executeUpdate(conn, UPDATE_WLXX_XSDZJ, xszdj, bindid, wlbh);
				return false;
			}
		};
	}

	/**
	 * 询单时，更新系统价税合计.
	 * 
	 * @param bindid
	 * @return
	 */
	public ResultPaserAbs getComputeInquireOrderAmount(final int bindid) {
		return new ResultPaserAbs() {

			private BigDecimal enquiryAmount;

			@Override
			public void init(Connection conn) {
				enquiryAmount = new BigDecimal(0);
			}
			
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String flfah = reset.getString("FLFAH");
				String flfs = reset.getString("FLFS");
				int flsl = reset.getInt("FLSL");
				BigDecimal flzcdj = reset.getBigDecimal("FLZCD");
				BigDecimal xsdj = reset.getBigDecimal("XSDJ");
				int ddsl = reset.getInt("DFSL");

				if (xsdj == null) {
					xsdj = new BigDecimal(0);
				}

				BigDecimal xszj = xsdj.multiply(new BigDecimal(ddsl));

				BigDecimal flhjj = null;
				if (flfah == null || flfah.trim().equals("") || flfs == null || flfs.trim().equals("") || flzcdj == null) {
					flhjj = xszj;
				} else if (flfs.equals(XSDDConstant.FL_FLFS_HFL)) {
					BigDecimal flzcje = flzcdj.multiply(new BigDecimal(flsl));
					flhjj = xszj.subtract(flzcje);
				} else {
					flhjj = xszj;
				}

				enquiryAmount = enquiryAmount.add(flhjj);
				return true;
			}

			@Override
			public void destory(Connection conn) throws SQLException {
				enquiryAmount = enquiryAmount.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
				DAOUtil.executeUpdate(conn, UPDATE_XSDD_XTJSHJ, enquiryAmount, bindid);
			}
		};
	}
}
