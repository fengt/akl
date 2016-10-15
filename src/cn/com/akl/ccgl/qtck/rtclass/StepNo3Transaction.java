package cn.com.akl.ccgl.qtck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	/**
	 * 查询出库单单身.
	 */
	private final String QUERY_CKD_BODY = "SELECT KWBH, SJSL, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	/**
	 * 库存操作类.
	 */
	private KCBiz kcbiz = new KCBiz();

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("流程流转后事件: 更新库存");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// 获取退回审核菜单.
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();

			// 删除锁库
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindid);
				
			// 不退回的时候扣减库存
			if(!backFlag){
				// 2、更新库存明细
				// 查询子表记录，扣减库存
				DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new DAOUtil.ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						kcbiz.outOfWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SJSL"));
						kcbiz.outOfWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SJSL"));
						return true;
					}
				}, bindid);
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
