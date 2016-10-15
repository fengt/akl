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

	public static final String table0 = "BO_AKL_KCPD_P";//����̵㵥��ͷ��Ϣ��
	public static final String table1 = "BO_AKL_KCPD_S";//����̵㵥������Ϣ��
	public static final String table2 = "BO_AKL_KCPD_FK_S";//����̵㷴����Ϣ��
	
	public static final String table3 = "BO_AKL_KC_KCHZ_P";//�����ܱ�
	public static final String table4 = "BO_AKL_KC_KCMX_S";//�ֿ���ϸ��
	
	public static final String table5 = "BO_AKL_DGKC_KCHZ_P";//���ܿ����ܱ�
	public static final String table6 = "BO_AKL_DGKC_KCMX_S";//���ܿ����ϸ��
	
	public static final String table7 = "BO_AKL_CK";//�ֿ���Ϣ��
	public static final String table8 = "BO_AKL_WLXX";//�ֿ���Ϣ��
	//�Ƿ���ܿ�
	public static final String isProxies0 = "025000";//��
	public static final String isProxies1 = "025001";//��
	
	public static final String pdfs_mx = "��ϸ";
	public static final String pdfs_hz = "����";
	
	public static final String auditName0 = "ͬ��";
	public static final String auditName1 = "����";
	public static final String auditName2 = "����";
	
	public static final String pdzt0 = ""; 
	public static final String pdzt1 = "�̵���";
	public static final String pdzt2 = "�Ѹ���";
	public static final String pdzt3 = "������";
	
	private UserContext uc;
	public StepNo3Transaction() {
	}

	public StepNo3Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("�̵�����г��ֲ��������������̵㵥������ʵ������������ԭ�򣩺Ϳ����ϸ����λ��������ֵ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int tastid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean audit_agree = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, auditName0);//��ȡ��ǰ��˲˵�
		Vector vectorFK = BOInstanceAPI.getInstance().getBODatas(table2, bindid);
		Vector vectorBy = BOInstanceAPI.getInstance().getBODatas(table1, bindid);
		
		String query_ckbm = "SELECT * FROM " + table0 + " WHERE bindid ="+bindid;
		String ckbm = DBSql.getString(query_ckbm, "CKBM");//�ֿ����
		String query_isProxies = "SELECT * FROM " + table7 + " WHERE CKDM='"+ckbm+"'";
		String isProxies = DBSql.getString(query_isProxies, "SFDGK");//�Ƿ���ܿ�
		
		String query_pdfs = "select * from " + StepNo3Transaction.table0 +" where bindid="+bindid;
		String pdfs = DBSql.getString(query_pdfs, "PDFS");//�̵㷽ʽ
		
		/**���Ϊͬ��*/
		if(audit_agree){
			Vector vector = fkPackage(uc,bindid);//��������ܷ�װ
			if(isProxies.equals(isProxies0)){
				kchzUpdate(vector,table5);//���´��ܻ��ܱ�
				kcmxUpdate(vectorFK,table6,bindid);//���´�����ϸ��
				StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt2);//�����̵�״̬
			}else if(isProxies.equals(isProxies1)){
				kchzUpdate(vector,table3);//���¿����ܱ�
				kcmxUpdate(vectorFK,table4,bindid);//���¿����ϸ��
				StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt2);//�����̵�״̬
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "�޷��ж��̵�ֿ��Ƿ�Ϊ���ܿ⣬����ˣ�");
				return false;
			}
		}else{/**���Ϊ��ͬ��*/
			Vector fk_vector = null;
			try {
				if(pdfs_mx.equals(pdfs)){//1����ϸ��ʽ
					fk_vector = StepNo1Transaction.fillPdfk_mx(vectorBy,bindid);
				}else if(pdfs_hz.equals(pdfs)){//2�����ܷ�ʽ
					fk_vector = StepNo1Transaction.fillPdfk_hz(uc,vectorBy, bindid);
				}
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(table2, bindid);//ɾ��������
				BOInstanceAPI.getInstance().createBOData(table2, fk_vector, bindid, uc.getUID());
			} catch (AWSSDKException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * ��ϸ�������
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
	 * ���ܲ������
	 * @param vector
	 * @param table
	 */
	public void kchzUpdate(Vector vector,String table){
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String pc = rec.get("PC").toString();
				int kwsl = Integer.parseInt((rec.get("KWSL").toString()));//��������
				int pksjsl = Integer.parseInt(rec.get("PKSJSL").toString());//����ʵ������
				int cyz = Math.abs(kwsl - pksjsl);//����ֵ
				
				String str0 = "update "+ table +" set RKSL=ISNULL(RKSL,0)-"+cyz+",PCSL=ISNULL(PCSL,0)-"+cyz+" where WLBH='"+wlbh+"' and PCH='"+pc+"'";
				String str1 = "update "+ table +" set RKSL=ISNULL(RKSL,0)+"+cyz+",PCSL=ISNULL(PCSL,0)+"+cyz+" where WLBH='"+wlbh+"' and PCH='"+pc+"'";
				
				if(kwsl>pksjsl){//�̿�
					DBSql.executeUpdate(str0);
				}else if(kwsl<pksjsl){//��ӯ
					DBSql.executeUpdate(str1);
				}
			}
		}
	}
	
	/**
	 * �̵㷴�����ݷ�װ
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
			"SELECT b.WLBH,b.PC,b.KWSL,b.PKSJSL,LEFT(CYYY_ALL, LEN(CYYY_ALL)-1) AS CYYY FROM(SELECT WLBH,PC,SUM (KWSL) AS KWSL,SUM (PKSJSL) AS PKSJSL,(SELECT (CASE WHEN CYYY='' THEN '��' ELSE CYYY END) +',' FROM BO_AKL_KCPD_FK_S WHERE WLBH=a.WLBH AND PC=a.PC AND BINDID="+bindid+" FOR XML PATH('')) AS CYYY_ALL FROM BO_AKL_KCPD_FK_S a WHERE BINDID="+bindid+" GROUP BY WLBH,PC)b";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(QUERY_FK);
			System.out.println();
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					rec = new Hashtable();
					String wlbh = rs.getString("WLBH").toString();
					rec.put("WLBH", StrUtil.returnStr(wlbh));//���ϱ��				
					rec.put("PC", StrUtil.returnStr(rs.getString("PC")));//���κ�
					rec.put("KWSL", rs.getInt("KWSL"));//������������ܡ�
					rec.put("PKSJSL", rs.getInt("PKSJSL"));//�̿�ʵ�����������ܡ�
					rec.put("CYYY", StrUtil.returnStr(rs.getString("CYYY")));//����ԭ�򡾻��ܡ�
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
