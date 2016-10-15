package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

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
		
		Connection conn = null;
		
		try{
			conn = DAOUtil.openConnectionTransaction();			
			// 4-5 出库单+预约单填充运单
			// 4-6 出库单+预约单填充运单（物流单）
			
			fillYD(conn, bindid);

			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 预约单+出库单填充运单
	 * @param conn
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	private void fillYD(Connection conn, int bindid) throws SQLException, AWSSDKException{
		Hashtable<String, String> qsData = new Hashtable<String, String>();
		//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
		PreparedStatement ckPs = null;
		ResultSet ckReset = null;
		try{
			ckPs = conn.prepareStatement("SELECT CKDH,RMAFXDH,TJ,ZL FROM BO_AKL_CKD_HEAD WHERE BINDID=?");
			ckReset = DAOUtil.executeFillArgsAndQuery(conn, ckPs, bindid);
			if(ckReset.next()){
				qsData.put("CKDH",ckReset.getString("CKDH"));
				qsData.put("DDH",ckReset.getString("RMAFXDH"));
				qsData.put("TJ",ckReset.getString("TJ"));
				qsData.put("ZL",ckReset.getString("ZL"));
			}
		} finally {
			DBSql.close(ckPs, ckReset);
		}
		
		PreparedStatement yydPs = null;
		ResultSet yydReset = null;
		try{
			yydPs = conn.prepareStatement("SELECT CYS,CYSDH,CYSDZ,CYSLXR FROM BO_BO_AKL_CK_YY_P WHERE BINDID=?");
			yydReset = DAOUtil.executeFillArgsAndQuery(conn, yydPs, bindid);
			if(yydReset.next()){
				qsData.put("CYS",parseNull(ckReset.getString("CYS")));
				qsData.put("LXFS",parseNull(ckReset.getString("CYSDH")));
				qsData.put("LXR",parseNull(ckReset.getString("CYSLXR")));
			}
		} finally {
			DBSql.close(yydPs, yydReset);
		}
		
		if(qsData.size() != 0)
		// 物料、型号、应收、产品名称、客户产品编号（自行关联）
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YD_P", qsData, bindid, getUserContext().getUID());
	}
	
	public String parseNull(String str){
		return str==null?"":str;
	}
}
