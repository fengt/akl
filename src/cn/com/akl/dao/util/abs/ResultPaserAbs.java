package cn.com.akl.dao.util.abs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil.ResultPaser;

/**
 * 结果处理接口抽象类..
 * 
 * @author huangming
 *
 */
public abstract class ResultPaserAbs implements ResultPaser {

	/**
	 * 结果集处理前置方法.
	 * 
	 * @param conn
	 */
	public void init(Connection conn) throws SQLException {
	};

	public abstract boolean parse(Connection conn, ResultSet reset) throws SQLException;

	/**
	 * 结果集处理后置方法.
	 * 
	 * @param conn
	 */
	public void destory(Connection conn) throws SQLException {
	};
}
