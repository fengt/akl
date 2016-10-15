package cn.com.akl.xsgl.xsdd.ddbg.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessRebateBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

public class StepNo1AfterSave extends WorkFlowStepRTClassA {

	/**
	 * 销售订单，单身.
	 */
	private static final String QUERY_XSDD_DS = "SELECT ID, WLBH, PCH, DDID, POSZCSL, CKID, DDZJE, FLSL, DDSL, POSID, POSFALX, POSJE, FLFAH, FLFAMC, FLFALX, FLFS, JJZE, FLZCJ, FLZCD, FLHJ, DFSL, SDZT  FROM BO_WXB_XSDD_BG_S WHERE BINDID=?";
	/**
	 * 计算应收合计.
	 */
	private static final String QUERY_XSDD_YSJE_SUM = "SELECT SUM(ISNULL(YSJE, 0)) FROM BO_WXB_XSDD_BG_S WHERE BINDID=?";
	/**
	 * 更新销售订单的系统价税合计。
	 */
	private static final String UPDATE_XSDD_XTJSHJ = "UPDATE BO_WXB_XSDD_BG_P SET ZDJSHJ=? WHERE BINDID=?";

	public StepNo1AfterSave() {
		super();
	}

	public StepNo1AfterSave(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("第一节点保存后事件，用于计算金额。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

		// 1、
		Connection conn = null;
		try {
			if ("BO_WXB_XSDD_BG_P".equals(tableName)) {
				conn = DAOUtil.openConnectionTransaction();
				computeYS(conn, bindid);
				BigDecimal xsjshj = DAOUtil.getBigDecimalOrNull(conn, QUERY_XSDD_YSJE_SUM, bindid);
				DAOUtil.executeUpdate(conn, UPDATE_XSDD_XTJSHJ, xsjshj, bindid);
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

	/**
	 * 计算应收.
	 * 
	 * @param conn
	 * @param bindId
	 * @param tyFlag
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void computeYS(Connection conn, int bindId) throws SQLException, AWSSDKException {
		PreparedStatement state = null;
		ResultSet reset = null;

		ProcessRebateBiz flbiz = new ProcessRebateBiz();
		try {
			// 查询出订单上所有的记录
			state = conn.prepareStatement(QUERY_XSDD_DS);
			reset = DAOUtil.executeFillArgsAndQuery(conn, state, bindId);
			while (reset.next()) {
				// 获取返利方式
				String flfs = reset.getString("FLFS");
				BigDecimal ddzje = reset.getBigDecimal("DDZJE");
				BigDecimal jjze = reset.getBigDecimal("JJZE");
				int id = reset.getInt("ID");
				BigDecimal flzcj = reset.getBigDecimal("FLZCJ");

				// 处理返利
				flbiz.processFLFS(conn, flfs, ddzje, jjze, flzcj, id);
			}
		} finally {
			DBSql.close(state, reset);
		}
	}

}
