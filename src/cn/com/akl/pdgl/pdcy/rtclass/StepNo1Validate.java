package cn.com.akl.pdgl.pdcy.rtclass;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private static final String zt0 = "������"; 
	private static final String zt1 = "�Ѵ���"; 
	private static final String table0 = "BO_AKL_PDCYCL_P"; //�̵���쵥ͷ��Ϣ��
	
	private UserContext uc;
	public StepNo1Validate() {
	}

	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("У�������/�Ѱ�����̵㵥���������ΰ���");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String query_pddh = "SELECT * FROM " +table0+ " WHERE BINDID="+bindid;
		String pddh = DBSql.getString(query_pddh, "PDDH");//�̵㵥��
		String query_zt = "SELECT * FROM " +table0+ " WHERE PDDH='"+pddh+"' AND (ZT='"+zt0+"' OR ZT='"+zt1+"')";
		String zt = DBSql.getString(query_zt, "ZT");//״̬
		
		if(zt0.equals(zt)){
			MessageQueue.getInstance().putMessage(uc.getUID(), "�ò��쵥���ڰ����У��������ٰ���");
			return false;
		}else if(zt1.equals(zt)){
			MessageQueue.getInstance().putMessage(uc.getUID(), "�ò��쵥�Ѱ���������������ٰ���");
			return false;
		}
		return true;
	}

}
