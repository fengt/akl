package cn.com.akl.util;

import java.sql.Connection;
import java.sql.SQLException;

public class WorkFlowUtil {
	/**
	 * 获取可回退的节点.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public static Integer getPreviousStepNo(Connection conn, int bindid, int currentStepNo) throws SQLException {
		Integer wfsid = DAOUtil
				.getIntOrNull(
						conn,
						"SELECT TOP 1 WFSID FROM WF_TASK_LOG WHERE BIND_ID=? AND WFSID IN (SELECT ID FROM SYSFLOWSTEP WHERE STEPNO<?) ORDER BY ENDTIME DESC ",
						bindid, currentStepNo);
		if (wfsid == null) {
			return null;
		}
		return DAOUtil.getIntOrNull(conn, "SELECT STEPNO FROM SYSFLOWSTEP WHERE ID=?", wfsid);
	}

	/**
	 * 获取流程实例的节点.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public static Integer getProcessInstanceStepNo(Connection conn, int bindid) throws SQLException {
		return DAOUtil.getIntOrNull(conn,
				"SELECT STEPNO FROM SYSFLOWSTEP WHERE ID=(SELECT TOP 1 WFSID FROM WF_TASK WHERE BIND_ID=? ORDER BY ENDTIME DESC)", bindid);
	}

}
