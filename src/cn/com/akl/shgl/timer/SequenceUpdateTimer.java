package cn.com.akl.shgl.timer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;

/**
 * 序列号定时更新处理，按照序列号周期进行更新清除序列号操作.
 * 
 * @author Administrator
 *
 */
public class SequenceUpdateTimer implements IJob {

	public static final String SEQ_KEY_PREFIX = "PCH";

	public static final String ZQ_MD = "每单";
	public static final String ZQ_MT = "每天";
	public static final String ZQ_MZ = "每周";
	public static final String ZQ_MY = "每月";
	public static final String ZQ_MN = "每年";

	public SequenceUpdateTimer() {
		super();
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		/**
		 * 定时器更新序列号： 1、获取当前日期，分析今天可做的任务. 2、更新数据.
		 */
		boolean isDay = true;
		boolean isWeek = false;
		boolean isMonth = false;
		boolean isYear = false;

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String pchPrefix = format.format(calendar.getTime());

		// 识别是否要进行周任务.
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 1) {
			isWeek = true;
		}
		// 识别是否要进行月任务.
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		if (dayOfMonth == 1) {
			isMonth = true;
		}
		// 设别是否要进行年任务.
		int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
		if (dayOfYear == 1) {
			isYear = true;
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			conn = DBSql.open();

			ps = conn.prepareStatement("SELECT * FROM BO_AKL_SH_PCHSCGZ");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps);
			while (reset.next()) {
				String xmlb = reset.getString("XMLB");
				String zq = reset.getString("ZQ");
				String key = SequenceUpdateTimer.SEQ_KEY_PREFIX + xmlb;

				// 查询序列号是否存在，不存在则插入.
				String seq = DAOUtil.getStringOrNull(conn, "SELECT SEQUENCEVALUE+1 AS SEQ FROM SYSSEQUENCE WHERE SEQUENCENAME=?", key);
				if (seq == null) {
					DAOUtil.executeUpdate(conn, "INSERT INTO SYSSEQUENCE VALUES(?, ?, ?)", key, pchPrefix + "001", 1);
					continue;
				}

				if ((isDay && ZQ_MT.equals(zq)) || (isWeek && ZQ_MZ.equals(zq)) || (isMonth && ZQ_MY.equals(zq)) || (isYear && ZQ_MN.equals(zq))) {
					DAOUtil.executeUpdate(conn, "UPDATE SYSSEQUENCE SET SEQUENCEVALUE=? WHERE SEQUENCENAME=?", pchPrefix + "001", key);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}

}
