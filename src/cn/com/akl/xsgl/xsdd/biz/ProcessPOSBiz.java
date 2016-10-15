package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;

public class ProcessPOSBiz {

	/**
	 * 查询单身POS记录.
	 */
	private static final String QUERY_DSPOS = "SELECT 	ID, WLBH, PCH, DDID, CKID, DDSL, DFSL, POSID, POSFALX, POSJE, POSZCSL, FLFAH, FLFAMC, FLFALX, FLFS, FLZCJ, FLZCD, FLHJ FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	/**
	 * 查询过期POS.
	 */
	private static final String QUERY_POSGQ = "SELECT a.POSID cc FROM (SELECT POSID, POSFALX FROM BO_AKL_WXB_XSDD_BODY WHERE bindid=? AND POSID IS NOT NULL AND POSID<>''  GROUP BY POSID, POSFALX) as a, BO_AKL_WXB_XS_POS_HEAD as b, BO_AKL_WXB_XS_POS_BODY as c WHERE a.POSID=b.POSBH AND b.BINDID=c.BINDID AND a.POSFALX=? AND (JSSJ<GETDATE() OR KSSJ>GETDATE())";
	/**
	 * 回退POS金额.
	 */
	private static final String UPDATE_ROLLBACK_POSJE = "UPDATE BO_AKL_POS_MXB SET YSYJE=ISNULL(YSYJE, 0)-? WHERE FABH=?";
	/**
	 * 回退POS数量.
	 */
	private static final String UPDATE_ROLLBACK_POSSL = "UPDATE BO_AKL_WXB_XS_POS_BODY SET YSYSL=ISNULL(YSYSL, 0)-? WHERE BINDID=? AND WLBH=?";
	/**
	 * 更新POS资金池的应收金额.
	 */
	private static final String UPDATE_POSZJCYSJE = "UPDATE BO_AKL_POS_MXB SET YSYJE=ISNULL(YSYJE, 0)+? WHERE FABH=? AND YSYJE+?<=POSJE";
	/**
	 * 更新POS方案数量.
	 */
	private static final String UPDATE_POSFASL = "UPDATE BO_AKL_WXB_XS_POS_BODY SET YSYSL=ISNULL(YSYSL, 0)+? WHERE BINDID=? AND WLBH=? AND ISNULL(YSYSL, 0)+?<=POSSL";
	/**
	 * 查询POS的bindid.
	 */
	private static final String QUERY_POS_BINDID = "SELECT BINDID FROM BO_AKL_WXB_XS_POS_HEAD WHERE POSBH=?";
	/**
	 * 按POSID分组查询每组的总数量.
	 */
	private static final String QUERY_XSDD_DS_POS_FA = "SELECT POSID, WLBH, SUM(ISNULL(POSZCSL, 0)) as zcsl FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND POSFALX=? GROUP BY POSID, WLBH";
	/**
	 * 查询POS方案数量.
	 */
	private static final String QUERY_VALIDATE_POS_FA_SL = "SELECT a.POSBH+','+b.XH FROM BO_AKL_WXB_XS_POS_HEAD a,BO_AKL_WXB_XS_POS_BODY b WHERE a.POSBH=? AND a.BINDID=b.BINDID AND b.WLBH=? AND ISNULL(b.POSSL, 0)-ISNULL(b.YSYSL, 0)<?";
	/**
	 * 查询销售订单中每个POS的汇总金额.
	 */
	private static final String QUERY_XSDD_DS_POS_ZJC = "SELECT POSID, SUM(ISNULL(POSJE, 0)) as posje FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND POSFALX=? GROUP BY POSID";
	/**
	 * 查询方案编号中.
	 */
	private static final String QUERY_VALIDATE_POS_ZJC_JE = "SELECT FABH FROM BO_AKL_POS_MXB WHERE FABH=? AND ISNULL(POSJE, 0)-ISNULL(YSYJE, 0)>=?";
	/**
	 * 查询POS原价格.
	 */
	private static final String QUERY_VALIDATE_POS_YJG = "SELECT YJG FROM BO_AKL_WXB_XS_POS_HEAD h, BO_AKL_WXB_XS_POS_BODY b WHERE h.bindid=b.bindid AND h.POSBH=? AND WLBH=?";
	/**
	 * 查询物料是否有POS可用.
	 */
	private static final String QUERY_VALIDATE_HAVE_POS = "SELECT COUNT(*) FROM BO_AKL_WXB_XS_POS_HEAD h, BO_AKL_WXB_XS_POS_BODY b WHERE  h.bindid=b.bindid AND POSZT='030003' AND (KHBM=? OR KHBM='' OR KHBM IS NULL) AND WLBH=? AND POSSL-ISNULL(YSYSL, 0)>0";

	/**
	 * 校验POS方案是否有数量超过的.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOSFAEqualsFA(Connection conn, int bindid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_XSDD_DS_POS_FA);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, XSDDConstant.POS_FALX_FA);
			while (reset.next()) {
				String posid = reset.getString("POSID");
				if (posid == null || "".equals(posid.trim()))
					continue;
				String wlbh = reset.getString("WLBH");
				int zcsl = reset.getInt("zcsl");
				String message = DAOUtil.getStringOrNull(conn, QUERY_VALIDATE_POS_FA_SL, posid, wlbh, zcsl);
				if (message != null)
					throw new RuntimeException("有POS出现支持数量过多，POS和支持的型号分别是：" + message);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 校验POS资金池是否有金额超过的.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOSFAEqualsZJC(Connection conn, int bindid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_XSDD_DS_POS_ZJC);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, XSDDConstant.POS_FALX_ZJC);
			while (reset.next()) {
				String posid = reset.getString("POSID");
				if (posid == null || "".equals(posid.trim()))
					continue;
				BigDecimal posje = reset.getBigDecimal("posje");
				String message = DAOUtil.getStringOrNull(conn, QUERY_VALIDATE_POS_ZJC_JE, posid, posje);
				if (message != null)
					throw new RuntimeException("POS资金池方案出现超额，POS方案号为：" + message);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 验证POS是否过期.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOSTimeOut(Connection conn, int bindid) throws SQLException {
		String message = DAOUtil.getStringOrNull(conn, QUERY_POSGQ, bindid, XSDDConstant.POS_FALX_FA);
		if (message != null)
			throw new RuntimeException("该POS方案" + message + "已过期。流程不向下办理。");
	}

	/**
	 * 更新POS.
	 * 
	 * @param conn
	 * @param posid
	 * @param posfalx
	 * @param posje
	 * @param possl
	 * @param wlbh
	 * @throws SQLException
	 */
	public void rollBackPOS(Connection conn, String posid, String posfalx, BigDecimal posje, int possl, String wlbh) throws SQLException {

		// 审核菜单选择不同意时，更新POS方案表中当前POS方案的POS金额，POS数量=POS数量+答复数量
		// 审核菜单选择同意时，则根据答复数量与订单数量的差，更新POS方案表中当前POS方案的POS数量。POS数量=POS数量+（答复数量-订单数量）
		// POS 常规方案：消减POS方案中“剩余支持数量”，计算订单单身表中的POS金额=订单数量*POS支持单价，记录明细。
		// POS
		// 资金池方案：自动根据POS方案的”POS支持单价“=（POS金额/订单数量），更新POS资金池汇总表中的”已使用金额“，订单单身表中的POS金额手工录入
		// 1、查出所有POS
		// 2、根据POS支持类型选择扣减的位置
		if (XSDDConstant.POS_FALX_ZJC.equals(posfalx)) {
			// 如果tyFlag=true 就是向下办理，不通过扣减会询单金额
			// 回退 posje
			// POS资金池 更新金额
			if (DAOUtil.executeUpdate(conn, UPDATE_ROLLBACK_POSJE, posje, posid) == 0)
				throw new RuntimeException("更新POS资金池出现错误 方案号:" + posid);
		}

		if (XSDDConstant.POS_FALX_FA.equals(posfalx)) {
			// POS 方案 更新数量
			// 回退 ddsl
			String posbindid = DAOUtil.getString(conn, QUERY_POS_BINDID, posid);
			if (DAOUtil.executeUpdate(conn, UPDATE_ROLLBACK_POSSL, possl, posbindid, wlbh) == 0)
				throw new RuntimeException("更新POS方案出现错误 方案号:" + posid);
		}
	}

	/**
	 * 更新POS.
	 * 
	 * @param conn
	 * @param posid
	 * @param posfalx
	 * @param posje
	 * @param possl
	 * @param wlbh
	 * @throws SQLException
	 */
	public void updatePOS(Connection conn, String posid, String posfalx, BigDecimal posje, int possl, String wlbh) throws SQLException {
		// 审核菜单选择不同意时，更新POS方案表中当前POS方案的POS金额，POS数量=POS数量+答复数量
		// 审核菜单选择同意时，则根据答复数量与订单数量的差，更新POS方案表中当前POS方案的POS数量。POS数量=POS数量+（答复数量-订单数量）
		// POS 常规方案：消减POS方案中“剩余支持数量”，计算订单单身表中的POS金额=订单数量*POS支持单价，记录明细。
		// POS
		// 资金池方案：自动根据POS方案的”POS支持单价“=（POS金额/订单数量），更新POS资金池汇总表中的”已使用金额“，订单单身表中的POS金额手工录入
		// 1、查出所有POS
		// 2、根据POS支持类型选择扣减的位置
		if (XSDDConstant.POS_FALX_ZJC.equals(posfalx)) {
			// 如果tyFlag=true 就是向下办理，不通过扣减会询单金额
			// POS资金池 更新金额
			if (DAOUtil.executeUpdate(conn, UPDATE_POSZJCYSJE, posje, posid, posje) == 0)
				throw new RuntimeException("更新POS资金池出现错误，可能是POS支持金额超过了资金池金额，方案号:" + posid);
		}

		if (XSDDConstant.POS_FALX_FA.equals(posfalx)) {
			// POS 方案 更新数量
			String posbindid = DAOUtil.getString(conn, QUERY_POS_BINDID, posid);
			if (DAOUtil.executeUpdate(conn, UPDATE_POSFASL, possl, posbindid, wlbh, possl) == 0)
				throw new RuntimeException("更新POS方案出现错误，可能是订单POS支持数量超过了可支持的数量，方案号:" + posid);
		}
	}

	/**
	 * 批量回退所有此流程的POS.
	 * 
	 * @param conn
	 * @param bindId
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void rollBackPOSFromBindIdForZF(Connection conn, int bindId) throws SQLException, AWSSDKException {
		PreparedStatement state = null;
		ResultSet reset = null;

		try {
			// 查询出订单上所有的记录
			state = conn.prepareStatement(QUERY_DSPOS);
			reset = DAOUtil.executeFillArgsAndQuery(conn, state, bindId);
			while (reset.next()) {
				String posfalx = reset.getString("POSFALX");
				String posid = reset.getString("POSID");
				BigDecimal posje = reset.getBigDecimal("POSJE");
				int dfsl = reset.getInt("POSZCSL");
				String wlbh = reset.getString("WLBH");
				// 回退POS
				rollBackPOS(conn, posid, posfalx, posje, dfsl, wlbh);
			}
		} finally {
			DBSql.close(state, reset);
		}
	}

	/**
	 * 校验POS原价格.
	 * 相等则返回TRUE
	 * 
	 * @param posid
	 * @param wlbh
	 * @param nowPrice
	 * @return
	 * @throws SQLException
	 */
	public boolean validatePOSSalaPrice(Connection conn, String posid, String wlbh, BigDecimal nowPrice) throws SQLException {
		BigDecimal oldPrice = DAOUtil.getBigDecimalOrNull(conn, QUERY_VALIDATE_POS_YJG, posid, wlbh);
		return oldPrice.doubleValue() == nowPrice.doubleValue();
	}

	/**
	 * 验证物料是否有可用POS.
	 * 
	 * @param conn
	 * @param wlbh
	 * @return
	 * @throws SQLException
	 */
	public boolean validateIsHavePOS(Connection conn, String khid, String wlbh) throws SQLException {
		Integer count = DAOUtil.getIntOrNull(conn, QUERY_VALIDATE_HAVE_POS, khid, wlbh);
		return count == null || count == 0;
	}

}
