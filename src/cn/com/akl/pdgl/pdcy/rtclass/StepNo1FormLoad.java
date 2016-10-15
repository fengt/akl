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
		setDescription("�����̵����̴��ڲ�������ݣ�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable hash = BOInstanceAPI.getInstance().getBOData(table0, bindid);
		if(hash.isEmpty()){
			return true;
		}
		String pddh = hash.get("PDDH").toString();//�̵㵥��
		String cydh_db = hash.get("CYDH").toString();//���쵥��(���ݿ��ȡ)
		
		if("".equals(cydh_db)){
			Hashtable head = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
			String cyh = head.get("CYDH").toString();
			String cydh = cyh.substring(cyh.length()-15);//���쵥��(����ȡ)
			
			/**�����̵��������**/
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
	 * �̵㷴��(�в�������)
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
		String ppbh_tmp = StrUtil.returnStr(DBSql.getString(QUERY_PPBH, "PPBH"));//Ʒ�Ʊ��
		String ppbh = DictionaryUtil.parsePPToName(ppbh_tmp);//Ʒ������
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(QUERY_PDFK);
			rs = ps.executeQuery();
			while(rs.next()){
				rec = new Hashtable();
				rec.put("PDDH", StrUtil.returnStr(rs.getString("PDDH")));//�̵㵥��
				rec.put("CYDH", StrUtil.returnStr(cydh));//���쵥��
				rec.put("PPBH", ppbh);//Ʒ��
				rec.put("WLBH", StrUtil.returnStr(rs.getString("WLBH")));//���ϱ��
				rec.put("WLMC", StrUtil.returnStr(rs.getString("WLMC")));//��������
				rec.put("XH", StrUtil.returnStr(rs.getString("XH")));//�ͺ�
				rec.put("SX", StrUtil.returnStr(rs.getString("SX")));//����
				rec.put("PC", StrUtil.returnStr(rs.getString("PC")));//���κ�
				rec.put("KWBM", StrUtil.returnStr(rs.getString("HWDM")));//��λ����
				int kwsl = rs.getInt("KWSL");
				int pksjsl = rs.getInt("PKSJSL");
				int cysl = pksjsl - kwsl;
				rec.put("KWSL", kwsl);//��λ����
				rec.put("PKSJSL", pksjsl);//�̵�����
				rec.put("CYSL", cysl);//��������
				rec.put("CYYY", StrUtil.returnStr(rs.getString("CYYY")));//����ԭ��
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
