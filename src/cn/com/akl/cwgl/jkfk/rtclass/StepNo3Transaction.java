package cn.com.akl.cwgl.jkfk.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	/**
	 * 应付状态已付款
	 */
	private static final String YF_ZT_YFK = "已付";
	/**
	 * 入库单已付款状态
	 */
	private static final String RKD_DDZT_YFK = "已付";
	/**
	 * 采购单已付款状态
	 */
	private static final String CGD_DDZT_YFK = "已付";
	/**
	 * POS资金池已抵扣状态
	 */
	private static final String POSZJC_YDK = "0";
	
	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("出纳付款节点流程流转事件");
	}

	/**
	 * 4.出纳付款节点，如果单身某条记录为“未付款”状态，则流程不结束，继续跳转至出纳付款，如果单身所有记录均为已付款状态，则流程结束
	 * 5.单身已付款状态的记录，办理结束后，本条记录不可编辑
	 * 8.出纳节点办理后更新状态，已抵扣\已付款=1，未抵扣\未付款=0，如果状态=1，
	 * 	  则本条记录不可编辑，如果状态=0，则本条记录可编辑，
	 *   如果单身中付款状态全部为已付款，则流程结束，如果有未付款状态，则流程继续等待在本节点
	 */
	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			// 存在未付款记录则继续等待
			//int countFk = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_HKJK_S WHERE BINDID=? AND FKLX=?", bindid, 0);
			//int countDk = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_HKJK_DK_S WHERE BINDID=? AND DKZT=?", bindid, 0);
				
			// 1.流程结束后更新应付信息表中该状态的字段（已抵扣\未抵扣）
			DAOUtil.executeQueryForParser(conn, "SELECT RKDH, CGDH FROM BO_AKL_HKJK_S WHERE BINDID=?", new ResultPaser() {
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					if(reset.getString("CGDH") != null && !reset.getString("CGDH").trim().equals("")){
						// 更新已付款
						DAOUtil.executeUpdate(conn, "update BO_AKL_CCB_RKD_HEAD set FKZT=? where RKDH=?", RKD_DDZT_YFK, reset.getString("RKDH"));
//						DAOUtil.executeUpdate(conn, "update BO_AKL_CGDD_HEAD set FKZT=? where DDID=?", CGD_DDZT_YFK, reset.getString("CGDH"));
					}
					// TODO 抵扣状态不清楚
					DAOUtil.executeUpdate(conn, "update BO_AKL_YF set zt=? where RKDH=?", YF_ZT_YFK, reset.getString("RKDH"));
					return true;
				}
			}, bindid);
			
			// 2.流程结束后，更新POS资金池表中对应TPM号的状态（已抵扣\未抵扣）
			DAOUtil.executeQueryForParser(conn, "SELECT POSZJCID FROM BO_AKL_HKJK_DK_S WHERE BINDID=?", new ResultPaser() {
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					// TODO 抵扣状态不清楚
					DAOUtil.executeUpdate(conn, "update BO_AKL_POS_MXB set zt=?, SSJE=POSJE where ID=?", POSZJC_YDK, reset.getInt("POSZJCID"));
					return true;
				}
			}, bindid);
			
			// TODO 3.差异金额=应付金额-发票金额，如果差异金额<>0，流程结束后，向财务系统推送应付调整单
			conn.commit();
			return true;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	

}
