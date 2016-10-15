package cn.com.akl.util;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import com.actionsoft.awf.util.DBSql;

public class StrUtil {

	/**
	 * �ж�str �Ƿ�Ϊnull
	 * @param str
	 * @return
	 */
	public static boolean isNotNull(Object str){
		if(null!=str && !"".equals(str)){
			return true;
		}else
			return false;
	}
	/**
	 * �ж��ַ����Ƿ�Ϊnull�������ַ���
	 * 
	 * @param str
	 * @return
	 */
	public static String returnStr(String str) {
		if (StrUtil.isNotNull(str)) {
			return str.trim();
		} else {
			return "";
		}
	}
	/**
	 * ��doubleת����λС��
	 * @param value
	 * @return
	 */
  public static String formatDouble(Double value){
	  if(StrUtil.isNotNull(value)){
		  DecimalFormat df   =new DecimalFormat("######0.0000");
		  return df.format(value);
	  }else{
		  return "";
	  }
  }
  
  public static Object nullTOString(Object obj){
      if(isNotNull(obj)){
          return obj;
      }else{
          return "";
      }
  }
  
	/**
	 * ִ��sql����
	 * @param sql sql���
	 * @return ִ�н������
	 * @author panj
	 */
	public static int executeUpdate(String sql){
		Connection conn = null;
		Statement stat = null;
		int result = 0;
		try{
			conn = DBSql.open();
			stat = conn.createStatement();
			result = stat.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn,stat,null);
		}
		return result;
	}
}




