package cn.com.akl.ccgl.qtck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

	/**
	 * 查询出库单单身.
	 */
	private final String QUERY_CKD_BODY = "SELECT KWBH, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	/**
	 * 填充操作类
	 */
	private FillBiz fillbiz = new FillBiz();
	/**
	 * 库存操作类
	 */
	private KCBiz kcbiz = new KCBiz();

	public StepNo4Transaction() {
		super();
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("填充运单");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// 退回标记
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");

		Connection conn = null;

		try {
			conn = DAOUtil.openConnectionTransaction();
			if (backFlag) {
				// 重新锁库
				kcbiz.insertLockBase(conn, bindid, uid);

				// 2、更新库存明细
				// 查询子表记录，扣减库存
				DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new DAOUtil.ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						kcbiz.enterWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SL"));
						kcbiz.enterWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SL"));
						return true;
					}
				}, bindid);
			} else {
				// 4-5 出库单+预约单填充运单
				// 4-6 出库单+预约单填充运单（物流单）
				fillbiz.fillYD(conn, bindid, getUserContext().getUID());
			}
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
}
