package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCK_QSDTC extends WorkFlowStepRTClassA{
	public DGCK_QSDTC() {
		super();
	}

	public DGCK_QSDTC(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("填充签收单");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			// 出库单填充签收单
//			String message = DAOUtil.getString(conn, "SELECT TOP 1 SHDZ1+'{}'+CKDH+'{}'+KHMC+'{}'+CXFZR+'{}'+CXDH+'{}'+CK+'{}'+REPLACE(STR(YSHJ),' ','') FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
//			String[] split = message.split("\\{\\}");
			
			String sql = "select TOP 1 SHDZ1, CKDH, KHMC from BO_BO_AKL_DGCK_P where bindid ="+bindid;
			ps = conn.prepareStatement(sql);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			Hashtable<String, String> qsData = new Hashtable<String, String>();
			while(reset.next()){

				//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
				qsData.put("SHDZ", reset.getString(1));
				qsData.put("CKDH", reset.getString(2));
				qsData.put("SHDW", reset.getString(3));
			}
			
			Vector<Hashtable<String, String>> qsDatas = new Vector<Hashtable<String,String>>();
			
			ps = conn.prepareStatement("SELECT WLMC,XH,WLH,SJSL,JLDW FROM BO_AKL_CKD_BODY WHERE BINDID=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			
			while(reset.next()){
				Hashtable<String, String> qsDatas_s = new Hashtable<String, String>();
				String wlh = reset.getString("WLH");
				String khspsku = DAOUtil.getString(conn, "SELECT KHSPSKU FROM BO_AKL_KHSPBMGL WHERE YKSPSKU=?", wlh);
				qsDatas_s.put("KHSPBH", khspsku);
				qsDatas_s.put("CPMC", reset.getString("WLMC"));
				qsDatas_s.put("XH", reset.getString("XH"));
				qsDatas_s.put("WLH", reset.getString("WLH"));
				qsDatas_s.put("YSSL", String.valueOf(reset.getInt("SJSL")));
				qsDatas_s.put("DW", reset.getString("JLDW"));
				qsDatas.add(qsDatas_s);
			}
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_P", qsData, bindid, getUserContext().getUID());
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_S", qsDatas, bindid, getUserContext().getUID());
			
			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return true;
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}
}
