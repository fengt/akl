package cn.com.akl.cwgl.yslc.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {
	
	/**
	 * 应收状态已付款
	 */
	private static final String YS_ZT_YFK = "已付";

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("第三节点节点办理事件");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		
		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
//			int count = DAOUtil.getInt(conn, "SELECT count(*) FROM BO_AKL_YS_S WHERE bindid=?", bindid);
			// 如果有未付款状态，则流程继续等待在本节点
			//如果单身中付款状态全部为已付款，则流程结束，
			
			boolean kpFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "开票");
			// TODO Other: 如果状态=1，则本条记录不可编辑，如果状态=0，则本条记录可编辑，
			if(kpFlag){
				// TODO 开票
				//4.开票：读取财务系统出库单，根据客户采购订单号，取回发票号，更新至出库表中的发票号字段
			} else{
				// 已收款
				DAOUtil.executeQueryForParser(conn, "SELECT KHBH, XSDH, CKDH, YSJE, SSJE FROM BO_AKL_YS_P p, BO_AKL_YS_S s WHERE p.BINDID=s.BINDID AND p.BINDID=?", 
						new DAOUtil.ResultPaser(){
							@Override
							public boolean parse(Connection conn, ResultSet reset) throws SQLException {
								//已收款：更新销售订单，出库表的状态为已收款
								//流程结束后更新应收信息表中该状态的字段（已抵扣\未抵扣），
								//	更新应收	
								DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_YS SET ZT=? WHERE KHBM=? AND CKDH=?", YS_ZT_YFK, reset.getString("KHBH"), reset.getString("CKDH"));
								
								if(reset.getString("XSDH") != null && !reset.getString("XSDH").trim().equals("")) {
									// 更新订单
									DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_YFK, reset.getString("XSDH"));
									// 更新出库
									// DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CGDD_HEAD SET DDZT=? WHERE DDID=?", "已收款", reset.getString("CKDH"));
								}
								return true;
							}
				}, bindid);
				
				// 更新返利资金池中状态字段(已抵扣\未抵扣)
				/*
				DAOUtil.executeQueryForParser(conn, "SELECT FLZJCID FROM BO_AKL_YS_DK_S WHERE BINDID=?", new DAOUtil.ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						// TODO 抵扣状态不清楚
						DAOUtil.executeUpdate(conn, "update BO_AKL_FL_MXB set zt=? where ID=?", "已抵扣", reset.getInt("FLZJCID"));
						return true;
					}
				}, bindid);
				*/
				// 获取客户，更新客户的返利抵扣
				
			}
			
			conn.commit();
			return true;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
	}

}
