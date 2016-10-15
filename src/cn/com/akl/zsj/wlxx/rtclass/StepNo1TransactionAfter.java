package cn.com.akl.zsj.wlxx.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.u8.senddata.SendInventoryData;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1TransactionAfter extends WorkFlowStepRTClassA {

	private Connection conn = null;
	@SuppressWarnings("unused")
	private UserContext uc;

	public StepNo1TransactionAfter(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("wjj");
		setDescription("物料信息接口测试");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		int bindid=this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String,String> head=BOInstanceAPI.getInstance().getBOData("BO_AKL_WLXX",bindid);
		Vector<Hashtable<String,String>> body=BOInstanceAPI.getInstance().getBODatas("BO_AKL_WLXX",bindid);
		for(Hashtable<String, String>ht:body){
			if(!"01065".equals(ht.get("HZBM"))){
				continue;
			}else
			{
				SendInventoryData sid=new SendInventoryData();
				try {
					sid.sendData(body,null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;
	}
}
