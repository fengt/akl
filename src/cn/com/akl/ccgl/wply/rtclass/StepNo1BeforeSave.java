package cn.com.akl.ccgl.wply.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo1BeforeSave() {
	}

	public StepNo1BeforeSave(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("���òֿ�ѡ�����仯ʱ������ӱ���Ϣ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable frmHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();//����ͷ����
		Hashtable dbHead = BOInstanceAPI.getInstance().getBOData("BO_AKL_WPLY_P", bindid);//���ݿⵥͷ����
		Vector body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WPLY_S", bindid);//���ݿ���ⵥ������
		String frmRkdb = frmHead.get("LYCK") == null ?"": frmHead.get("LYCK").toString();
		String dbRkdb = dbHead.get("LYCK") == null ?"": dbHead.get("LYCK").toString();
		
		if("BO_AKL_WPLY_P".equals(tablename)&&!frmRkdb.equals(dbRkdb)){
			if(body != null){
				BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_WPLY_S", bindid);
			}
		}
		return true;
	}

}
