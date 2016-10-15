package cn.com.akl.pdgl.kcpd.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo1Transaction() {
	}

	public StepNo1Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("带出第一节点盘点单身数据，填写到反馈信息中。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas(StepNo3Transaction.table1, bindid);//盘点单身数据
		String query_pdfs = "select * from " + StepNo3Transaction.table0 +" where bindid="+bindid;
		String pdfs = DBSql.getString(query_pdfs, "PDFS");//盘点方式
		
		Vector fk_vector = null;
		if(StepNo3Transaction.pdfs_mx.equals(pdfs)){//1、明细方式
			fk_vector = fillPdfk_mx(vector,bindid);
		}else if(StepNo3Transaction.pdfs_hz.equals(pdfs)){//2、汇总方式
			fk_vector = fillPdfk_hz(uc,vector, bindid);
		}
		
		try {
			BOInstanceAPI.getInstance().createBOData(StepNo3Transaction.table2, fk_vector, bindid, uc.getUID());
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
		
		/**更新盘点状态*/
		update_Pdzt(bindid,StepNo3Transaction.pdzt1);
		
		return true;
	}

	/**
	 * 回填盘点反馈(盘点方式：明细)
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public static Vector fillPdfk_mx(Vector vector, int bindid){
		Vector re_vector = new Vector();
		Hashtable hash = null;
		for (int i = 0; i < vector.size(); i++) {
			hash = new Hashtable();
			Hashtable rec = (Hashtable)vector.get(i);			
			hash.put("PDDH", rec.get("PDDH"));//盘点单号
			hash.put("WLBH", rec.get("WLBH"));//物料编号
			hash.put("PC", rec.get("PC"));//批次
			hash.put("WLMC", rec.get("WLMC"));//物料名称
			hash.put("XH", rec.get("XH"));//型号
			hash.put("SX", rec.get("SX"));//属性
			hash.put("HWDM", rec.get("KWBM"));//货位代码
			hash.put("KWSL", rec.get("KWSL"));//库位数量
			hash.put("PKSJSL", rec.get("KWSL"));//盘库实际数量
			re_vector.add(hash);
		}
		return re_vector;
	}
	
	/**
	 * 回填盘点反馈(盘点方式：汇总)
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public static Vector fillPdfk_hz(UserContext uc, Vector vector, int bindid){

		String query_ckbm = "SELECT * FROM " + StepNo3Transaction.table0 + " WHERE bindid ="+bindid;
		String ckbm = DBSql.getString(query_ckbm, "CKBM");//仓库编码
		String query_isProxies = "SELECT * FROM " + StepNo3Transaction.table7 + " WHERE CKDM='"+ckbm+"'";
		String isProxies = DBSql.getString(query_isProxies, "SFDGK");//是否代管库
		
		Vector re_vector = new Vector();
		Hashtable hash = null;
		for (int i = 0; i < vector.size(); i++) {
			hash = new Hashtable();
			Hashtable rec = (Hashtable)vector.get(i);
			String pddh = rec.get("PDDH").toString();
			String wlbh = rec.get("WLBH").toString();
			String pc = rec.get("PC").toString();
			
			if(isProxies.equals(StepNo3Transaction.isProxies0)){//是
				String sql_a = "SELECT * FROM " +StepNo3Transaction.table6+ " WHERE WLBH='"+wlbh+"' AND PCH='"+pc+"' AND KWSL>0 AND CKDM='"+ckbm+"'";
				Vector temp = pdfs_HZ(uc,sql_a,pddh);
				Iterator it = temp.iterator();
				while(it.hasNext()){
					re_vector.add(it.next());
				}
			}else if(isProxies.equals(StepNo3Transaction.isProxies1)){//否
				String sql_b = "SELECT * FROM " +StepNo3Transaction.table4+ " WHERE WLBH='"+wlbh+"' AND PCH='"+pc+"' AND KWSL>0 AND CKDM='"+ckbm+"'";
				Vector temp = pdfs_HZ(uc,sql_b,pddh);
				Iterator it = temp.iterator();
				while(it.hasNext()){
					re_vector.add(it.next());
				}
			}
		}
		return re_vector;
		
	}
	
	/**
	 * 汇总盘点方式，数据封装
	 * @param uc
	 * @param sql
	 * @param pddh
	 * @return
	 */
	public static Vector pdfs_HZ(UserContext uc, String sql, String pddh){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Vector vector = new Vector();
		Hashtable rec = null;
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				rec = new Hashtable();
				String sx_tmp = StrUtil.returnStr(rs.getString("SX"));//属性编码
				String sx = DictionaryUtil.parseSXToName(sx_tmp);//属性名称
				rec.put("PDDH", pddh);
				rec.put("WLBH", StrUtil.returnStr(rs.getString("WLBH")));
				rec.put("WLMC", StrUtil.returnStr(rs.getString("WLMC")));
				rec.put("XH", StrUtil.returnStr(rs.getString("XH")));
				rec.put("SX", sx);
				rec.put("PC", StrUtil.returnStr(rs.getString("PCH")));
				rec.put("HWDM", StrUtil.returnStr(rs.getString("HWDM")));
				rec.put("KWSL", rs.getInt("KWSL"));
				rec.put("PKSJSL", rs.getInt("KWSL"));
				vector.add(rec);
			}
		} catch (SQLException e) {
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出错，请联系管理员！");
			e.printStackTrace();
		}
		return vector;
	}
	
	/**
	 * 更新盘点状态(盘点中)
	 * @param bindid
	 */
	public static void update_Pdzt(int bindid, String pdzt){
		String str = "UPDATE " +StepNo3Transaction.table0+ " SET PDZT='"+pdzt+"' WHERE BINDID="+bindid;
		DBSql.executeUpdate(str);
	}
	
}
