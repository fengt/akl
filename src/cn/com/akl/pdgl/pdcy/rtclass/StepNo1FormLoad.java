package cn.com.akl.pdgl.pdcy.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.pdgl.kcpd.rtclass.StepNo3Transaction;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1FormLoad extends WorkFlowStepRTClassA {

	private static final String table0 = "BO_AKL_PDCYCL_P";
	private static final String table1 = "BO_AKL_PDCYCL_S";
	private UserContext uc;
	public StepNo1FormLoad() {
	}

	public StepNo1FormLoad(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("加载盘点流程存在差异的数据！");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable hash = BOInstanceAPI.getInstance().getBOData(table0, bindid);
		if(hash.isEmpty()){
			return true;
		}
		String pddh = hash.get("PDDH").toString();//盘点单号
		String cydh_db = hash.get("CYDH").toString();//差异单号(数据库读取)
		
		if("".equals(cydh_db)){
			Hashtable head = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
			String cyh = head.get("CYDH").toString();
			String cydh = cyh.substring(cyh.length()-15);//差异单号(表单读取)
			
			/**插入盘点差异数据**/
			Vector vector = pdFillBackData(head,cydh,pddh);
			try {
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(table1, bindid);
				BOInstanceAPI.getInstance().createBOData(table1, vector, bindid, uc.getUID());
			} catch (AWSSDKException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	
	/**
	 * 盘点反馈(有差异数据)
	 * @param head
	 * @param bindid
	 * @return
	 */
	public Vector pdFillBackData(Hashtable head, String cydh, String pddh){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Vector vector = new Vector();
		Hashtable rec = null;
		
		String QUERY_PDFK = "SELECT * FROM " + StepNo3Transaction.table2 + " WHERE KWSL<>PKSJSL AND PDDH='"+pddh+"'";
		String QUERY_PPBH = "SELECT * FROM " + StepNo3Transaction.table0 + " WHERE PDDH='"+pddh+"'";
		String ppbh_tmp = StrUtil.returnStr(DBSql.getString(QUERY_PPBH, "PPBH"));//品牌编号
		String ppbh = DictionaryUtil.parsePPToName(ppbh_tmp);//品牌名称
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(QUERY_PDFK);
			rs = ps.executeQuery();
			while(rs.next()){
				rec = new Hashtable();
				rec.put("PDDH", StrUtil.returnStr(rs.getString("PDDH")));//盘点单号
				rec.put("CYDH", StrUtil.returnStr(cydh));//差异单号
				rec.put("PPBH", ppbh);//品牌
				rec.put("WLBH", StrUtil.returnStr(rs.getString("WLBH")));//物料编号
				rec.put("WLMC", StrUtil.returnStr(rs.getString("WLMC")));//物料名称
				rec.put("XH", StrUtil.returnStr(rs.getString("XH")));//型号
				rec.put("SX", StrUtil.returnStr(rs.getString("SX")));//属性
				rec.put("PC", StrUtil.returnStr(rs.getString("PC")));//批次号
				rec.put("KWBM", StrUtil.returnStr(rs.getString("HWDM")));//货位代码
				int kwsl = rs.getInt("KWSL");
				int pksjsl = rs.getInt("PKSJSL");
				int cysl = pksjsl - kwsl;
				rec.put("KWSL", kwsl);//库位数量
				rec.put("PKSJSL", pksjsl);//盘点数量
				rec.put("CYSL", cysl);//差异数量
				rec.put("CYYY", StrUtil.returnStr(rs.getString("CYYY")));//差异原因
				vector.add(rec);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DBSql.close(conn, ps, rs);
		}
		return vector;
	}

}
