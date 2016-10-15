package cn.com.akl.xsgl.khjxc.schedule;

import java.sql.Connection;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

public class UpdateWLXXSchedule implements IJob {

	// ����״̬����Ʒ
	private static final String WLXX_WLZT_XP = "4";
	// ����״̬������
	private static final String WLXX_WLZT_ZC = "0";
	
	// ���µ��ڵ�����Ϊ����
	private static final String updateWLXX = "UPDATE BO_AKL_WLXX SET WLZT="+WLXX_WLZT_ZC+" WHERE CONVERT(DATE, DATEADD(day, 14, CREATEDATE), 120)=CONVERT(DATE, GETDATE(), 120) AND WLZT="+WLXX_WLZT_XP;
	
	/**
	 * 1����ʱ������������Ϣ���еġ�����״̬���ֶΣ���������¼��ʱ��+14��Ȼ�գ���������Ϊ������Ʒ:4������Ϊ������:0��
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
			MessageQueue.getInstance().putMessage("admin", "UpdateWLXXSchedule��ʱ�����ִ���");
		} finally {
			DBSql.close(conn, null, null);
		}
		
	}

}
