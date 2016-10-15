package cn.com.akl.authority;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;

public class GiveUserAuthority {

	// public void execute(JobExecutionContext arg0) throws
	// JobExecutionException {
	// DepartmentUtil dpu = new DepartmentUtil();
	// List<String> list = dpu.getAllUserid(6299);
	// for (int i = 0; i < list.size(); i++) {
	// removePower("测试权限组", list.get(i));
	// //givePower("测试权限组", list.get(i));
	// }
	// }

	//移除权限组的权限
	public void removePower(int securityGroupId, String userid) {
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "delete from SYS_USERSECURITY where SECURITYGROUPID=? and USERID=?";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, securityGroupId);
			ps.setString(2, userid);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, null);
		}
	}

	// 向权限表插入赋予的权限
	public void givePower(int securityGroupId, String userid) {
		int sequence = getSequence();
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "INSERT INTO SYS_USERSECURITY(ID,SECURITYGROUPID,USERID)VALUES(?,?,?)";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, sequence);
			ps.setInt(2, securityGroupId);
			ps.setString(3, userid);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, null);
		}
	}

	// 获取sequence
	public int getSequence() {
		addSequence();
		return DBSql
				.getInt("select SequenceValue from SYSSEQUENCE where SequenceName='SYS_SECURITY'",
						"SequenceValue");

	}

	// sequence加1
	public void addSequence() {
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "update SYSSEQUENCE set SEQUENCEVALUE= SEQUENCEVALUE+SequenceStep where SequenceName='SYS_SECURITY'";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, null);
		}
	}
}
