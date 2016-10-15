package cn.com.akl.hhgl.hhrk.rtclass;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

//import cn.com.akl.hhgl.hhdd.constant.CgddConstant;
import cn.com.akl.hhgl.hhrk.biz.DealHhrkBodyDatasBiz;
import cn.com.akl.hhgl.hhrk.biz.DealRkDatasBiz;
import cn.com.akl.hhgl.hhrk.biz.DealRkForZcxxBiz;
import cn.com.akl.hhgl.hhrk.biz.UpdateDdztForCgddBiz;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class UpdateDdztForHhddStepNo1Event extends WorkFlowStepRTClassA{

	private Connection conn = null;
	private UserContext uc;
	public UpdateDdztForHhddStepNo1Event(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("qjc");
		setDescription("V1.0");
		setDescription("办理完毕后，更新采购订单的订单状态为：已转仓、插入库存汇总及明细");
	}
	@Override
	public boolean execute() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(HHDJConstant.tableName0, bindid);
		String rkdb = pTable.get("LYDH").toString();
		//System.out.println(rkdb);
		
		///**第一步：将转仓信息回填至入库单身**/
		//if(!rkdb.equals("A")){
			//DealHhrkBodyDatasBiz.dealDatas(bindid);
		//}
		
		/**第二步：将入库单头、单身相关数据信息插入至库存汇总表、库存明细表**/
		Vector sVector = BOInstanceAPI.getInstance().getBODatas(HHDJConstant.tableName1, bindid); 
		if(sVector == null || sVector.size() < 1 ){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "入库单单身中不存在记录",true);
			return false;
		}
		//DealRkDatasBiz rkUtil = new DealRkDatasBiz();
		//rkUtil.dealDatas(uc, pTable, sVector,rkdb);	
		Vector reSvector = new Vector();
		Hashtable reStable = null;
		for(int i=0;i<sVector.size();i++)
			
		{
			reStable = new Hashtable();
			reStable = (Hashtable)sVector.get(i);
			String a=reStable.get("LYDH").toString();
			
			String b=reStable.get("WLBH").toString();
			if(rkdb.substring(0,1).equals("H"))
			{
						String	sql2 = "update BO_AKL_HHDD_BODY_TOTAL set hhzt = '1' where [HH] = '" + a + "' and WLBH = '" + b +"'";
						
			int cnt = DBSql.executeUpdate(sql2);
			}
			else if(rkdb.substring(0,1).equals("J"))
				
			{String	sql2 = "update BO_JHDD_BODY_TOTAL set Jhzt = '1' where [HH] = '" + a + "' and WLBH = '" + b +"' and JHDH='"+pTable.get("LYDH").toString()+"'";
			
int cnt = DBSql.executeUpdate(sql2);}
			
		}
		return true;
	}
	
}
