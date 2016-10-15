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

	//换出总量
	private static final String QUERY_HCZL = "SELECT SUM (ISNULL(SL, 0)) HCZL FROM BO_AKL_WXJF_P p, BO_AKL_WXJF_S s WHERE p.BINDID = s.BINDID AND p.ISEND = 1 AND p.XMLB =? AND s.WLBH =? AND s.SX =? AND s.CLFS IN ('064286', '064289') AND s.CKDM =? AND p.CREATEDATE < DATEADD(MONTH, - 3, GETDATE())";
	
	//查询安全库存维护信息
	private static final String QUERY_AQKC = "SELECT * FROM BO_AKL_AQKCWH_S WHERE ISEND = 1 AND 1=?";
	
	//更新安全库存：每月换出量、库存下限
	private static final String UPDATE_AQKC = "UPDATE BO_AKL_AQKCWH_S SET KCXX=?, HCLCK=? WHERE XMLB=? AND WLBH=? AND SXID=? AND CKBM=?";
	
	private Connection conn;
	public AutoCalSchedule() {
		// TODO Auto-generated constructor stub 安全库存：自动计算每月换出量、库存下限
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
			MessageQueue.getInstance().putMessage("admin", "后台出现异常，请检查控制台", true);
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
				if(updateCount != 1) throw new RuntimeException("安全库存下限值更新失败，请联系管理员！");
				return true;
			}
		}, 1);
	}

}
