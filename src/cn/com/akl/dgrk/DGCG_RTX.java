package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.rtx.RTXIMSend;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.workflow.execute.dao.ProcessInstance;
import com.actionsoft.awf.workflow.execute.dao.TaskInstance;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.loader.core.ValueAdapter;

public class DGCG_RTX extends SubWorkflowEventClassA {

	public DGCG_RTX() {
	}

	public DGCG_RTX(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("发送RTX通知");
	}
	
	@Override
	public boolean execute() {
		//获取子流程bindid
		Hashtable process = getParameter(this.PARAMETER_SUB_PROCESS_INSTANCE_ID).toHashtable();
		String p = process.get(0) == null?"":process.get(0).toString();
		if(p.equals("")){
			return true;
		}
		int processid = Integer.parseInt(p);
		
		RTXIMSend rt = new RTXIMSend();
		UserContext userModel = getUserContext();
		//查询子流程taskid
		String tasksql = "select ID,TARGET from wf_task where bind_id="+processid+"";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = DBSql.open();
		try {
			ps = conn.prepareStatement(tasksql);
			rs = ps.executeQuery();
			while(rs.next()){
				int taskid = rs.getInt("ID");
				if(taskid == 0){
					return true;
				}
				//发送RTX通知
				TaskInstanceModel taskModel = new TaskInstance().getInstanceOfActive(taskid);
				ProcessInstanceModel processModel = new ProcessInstance().getInstance(processid);
				rt.notifyTaskMessage(userModel, taskModel, processModel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return true;
	}
}
