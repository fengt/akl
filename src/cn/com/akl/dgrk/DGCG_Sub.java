package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCG_Sub extends SubWorkflowEventClassA {

	public DGCG_Sub() {
	}

	public DGCG_Sub(UserContext uc) {
		super(uc);
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGCG_P", bindid);//采购单头
		String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//客户编号
		String khmc = rkdtData.get("KHMC") == null ?"":rkdtData.get("KHMC").toString();//客户名称
		String khbmbh = rkdtData.get("KHBMBM") == null ?"":rkdtData.get("KHBMBM").toString();//客户部门编号
		
		String sql = "SELECT KHBH,KHBMBM,USERID FROM BO_AKL_DGK_LYGX";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = DBSql.open();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String khbm = rs.getString("KHBH") == null ?"":rs.getString("KHBH").toString();//客户编码
					String khbmbm = rs.getString("KHBMBM") == null ?"":rs.getString("KHBMBM").toString();//客户部门编码
					String userid = rs.getString("USERID") == null ?"":rs.getString("USERID").toString();//用户账号
					if(khbh.equals(khbm) && khbmbh.equals(khbmbm)){
						Hashtable para = super.getParameter(super.PARAMETER_PROFILE).toHashtable();
						para.put(super.PROFILE_PARTICIPANT, userid);
						para.put(super.PROFILE_TITLE, "代管入库-"+khmc+"");
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
