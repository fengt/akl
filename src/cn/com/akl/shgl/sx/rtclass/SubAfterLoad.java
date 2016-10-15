package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class SubAfterLoad extends SubWorkflowEventClassA {

	private static final String QUERY_SX_S = "SELECT * FROM BO_AKL_SX_S WHERE BINDID=? AND SFDC='025000'";
	
	private Connection conn = null;
	private UserContext uc;
	public SubAfterLoad() {
	}

	public SubAfterLoad(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("加载数据到调查子流程。");
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
			
			//加载数据到调查子表
			DAOUtil.executeQueryForParser(conn, QUERY_SX_S, new ResultPaserAbs(){
				public boolean parse(Connection conn, ResultSet rs) throws SQLException{
					insertDC(conn, rs, sub_bindid, uid);
					return true;
				}
			}, parent_bindid);
			
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
	 */
	private void insertDC(Connection conn, ResultSet rs, int bindid, String uid) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		try {
			rec.put("WLBH", StrUtil.returnStr(rs.getString("WLBH")));
			rec.put("WLMC", StrUtil.returnStr(rs.getString("WLMC")));
			rec.put("XH", StrUtil.returnStr(rs.getString("XH")));
			rec.put("GMRQ", StrUtil.returnStr(rs.getString("GMRQ")));//购买时间
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DC_S", rec, bindid, uid);
		} catch (AWSSDKException e) {
			throw new RuntimeException(e);
		}
	}

}
