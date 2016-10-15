package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMA_KCGX extends WorkFlowStepRTClassA{

	public RMA_KCGX(UserContext uc){
		super(uc);
		setVersion("RMA返新退货v1.0");
		setProvider("刘松");
		setDescription("更新RMA库房库存");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		Connection conn = DBSql.open();
		Statement stat = null;
		ResultSet rs = null;
		String sql = null;
		int ID = 0;
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable hft = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
		Hashtable h = null;
		Hashtable hh = null;
		try {
			if(v!=null){
				Iterator it = v.iterator();
				while(it.hasNext()){
					h = (Hashtable)it.next();
					sql = "select ID from BO_AKL_RMA_KCMX where ZJM = '"+h.get("THJBM")+"' and CKDH = '"+h.get("KHDH")+"'";

					ID = DBSql.getInt(conn, sql, "ID");
					hh = new Hashtable();
					if(h.get("LX").toString().equals("退回")){
						hh.put("KWSL", 0);
						hh.put("LX","退回已处理");
					}
					else
						hh.put("LX","返新已处理");

					BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_RMA_KCMX", hh, ID);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			DBSql.close(conn, stat, rs);
		}


		return true;
	}

}
