package cn.com.akl.cwgl.gysfyzc.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepN2Validate extends WorkFlowStepRTClassA{
	
	public StepN2Validate() {
		super();
	}

	public StepN2Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("供应商费用支持申请流程，第二个节点的节点表单校验事件，用于检测是否填写了闪迪的TPM号");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = this.getParameter(PARAMETER_TASK_ID).toInt();
		
		// 获取审核菜单标记
		boolean flag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "申请通过");
		
		if(flag){
				Connection conn = null;
				try{
					conn = DBSql.open();
					
					int count = DAOUtil.getInt(conn, "select count(*) c from BO_AKL_WXB_XS_POS_BODY  where Bindid=? and (TPM is null or TPM='')", bindid);
					if(count >0){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "有TPM号为空，请填写TPM号！");
						return false;
					}
					
					count = DAOUtil.getInt(conn, "select count(*) c from BO_AKL_WXB_XS_POS_HEAD  where Bindid=? and (TPM is null or TPM='')", bindid);
					if(count > 0){
						MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "有TPM号为空，请填写TPM号！");
						return false;
					}
					return true;
				} catch(Exception e){
					e.printStackTrace();
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
					return true;
				} finally {
					DBSql.close(conn, null, null);
				}
		}
		
		return true;
	}

}
