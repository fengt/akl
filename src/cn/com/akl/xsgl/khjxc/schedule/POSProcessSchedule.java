package cn.com.akl.xsgl.khjxc.schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

public class POSProcessSchedule implements IJob {

	/**
	 * 供应商费用支持申请方案状态： 执行中
	 */
	private static final String GYSFYFAZT_ZXZ = "030003";
	/**
	 * 供应商费用支持申请方案状态： 执行完毕
	 */
	private static final String GYSFYFAZT_ZXWB = "030004";
	/**
	 * 查询符合条件的DD、MDF等
	 */
	private static final String QUERY_PROCESS_OTHER = "SELECT POSBH FROM BO_AKL_WXB_XS_POS_HEAD WHERE ISEND=1 AND POSZT='" + GYSFYFAZT_ZXZ
			+ "' AND JSSJ<=GETDATE()";
	/**
	 * 更新POS状态
	 */
	private static final String UPDATE_PROCESS_POSZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET POSZT='" + GYSFYFAZT_ZXWB + "' WHERE POSBH=?";

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		/**
		 * 1.通过定时器，检索POS方案表中每个未结束的POS方案， POS截止日期>=当前日期，将POS方案的状态更新为已结束，同时，向POS资金池追加POS方案剩余金额。
		 * POS剩余金额=订单金额-（POS数量*单价）POS数量如果不等于0，不允许向POS资金池中注 入POS金额。
		 */
		// BO_AKL_POSBX_P
		// BO_AKL_POSBX_S
		// SELECT * FROM BO_AKL_POSBX_P WHERE LB=043028 AND POSZT=4
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			conn = DAOUtil.openConnectionTransaction();

			// 更新供应商费用支持中非POS的方案
			try {
				List<String> otherList = new ArrayList<String>(30);
				ps = conn.prepareStatement(QUERY_PROCESS_OTHER);
				reset = ps.executeQuery();
				while (reset.next()) {
					otherList.add(reset.getString("POSBH"));
				}
				DAOUtil.executeBatchUpdate(conn, UPDATE_PROCESS_POSZT, otherList);
			} finally {
				DBSql.close(ps, reset);
			}
			
			conn.commit();
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage("admin", "POSProcessSchedule定时器出现问题，请检查控制台！");
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, reset);
		}

	}

}
