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
				String yzbh = rs.getString("YZSQDH");//�������뵥��
				String sqr = rs.getString("CREATEUSER");//������
				//�����ʼ�
				sendMessage(yzbh,sqr);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void sendMessage(String yzbh, String sqr){
		StringBuilder titleSb = new StringBuilder();
		titleSb.append("�������뵥�ţ�");
		titleSb.append(yzbh).append("�ѳ�ʱ");
		
		StringBuilder contentSb = new StringBuilder();
		contentSb.append("���ã�<br/>&nbsp;&nbsp;");
		contentSb.append("���������뵥�ţ�").append(yzbh).append("�ѳ�������ʱ�䣬�ش�֪ͨ��");
		
		//�����ʼ�
		IMAPI.getInstance().sendMail("admin", sqr, titleSb.toString(), contentSb.toString());
	}
	

}
