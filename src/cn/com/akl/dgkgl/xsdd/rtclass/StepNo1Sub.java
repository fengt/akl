package cn.com.akl.dgkgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Sub extends SubWorkflowEventClassA{
	
	public StepNo1Sub(){
	}
	public StepNo1Sub(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("����������ǰ�жϷ���ָ����");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = this.getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGXS_P", bindid);//���۵�ͷ
		String hzbh = rkdtData.get("HZBH") == null ?"":rkdtData.get("HZBH").toString();//�������
		String bmbh = rkdtData.get("BMBH") == null ?"":rkdtData.get("BMBH").toString();//�������ű��
		String khmc = rkdtData.get("KHMC") == null ?"":rkdtData.get("KHMC").toString();//�ͻ�����
		String bm = rkdtData.get("BM") == null ?"":rkdtData.get("BM").toString();//��������

		String khbm = null;
		String khbmbm = null;
		int a = 0;
		String sql = "SELECT KHBH,KHBMBM,USERID FROM BO_AKL_DGK_LYGX";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = DBSql.open();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					khbmbm = rs.getString("KHBMBM") == null ?"":rs.getString("KHBMBM").toString();//�ͻ����ű���
					khbm = rs.getString("KHBH") == null ?"":rs.getString("KHBH").toString();//�ͻ����ű���
					if(hzbh.equals(khbm) && bmbh.equals(khbmbm)){
						bmbh = khbmbm;
						a = 1;
					}
				}
				if(a==0){
					bmbh = "";
				}
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while(rs.next()){
					khbm = rs.getString("KHBH") == null ?"":rs.getString("KHBH").toString();//�ͻ�����
					khbmbm = rs.getString("KHBMBM") == null ?"":rs.getString("KHBMBM").toString();//�ͻ����ű���
					String userid = rs.getString("USERID") == null ?"":rs.getString("USERID").toString();//�û��˺�
					if(hzbh.equals(khbm) && bmbh.equals(khbmbm)){
						Hashtable para = super.getParameter(super.PARAMETER_PROFILE).toHashtable();
						para.put(super.PROFILE_PARTICIPANT, userid);
						para.put(super.PROFILE_TITLE,"����Ԥ���� "+bm+" "+khmc+" �ȴ�����");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return true;
	}
		
}
