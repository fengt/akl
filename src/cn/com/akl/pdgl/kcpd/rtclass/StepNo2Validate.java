package cn.com.akl.pdgl.kcpd.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo2Validate extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo2Validate() {
	}

	public StepNo2Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("�̵㷴����������ʱ��У�鵼���̵㵥���Ƿ�͵�ͷһ�¡�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable head = BOInstanceAPI.getInstance().getBOData(StepNo3Transaction.table0, bindid);
		Vector vector = BOInstanceAPI.getInstance().getBODatas(StepNo3Transaction.table2, bindid);
		
		String query_pddh = "select * from " +StepNo3Transaction.table0+ " where bindid = "+bindid;
		String mul_pddh = "select count(*) num from (select distinct PDDH from " +StepNo3Transaction.table2+ " where bindid = "+bindid+")a";
		String query_pddh_fk = "select distinct PDDH from " +StepNo3Transaction.table2+ " where bindid = "+bindid;
		
		int num = DBSql.getInt(mul_pddh, "num");//�̵㵥����ֵ(����)
		String pddh_fk = DBSql.getString(query_pddh_fk, "PDDH");//�̵㵥��(����)
		String pddh = head.get("PDDH").toString();//�̵㵥��(��ͷ)
		if(num == 1){
			if(pddh.equals(pddh_fk)){
				return pdfkCheck(vector,bindid);
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "������������С��̵㵥�š�����ͬһ���ţ������µ��룡");
				return false;
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㷴�������С��̵㵥�š���Ψһ����˲飡");
			return false;
		}		
	}
	
	/**
	 * У���Ƿ��̵�������ϱ�ź����κ�
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean pdfkCheck(Vector vector, int bindid){
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String wlbh = rec.get("WLBH").toString();
			String pc = rec.get("PC").toString();
			String hwdm = rec.get("HWDM").toString();
			
			String isRepeat = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table2+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND HWDM='"+hwdm+"' AND BINDID="+bindid;
			String isExist = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table1+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND BINDID="+bindid; 
			int isR = DBSql.getInt(isRepeat, "NUM");//�����Ƿ��ظ�
			int isE = DBSql.getInt(isExist, "NUM");//�����Ƿ��̵��
			
			if(isR == 1){
				if(isE <= 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㷴�������С����ϱ�ţ�"+wlbh+"���͡����κţ�"+pc+"��������û���ڸõ��̵������˲飡");
					return false;
				}
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "�̵㷴�������С����ϱ�ţ�"+wlbh+"�����κţ�"+pc+"�ͻ�λ���룺"+hwdm+"����������Ϣ�ظ�����˲飡");
				return false;
			}
		}
		return true;
	}

}
