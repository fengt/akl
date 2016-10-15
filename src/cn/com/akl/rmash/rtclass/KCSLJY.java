package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class KCSLJY extends WorkFlowStepRTClassA {
	public KCSLJY(){}
	public KCSLJY(UserContext uc){
		super(uc);
		setVersion("RMA返新退货流程v1.0");
		setProvider("刘松");
		setDescription("用于验证返新商品库存数量是否足够");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		//获取RMA返新退货申请表的数据
		Hashtable h = null;
		String sql = null;
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable hft = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
		Vector vft = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
		int i = 1;
		int KWSL = 0;
		Connection conn = DBSql.open();
		try {
			Iterator it = vft.iterator();
			while(it.hasNext()){
				h = (Hashtable)it.next();
				if(h.get("LX").toString().equals("坏品返新")){
					sql = "select sum(PCSL) PCSL from BO_AKL_KC_KCHZ_P where WLBH = '"+h.get("FXWLBH")+"' and ZT = '042022'";
					conn.setAutoCommit(false);
					KWSL = DBSql.getInt(conn, sql, "PCSL");
					if(KWSL<Integer.parseInt(h.get("FXSL").toString())||KWSL==0){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "第"+i+"行,物料号："+h.get("FXWLBH")+",返新商品此库存数量不足，请检查！");
						return false;
					}
				}
				i++;
				conn.commit();
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true; 
	}

}
