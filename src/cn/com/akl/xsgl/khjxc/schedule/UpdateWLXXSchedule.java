package cn.com.akl.xsgl.khjxc.schedule;

import java.sql.Connection;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

public class UpdateWLXXSchedule implements IJob {

	// 物料状态，新品
	private static final String WLXX_WLZT_XP = "4";
	// 物料状态，正常
	private static final String WLXX_WLZT_ZC = "0";
	
	// 更新到期的物料为正常
	private static final String updateWLXX = "UPDATE BO_AKL_WLXX SET WLZT="+WLXX_WLZT_ZC+" WHERE CONVERT(DATE, DATEADD(day, 14, CREATEDATE), 120)=CONVERT(DATE, GETDATE(), 120) AND WLZT="+WLXX_WLZT_XP;
	
	/**
	 * 1、定时器更新物料信息表中的“物料状态”字段，根据物料录入时间+14自然日，更新内容为将“新品:4”更新为“正常:0”
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Connection conn = null;
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			DAOUtil.executeUpdate(conn, updateWLXX);
			conn.commit();
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage("admin", "UpdateWLXXSchedule定时器出现错误");
		} finally {
			DBSql.close(conn, null, null);
		}
		
	}

}
