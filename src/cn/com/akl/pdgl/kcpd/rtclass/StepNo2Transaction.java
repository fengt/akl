package cn.com.akl.pdgl.kcpd.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo2Transaction() {
	}

	public StepNo2Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("������¼����̵㷴����Ϣ��������ӱ���");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		
		int tastid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean audit_fk = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, StepNo3Transaction.auditName1);//��ȡ��˲˵�
		boolean audit_zf = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, StepNo3Transaction.auditName2);//��ȡ��˲˵�
		
		Vector vector = BOInstanceAPI.getInstance().getBODatas(StepNo3Transaction.table2, bindid);
		String query_pdfs = "select * from " + StepNo3Transaction.table0 +" where bindid="+bindid;
		String pdfs = DBSql.getString(query_pdfs, "PDFS");//�̵㷽ʽ
		if(audit_fk){//���Ϊ����
			if(StepNo3Transaction.pdfs_mx.equals(pdfs)){//��ϸ��ʽ
				pdFillBack(vector,bindid,pdfs);
			}else if(StepNo3Transaction.pdfs_hz.equals(pdfs)){//���ܷ�ʽ
				Vector fk_vector = StepNo3Transaction.fkPackage(uc, bindid);
				pdFillBack(fk_vector,bindid,pdfs);
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㷽ʽ����ȷ����˲飡");
				return false;
			}
			
			/**ɾ���̵㷴����û�в���ļ�¼**/
			String del_sql = "DELETE FROM " +StepNo3Transaction.table2+ " WHERE KWSL=PKSJSL AND BINDID="+bindid;
			DBSql.executeUpdate(del_sql);
		}else if(audit_zf){//���Ϊ����
			/**ɾ���̵㵥���������ݣ�����Ϊ����״̬*/
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(StepNo3Transaction.table1, bindid);//ɾ���̵㵥������
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(StepNo3Transaction.table2, bindid);//ɾ���̵㷴������
			StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt3);//����Ϊ������
		}else{//���Ϊ������
			/**ɾ���̵㷴�����еļ�¼�������̵�״̬**/
			String del_sql = "DELETE FROM " +StepNo3Transaction.table2+ " WHERE BINDID="+bindid;
			DBSql.executeUpdate(del_sql);
			StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt0);
		}
		
		return true;
	}
	
	/**
	 * �̵㷴����д���̵㵥��
	 * @param vector
	 * @param bindid
	 * @param pdfs
	 */
	public void pdFillBack(Vector vector, int bindid, String pdfs){
		for (int i = 0; i < vector.size(); i++) {
			Hashtable table = (Hashtable)vector.get(i); 
			String wlbh = (String)table.get("WLBH");
			String pc = (String)table.get("PC");
			int kwsl = Integer.parseInt(table.get("KWSL").toString());
			int pksjsl = Integer.parseInt(table.get("PKSJSL").toString());
			String cyyy = table.get("CYYY").toString();
			int cysl = pksjsl - kwsl;
			
			if(StepNo3Transaction.pdfs_mx.equals(pdfs)){
				String hwdm = (String)table.get("HWDM");
				String sql = "update " + StepNo3Transaction.table1 + " set PKSJSL="+pksjsl+", CYSL="+cysl+", CYYY='"+cyyy+"' where WLBH='"+wlbh+"' and PC='"+pc+"' and KWBM='"+hwdm+"' and bindid="+bindid ;
				DBSql.executeUpdate(sql);
			}else if(StepNo3Transaction.pdfs_hz.equals(pdfs)){
				String sql2 = "update " + StepNo3Transaction.table1 + " set PKSJSL="+pksjsl+", CYSL="+cysl+", CYYY='"+cyyy+"' where WLBH='"+wlbh+"' and PC='"+pc+"' and bindid="+bindid;
				DBSql.executeUpdate(sql2);
			}
		}
	}

}
