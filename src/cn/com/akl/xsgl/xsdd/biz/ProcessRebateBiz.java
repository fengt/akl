package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 返利处理类.
 * 
 * @author huangming
 *
 */
public class ProcessRebateBiz {

	/**
	 * 更新返利数量.
	 */
	private static final String UPDATE_FLSL = "UPDATE BO_AKL_WXB_XS_FL_BODY SET YSYSL=ISNULL(YSYSL, 0)+? WHERE FLBH=? AND WLBH=? AND ISNULL(YSYSL, 0)+?<=FLSL";
	/**
	 * 回滚返利数量.
	 */
	private static final String UPDATE_ROLLBACK_FLSL = "UPDATE BO_AKL_WXB_XS_FL_BODY SET YSYSL=ISNULL(YSYSL, 0)-? WHERE FLBH=? AND WLBH=? AND ISNULL(YSYSL, 0)-?>=0";
	/**
	 * 更新应收金额.
	 */
	private static final String UPDATE_YSJE = "UPDATE BO_AKL_WXB_XSDD_BODY SET YSJE=? WHERE ID=?";
	/**
	 * 增加已使用金额.
	 */
	private static final String UPDATE_ADD_YSYJE = "UPDATE BO_AKL_FL_MXB SET YSYJE=ISNULL(YSYJE,0)+? WHERE KHBM=?";
	/**
	 * 查询返利是否在时间范围内.
	 */
	private static final String QUERY_FL_INRANGE_DATE = "SELECT b.FLFAH cc FROM BO_AKL_WXB_XS_FL_HEAD a RIGHT JOIN BO_AKL_WXB_XSDD_BODY b ON b.FLFAH=a.FLFABH AND FLFAH IS NOT NULL AND FLFAH<>'' WHERE (JSSJ<GETDATE() OR KSSJ>GETDATE()) AND b.BINDID=?";
	/**
	 * 查询返利资金池中此客户本方案的记录总数.
	 */
	private static final String QUERY_YS_RECORDCOUNT = "SELECT COUNT(*) FROM BO_AKL_FL_MXB WHERE KHBM=?";
	/**
	 * 查询返利方案数量.
	 */
	private static final String QUERY_FL_FLFAH_SL = "SELECT FLFAH, WLBH, SUM(ISNULL(FLSL, 0)) as FLZCSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND FLFAH<>'' AND FLFAH is not null GROUP BY FLFAH, WLBH";
	/**
	 * 校验返利方案数量.
	 */
	private static final String QUERY_VALIDATE_FL_FLFAH_SL = "SELECT FLFABH+','+b.XH FROM BO_AKL_WXB_XS_FL_HEAD a, BO_AKL_WXB_XS_FL_BODY b WHERE a.BINDID=b.BINDID AND FLFABH=? AND WLBH=? AND FLSL-YSYSL<?";

	/**
	 * 更新返利.
	 * 
	 * @param conn
	 * @param reset
	 * @param ddsl
	 * @throws SQLException
	 */
	public void updateFL(Connection conn, String flfah, int flsl, String wlbh) throws SQLException {
		// 审核菜单选择不同意时，更新返利方案表中当前返利方案的返利金额，返利数量=返利数量+答复数量
		// 审核菜单选择同意时，则根据答复数量与订单数量的差，更新返利方案表中当前返利方案的返利数量。返利数量=返利数量+（答复数量-订单数量）
		// 返利 价格支持方案：根据开始结束时间，根据订单数量，更新返利支持方案表中返利方案中返利数量。
		// 返利 资金池方案：根据订单单身的返利支持金额更新返利资金池表中的已使用金额。
		// 后返利操作，“返利支持单价”*“数量”写入返利资金池中；可报表导出所有返利明细；}
		// 返利类型
		// 返利方案号
		// if (XSDDConstant.FL_FALX_JGZC.equals(flfalx)) {
		// 返利正常方案
		// 回退 -ddsl
		if (DAOUtil.executeUpdate(conn, UPDATE_FLSL, flsl, flfah, wlbh, flsl) == 0)
			throw new RuntimeException("返利方案更新失败");
		// }
		// 先返利 应收金额=净价总额，后返利 应收金额=订单总额
		/*
		 * BigDecimal yfje = ("0".equals(flfs)) ? jjze : ddzje; if (DAOUtil.executeUpdate(conn,
		 * updateYSJE, yfje, id) == 0) throw new RuntimeException("返利应付更新失败");
		 */
	}

	/**
	 * 校验返利数量.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateFLSL(Connection conn, int bindid) throws SQLException {
		// 返利数量
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_FL_FLFAH_SL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String flfah = reset.getString("FLFAH");
				if (flfah == null || "".equals(flfah.trim()))
					continue;

				String wlbh = reset.getString("WLBH");
				int flzcsl = reset.getInt("FLZCSL");
				String message = DAOUtil.getStringOrNull(conn, QUERY_VALIDATE_FL_FLFAH_SL, flfah, wlbh, flzcsl);
				if (message != null)
					throw new RuntimeException("返利方案数量不足，返利方案号和物料号为：" + message);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 校验返利方案是否超时.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateFLTimeOut(Connection conn, int bindid) throws SQLException {
		// 返利金额是否超时判断
		String message = DAOUtil.getStringOrNull(conn, QUERY_FL_INRANGE_DATE, bindid);
		if (message != null) {
			throw new RuntimeException("返利方案号" + message + " 过期");
		}
	}

	/**
	 * 回退返利.
	 * 
	 * @param conn
	 * @param flfah
	 * @param flsl
	 * @param wlbh
	 * @throws SQLException
	 */
	public void rollbackFL(Connection conn, String flfah, int flsl, String wlbh) throws SQLException {
		// 审核菜单选择不同意时，更新返利方案表中当前返利方案的返利金额，返利数量=返利数量+答复数量
		// 审核菜单选择同意时，则根据答复数量与订单数量的差，更新返利方案表中当前返利方案的返利数量。返利数量=返利数量+（答复数量-订单数量）
		// 返利 价格支持方案：根据开始结束时间，根据订单数量，更新返利支持方案表中返利方案中返利数量。
		// 返利 资金池方案：根据订单单身的返利支持金额更新返利资金池表中的已使用金额。
		// 后返利操作，“返利支持单价”*“数量”写入返利资金池中；可报表导出所有返利明细；}
		// 返利类型
		// 返利方案号
		// if (XSDDConstant.FL_FALX_JGZC.equals(flfalx)) {
		// 返利正常方案
		// 回退 -ddsl
		if (DAOUtil.executeUpdate(conn, UPDATE_ROLLBACK_FLSL, flsl, flfah, wlbh, flsl) == 0)
			throw new RuntimeException("返利方案更新失败");
		// }
	}

	/**
	 * 处理后返利.
	 * 
	 * @param conn
	 * @param bindId
	 * @param uid
	 * @param khid
	 * @param flfs
	 * @param flfah
	 * @param ddzje
	 * @param jjze
	 * @param flzcj
	 * @param id
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void processHFL(Connection conn, int bindId, String uid, String khid, String flfs, String flfah, BigDecimal jjze,
			BigDecimal flzcj) throws SQLException, AWSSDKException {
		if (XSDDConstant.FL_FLFS_HFL.equals(flfs)) {
			// 后返
			// 注入返利资金池
			// TODO 每个 返利方案+客户 = 主键
			int count = DAOUtil.getInt(conn, QUERY_YS_RECORDCOUNT, khid);
			if (count == 0) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("FABH", flfah);
				hashtable.put("KHBM", khid);
				hashtable.put("LX", "0");
				hashtable.put("YSYJE", String.valueOf(flzcj.floatValue()));
				hashtable.put("FLJE", String.valueOf(flzcj.floatValue()));
				hashtable.put("BXSQJE", "0");
				hashtable.put("YSJE", "0");
				hashtable.put("SSJE", "0");
				hashtable.put("ZT", "0");
				BOInstanceAPI.getInstance().createBOData("BO_AKL_FL_MXB", hashtable, bindId, uid);
			} else {
				DAOUtil.executeUpdate(conn, UPDATE_ADD_YSYJE, flzcj, khid);
			}
		}
	}
	
	/**
	 * 回退后返利.
	 * 
	 * @param conn
	 * @param khid
	 * @param flfs
	 * @param ddzje
	 * @param flzcj
	 * @param id
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void rollbackHFL(Connection conn, String khid, String flfs, BigDecimal ddzje,
			BigDecimal flzcj) throws SQLException, AWSSDKException {
		if (XSDDConstant.FL_FLFS_HFL.equals(flfs)) {
			// 后返
			// 返利金额
			// 注入返利资金池
			// TODO 每个 返利方案+客户 = 主键
			int count = DAOUtil.getInt(conn, QUERY_YS_RECORDCOUNT, khid);
			if (count == 0) {
				throw new RuntimeException("找不到此客户的返利资金池记录，无法回退! 客户ID:" + khid);
			} else {
				DAOUtil.executeUpdate(conn, UPDATE_ADD_YSYJE, -flzcj.doubleValue(), khid);
			}
		}
	}

	/**
	 * 处理返利方式，计算应收
	 * 
	 * @param conn
	 * @param bindId
	 * @param uid
	 * @param khid
	 * @param flfs
	 * @param flfah
	 * @param ddzje
	 * @param jjze
	 * @param flzcj
	 * @param id
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void processFLFS(Connection conn, String flfs, BigDecimal ddzje, BigDecimal jjze, BigDecimal flzcj, int id) throws SQLException{
		if (XSDDConstant.FL_FLFS_XFL.equals(flfs)) {
			// 先返
			DAOUtil.executeUpdate(conn, UPDATE_YSJE, jjze, id);
		}

		if (XSDDConstant.FL_FLFS_HFL.equals(flfs)) {
			// 后返
			DAOUtil.executeUpdate(conn, UPDATE_YSJE, ddzje, id);
		}

		if (XSDDConstant.FL_FLFS_BFL.equals(flfs) || flfs == null || "".equals(flfs.trim())) {
			// 不返
			DAOUtil.executeUpdate(conn, UPDATE_YSJE, ddzje, id);
		}
	}
}
