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
	 * ��Ӧ�̷���֧�����뷽��״̬�� ִ����
	 */
	private static final String GYSFYFAZT_ZXZ = "030003";
	/**
	 * ��Ӧ�̷���֧�����뷽��״̬�� ִ�����
	 */
	private static final String GYSFYFAZT_ZXWB = "030004";
	/**
	 * ��ѯ����������DD��MDF��
	 */
	private static final String QUERY_PROCESS_OTHER = "SELECT POSBH FROM BO_AKL_WXB_XS_POS_HEAD WHERE ISEND=1 AND POSZT='" + GYSFYFAZT_ZXZ
			+ "' AND JSSJ<=GETDATE()";
	/**
	 * ����POS״̬
	 */
	private static final String UPDATE_PROCESS_POSZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET POSZT='" + GYSFYFAZT_ZXWB + "' WHERE POSBH=?";

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		/**
		 * 1.ͨ����ʱ��������POS��������ÿ��δ������POS������ POS��ֹ����>=��ǰ���ڣ���POS������״̬����Ϊ�ѽ�����ͬʱ����POS�ʽ��׷��POS����ʣ���
		 * POSʣ����=�������-��POS����*���ۣ�POS�������������0����������POS�ʽ����ע ��POS��
		 */
		// BO_AKL_POSBX_P
		// BO_AKL_POSBX_S
		// SELECT * FROM BO_AKL_POSBX_P WHERE LB=043028 AND POSZT=4
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			conn = DAOUtil.openConnectionTransaction();

			// ���¹�Ӧ�̷���֧���з�POS�ķ���
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
			MessageQueue.getInstance().putMessage("admin", "POSProcessSchedule��ʱ���������⣬�������̨��");
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, reset);
		}

	}

}
