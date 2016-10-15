package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCK_YDTC extends WorkFlowStepRTClassA{
	
	public DGCK_YDTC() {
		super();
	}

	public DGCK_YDTC(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("填充代管运单");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			// 4-5 出库单+预约单填充运单
			// 4-6 出库单+预约单填充运单（物流单）
			
			fillYD(conn, bindid);
			/*
			Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
			Vector<Hashtable<String, String>> boDatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CKD_BODY", bindid);
			
			Hashtable<String, String> qsData = new Hashtable<String, String>();
			//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
			
			qsData.put("DDH", boData.get("CKDH"));
			qsData.put("TJ", boData.get("TJ"));
			qsData.put("ZL", boData.get("SJZL"));
			// 物料、型号、应收、产品名称、客户产品编号（自行关联）
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YD_P", qsData, bindid, getUserContext().getUID());
			*/
			
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
	private void fillYD(Connection conn, int bindid) throws SQLException, AWSSDKException{
		Hashtable<String, String> qsData = new Hashtable<String, String>();
		//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
		String sql = "select a.DDH, sum(b.TJ) as TJ,sum(b.ZL) ZL from BO_BO_AKL_DGCK_S a, BO_AKL_WLXX b where a.WLBH = b.WLBH group by a.DDH";
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(sql);
		while(rs.next()){
			qsData.put("DDH", rs.getString(1));
			qsData.put("TJ", rs.getString(2));
			qsData.put("ZL", rs.getString(3));
		}
		
		String yydMessage = DAOUtil.getString(conn, "SELECT CYS+'{}'+CYSDH+'{}'+CYSDZ+'{}'+CYSLXR FROM BO_BO_AKL_CK_YY_P WHERE BINDID=?", bindid);

		String[] yydSplit = yydMessage.split("\\{\\}");
		qsData.put("CYS", yydSplit[0]);
		qsData.put("LXR", yydSplit[3]);
		qsData.put("LXFS", yydSplit[1]);
		
		// 物料、型号、应收、产品名称、客户产品编号（自行关联）
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YD_P", qsData, bindid, getUserContext().getUID());
	}

}
