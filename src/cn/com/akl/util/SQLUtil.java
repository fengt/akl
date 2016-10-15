package cn.com.akl.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.actionsoft.awf.util.DBSql;

/**
 * ���ڷ�װͨ�õ�SQL���.
 * 
 * @author huangming
 *
 */
public class SQLUtil {

	/**
	 * �����кŵ�SQL
	 */
	private static final String UPDATE_ROW_SQL = "update {TABLENAME} set {FIELD}=t.rowId from (select id,ROW_NUMBER() over(Order by id) as rowId from {TABLENAME} WHERE BINDID=?) as t where t.id={TABLENAME}.id AND BINDID=?";

	/**
	 * ������ĳ���ֶθ���Ϊ�к�.
	 * 
	 * @param conn
	 * @param tableName
	 * @param fieldName
	 * @param whereSql
	 * @throws SQLException
	 */
	public static void updateRow(Connection conn, String tableName, String fieldName, int bindid) throws SQLException {
		DAOUtil.executeUpdate(conn, UPDATE_ROW_SQL.replaceAll("\\{TABLENAME\\}", tableName).replaceAll("\\{FIELD\\}", fieldName), bindid, bindid);
	}

	/**
	 * ����likeƥ�����ֵ.
	 */
	private static final String QUERY_MAXFIELD_FOR_LIKE = "SELECT MAX({FIELD}) FROM {TABLE} WHERE {FIELD} like ?";
	/**
	 * �������к�.
	 */
	private static final String UPDATE_SEQUECE = "UPDATE {TABLE} SET {FIELD1}=? WHERE ID=?";

	/**
	 * ���к����� ���к����ɹ���= �����ֶ�ֵ +��λ���к�.
	 * 
	 * @param uid
	 * @param hashtable
	 * @param id
	 * @param tableName
	 * @param sequeceField
	 * @param assistField
	 * @throws SQLException
	 */
	public static void sequeceGenerate(String uid, String tableName, int id, String sequeceField, String assistField, String assistValue)
			throws SQLException {
		Connection conn = null;
		if (assistValue == null || assistValue.trim().length() == 0)
			return;
		try {
			conn = DBSql.open();
			String maxWlbh = DAOUtil.getString(conn,
					QUERY_MAXFIELD_FOR_LIKE.replaceAll("\\{FIELD\\}", sequeceField).replaceAll("\\{TABLE\\}", tableName), assistValue.substring(3)
							+ "%");
			String seqno = "00001";
			StringBuilder sb = new StringBuilder(assistValue.substring(3));
			if (maxWlbh != null) {
				seqno = String.valueOf(Integer.parseInt(maxWlbh.substring(3)) + 1);
				if (seqno.length() != 5) {
					sb.append("0000", 0, 5 - seqno.length());
				}
			}
			sb.append(seqno);
			DAOUtil.executeUpdate(conn, UPDATE_SEQUECE.replaceAll("\\{TABLE\\}", tableName).replaceAll("\\{FIELD1\\}", sequeceField), sb.toString(),
					id);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * �������к�.
	 * 
	 * @param conn
	 *            ���ݿ�
	 * @param tableName
	 *            ���к��ֶ����ڵı�
	 * @param sequeceField
	 *            ���к��ֶ�
	 * @param assistValue
	 *            ǰ׺����
	 * @param seqLength
	 *            ���кų��ȣ����ɴ���10λ
	 * @return ����һ���������ҵ�������к�+1�����к�
	 * @throws SQLException
	 */
	public static String sequeceGenerate(Connection conn, String tableName, String sequeceField, String assistValue, int seqLength)
			throws SQLException {
		String maxWlbh = DAOUtil.getString(conn,
				QUERY_MAXFIELD_FOR_LIKE.replaceAll("\\{FIELD\\}", sequeceField).replaceAll("\\{TABLE\\}", tableName), assistValue + "%");
		String seqno = "00001";
		StringBuilder sb = new StringBuilder(assistValue);
		if (maxWlbh != null) {
			seqno = fillZero(Integer.parseInt(maxWlbh.replaceFirst(assistValue, "")) + 1, seqLength);
		}
		sb.append(seqno);
		return sb.toString();
	}

	/**
	 * ��ȡ������к�.
	 * 
	 * @param conn
	 * @param tableName
	 * @param sequeceField
	 * @param assistValue
	 * @return
	 * @throws SQLException
	 */
	public static int sequeceGenerateGetInt(Connection conn, String tableName, String sequeceField, String assistValue) throws SQLException {
		String maxWlbh = DAOUtil.getString(conn,
				QUERY_MAXFIELD_FOR_LIKE.replaceAll("\\{FIELD\\}", sequeceField).replaceAll("\\{TABLE\\}", tableName), assistValue + "%");
		if (maxWlbh == null) {
			return 0;
		} else {
			return Integer.parseInt(maxWlbh.replaceFirst(assistValue, ""));
		}
	}

	/**
	 * �����.
	 * 
	 * @param value
	 * @param length
	 * @return
	 */
	public static String fillZero(int value, int length) {
		StringBuilder sb = new StringBuilder();
		String seqno = String.valueOf(value);
		if (sb.length() < length && seqno.length() < length) {
			sb.append("0000000000", 0, length - seqno.length());
		} else {
			seqno = seqno.substring(seqno.length() - length, seqno.length());
		}
		sb.append(seqno);
		return sb.toString();
	}

	/**
	 * ��ѯ�ֶ�ֵ��ȵļ�¼��.
	 */
	private static final String findEqualsFieldValue = "SELECT COUNT(*) FROM {TABLE} WHERE {FIELD}=?";

	/**
	 * ��ѯ�ֶ�ֵ��ȵļ�¼��.
	 * 
	 * @param conn
	 * @param table
	 * @param field
	 * @param value
	 * @return
	 * @throws SQLException
	 */
	public static int findEqualsFieldValue(Connection conn, String table, String field, String value) throws SQLException {
		return DAOUtil.getInt(conn, findEqualsFieldValue.replaceAll("\\{TABLE\\}", table).replaceAll("\\{FIELD\\}", field), value);
	}

	/**
	 * ��ѯ�ֶ�ֵ��ȵļ�¼����֧�ּ��������磺whereSql��ֵ������Ϊ" A=1 AND B=1".
	 * 
	 * @param conn
	 * @param table
	 * @param field
	 * @param value
	 * @param whereSql
	 * @return
	 * @throws SQLException
	 */
	public static int findEqualsFieldValue(Connection conn, String table, String field, String value, String whereSql) throws SQLException {
		return DAOUtil.getInt(conn, findEqualsFieldValue.replaceAll("\\{TABLE\\}", table).replaceAll("\\{FIELD\\}", field) + " AND " + whereSql,
				value);
	}

	/**
	 * ��ȡBO�������з�ϵͳ�ֶεļ�¼����.
	 * 
	 * @param conn
	 * @param tableName
	 * @param whereSql
	 * @return
	 * @throws SQLException
	 */
	public static Hashtable<String, String> getBOHashtableSlow(Connection conn, String tableName, String whereSql) throws SQLException {
		ArrayList<String> fieldCollection = DAOUtil.getStringCollection(conn,
				"SELECT b.FIELD_NAME FROM SYS_BUSINESS_METADATA a INNER JOIN SYS_BUSINESS_METADATA_MAP b ON b.METADATA_ID=a.ID WHERE ENTITY_NAME=?",
				tableName);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (String field : fieldCollection) {
			sb.append(field).append(",");
		}
		sb.setCharAt(sb.length() - 1, ' ');
		sb.append(" FROM ").append(tableName);
		sb.append(" WHERE ").append(whereSql);
		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			ps = conn.prepareStatement(sb.toString());
			reset = ps.executeQuery();
			if (reset.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				for (String field : fieldCollection) {
					hashtable.put(field, reset.getString(field));
				}
				return hashtable;
			} else {
				return null;
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ��ȡBO�������з�ϵͳ�ֶεļ�¼����.
	 * 
	 * @param conn
	 * @param tableName
	 * @param whereSql
	 * @return
	 * @throws SQLException
	 */
	public static Hashtable<String, String> getBOHashtable(Connection conn, String tableName, String whereSql) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(tableName);
		sb.append(" WHERE ").append(whereSql);
		try {
			ps = conn.prepareStatement(sb.toString());
			reset = ps.executeQuery();
			ResultSetMetaData metaData = reset.getMetaData();
			List<String> fieldList = new ArrayList<String>(50);
			int columnCount = metaData.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				// �򵥵Ĺ������ڽ�ϵͳ�ֶι��˵�.
				if (i < 10 && i > 0) {
					continue;
				}
				fieldList.add(metaData.getColumnLabel(i));
			}
			Hashtable<String, String> hashtable = new Hashtable<String, String>();
			if (reset.next()) {
				for (String field : fieldList) {
					String value = reset.getString(field);
					hashtable.put(field, value == null ? "" : value);
				}
				return hashtable;
			} else {
				return null;
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

}
