package cn.com.akl.cwgl.qtysyf.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {
	
	private static DAOUtil.ResultPaser parser1 = null;
	
	public StepNo4Transaction() {
		super();
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("第二节点流程");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		
		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
		
			 String lx = DAOUtil.getString(conn, "SELECT LX FROM BO_AKL_QTYSYF_P WHERE BINDID=?", bindid);
			 
			 DAOUtil.getString(conn, "", bindid);
			 
			 String feat = lx.substring(lx.length()-2, lx.length());
		 	
			// 流程结束后，其它应收\应付费用，推送至财务系统的其它应收\应付
				// 如果类型为应收\应付调整，流程结束后，将推送至财务系统的应收\其它应付调整单
			 
			 // 7.1 促销、客户市场费用支持、客户库存价保支持，同时更新客户表的应付总额
			if("04".equals(feat) || "05".equals(feat) || "10".equals(feat)) {
				String khbm = DAOUtil.getString(conn, "SELECT KHBM FROM BO_AKL_QTYSYF_P WHERE BINDID=?", bindid);
				PreparedStatement ps = null;
				ResultSet reset = null; 
				try{
					ps = conn.prepareStatement("SELECT FX,KM,BZ,HL,JE,BBJE,BM,YWY,XM FROM BO_AKL_QTYSYF_S WHERE BINDID=?");
					reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
					
					while(reset.next()){
						// 流程结束后，数据（客户市场费用支持、客户库存价保支持、市场支持金额）放入返利资金池中（向返利资金池表中插入数据）；
						BigDecimal bbje = reset.getBigDecimal("BBJE");
						Hashtable<String, String> hashtable = new Hashtable<String, String>();
						hashtable.put("FLJE", bbje.toString());
						hashtable.put("KHBM", khbm);
						hashtable.put("LX", lx);
						hashtable.put("ZT", "0");
						
						try {
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FL_MXB", hashtable, bindid, uid);
						} catch (AWSSDKException e) {
							throw new RuntimeException(e);
						}
					}	
				} finally {
					DBSql.close(ps, reset);
				}
			}
			
			//TODO 	7.2 供应商服务费、供应商订购标签费用，通过查询的方式展示
			if("07".equals(feat) || "08".equals(feat)) {
			}
			
			// 	7.3 其它应收\应付费用，将推送至财务系统的应收\其它应付调整单
			// 1、流程结束后，数据（促销、客户市场费用支持、客户库存价保支持、市场支持金额）放入返利资金池中（向返利资金池表中插入数据）；
			// 2、流程结束后，其它应收\应付费用，推送至财务系统的其它应收\应付
			if("09".equals(feat)) {
				
			}
			
			// 3、流程结束后，促销、客户市场费用支持、客户库存价保支持，推送至财务系统的应付
			if("04".equals(feat) || "05".equals(feat)) {
				
			}
			 
			DAOUtil.executeQueryForParser(conn, "SELECT FX,KM,BZ,HL,JE,BBJE,BM,YWY,XM FROM BO_AKL_QTYSYF_S WHERE BINDID=?", parser1, null, bindid);
			conn.commit();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 插入资金池，用于销售的时候抵扣
		039004	客户市场费用支持
		039005	客户库存价保支持
		039010	市场费用（MDF）
	
		做报表统计
		039007	供应商服务费
		039008	供应商订购标签费用

		插入财务系统
		039006	应收调整
		039011	应付调整
		039009	其它应收费用
		039012	其它应付费用
	 */
}
