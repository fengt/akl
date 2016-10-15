package cn.com.akl.hhgl.hhrk.rtclass;

import java.util.Hashtable;
import java.util.List;
import cn.com.akl.hhgl.hhrk.biz.DealImportXhForUniqueBiz;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DealImportXhForUniqueEvent extends WorkFlowStepRTClassA{

	private UserContext uc;
	public DealImportXhForUniqueEvent(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("qjc");
		setDescription("V1.0");
		setDescription("�����뵥������Ψһ�Ҳ�Ϊ���Լ���������Ƿ���ת���������!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(HHDJConstant.tableName0, bindid);//�ɹ���ⵥͷ
		
		/**�����뵥������Ψһ�Ҳ�Ϊ��**/
		List list = DealImportXhForUniqueBiz.DealUtil(bindid);
		
		if(list.size()>0 && list.size()<15){
			MessageQueue.getInstance().putMessage(uc.getUID(), "����ת����Ϣ�в���������ظ����Ϻš���Ϣ��" + list.toString());
			System.out.println(list.toString());
			return false;
		}else if(list.size()>=15){
			MessageQueue.getInstance().putMessage(uc.getUID(), "����ת����Ϣ�в���������ظ����Ϻš���Ϣ����ȥ�غ����°���");
			return false;
		}
		
		return true;
	}
}