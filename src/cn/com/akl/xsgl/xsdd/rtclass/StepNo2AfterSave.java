package cn.com.akl.xsgl.xsdd.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ComputeBiz;
import cn.com.akl.xsgl.xsdd.biz.ProcessPOSBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo2AfterSave extends WorkFlowStepRTClassA {

	/**
	 * 销售订单，单身.
	 */
	private static final String QUERY_XSDD_DS = "SELECT ID, WLBH, PCH, DDID, POSZCSL, CKID, DDZJE, FLSL, DDSL, POSID, POSFALX, POSJE, FLFAH, FLFAMC, FLFALX, FLFS, JJZE, FLZCJ, FLZCD, FLHJ, DFSL, SDZT  FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	/**
	 * 查询销售订单应收金额汇总.
	 */
	private static final String QUERY_XSDD_YSJE_SUM = "SELECT SUM(ISNULL(YSJE, 0)) FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	/**
	 * 更新销售订单的系统价税合计.
	 */
	private static final String UPDATE_XSDD_ZDJSHJ = "UPDATE BO_AKL_WXB_XSDD_HEAD SET ZDJSHJ=? WHERE BINDID=?";
	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_DS_POS = "SELECT POSID, XH, WLBH, PCCBJ FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	
	private ComputeBiz computeBiz = new ComputeBiz();
	private ProcessPOSBiz posbiz = new ProcessPOSBiz();
	
	public StepNo2AfterSave() {
		super();
	}

	public StepNo2AfterSave(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("节点二，保存后事件：计算应收");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

		// 1、
		Connection conn = null;
		try {
			if ("BO_AKL_WXB_XSDD_HEAD".equals(tableName)) {
				conn = DAOUtil.openConnectionTransaction();
				final String khid = DAOUtil.getStringOrNull(conn, "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?", bindid);
				// 校验POS
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS_POS, new DAOUtil.ResultPaser() {
					@Override
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						String posid = reset.getString("POSID");
						if (posid == null || posid.trim().equals("")) {
							if (!posbiz.validateIsHavePOS(conn, khid, reset.getString("WLBH"))) {
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "型号：" + reset.getString("XH") + " 有可选POS!", true);
								return false;
							}
						} else {
							if (!posbiz.validatePOSSalaPrice(conn, posid, reset.getString("WLBH"), reset.getBigDecimal("PCCBJ"))) {
								MessageQueue.getInstance()
										.putMessage(getUserContext().getUID(), "型号：" + reset.getString("XH") + " 批次成本价与POS原价格不同!", true);
								return false;
							}
						}
						return true;
					}
				}, bindid);
				
				// 更新应收金额.
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, computeBiz.getComputeReceivable(), bindid);
				// 更新系统价税合计.
				BigDecimal xsjshj = DAOUtil.getBigDecimalOrNull(conn, QUERY_XSDD_YSJE_SUM, bindid);
				DAOUtil.executeUpdate(conn, UPDATE_XSDD_ZDJSHJ, xsjshj, bindid);
				conn.commit();
			}
			return true;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现问题，请联系系统管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
