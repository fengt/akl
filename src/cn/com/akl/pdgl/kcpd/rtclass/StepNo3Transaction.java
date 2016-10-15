package cn.com.akl.pdgl.kcpd.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	public static final String table0 = "BO_AKL_KCPD_P";//库存盘点单单头信息表
	public static final String table1 = "BO_AKL_KCPD_S";//库存盘点单单身信息表
	public static final String table2 = "BO_AKL_KCPD_FK_S";//库存盘点反馈信息表
	
	public static final String table3 = "BO_AKL_KC_KCHZ_P";//库存汇总表
	public static final String table4 = "BO_AKL_KC_KCMX_S";//仓库明细表
	
	public static final String table5 = "BO_AKL_DGKC_KCHZ_P";//代管库存汇总表
	public static final String table6 = "BO_AKL_DGKC_KCMX_S";//代管库存明细表
	
	public static final String table7 = "BO_AKL_CK";//仓库信息表
	public static final String table8 = "BO_AKL_WLXX";//仓库信息表
	//是否代管库
	public static final String isProxies0 = "025000";//是
	public static final String isProxies1 = "025001";//否
	
	public static final String pdfs_mx = "明细";
	public static final String pdfs_hz = "汇总";
	
	public static final String auditName0 = "同意";
	public static final String auditName1 = "反馈";
	public static final String auditName2 = "作废";
	
	public static final String pdzt0 = ""; 
	public static final String pdzt1 = "盘点中";
	public static final String pdzt2 = "已复核";
	public static final String pdzt3 = "已作废";
	
	private UserContext uc;
	public StepNo3Transaction() {
	}

	public StepNo3Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("盘点过程中出现差异数量，更新盘点单身表（库存实际数量、差异原因）和库存明细（库位数量）的值。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int tastid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean audit_agree = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, auditName0);//获取当前审核菜单
		Vector vectorFK = BOInstanceAPI.getInstance().getBODatas(table2, bindid);
		Vector vectorBy = BOInstanceAPI.getInstance().getBODatas(table1, bindid);
		
		String query_ckbm = "SELECT * FROM " + table0 + " WHERE bindid ="+bindid;
		String ckbm = DBSql.getString(query_ckbm, "CKBM");//仓库编码
		String query_isProxies = "SELECT * FROM " + table7 + " WHERE CKDM='"+ckbm+"'";
		String isProxies = DBSql.getString(query_isProxies, "SFDGK");//是否代管库
		
		String query_pdfs = "select * from " + StepNo3Transaction.table0 +" where bindid="+bindid;
		String pdfs = DBSql.getString(query_pdfs, "PDFS");//盘点方式
		
		/**审核为同意*/
		if(audit_agree){
			Vector vector = fkPackage(uc,bindid);//反馈表汇总封装
			if(isProxies.equals(isProxies0)){
				kchzUpdate(vector,table5);//更新代管汇总表
				kcmxUpdate(vectorFK,table6,bindid);//更新代管明细表
				StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt2);//更新盘点状态
			}else if(isProxies.equals(isProxies1)){
				kchzUpdate(vector,table3);//更新库存汇总表
				kcmxUpdate(vectorFK,table4,bindid);//更新库存明细表
				StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt2);//更新盘点状态
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "无法判断盘点仓库是否为代管库，请审核！");
				return false;
			}
		}else{/**审核为不同意*/
			Vector fk_vector = null;
			try {
				if(pdfs_mx.equals(pdfs)){//1、明细方式
					fk_vector = StepNo1Transaction.fillPdfk_mx(vectorBy,bindid);
				}else if(pdfs_hz.equals(pdfs)){//2、汇总方式
					fk_vector = StepNo1Transaction.fillPdfk_hz(uc,vectorBy, bindid);
				}
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(table2, bindid);//删除旧数据
				BOInstanceAPI.getInstance().createBOData(table2, fk_vector, bindid, uc.getUID());
			} catch (AWSSDKException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * 明细差异更新
	 * @param vector
	 * @param table
	 * @param bindid
	 */
	public void kcmxUpdate(Vector vector, String table, int bindid){
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable FKrecord = (Hashtable)vector.get(i);
				String wlbh = FKrecord.get("WLBH").toString();
				String pc = FKrecord.get("PC").toString();
				String hwdm = FKrecord.get("HWDM").toString();
				String sx = FKrecord.get("SX").toString();
				int kwsl = Integer.parseInt(FKrecord.get("KWSL").toString());
				int pksjsl = Integer.parseInt(FKrecord.get("PKSJSL").toString());
				String cyyy = FKrecord.get("CYYY").toString();
				int cysl = kwsl - pksjsl;
				
				String str0 = "update " + table + " set KWSL="+pksjsl+" where WLBH='"+wlbh+"' and PCH='"+pc+"' and HWDM='"+hwdm+"' and SX='"+sx+"'";
				if(cysl != 0){
					DBSql.executeUpdate(str0);
				}
			}
		}
	}
	
	/**
	 * 汇总差异更新
	 * @param vector
	 * @param table
	 */
	public void kchzUpdate(Vector vector,String table){
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String pc = rec.get("PC").toString();
				int kwsl = Integer.parseInt((rec.get("KWSL").toString()));//汇总数量
				int pksjsl = Integer.parseInt(rec.get("PKSJSL").toString());//汇总实际数量
				int cyz = Math.abs(kwsl - pksjsl);//差异值
				
				String str0 = "update "+ table +" set RKSL=ISNULL(RKSL,0)-"+cyz+",PCSL=ISNULL(PCSL,0)-"+cyz+" where WLBH='"+wlbh+"' and PCH='"+pc+"'";
				String str1 = "update "+ table +" set RKSL=ISNULL(RKSL,0)+"+cyz+",PCSL=ISNULL(PCSL,0)+"+cyz+" where WLBH='"+wlbh+"' and PCH='"+pc+"'";
				
				if(kwsl>pksjsl){//盘亏
					DBSql.executeUpdate(str0);
				}else if(kwsl<pksjsl){//盘盈
					DBSql.executeUpdate(str1);
				}
			}
		}
	}
	
	/**
	 * 盘点反馈数据封装
	 * @param uc
	 * @param bindid
	 * @return
	 */
	public static Vector fkPackage(UserContext uc, int bindid){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Vector vector = new Vector();
		Hashtable rec = null;
		String QUERY_FK = 
			"SELECT b.WLBH,b.PC,b.KWSL,b.PKSJSL,LEFT(CYYY_ALL, LEN(CYYY_ALL)-1) AS CYYY FROM(SELECT WLBH,PC,SUM (KWSL) AS KWSL,SUM (PKSJSL) AS PKSJSL,(SELECT (CASE WHEN CYYY='' THEN '无' ELSE CYYY END) +',' FROM BO_AKL_KCPD_FK_S WHERE WLBH=a.WLBH AND PC=a.PC AND BINDID="+bindid+" FOR XML PATH('')) AS CYYY_ALL FROM BO_AKL_KCPD_FK_S a WHERE BINDID="+bindid+" GROUP BY WLBH,PC)b";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(QUERY_FK);
			System.out.println();
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					rec = new Hashtable();
					String wlbh = rs.getString("WLBH").toString();
					rec.put("WLBH", StrUtil.returnStr(wlbh));//物料编号				
					rec.put("PC", StrUtil.returnStr(rs.getString("PC")));//批次号
					rec.put("KWSL", rs.getInt("KWSL"));//库存数量【汇总】
					rec.put("PKSJSL", rs.getInt("PKSJSL"));//盘库实际数量【汇总】
					rec.put("CYYY", StrUtil.returnStr(rs.getString("CYYY")));//差异原因【汇总】
					vector.add(rec);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DBSql.close(conn, ps, rs);
		}
		return vector;
	}
}
