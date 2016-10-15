package cn.com.akl.authority;

import java.sql.Connection;

import com.actionsoft.awf.util.DBSql;

public class GiveUserRole {
	private Connection conn = null;
	// 向权限表插入赋予的权限
	public void giveRoleName(int roleID, String userid) {
		conn = DBSql.open();
		String sql2 = "select count(id) cnt from orguser where roleid = " + roleID + " and userid = '" + userid + "'";
		int cnt = DBSql.getInt(sql2, "cnt");
		if(cnt==0){
			String sql = "update ORGUSER set roleid = " + roleID + " where userid = '" + userid + "'";
			int flag = DBSql.executeUpdate(sql);
		}else{

		}
	}
}
