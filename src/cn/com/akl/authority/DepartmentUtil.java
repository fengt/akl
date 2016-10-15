package cn.com.akl.authority;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.actionsoft.awf.util.DBSql;

public class DepartmentUtil {

	// ��ȡ��������Ա����userid�������¼����ţ�
	public static List<String> getAllUserid(int deptId) {
		List<String> useridList = new ArrayList<String>();
		List<Integer> deptList = getSonDept(deptId);
		deptList.add(deptId);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select userid,departmentid from orguser";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (deptList.contains(rs.getInt("departmentid"))) {
					useridList.add(rs.getString("userid"));
				}
			}
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return useridList;
	}

	// ��ȡ���ŵ������¼�����id(�������˲���)
	public static List<Integer> getSonDept(int fatherDeptId) {
		List<Integer> list = new ArrayList<Integer>();
		int layer = DBSql.getInt("select layer from orgdepartment where id="
				+ fatherDeptId, "layer");
		int companyId = DBSql.getInt(
				"select companyid from orgdepartment where id=" + fatherDeptId,
				"companyid");

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from orgdepartment where layer>? and companyid=?";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, layer);
			ps.setInt(2, companyId);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (isHigherDept(fatherDeptId, rs.getInt("id"))) {
					list.add(rs.getInt("id"));
				}
			}
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return list;
	}

	// �ж�fatherDeptid�ǲ���sonDeptId���ϼ�����
	public static boolean isHigherDept(int fatherDeptId, int sonDeptId) {
		while (true) {
			sonDeptId = DBSql.getInt(
					"select parentdepartmentid from orgdepartment where id="
							+ sonDeptId, "parentdepartmentid");
			if (sonDeptId == fatherDeptId) {
				return true;
			} else if (sonDeptId == 0) {
				return false;
			}
		}
	}

}
