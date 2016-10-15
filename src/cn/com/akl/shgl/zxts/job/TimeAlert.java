package cn.com.akl.shgl.zxts.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.IMAPI;

public class TimeAlert implements IJob {

	private static final String QUERY_YJSZ = "SELECT ZXBH,CREATEUSER FROM BO_AKL_ZXTS_P WHERE ISEND=0 AND SFCLWB='025001' AND CONVERT(VARCHAR, GETDATE(),120)>CONVERT(VARCHAR, CLSXYJSZ,120)"; 
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
			ps = conn.prepareStatement(QUERY_YJSZ);
			rs = ps.executeQuery();
			while(rs.next()){
				String zxbh = rs.getString("ZXBH");//��ѯ��Ͷ�ߵ���
				String sqr = rs.getString("CREATEUSER");//������
				//�����ʼ�
				sendMessage(zxbh,sqr);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void sendMessage(String zxbh, String sqr){
		StringBuilder titleSb = new StringBuilder();
		titleSb.append("��ѯ��Ͷ�ߵ��ţ�");
		titleSb.append(zxbh).append("�ѳ�ʱ");
		
		StringBuilder contentSb = new StringBuilder();
		contentSb.append("���ã�<br/>&nbsp;&nbsp;");
		contentSb.append("����ѯ��Ͷ�ߵ��ţ�").append(zxbh).append("�ѳ���Ԥ��ʱ�ޣ��ش�֪ͨ��");
		
		//�����ʼ�
		IMAPI.getInstance().sendMail("admin", sqr, titleSb.toString(), contentSb.toString());
	}
	

}
