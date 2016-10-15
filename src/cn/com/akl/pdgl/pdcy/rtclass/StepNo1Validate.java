package cn.com.akl.pdgl.pdcy.rtclass;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private static final String zt0 = "处理中"; 
	private static final String zt1 = "已处理"; 
	private static final String table0 = "BO_AKL_PDCYCL_P"; //盘点差异单头信息表
	
	private UserContext uc;
	public StepNo1Validate() {
	}

	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("校验办理中/已办理的盘点单，不允许多次办理。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String query_pddh = "SELECT * FROM " +table0+ " WHERE BINDID="+bindid;
		String pddh = DBSql.getString(query_pddh, "PDDH");//盘点单号
		String query_zt = "SELECT * FROM " +table0+ " WHERE PDDH='"+pddh+"' AND (ZT='"+zt0+"' OR ZT='"+zt1+"')";
		String zt = DBSql.getString(query_zt, "ZT");//状态
		
		if(zt0.equals(zt)){
			MessageQueue.getInstance().putMessage(uc.getUID(), "该差异单已在办理中，您不能再办理！");
			return false;
		}else if(zt1.equals(zt)){
			MessageQueue.getInstance().putMessage(uc.getUID(), "该差异单已办理结束，您不能再办理！");
			return false;
		}
		return true;
	}

}
