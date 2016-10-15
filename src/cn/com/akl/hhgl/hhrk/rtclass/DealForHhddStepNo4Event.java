package cn.com.akl.hhgl.hhrk.rtclass;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.hhgl.hhrk.biz.DealRkDatasBiz;
import cn.com.akl.hhgl.hhrk.biz.DealYsyfInfoBiz;
import cn.com.akl.hhgl.hhrk.biz.UpdateDdztForCgddBiz;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DealForHhddStepNo4Event extends WorkFlowStepRTClassA{

	private Connection conn = null;
	private UserContext uc;
	public DealForHhddStepNo4Event(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("qjc");
		setDescription("V1.0");
		setDescription("������Ϻ󣬸��²ɹ������Ķ���״̬Ϊ�������/�������!");
	}
	
	@Override
	public boolean execute() {
		conn = DBSql.open();
		PreparedStatement ps = null;
		ResultSet rs = null;
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		///**��һ�������²ɹ���������״̬**/
		//String sql = "select lydh,xh,sssl,pch from " + HHDJConstant.tableName1 + " where bindid = " + bindid;
		
		//try {
			//ps = conn.prepareStatement(sql);
			//rs = ps.executeQuery();
			//if(rs!=null){
				//while(rs.next()){
					//String aklOrderId = StrUtil.returnStr(rs.getString("lydh"));
					//String xh = StrUtil.returnStr(rs.getString("xh"));
					//String pch = StrUtil.returnStr(rs.getString("pch"));
					//int sssl = rs.getInt("sssl");
					//if(StrUtil.isNotNull(aklOrderId) && StrUtil.isNotNull(xh)){
						//String zt = judege( conn, aklOrderId, xh, sssl);
						//UpdateDdztForCgddBiz.updateDatas(aklOrderId,xh,zt,sssl);
						///**���¿����ܱ��е�״̬Ϊ"����"**/
						//dealStatusForKCHZ(xh, pch);
					//}
				//}
			//}
		//} catch (SQLException e) {
			//e.printStackTrace();
		//}finally{
		//	DBSql.close(conn, ps, rs);
		//}
		
	
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(HHDJConstant.tableName0, bindid);
		String rkdb = pTable.get("LYDH").toString();
		//System.out.println(rkdb);
		
		
		
		/**�ڶ���������ⵥͷ���������������Ϣ�����������ܱ������ϸ��**/
		Vector sVector = BOInstanceAPI.getInstance().getBODatas(HHDJConstant.tableName1, bindid); 
		
		DealRkDatasBiz rkUtil = new DealRkDatasBiz();
		rkUtil.dealDatas(uc, pTable, sVector,rkdb);	
		
		
		
		return false;
	}
	
	
	
	
}
