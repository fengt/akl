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
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCK_KCGX_update extends WorkFlowStepRTClassA{

	public DGCK_KCGX_update(UserContext uc){
		super(uc);
		setVersion("代管出库流程v1.0");
		setProvider("刘松");
		setDescription("代管出库，扣除库存");
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		String sql = null;
		int KWSL = 0;
		int PCSL = 0;
		int CKSL = 0;
		int ZZ = 0;
		int ZZPC = 0;
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable h = BOInstanceAPI.getInstance().getBOData("BO_BO_AKL_DGCK_P", bindid);
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_BO_AKL_DGCK_S", bindid);
		Hashtable hckds = null;
		Hashtable hkcsl = null;
		Connection conn = DBSql.open();

		Statement stat = null;
		ResultSet rs = null;
		try {
			conn.setAutoCommit(false);
			stat = conn.createStatement();
			if(v!=null){
				Iterator it = v.iterator();
				while(it.hasNext()){
					hckds = (Hashtable)it.next();

					sql = "select KWSL from BO_AKL_DGKC_KCMX_S where WLBH = '"+hckds.get("WLBH")+"' and PCH = '"+hckds.get("PCH")+"' and HWDM = '"+hckds.get("HWDM")+"'";
					KWSL = DBSql.getInt(conn, sql, "KWSL");
					ZZ = KWSL-Integer.parseInt(hckds.get("SFSL").toString());
					sql = "update BO_AKL_DGKC_KCMX_S set KWSL="+ZZ+" where WLBH = '"+hckds.get("WLBH")+"' and PCH = '"+hckds.get("PCH")+"' and HWDM = '"+hckds.get("HWDM")+"'";
					stat.executeUpdate(sql);

					sql = "select PCSL, CKSL from BO_AKL_DGKC_KCHZ_P where WLBH = '"+hckds.get("WLBH")+"' and PCH = '"+hckds.get("PCH")+"'";
					rs = stat.executeQuery(sql);
					if(rs!=null){
						while(rs.next()){
							PCSL = rs.getInt(1);
							CKSL = rs.getInt(2);
						}
					}
					ZZ = PCSL-Integer.parseInt(hckds.get("SFSL").toString());
					ZZPC = CKSL+Integer.parseInt(hckds.get("SFSL").toString());
					sql = "update BO_AKL_DGKC_KCHZ_P set PCSL="+ZZ+", CKSL="+ZZPC+" where WLBH = '"+hckds.get("WLBH")+"' and PCH = '"+hckds.get("PCH")+"'";
					stat.executeUpdate(sql);
					conn.commit();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			DBSql.close(conn, stat, rs);
		}
		return true;
	}

}
