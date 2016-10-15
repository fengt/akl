package cn.com.akl.pdgl.kcpd.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo1Validate() {
	}

	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("���鵥�������Ƿ��ظ���");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable head = BOInstanceAPI.getInstance().getBOData(StepNo3Transaction.table0, bindid);
		Vector vector = BOInstanceAPI.getInstance().getBODatas(StepNo3Transaction.table1, bindid);
		
		String pdfs = head.get("PDFS").toString();//�̵㷽ʽ
		
		if(vector != null){
			if(StepNo3Transaction.pdfs_mx.equals(pdfs)){//��ϸ
				return pd_mxCheck(vector,bindid);
			}else if(StepNo3Transaction.pdfs_hz.equals(pdfs)){//����
				return pd_hzCheck(vector,bindid);
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㷽ʽ����ȷ����˲飡");
				return false;
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㵥������Ϊ�գ�");
			return false;
		}
	}
	
	/**
	 * ��ϸ�ظ�У��
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean pd_mxCheck(Vector vector, int bindid){
		String str = "SELECT COUNT(*) num FROM " +StepNo3Transaction.table1+ " WHERE KWBM='' AND BINDID="+bindid;
		int n = DBSql.getInt(str, "num");//�Ƿ��пջ�λ
		if(n == 0){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String pc = rec.get("PC").toString();
				String hwdm = rec.get("KWBM").toString();
				
				String isRepeat = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table1+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND KWBM='"+hwdm+"' AND BINDID="+bindid;
				int isR = DBSql.getInt(isRepeat, "NUM");//�����Ƿ��ظ�
				
				if(isR != 1){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㵥���С����ϱ�ţ�"+wlbh+"�����κţ�"+pc+"�ͻ�λ���룺"+hwdm+"����������Ϣ�ظ�����˲飡");
					return false;
				}
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "�����������̵㷽ʽ�����������������ݲ��ԣ���˲飡");
			return false;
		}
		return true;
	}
	
	/**
	 * �����ظ�У��
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean pd_hzCheck(Vector vector, int bindid){
		String str = "SELECT COUNT(*) num FROM " +StepNo3Transaction.table1+ " WHERE KWBM<>'' AND BINDID="+bindid;
		int n = DBSql.getInt(str, "num");//�Ƿ��зǿջ�λ
		if(n == 0){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String pc = rec.get("PC").toString();
				
				String isRepeat = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table1+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND BINDID="+bindid;
				int isR = DBSql.getInt(isRepeat, "NUM");//�����Ƿ��ظ�
				
				if(isR != 1){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㵥���С����ϱ�ţ�"+wlbh+"�����κţ�"+pc+"����������Ϣ�ظ�����˲飡");
					return false;
				}
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "�����������̵㷽ʽ�����������������ݲ��ԣ���˲飡");
			return false;
		}
		return true;
	}

}
