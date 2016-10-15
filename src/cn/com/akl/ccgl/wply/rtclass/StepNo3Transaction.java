package cn.com.akl.ccgl.wply.rtclass;

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

	//查询物品领用单身.
	private final String QUERY_WPLY_BODY = "SELECT WLBH,PCH,SL,HWDM FROM BO_AKL_WPLY_S WHERE BINDID=?";
	
	//领用出库，出明细.
	private static final String UPDATE_OUT_MX = "update BO_AKL_KC_KCMX_S set KWSL=ISNULL(KWSL, 0)-? "
			+ "where HWDM=? AND WLBH=? AND PCH=? AND KWSL>=?";
	
	//领用出库，出汇总.
	private static final String UPDATE_OUT_HZ = "update BO_AKL_KC_KCHZ_P set CKSL=ISNULL(CKSL, 0)+? "
			+ "where WLBH=? AND PCH=? AND ?<=RKSL";
	
	//出库更新汇总表的批次数量.
	private static final String UPDATE_OUT_HZ_PCSL = "update BO_AKL_KC_KCHZ_P set PCSL=ISNULL(RKSL, 0)-ISNULL(CKSL, 0) "
			+ "where WLBH=? AND PCH=?";
	
	//更新领用品状态
	private static final String zt0 = "已生效";
	private static final String UPDATE_WPLY_ZT = "UPDATE BO_AKL_WPLY_P SET ZT=? WHERE BINDID=?";

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
				DAOUtil.executeQueryForParser(conn, QUERY_WPLY_BODY, new DAOUtil.ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						outOfWarehouseMX(conn, reset.getString("WLBH"), reset.getString("PCH"), reset.getString("HWDM"), reset.getInt("SL"));
						outOfWarehouseHZ(conn, reset.getString("WLBH"), reset.getString("PCH"), reset.getInt("SL"));
						return true;
					}
				}, bindid);
				//3、更新领用单状态:已生效
				DAOUtil.executeUpdate(conn, UPDATE_WPLY_ZT, zt0,bindid);
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
	
	/**
	 * 领用出库，出明细.
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param kwbh
	 * @param sl
	 * @throws SQLException
	 */
	public void outOfWarehouseMX(Connection conn, String wlbh, String pch, String kwbh, Integer sl) throws SQLException {
		if (0 == DAOUtil.executeUpdate(conn, UPDATE_OUT_MX, sl, kwbh, wlbh, pch, sl)) {
			throw new RuntimeException("物料号:" + wlbh + " 在货位代码：" + kwbh + "库存不足" + sl + "!");
		}
	}
	
	/**
	 * 领用出库，出汇总.
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param sl
	 * @throws SQLException
	 */
	public void outOfWarehouseHZ(Connection conn, String wlbh, String pch, Integer sl) throws SQLException {
		if (0 == DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ, sl, wlbh, pch, sl)) {
			throw new RuntimeException("物料号:" + wlbh + " 这批" + pch + "物料的出库数量已经达到上限，请检查库存汇总表");
		}
		DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ_PCSL, wlbh, pch);
	}

}
