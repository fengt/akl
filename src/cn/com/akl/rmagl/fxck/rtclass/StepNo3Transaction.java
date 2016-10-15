package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {
	/**
	 * 查询出库单单身.
	 */
	private final String QUERY_CKD_BODY = "SELECT KWBH, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
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
		
		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
			String xsddh = DAOUtil.getString(conn, "SELECT RMAFXDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			
			// 1、解锁库存
			DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_KC_SPPCSK WHERE DDH=?", xsddh);
			
			// 2、更新库存明细
						// 查询子表记录，扣减库存
						DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new DAOUtil.ResultPaser() {
							public boolean parse(Connection conn, ResultSet reset) throws SQLException {
								kcbiz.outOfWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SL"));
								kcbiz.outOfWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SL"));
								return true;
							}
						}, bindid);
			
			// 更新销售订单状态
			DAOUtil.executeUpdate(conn, "Update BO_AKL_WXB_RMAFX_P Set DDZT=3 WHERE FHDH=?", xsddh);
			
			String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			FillBiz fillBiz = new FillBiz();

			if("否".equals(sfyy)||XSDDConstant.NO.equals(sfyy)){
				// 非预约 填充运单
				fillBiz.fillYD(conn, bindid, getUserContext().getUID());
			} else {
				// 填充预约单
				fillBiz.fillYYD(conn, bindid, getUserContext().getUID());
			}
			
			conn.commit();
			return true;
		} catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch(Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
}
