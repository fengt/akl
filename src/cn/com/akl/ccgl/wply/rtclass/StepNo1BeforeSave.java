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
		setDescription("领用仓库选择发生变化时，清空子表信息。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable frmHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();//表单单头数据
		Hashtable dbHead = BOInstanceAPI.getInstance().getBOData("BO_AKL_WPLY_P", bindid);//数据库单头数据
		Vector body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WPLY_S", bindid);//数据库入库单身数据
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
