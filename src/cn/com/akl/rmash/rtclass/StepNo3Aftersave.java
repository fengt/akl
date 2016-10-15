package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Aftersave extends WorkFlowStepRTClassA{

	public StepNo3Aftersave(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("当点击同意时，更新返新单状态");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
		boolean ty = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
		Hashtable h = null;
		int a = 0;
		Connection conn = null;
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			if(ty){
				if(v!=null){
					Iterator it = v.iterator();
					while(it.hasNext()){
						h = (Hashtable)it.next();
						if(Integer.parseInt(h.get("FXSL").toString())>0)
							a++;
					}
					if(a>0)
						DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_RMAFX_P SET DDZT=? WHERE bindid=?", 2, bindid);
				}
			}
			else{
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_RMAFX_P SET DDZT=? WHERE bindid=?", 1, bindid);
			}
			conn.commit();
		} catch (SQLException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}


		return true;
	}

}
