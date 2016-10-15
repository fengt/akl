package cn.com.akl.ccgl.cgrk.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.u8.senddata.SendPurchaseOrderData;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo4TransactionAfter extends WorkFlowStepRTClassA {

	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";
	private Connection conn = null;
	private UserContext uc;
	public StepNo4TransactionAfter() {
		// TODO Auto-generated constructor stub
	}

	public StepNo4TransactionAfter(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("用于U8接口传数据。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//单头
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);//单身
		try{
			conn = DBSql.open();
			String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//获取客户代码
			if(khdm.equals(CgrkCnt.khdm0)){//闪迪采购入库
				SendPurchaseOrderData sendData = new SendPurchaseOrderData();
				sendData.sendData(head, body);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DBSql.close(conn, null, null);
		}
		return true;
	}

}
