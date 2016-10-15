package cn.com.akl.shgl.fjjh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

	//查询复检计划子表
	public static final String QUERY_FJJH = "SELECT * FROM BO_AKL_FJJH_S WHERE BINDID=?";
	
	private Connection conn;
	private UserContext uc;
	public StepNo4Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("扫描所有调拨单汇总，将数据推送到调拨子流程。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	public Vector<Hashtable<String, String>> method(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		try{
			ps = conn.prepareStatement(QUERY_FJJH);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
				String dbdh = StrUtil.returnStr(rs.getString("DBDH"));//调拨单号
				int fjjhsl = rs.getInt("FJJHSL");//复检计划数量
				rec.put("WLBH", wlbh);
				rec.put("DBDH", dbdh);
				rec.put("FJSL", String.valueOf(fjjhsl));
				vector.add(rec);
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return vector;
	}
	
	
	

}
