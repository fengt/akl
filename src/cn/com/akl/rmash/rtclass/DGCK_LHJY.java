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

public class DGCK_LHJY extends WorkFlowStepRTClassA{

	public DGCK_LHJY(UserContext uc){
		super(uc);
		setVersion("代管出库流程v1.0");
		setProvider("刘松");
		setDescription("用于校验物料序列号");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		Connection conn = DBSql.open();
		int a = 0;
		int i = 1;
		int ID = 0;
		int SFSL = 0;
		int SFXLH = 0;
		Statement stat = null;
		ResultSet rs = null;
		String sql = null;
		String XLH = null;
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable h = null;
		Hashtable hh = null;
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_BO_AKL_DGCK_S", bindid);
		Vector vv = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CCB_CKD_XLH_S", bindid);
		if(v!=null){
			try {
				stat = conn.createStatement();
				Iterator it = v.iterator();
				//遍历出库单身表
				while(it.hasNext()){
					h = (Hashtable)it.next();
					//判断此物料是否序列号
					sql = "select SFXLH from BO_AKL_WLXX where WLBH = '"+h.get("WLBH")+"'";
					SFXLH = DBSql.getInt(conn, sql, "SFXLH");
					//此物料需要序列号
					if(SFXLH == 1&&vv!=null){
						Iterator itt = vv.iterator();
						//遍历出库序列号表
						while(itt.hasNext()){
							hh = (Hashtable)itt.next();
							//判断此物料的序列号在出库序列号表内是否存在
							if(hh.get("WLBH").toString().equals(h.get("WLBH"))){
								a++;
								sql = "select ID,XLH from BO_AKL_CCB_RKD_XLH_S where WLBH = '"+hh.get("WLBH")+"' and ZT = '在库' and XLH='"+hh.get("XLH")+"'";
								rs = stat.executeQuery(sql);
								while(rs.next()){
									ID = rs.getInt(1);
									XLH = rs.getString(2);
								}
								if(XLH==null){
									MessageQueue.getInstance().putMessage(getUserContext().getUID(), "序列号表第"+i+"行,"+h.get("WLBH")+"物料号所对应的序列号在入库序列表里不存在或已出库,请检查！");
									return false;
								}
							}
							else{
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), h.get("WLBH")+"物料号所对应的序列号在出库序列号表中不存在,请检查！");
								return false;
							}
							i++;
						}
						//料号数量
						sql = "select sum(SFSL) SFSL from BO_BO_AKL_DGCK_S where DDH = '"+h.get("DDH")+"' and WLBH = '"+h.get("WLBH")+"' group by WLBH";
						SFSL = DBSql.getInt(conn, sql, "SFSL");
						if(a>SFSL){
							MessageQueue.getInstance().putMessage(getUserContext().getUID(), h.get("WLBH")+"物料多"+(a-SFSL)+"序列号,请检查！");
						}
						else{
							MessageQueue.getInstance().putMessage(getUserContext().getUID(), h.get("WLBH")+"物料少"+(SFSL-a)+"于物料号,请检查！");
						}
					}
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				DBSql.close(conn, stat, null);
			}
		}
		return true;
	}

}
