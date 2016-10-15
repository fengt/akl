package cn.com.akl.hhgl.hhrk.util;

import com.actionsoft.awf.util.DBSql;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;

/**
 * 根据型号，获取物料信息
 * @author ActionSoft_2013
 *
 */
public class GetWLXXForXHUtil {

	public static String getWLXX(String xh){
		String wlbh = "";
		String sql = "select * from " + HHDJConstant.tableName8 + " where xh = '" + xh + "'";
		wlbh = StrUtil.returnStr(DBSql.getString(sql, "wlbh"));
		return wlbh;
	}
}
