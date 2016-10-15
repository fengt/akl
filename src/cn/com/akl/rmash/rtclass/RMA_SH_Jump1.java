package cn.com.akl.rmash.rtclass;

import java.util.Hashtable;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMA_SH_Jump1 extends WorkFlowStepJumpRuleRTClassA{

	public RMA_SH_Jump1(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		setVersion("RMA�ջ�����v1.0");
		setProvider("����");
		setDescription("���ڴ����һ�ڵ���ת����");
	}

	@Override
	public int getNextNodeNo() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public String getNextTaskUser() {
		// TODO Auto-generated method stub
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		if(ha.get("PP").toString().equals("����")){
			return "100835";
		}
		else
			return "100331 100500";
	}

}
