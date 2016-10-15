package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class DGRK_Step2 extends WorkFlowStepRTClassA {

	public DGRK_Step2() {
	}

	public DGRK_Step2(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("判断审核菜单：调度继续办理;退回反写预入库操作，更改采购单头、单身采购状态，删除入库单身批次号、库存汇总");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		//入库单头信息
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//入库单号
		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//采购单号
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//入库类型

		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "调度")){
			return true;
		}else if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回")){
			//删除库存汇总
			String hzsql = "delete from BO_AKL_DGKC_KCHZ_P where RKDH='"+rkdh+"'";
			DBSql.executeUpdate(hzsql);
			//如果入库类型不是其它入库
			if(!rklx.equals("其它入库")){
				//更新采购单身采购状态
				String cgdssql = "update BO_AKL_DGCG_S set CGZT='待采购' where DDBH='"+ydh+"'";
				DBSql.executeUpdate(cgdssql);
				//更新采购单头
				String cgdtsql = "update BO_AKL_DGCG_P set CGZT='待采购' where DDBH='"+ydh+"'";
				DBSql.executeUpdate(cgdtsql);
			}
			//删除入库单身批次号
			String pchsql = "update BO_AKL_DGRK_S set PCH='' where bindid='"+bindid+"'";
			DBSql.executeUpdate(pchsql);
			return true;
		}
		return false;
	}
}
