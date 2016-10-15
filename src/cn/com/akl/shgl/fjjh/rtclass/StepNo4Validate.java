package cn.com.akl.shgl.fjjh.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Validate extends WorkFlowStepRTClassA {

	private UserContext uc;
	private Connection conn;
	public StepNo4Validate() {
	}

	public StepNo4Validate(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("校验被检测的所有客服中心的季度返京调拨是否已到返货库。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "出库");
		final String uid = uc.getUID();
		try {
			conn = DBSql.open();
			if(yes){
				DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_FJJH_S_DBDH, new ResultPaser(){
					public boolean parse(Connection conn, ResultSet rs) throws SQLException{
						String dbdh = rs.getString("DBDH");
						int isEnd = DAOUtil.getInt(conn, FJJHCnt.QUERY_DB_P_ISEND, dbdh);
						if(isEnd == 0){
							throw new RuntimeException("计划子表中该调拨单【"+dbdh+"】还未完成调拨，暂无法办理。");
						}
						return true;
					}
				}, bindid);
			}
			return true;
		} catch(RuntimeException e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch(Exception e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系管理员！");
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}
}
