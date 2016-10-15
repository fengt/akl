package cn.com.akl.shgl.fjjh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	//删除复检计划数量为零的记录
	private static final String DELETE_FJJH = "DELETE FROM BO_AKL_FJJH_S WHERE FJJHSL=0 AND BINDID=?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo2Transaction() {
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("删除复检数量为零记录。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_FJJH_P", bindid);
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
		
		String kfckbm = head.get("KFCKBM").toString();//客服仓库编码
		double fjbl = Double.parseDouble(head.get("FJBL").toString());//复检比率 
		PreparedStatement ps = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			//删除复检数量为零的记录
			if(yes){
				ps = conn.prepareStatement(DELETE_FJJH);
				ps.setObject(1, bindid);
				ps.executeUpdate();
			}
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		} catch (Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台");
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}

}
