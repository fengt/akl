package cn.com.akl.xsgl.khjxc.schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

/**
 * 定时器： 处理POS方案和返利方案的激活.
 * 
 * @author huangming
 *
 */
public class POSAndFLActiveSchedule implements IJob {

	/**
	 * 方案状态，执行中.
	 */
	private static final String FAZT_ZXZ = "030003";
	/**
	 * 查询POS方案.
	 */
	private static final String QUERY_POSFA = "SELECT ID, KSSJ,JSSJ FROM BO_AKL_WXB_XS_POS_HEAD WHERE POSZT='030001' AND ISEND=1";
	/**
	 * 查询返利方案.
	 */
	private static final String QUERY_FLFA = "SELECT ID, KSSJ,JSSJ FROM BO_AKL_WXB_XS_FL_HEAD WHERE FAZT='030001' AND ISEND=1";
	/**
	 * 更新POS状态.
	 */
	private static final String UPDATE_POS_ZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET POSZT='" + FAZT_ZXZ + "' WHERE ID=?";
	/**
	 * 更新返利状态.
	 */
	private static final String UPDATE_FL_ZT = "UPDATE BO_AKL_WXB_XS_FL_HEAD SET FAZT='" + FAZT_ZXZ + "' WHERE ID=?";

	public POSAndFLActiveSchedule() {
		super();
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			updatePOS(conn);
			updateFL(conn);
			conn.commit();
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage("admin", "POSAndFLActiveSchedule定时器类出现错误!");
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 更新POS.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	private void updatePOS(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		Date now = Calendar.getInstance().getTime();
		List<Integer> list = new ArrayList<Integer>();

		try {
			ps = conn.prepareStatement(QUERY_POSFA);
			reset = ps.executeQuery();
			while (reset.next()) {
				int id = reset.getInt("ID");
				Date kssj = reset.getDate("KSSJ");
				if (kssj.getTime() < now.getTime())
					list.add(id);
			}
		} finally {
			DBSql.close(ps, reset);
		}
		DAOUtil.executeBatchUpdate(conn, UPDATE_POS_ZT, list);
	}

	/**
	 * 更新返利.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	private void updateFL(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		Date now = Calendar.getInstance().getTime();
		List<Integer> list = new ArrayList<Integer>();

		try {
			ps = conn.prepareStatement(QUERY_FLFA);
			reset = ps.executeQuery();
			while (reset.next()) {
				int id = reset.getInt("ID");
				Date kssj = reset.getDate("KSSJ");
				if (kssj.getTime() < now.getTime())
					list.add(id);
			}
		} finally {
			DBSql.close(ps, reset);
		}
		DAOUtil.executeBatchUpdate(conn, UPDATE_FL_ZT, list);
	}

}
