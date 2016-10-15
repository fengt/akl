package cn.com.akl.kwgl.kwtz.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.kwgl.constant.KwglConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	public StepNo1Transaction() {
	}

	public StepNo1Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("库位调整后，更新库存明细表的相关记录！");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas(KwglConstant.table1, bindid);
		/*取仓库编码和仓库名称，库位调整时插入数据需要用到*/
		String ckbm = DBSql.getString("SELECT CKBM FROM " +KwglConstant.table0+ " WHERE bindid ="+bindid, "ckbm");
		String ckmc = DBSql.getString("SELECT CKMC FROM " +KwglConstant.table0+ " WHERE bindid ="+bindid, "ckmc");
		String sfdgk = DBSql.getString("SELECT SFDGK FROM " +KwglConstant.table4+ " WHERE CKDM='"+ckbm+"'", "SFDGK");
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			if(vector != null){
				for (int i = 0; i < vector.size(); i++) {
					Hashtable table = (Hashtable) vector.get(i);
					String wlbh = table.get("WLBH").toString();
					String wlmc = table.get("WLMC").toString();
					String wlxh = table.get("WLXH").toString();
					String tzqpc = table.get("TZQPC").toString();
					String tzhpc = table.get("TZHPC").toString();
					int tzqsl = Integer.parseInt(table.get("TZQSL").toString());//调整前数量
					int tzhsl = Integer.parseInt(table.get("TZHSL").toString());//调整后数量
					String tzqkw = table.get("TZQKW").toString();//调整前货位
					String tzhq = table.get("TZHQ").toString();
					String tzhd = table.get("TZHD").toString();
					String tzhkw = table.get("TZHKW").toString();
					String tzhhw = table.get("TZHHW").toString();
					String tzck = table.get("TZHCK").toString();//调整后仓库
					String sx = table.get("SX").toString();//属性
					int bz = Integer.parseInt(table.get("BZ").toString());//标识，确定物料信息唯一ID
					
					/*将库位调整表中取出的值放入Hashtable中*/
					Hashtable recordData = new Hashtable();
					recordData.put("WLBH", wlbh);
					recordData.put("WLMC", wlmc);
					recordData.put("XH", wlxh);
					recordData.put("KWSL", tzhsl);
					
					recordData.put("CKDM", ckbm);
					recordData.put("CKMC", ckmc);
					recordData.put("QDM", tzhq);
					recordData.put("DDM", tzhd);
					recordData.put("KWDM", tzhkw);
					recordData.put("HWDM", tzhhw);
					recordData.put("SX", sx);
					
					if(sfdgk.equals(KwglConstant.isProxies0)){
						recordData.put("PCH", tzhpc);
						/*根据物料号、型号、批次号带出库存明细其他字段*/
						//String sql = "SELECT * FROM " +KwglConstant.table3+ " WHERE WLBH='"+wlbh+"' AND XH='"+wlxh+"' AND PCH='"+tzqpc+"' AND HWDM='"+tzqkw+"' AND SX='"+sx+"'";
						String sql = "SELECT * FROM " +KwglConstant.table3+ " WHERE ID="+bz;
						String zjm = DBSql.getString(conn,sql, "ZJM");
						String fzsx = DBSql.getString(conn,sql, "FZSX");
						int bzq = DBSql.getInt(conn,sql, "BZQ");
						String scrq = DBSql.getString(conn,sql, "SCRQ");
						String jldw = DBSql.getString(conn,sql, "JLDW");
						String gg = DBSql.getString(conn,sql, "GG");
						int kwsl = DBSql.getInt(conn,sql, "KWSL");//此时的库存数量
						//String sx = DBSql.getString(sql, "SX");
						
						recordData.put("ZJM", StrUtil.returnStr(zjm));
						recordData.put("FZSX", StrUtil.returnStr(fzsx));
						recordData.put("BZQ", bzq);
						recordData.put("SCRQ", StrUtil.returnStr(scrq));
						recordData.put("JLDW", StrUtil.returnStr(jldw));
						recordData.put("GG", StrUtil.returnStr(gg));
						//recordData.put("SX", StrUtil.returnStr(sx));
						
						//a.调整前数量和库存数量是否一致
						if(tzqsl != kwsl){
							throw new RuntimeException("该物料批次【"+wlbh+","+tzqpc+","+tzqkw+"】的调整前数量未及时更新，请重新引入后再调整！");
						}
						
						//b.判断调整批次是否变化
						if(tzhpc.equals(tzqpc)){
							KCMXUpdate(conn,bindid,uc,KwglConstant.table3,wlbh,tzqpc,tzhhw,tzqkw,tzhsl,sx,bz,recordData);//代管库存明细更新或插入
						}else{
							KCMXUpdate(conn,bindid,uc,KwglConstant.table3,wlbh,tzqpc,tzhpc,tzhhw,tzqkw,tzhsl,sx,bz,recordData);
							DGHZUpdate(conn,wlbh,tzqpc,tzhpc,tzhsl);//更新代管库汇总表信息
						}
					}else{
						recordData.put("PCH", tzqpc);
						String sql = "SELECT * FROM " +KwglConstant.table2+ " WHERE ID="+bz;//根据物料号、型号、批次号带出库存明细其他字段
						String zjm = DBSql.getString(conn,sql, "ZJM");
						String fzsx = DBSql.getString(conn,sql, "FZSX");
						int bzq = DBSql.getInt(conn,sql, "BZQ");
						String scrq = DBSql.getString(conn,sql, "SCRQ");
						String jldw = DBSql.getString(conn,sql, "JLDW");
						String gg = DBSql.getString(conn,sql, "GG");
						int kwsl = DBSql.getInt(conn,sql, "KWSL");//此时的库存数量
						//String sx = DBSql.getString(sql, "SX");
						
						recordData.put("ZJM", StrUtil.returnStr(zjm));
						recordData.put("FZSX", StrUtil.returnStr(fzsx));
						recordData.put("BZQ", bzq);
						recordData.put("SCRQ", StrUtil.returnStr(scrq));
						recordData.put("JLDW", StrUtil.returnStr(jldw));
						recordData.put("GG", StrUtil.returnStr(gg));
						//recordData.put("SX", StrUtil.returnStr(sx));
						
						//a.调整前数量和库存数量是否一致
						if(tzqsl != kwsl){
							throw new RuntimeException("该物料批次【"+wlbh+","+tzqpc+","+tzqkw+"】的调整前数量未及时更新，请重新引入后再调整！");
						}
						
						//b.库存明细更新或插入
						KCMXUpdate(conn,bindid,uc,KwglConstant.table2,wlbh,tzqpc,tzhhw,tzqkw,tzhsl,sx,bz,recordData);
					}
					
				}
			}
			conn.commit();
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现错误，请检查控制台。");
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
		
	}
	
	/**
	 * 库存明细更新或插入(调整批次无变化)
	 * @param uc
	 * @param table
	 * @param wlbh
	 * @param pch
	 * @param tzhhw
	 * @param tzqkw
	 * @param tzhsl
	 * @param recordData
	 */
	public static void KCMXUpdate(
			Connection conn, int bindid, UserContext uc, String table, String wlbh, String pch, String tzhhw, String tzqkw, int tzhsl, String sx, int bz, Hashtable recordData) throws SQLException, AWSSDKException{
		/*查询是否存在调整后库位信息*/
		String str = "SELECT count(*) cnt from " +table+ " WHERE WLBH = '"+wlbh+"' AND PCH = '"+pch+"' AND HWDM = '"+tzhhw+"' AND SX = '"+sx+"'";
		int count = DBSql.getInt(conn,str,"cnt");
		if(count<=0){
			BOInstanceAPI.getInstance().createBOData(conn, table, recordData, bindid, uc.getUID());
		}else{
			String str2 = "UPDATE " +table+ " SET KWSL=ISNULL(KWSL,0)+"+tzhsl+" WHERE HWDM='"+tzhhw+"' AND WLBH ='"+wlbh+"' AND PCH = '"+pch+"' AND SX = '"+sx+"'";
			DBSql.executeUpdate(conn, str2);
		}
		//String str3 = "UPDATE " +table+ " SET KWSL =ISNULL(KWSL,0)-"+tzhsl+" WHERE HWDM ='"+tzqkw+"' AND WLBH ='"+wlbh+"' AND PCH = '"+pch+"' AND SX = '"+sx+"'";
		String str3 = "UPDATE " +table+ " SET KWSL =ISNULL(KWSL,0)-"+tzhsl+" WHERE ID="+bz;
		DBSql.executeUpdate(conn, str3);
	}
	
	/**
	 * 库存明细更新或插入(调整批次有变化)
	 * @param uc
	 * @param table
	 * @param wlbh
	 * @param tzqpc
	 * @param tzhpc
	 * @param tzhhw
	 * @param tzqkw
	 * @param tzhsl
	 * @param recordData
	 */
	public static void KCMXUpdate(
			Connection conn, int bindid,UserContext uc, String table, String wlbh, String tzqpc, String tzhpc, String tzhhw, String tzqkw, int tzhsl, String sx, int bz, Hashtable recordData) throws SQLException, AWSSDKException{
		/*查询是否存在调整后库位信息*/
		String str = "SELECT count(*) cnt from " +table+ " WHERE WLBH = '"+wlbh+"' AND PCH = '"+tzhpc+"' AND HWDM = '"+tzhhw+"' AND SX = '"+sx+"'";
		int count = DBSql.getInt(conn,str, "cnt");
		if(count<=0){
			BOInstanceAPI.getInstance().createBOData(conn, table, recordData, bindid, uc.getUID());
		}else{
			String str2 = "UPDATE " +table+ " SET KWSL=ISNULL(KWSL,0)+"+tzhsl+" WHERE HWDM='"+tzhhw+"' AND WLBH ='"+wlbh+"' AND PCH = '"+tzhpc+"' AND SX = '"+sx+"'";
			DBSql.executeUpdate(conn, str2);
		}
		//String str3 = "UPDATE " +table+ " SET KWSL=ISNULL(KWSL,0)-"+tzhsl+" WHERE HWDM ='"+tzqkw+"' AND WLBH ='"+wlbh+"' AND PCH = '"+tzqpc+"' AND SX = '"+sx+"'";
		String str3 = "UPDATE " +table+ " SET KWSL =ISNULL(KWSL,0)-"+tzhsl+" WHERE ID="+bz;
		DBSql.executeUpdate(conn, str3);
	}
	
	public static void DGHZUpdate(Connection conn, String wlbh, String tzqpc, String tzhpc,int tzhsl) throws SQLException{
		String str1 = "UPDATE " + KwglConstant.table5 + " SET CKSL=ISNULL(CKSL,0)+ "+tzhsl+", PCSL=ISNULL(PCSL,0)- "+tzhsl+" WHERE WLBH='"+wlbh+"' AND PCH='"+tzqpc+"'";
		String str2 = "UPDATE " + KwglConstant.table5 + " SET RKSL=ISNULL(RKSL,0)+ "+tzhsl+", PCSL=ISNULL(PCSL,0)+ "+tzhsl+" WHERE WLBH='"+wlbh+"' AND PCH='"+tzhpc+"'";
		DBSql.executeUpdate(conn, str1);
		DBSql.executeUpdate(conn, str2);
	}
	

}
