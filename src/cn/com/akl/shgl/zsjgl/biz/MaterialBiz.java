package cn.com.akl.shgl.zsjgl.biz;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;

public class MaterialBiz {

	private Map<String, String> cacheWlbhMap = new HashMap<String, String>();

	/**
	 * ��ȡ������к�.
	 * 
	 * @param str
	 * @return
	 */
	public String getMaxWlbh(String str) {
		String number = cacheWlbhMap.get(str);
		if (number == null) {
			Connection conn = null;
			try {
				conn = DBSql.open();
				// TODO ��ѯ������ϱ���.
				return DAOUtil.getStringOrNull(conn, "SELECT MAX(WLBH) FROM BO_AKL_CPXX", str);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				DBSql.close(conn, null, null);
			}
		} else {
			return null;
		}
	}

	/**
	 * ���ϱ������.
	 * 
	 * @return
	 */
	public String addWlbh(String lb) {
		String wlbh = getMaxWlbh(lb);
		if (wlbh == null) {
			wlbh = lb + "00001";
		} else {
			String numStr = wlbh.substring(3);
			int num = Integer.parseInt(numStr);
			num++;
			wlbh = lb + String.valueOf(num);
		}
		cacheWlbhMap.put(lb, wlbh);
		return wlbh;
	}
}
