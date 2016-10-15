package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class RMA_FX_KCXR extends WorkFlowStepRTClassA {

	public RMA_FX_KCXR(UserContext uc){
		super(uc);
		setVersion("RMA返新退货v1.0");
		setProvider("刘松");
		setDescription("用于写入返新商品库存数量");
	} 
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		boolean TY = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
		if(TY){
			String ZBname = getParameter(PARAMETER_TABLE_NAME).toString();
			if(ZBname.equals("BO_AKL_WXB_RMAFX_P")){
				Connection conn = DBSql.open();
				Statement state = null;
				ResultSet rs = null;

				Hashtable ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
				Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
				Hashtable h = null;
				String sql = null;
				try {
					if(v!=null){
						Iterator it = v.iterator();
						while(it.hasNext()){
							h = (Hashtable)it.next();
							sql = "SELECT sum(PCSL) PCSL from BO_AKL_KC_KCHZ_P where WLBH = '"+h.get("FXWLBH")+"'";
							int PCSL = DBSql.getInt(conn, sql, "PCSL");
							sql = "update BO_AKL_WXB_RMAFX_S set KCSL = "+PCSL+" where THJBM = '"+h.get("THJBM")+"' and DDH = '"+h.get("SHDJBH")+"'";
							DBSql.executeUpdate(conn, sql);
						}
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					DBSql.close(conn, state, rs);
				}
				return true;
			}
		}
		return true;
	}

}
