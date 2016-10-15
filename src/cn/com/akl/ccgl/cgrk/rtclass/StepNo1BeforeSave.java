package cn.com.akl.ccgl.cgrk.rtclass;

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
		setDescription("入库单别选择发生变化时，清空子表信息。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable frmHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();//表单单头数据
		Hashtable dbHead = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//数据库单头数据
		
		//Vector zc_body = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName2, bindid);//数据库转仓单身数据
		String frmRkdb = frmHead.get("RKDB") == null ?"": frmHead.get("RKDB").toString();
		String dbRkdb = dbHead.get("RKDB") == null ?"": dbHead.get("RKDB").toString();
		
		if(CgrkCnt.tableName0.equals(tablename)&&!frmRkdb.equals(dbRkdb)){
			Vector body = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);//数据库入库单身数据
			if(body != null){
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(CgrkCnt.tableName1, bindid);
				//BOInstanceAPI.getInstance().removeProcessInstanceBOData(CgrkCnt.tableName2, bindid);
			}
		}
		return true;
	}

}
