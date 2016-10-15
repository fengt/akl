package cn.com.akl.shgl.zsjgl.jggz.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	/**加工视图BINDID.*/
	private static final String viewBindid = "SELECT TOP 1 BINDID,COUNT(1)N FROM BO_AKL_SH_JGGZ_VIEW WHERE 1=? GROUP BY BINDID ORDER BY N";
	
	/**加工物料消耗子表查询*/
	private static final String QUERY_JGXH = "SELECT * FROM BO_AKL_SH_JGGZ_S WHERE BINDID=?";
	
	/**加工生产物料子表查询*/
	private static final String QUERY_JGSC = "SELECT * FROM BO_AKL_SH_JGGZ_SC_S WHERE BINDID=?";
	
	/**加工配件消耗子表查询*/
	private static final String QUERY_JGPJ = "SELECT * FROM BO_AKL_SH_JGGZ_PJ_S WHERE BINDID=?";
	
	private static final String QUERY_GZBH = "SELECT GZBH FROM BO_AKL_SH_JGGZ_P WHERE BINDID=?";
	private static final String QUERY_XMLX = "SELECT XMLX FROM BO_AKL_SH_JGGZ_P WHERE BINDID=?";
	private static final String QUERY_JGWLXH = "SELECT JGWLXH FROM BO_AKL_SH_JGGZ_P WHERE BINDID=?";
	private static final String QUERY_JGWLBH = "SELECT JGWLBH FROM BO_AKL_SH_JGGZ_P WHERE BINDID=?";
	private static final String QUERY_JGLX = "SELECT JGLX FROM BO_AKL_SH_JGGZ_P WHERE BINDID=?";
	
	private Connection conn;
	private UserContext uc;
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("办理后插入售后加工视图表中。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			Integer jgBindid2 = DAOUtil.getIntOrNull(conn, viewBindid, 1);
			int jgBindid = jgBindid2 == null ? 0 : jgBindid2.intValue();
			if(jgBindid == 0){
				throw new RuntimeException("请先新建一个售后加工规则视图，然后再办理！");
			}
			service(conn, bindid, uid, jgBindid);
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	public void service(Connection conn, final int bindid, final String uid, final int viewBindid) throws SQLException{
		
		//插入加工物料消耗视图表、BO_AKL_SH_JGGZ_VIEW
		DAOUtil.executeQueryForParser(conn, QUERY_JGXH, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				insertHandle(conn, bindid, viewBindid, uid, rs, "BO_AKL_SH_JGGZ_VIEW", false);
				return true;
			}
		}, bindid);
		
		//插入加工生产物料视图表、BO_AKL_SH_JGSC_VIEW
		DAOUtil.executeQueryForParser(conn, QUERY_JGSC, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				insertHandle(conn, bindid, viewBindid, uid, rs, "BO_AKL_SH_JGSC_VIEW", false);
				return true;
			}
		}, bindid);
		
		//插入加工配件消耗视图表、BO_AKL_SH_JGPJ_VIEW
		DAOUtil.executeQueryForParser(conn, QUERY_JGPJ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				insertHandle(conn, bindid, viewBindid, uid, rs, "BO_AKL_SH_JGPJ_VIEW", true);
				return true;
			}
		}, bindid);
	}

	/**
	 * 插入操作
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param rs
	 * @param tableName
	 * @throws SQLException
	 */
	public void insertHandle(Connection conn, int bindid, int viewBindid, String uid, ResultSet rs, String tableName, Boolean flag) throws SQLException{
		
		String xmlx = DAOUtil.getString(conn, QUERY_XMLX, bindid);
		String gzbh = DAOUtil.getString(conn, QUERY_GZBH, bindid);
		String jgwlxh = DAOUtil.getString(conn, QUERY_JGWLXH, bindid);
		String jgwlbh = DAOUtil.getString(conn, QUERY_JGWLBH, bindid);
		String jglx = DAOUtil.getString(conn, QUERY_JGLX, bindid);
		
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		String gg = StrUtil.returnStr(rs.getString("GG"));
		String sx = StrUtil.returnStr(rs.getString("SX"));
//		String gzbh = StrUtil.returnStr(rs.getString("GZBH"));
//		String xmlx = StrUtil.returnStr(rs.getString("XMLX"));
//		String jgwlxh = StrUtil.returnStr(rs.getString("JGWLXH"));
//		String jglx = StrUtil.returnStr(rs.getString("JGLX"));
//		String jgwlbh = StrUtil.returnStr(rs.getString("JGWLBH"));
		int sl = rs.getInt("SL");
		double dj = 0.0;
		if(flag) dj = rs.getDouble("DJ");
		
		rec.put("XMLX", xmlx);//项目类别
		rec.put("WLBH", wlbh);//物料编号
		rec.put("WLMC", wlmc);//物料名称
		rec.put("XH", xh);//型号
		rec.put("GG", gg);//规格
		rec.put("SX", sx);//属性
		rec.put("GZBH", gzbh);//规则编号
		rec.put("JGWLXH", jgwlxh);//加工物料PN
		rec.put("JGLX", jglx);//加工类型
		rec.put("JGWLBH", jgwlbh);//加工物料编号
		rec.put("SL", String.valueOf(sl));//数量
		if(flag) rec.put("DJ", String.valueOf(dj));//单价
		
		try {
			BOInstanceAPI.getInstance().createBOData(conn, tableName, rec, viewBindid, uid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
