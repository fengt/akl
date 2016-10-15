package cn.com.akl.shgl.yzsq.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.IMAPI;

public class TimeAlert implements IJob {

	private static final String QUERY_YZSQ = "SELECT YZSQDH,CREATEUSER FROM BO_AKL_YZSQ_P WHERE ISEND=0 AND CONVERT(VARCHAR, GETDATE(),120)>CONVERT(VARCHAR, FHSJ,120)"; 
	public TimeAlert() {
		super();
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(QUERY_YZSQ);
			rs = ps.executeQuery();
			while(rs.next()){
				String yzbh = rs.getString("YZSQDH");//用章申请单号
				String sqr = rs.getString("CREATEUSER");//申请人
				//发送邮件
				sendMessage(yzbh,sqr);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void sendMessage(String yzbh, String sqr){
		StringBuilder titleSb = new StringBuilder();
		titleSb.append("用章申请单号：");
		titleSb.append(yzbh).append("已超时");
		
		StringBuilder contentSb = new StringBuilder();
		contentSb.append("您好：<br/>&nbsp;&nbsp;");
		contentSb.append("该用章申请单号：").append(yzbh).append("已超出返回时间，特此通知！");
		
		//发送邮件
		IMAPI.getInstance().sendMail("admin", sqr, titleSb.toString(), contentSb.toString());
	}
	

}
