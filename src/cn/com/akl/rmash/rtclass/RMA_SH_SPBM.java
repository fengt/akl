package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMA_SH_SPBM extends WorkFlowStepRTClassA{
	public RMA_SH_SPBM(UserContext uc){
		super(uc);
		setVersion("RMA收货流程v1.0");
		setProvider("刘松");
		setDescription("用于自动匹配亚昆sku");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		String ZBname = getParameter(PARAMETER_TABLE_NAME).toString();
		if(ZBname.equals("BO_AKL_WXB_XS_RMASH_P")){
			Connection conn = DBSql.open();
			Statement state = null;
			ResultSet rs = null;
			int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
			Hashtable ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
			Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);
			Hashtable h = null;
			String sql = null;
			List l = null;
			Hashtable hh = null;
			int i = 1;
			try {
				state = conn.createStatement();
				conn.setAutoCommit(false);
				if(v!=null){
					Iterator it = v.iterator();
					while(it.hasNext()){
						l = new ArrayList();
						h = (Hashtable)it.next();
						sql = "select count(ID) ID from BO_AKL_WXB_XS_RMASH_S where BJTM = '"+h.get("BJTM")+"' and bindid = "+bindid;
						int iD = DBSql.getInt(conn, sql, "ID");
						if(iD>1){
							MessageQueue.getInstance().putMessage(getUserContext().getUID(), "单身第"+i+"行，备件条码："+h.get("BJTM")+"重复，请检查！");
							return false;
						}
						sql = "SELECT YKSPSKU, KHBM, SPMC, XH FROM BO_AKL_KHSPBMGL where KHSPSKU = '"+h.get("KHSPBH")+"'";
						rs = state.executeQuery(sql);
						if(rs.next()){
							sql = "update BO_AKL_WXB_XS_RMASH_S set DJBH = '"+ha.get("DJBH")+"',YKSKU = '"+rs.getString(1)+"', SHID = '"+rs.getString(2)+"', SPMC = '"+rs.getString(3)+"', XH = '"+rs.getString(4)+"' where BJTM = '"+h.get("BJTM")+"' and bindid="+bindid;
							DBSql.executeUpdate(conn, sql);	
						}
						else{
							MessageQueue.getInstance().putMessage(getUserContext().getUID(), "单身第"+i+"行，客户编码："+h.get("BJTM")+"对应的亚昆物料编号不存在！怀疑非本公司产品，请检查");
						}
						i++;
						conn.commit();
					}
				}
				return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				DBSql.close(conn, state, rs);
			}
			return false;
		}
		return true;
	}

}
