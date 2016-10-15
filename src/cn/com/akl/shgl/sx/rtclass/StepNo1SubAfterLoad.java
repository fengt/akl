package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;

public class StepNo1SubAfterLoad extends SubWorkflowEventClassA {

	private static final String ydlx0 = "077284";//售后客户到客服的收货
	private static final String QUERY_SX_S = "SELECT * FROM BO_AKL_SX_S WHERE BINDID=?";
	private static final String UPDATE_WLD_P = "UPDATE BO_AKL_WLYSD_P SET YDLX='"+ydlx0+"' WHERE BINDID=?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo1SubAfterLoad() {
	}

	public StepNo1SubAfterLoad(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("加载数据到物流子流程。");
	}

	@Override
	public boolean execute() {
		int parent_bindid = this.getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();//父流程bindid
		Hashtable process = getParameter(this.PARAMETER_SUB_PROCESS_INSTANCE_ID).toHashtable();
		String processid = process.get(0) == null?"":process.get(0).toString();
		if(processid.equals("")){
			return true;
		}
		final int sub_bindid = Integer.parseInt(processid);//子流程bindid
		final String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();

			//1、主表更新
			DAOUtil.executeUpdate(conn, UPDATE_WLD_P, sub_bindid);
			
			//2、插入待发货记录
//			ShipmentsBiz.insertShipments(conn, parent_bindid, uid);
			
			/*//2、子表插入
			DAOUtil.executeQueryForParser(conn, QUERY_SX_S, new ResultPaserAbs(){
				public boolean parse(Connection conn, ResultSet rs) throws SQLException{
					insertWL(conn, rs, sub_bindid, uid);
					return true;
				}
			}, parent_bindid);*/
			
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
	 * 插入调查记录
	 * @param conn
	 * @param rs
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 *//*
	private void insertWL(Connection conn, ResultSet rs, int bindid, String uid) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		try {
			rec.put("WLBH", rs.getString("WLBH"));
			rec.put("XH", rs.getString("XH"));
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DC_S", rec, bindid, uid);
		} catch (AWSSDKException e) {
			throw new RuntimeException(e);
		}
	}*/

}
