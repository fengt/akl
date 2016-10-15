package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dgkgl.xsck.biz.DGOutBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	/**
	 * 查询销售单号.
	 */
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * 出库节点.
	 */
	private static final int CK_STEPNO = 3;

	private DGOutBiz outBiz = new DGOutBiz();

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第一节点校验事件");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;

		try {
			conn = DBSql.open();

			String xsdh = DAOUtil.getString(conn, queryXSDH, bindid);

			/** 查询是否有相同的销售单号的出库流程(位于出库节点前的流程) */
			validateRepeatSalesOrderInOut(conn, bindid, xsdh);

			/** 验证销售订单的数量与出库数量是否相符 */
			outBiz.validateSalesAndOutNumIsEquals(conn, bindid, uid);

			/** 验证物料的库位数量是否充足 */
			outBiz.validateMaterialAvailableAmount(conn, bindid, uid);

			/** 查询销售状态 */
			outBiz.validateIsCanOut(conn, bindid, uid, xsdh);

			/** 验证出库数量是否超过了已出库数量 */
			outBiz.validateSalesOutNum(conn, bindid, xsdh);

			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 校验出库之前是否有相同的销售订单.
	 * 
	 * @param conn
	 * @param bindid
	 * @param xsddh
	 * @throws SQLException
	 */
	public void validateRepeatSalesOrderInOut(Connection conn, int bindid, String xsddh) throws SQLException {
		DAOUtil.executeQueryForParser(conn, "SELECT BINDID FROM BO_BO_AKL_DGCK_P WHERE XSDH=? AND ISEND=0 AND BINDID<>?", new DAOUtil.ResultPaser() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				/** 查询办理相同销售订单的出库流程，并且查出位于第几节点 */
				Integer wfsid = DAOUtil.getIntOrNull(conn, "SELECT TOP 1 WFSID FROM WF_TASK WHERE BIND_ID=? ORDER BY BEGINTIME DESC",
						reset.getInt("BINDID"));
				Integer stepNo = DAOUtil.getIntOrNull(conn, "SELECT STEPNO FROM SYSFLOWSTEP WHERE ID=?", wfsid);
				/** 比对节点是否大于出库节点 */
				if (stepNo <= CK_STEPNO) {
					throw new RuntimeException("有相同的销售订单正在被办理，一次只能做一单！");
				}
				return true;
			}
		}, xsddh, bindid);
	}

}
