package cn.com.akl.shgl.zsjgl.aqkc.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhbh.biz.LockBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

public class AutoCalSchedule implements IJob {

	//��������
	private static final String QUERY_HCZL = "SELECT SUM (ISNULL(SL, 0)) HCZL FROM BO_AKL_WXJF_P p, BO_AKL_WXJF_S s WHERE p.BINDID = s.BINDID AND p.ISEND = 1 AND p.XMLB =? AND s.WLBH =? AND s.SX =? AND s.CLFS IN ('064286', '064289') AND s.CKDM =? AND p.CREATEDATE < DATEADD(MONTH, - 3, GETDATE())";
	
	//��ѯ��ȫ���ά����Ϣ
	private static final String QUERY_AQKC = "SELECT * FROM BO_AKL_AQKCWH_S WHERE ISEND = 1 AND 1=?";
	
	//���°�ȫ��棺ÿ�»��������������
	private static final String UPDATE_AQKC = "UPDATE BO_AKL_AQKCWH_S SET KCXX=?, HCLCK=? WHERE XMLB=? AND WLBH=? AND SXID=? AND CKBM=?";
	
	private Connection conn;
	public AutoCalSchedule() {
		// TODO Auto-generated constructor stub ��ȫ��棺�Զ�����ÿ�»��������������
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			service(conn);
			
			conn.commit();
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage("admin", e.getMessage(), true);
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage("admin", "��̨�����쳣���������̨", true);
		} finally {
			DBSql.close(conn, null, null);
		}

	}
	
	public void service(Connection conn) throws SQLException{
		DAOUtil.executeQueryForParser(conn, QUERY_AQKC, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String xmlb = rs.getString("XMLB");
				String wlbh = rs.getString("WLBH");
				String sx = rs.getString("SXID");
				String ckdm = rs.getString("CKBM");
				
				int hczl = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QUERY_HCZL, xmlb, wlbh, sx, ckdm));
				int kcxx = (int)Math.floor(hczl / 3 / 2);
				
				int updateCount = DAOUtil.executeUpdate(conn, UPDATE_AQKC, kcxx, hczl,
						xmlb, wlbh, sx, ckdm);
				if(updateCount != 1) throw new RuntimeException("��ȫ�������ֵ����ʧ�ܣ�����ϵ����Ա��");
				return true;
			}
		}, 1);
	}

}
