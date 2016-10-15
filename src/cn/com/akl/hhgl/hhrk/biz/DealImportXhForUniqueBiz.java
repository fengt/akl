package cn.com.akl.hhgl.hhrk.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
/**
 * 处理导入单身数据唯一且不为空
 * @author ActionSoft_2013
 *
 */
public class DealImportXhForUniqueBiz {

	private static Connection conn = null;
	/**
	 * 处理导入单身数据唯一且不为空
	 * @param bindid
	 * @return
	 */
	public static List DealUtil(int bindid){
		conn = DBSql.open();
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();
		String sql = "select lh from " + HHDJConstant.tableName2 + " where bindid = " + bindid + " group by lh having count(lh)>1 ";
		try {
			conn = DBSql.open();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String lh = StrUtil.returnStr(rs.getString("lh"));
					if(StrUtil.isNotNull(lh)){
						list.add(lh);
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return list;
	}
}
