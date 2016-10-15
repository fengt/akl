/**
 * 
 */
package cn.com.akl.ccgl.xsck.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.u8.senddata.SendSaleOrderDate;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * @author hzy
 * 
 */
public class StepNo9TransactionAfter extends WorkFlowStepRTClassA {

	public StepNo9TransactionAfter() {
		super();
	}

	public StepNo9TransactionAfter(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第九节点办理后事件：向u8销售单中传入财务审核后的出库数据。");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "不同意");
		if(th)
			return true;
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CKD_BODY", bindid);
		SendSaleOrderDate sod = new SendSaleOrderDate();
		try {
			sod.sendData(head, body);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

}
