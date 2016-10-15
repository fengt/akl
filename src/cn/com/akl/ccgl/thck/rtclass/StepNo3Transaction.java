package cn.com.akl.ccgl.thck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {
	
	//更新采购订单的状态
	private final String UPDATE_CGDD = "UPDATE BO_AKL_CGDD_HEAD SET DZT='已退单' WHERE DDID=?";
	
	//出库单金额汇总
	private final String QUERY_CKD_JE = "SELECT SUM(SJSL*CBDJ)WSYFJE, SUM(SJSL*DJ)HSYFJE,YDH FROM BO_AKL_CKD_BODY WHERE BINDID=? GROUP BY YDH";
	
	//更新应付记录
	private final String UPDATE_YF = "UPDATE BO_AKL_YF SET WSYFJE=ISNULL(WSYFJE,0)-?,HSYFJE=ISNULL(HSYFJE,0)-?,LB=2 WHERE RKDH=?";//LB=2表示采购退货扣减应付
	
	//查询出库单单身.
	private final String QUERY_CKD_BODY = "SELECT KWBH, SJSL, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	
	//库存操作类.
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
			String ddid = DBSql.getString(conn, "SELECT THDH FROM BO_AKL_CKD_HEAD WHERE BINDID="+bindid, "THDH");//采购订单号

			// 删除锁库
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindid);
				
			// 不退回的时候扣减库存
			if(!backFlag){
				// 2、更新库存明细
				// 查询子表记录，扣减库存
				DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						kcbiz.outOfWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SJSL"));
						kcbiz.outOfWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SJSL"));
						return true;
					}
				}, bindid);
				
				// 3、根据入库单号扣减应付金额
				DAOUtil.executeQueryForParser(conn, QUERY_CKD_JE, new ResultPaser(){
					public boolean parse(Connection conn, ResultSet rs) throws SQLException{
						int count = DAOUtil.executeUpdate(conn, UPDATE_YF, rs.getDouble(1), rs.getDouble(2), rs.getString(3));
						if(count == 0){
							throw new RuntimeException("入库单号为"+rs.getString(3)+"的应付金额更新失败，请检查！");
						}
						
						return true;
					}
				}, bindid);
				
				//4、更新采购订单状态：已退单
				DAOUtil.executeUpdate(conn, UPDATE_CGDD, ddid);
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
