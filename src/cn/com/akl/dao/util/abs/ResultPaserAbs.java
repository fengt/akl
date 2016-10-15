package cn.com.akl.dao.util.abs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil.ResultPaser;

/**
 * �������ӿڳ�����..
 * 
 * @author huangming
 *
 */
public abstract class ResultPaserAbs implements ResultPaser {

	/**
	 * ���������ǰ�÷���.
	 * 
	 * @param conn
	 */
	public void init(Connection conn) throws SQLException {
	};

	public abstract boolean parse(Connection conn, ResultSet reset) throws SQLException;

	/**
	 * �����������÷���.
	 * 
	 * @param conn
	 */
	public void destory(Connection conn) throws SQLException {
	};
}
