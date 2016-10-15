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
 * ��ʱ���� ����POS�����ͷ��������ļ���.
 * 
 * @author huangming
 *
 */
public class POSAndFLActiveSchedule implements IJob {

	/**
	 * ����״̬��ִ����.
	 */
	private static final String FAZT_ZXZ = "030003";
	/**
	 * ��ѯPOS����.
	 */
	private static final String QUERY_POSFA = "SELECT ID, KSSJ,JSSJ FROM BO_AKL_WXB_XS_POS_HEAD WHERE POSZT='030001' AND ISEND=1";
	/**
	 * ��ѯ��������.
	 */
	private static final String QUERY_FLFA = "SELECT ID, KSSJ,JSSJ FROM BO_AKL_WXB_XS_FL_HEAD WHERE FAZT='030001' AND ISEND=1";
	/**
	 * ����POS״̬.
	 */
	private static final String UPDATE_POS_ZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET POSZT='" + FAZT_ZXZ + "' WHERE ID=?";
	/**
	 * ���·���״̬.
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
			MessageQueue.getInstance().putMessage("admin", "POSAndFLActiveSchedule��ʱ������ִ���!");
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ����POS.
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
	 * ���·���.
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
