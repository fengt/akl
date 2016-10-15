package cn.com.akl.authority;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 部门权限删除之后，删除相应账号的权限
 * @author zhaijw
 *
 */
public class DeleteTransactionAfter extends WorkFlowStepRTClassA{
	public DeleteTransactionAfter(UserContext uc) {
		super(uc);
	}
	@Override
	public boolean execute() {
		String boTableName = "BO_BMQXSC";
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int processTaskInstanceId = getParameter(this.PARAMETER_TASK_ID).toInt();
		Hashtable boT = (Hashtable)BOInstanceAPI.getInstance().getBOData(boTableName, processInstanceId);
		String bmid = boT.get("BMID")==null?"":boT.get("BMID").toString();
		String qxzmc =  boT.get("QXZMC")==null?"":boT.get("QXZMC").toString();
		int securityGroupId = DBSql.getInt(
				"select id from sys_securitygroup where groupname='"
						+ qxzmc + "'", "id");
		List<String> list = DepartmentUtil.getAllUserid(Integer.parseInt(bmid));
		 for (int i = 0; i < list.size(); i++) {
			 GiveUserAuthority gua=new GiveUserAuthority();
			 gua.removePower(securityGroupId, list.get(i));
		 }
		updateQXZTable(Integer.parseInt(bmid),qxzmc);
		return false;
	}
	
	//给权限添加表添加标示字段，表示此添加的权限已经被移除
	public void updateQXZTable(int bmid,String qxzmc){
		Connection conn=null;
		PreparedStatement ps=null;
		
		try {
			conn=DBSql.open();
			ps=conn.prepareStatement("update BO_BMQXXZ set iddelete=1 where bmid=? and qxzmc=?");
			ps.setInt(1, bmid);
			ps.setString(2, qxzmc);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, null);
		}
	}
}
