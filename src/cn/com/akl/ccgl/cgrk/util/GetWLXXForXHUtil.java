package cn.com.akl.ccgl.cgrk.util;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;

/**
 * �����ͺţ���ȡ������Ϣ
 * @author ActionSoft_2013
 *
 */
public class GetWLXXForXHUtil {

	public static String getWLXX(String xh){
		String wlbh = "";
		String sql = "select * from " + CgrkCnt.tableName8 + " where xh = '" + xh + "'";
		wlbh = StrUtil.returnStr(DBSql.getString(sql, "wlbh"));
		return wlbh;
	}
}
