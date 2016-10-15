package cn.com.akl.hhgl.hhrk.job;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.hhgl.hhrk.biz.DealPriceForEmailNoticeBiz;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.hhgl.hhrk.util.GetWLXXForXHUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;

/**
 * 定时器执行价格比对后，进行系统邮件通知
 * @author ActionSoft_2013
 *
 */
public class DealPriceForEmailNoticeJob implements IJob{

	private static Connection conn = null;
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		conn = DBSql.open();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector vector = new Vector();
		Hashtable table = null; 
		String sql = "select distinct(wlbh),zcrq,HSJG from " + HHDJConstant.tableName0 + " a," + HHDJConstant.tableName1 + " b where a.bindid = b.bindid and wlbh is not null and zcrq is not null" ;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					table = new Hashtable();
					String zcrq = StrUtil.returnStr(rs.getString("zcrq"));
					zcrq = zcrq.substring(0, 10);
					String wlbh = StrUtil.returnStr(rs.getString("wlbh"));
//					String xh = StrUtil.returnStr(rs.getString("xh"));
//					GetWLXXForXHUtil.getWLXX(xh);
					double hscgj = rs.getDouble("HSJG");
					table.put("zcrq", zcrq);
					table.put("wlbh", wlbh);
					table.put("hscgj", hscgj);
					vector.add(table);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		if(vector!=null){
			try {
				/**进行价格比对通知**/
				DealPriceForEmailNoticeBiz.getEmailContent(vector);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
