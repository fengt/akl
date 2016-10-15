package cn.com.akl.shgl.fjjh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.fjjh.biz.FJJHBiz;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	private Connection conn = null;
	private UserContext uc;
	private FJJHBiz fjjhBiz = new FJJHBiz();
	public StepNo1Transaction() {
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("相关复检数量的更新。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_FJJH_P", bindid);
		String kfckbm = head.get("KFCKBM").toString();//客服仓库编码
		double fjbl = Double.parseDouble(head.get("FJBL").toString());//复检比率 
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			//根据比例计算复检数
			/*Vector<Hashtable<String, String>> vector = fjjhBiz.queryByKfzx(conn, kfckbm, bindid);
			updateByWlbh(conn, vector, fjbl, bindid);*/
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		} catch (Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台");
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 更新复检计划和调拨单的复检数量
	 * @param conn
	 * @param vector
	 * @param fjbl
	 * @param bindid
	 * @throws SQLException
	 */
	/*public void updateByWlbh(Connection conn, Vector<Hashtable<String, String>> vector, double fjbl, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(FJJHCnt.QUERY_WLXX);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
				for (int i = 0; i < vector.size(); i++) {
					Hashtable<String, String> record = vector.get(i);
					String tmp_wlbh = record.get("WLBH").toString();
					String dbdh = record.get("DBDH").toString();
					String sx = record.get("SX").toString();
					int fjsl = Integer.parseInt(record.get("FJSL").toString());
					
					if(tmp_wlbh.equals(wlbh)){
						double tmp_fjjhsl = fjsl * (fjbl / 100);//复检计划数量
						int fjjhsl = (int)tmp_fjjhsl;
						DAOUtil.executeUpdate(conn, FJJHCnt.UPDATE_FJSL, fjjhsl, bindid, wlbh, dbdh, sx);//更新复检计划的复检数量
						DAOUtil.executeUpdate(conn, FJJHCnt.UPDATE_DB_FJSL, fjbl, fjjhsl, wlbh, sx, dbdh);//更新调拨单的复检数量
					}
				}
			}
		} finally{
			DBSql.close(ps, rs);
		}
	}*/
	
}
