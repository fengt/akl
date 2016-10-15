package cn.com.akl.ccgl.jgsq.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;

public class JGGZBiz {

	/**
	 * �ӹ�����ԭ�ϼ�������ѯ.
	 */
	private static final String QUERY_JGGZ_YL = "SELECT s.WLBH, s.SL FROM BO_AKL_JG_JGGZ_P a JOIN BO_AKL_JG_JGGZ_YL_S s ON a.BINDID=s.BINDID WHERE a.GZBH=?";
	/**
	 * �ӹ�����ĳ�Ʒ��������ѯ.
	 */
	private static final String QUERY_JGGZ_CP = "SELECT s.WLBH, s.SL FROM BO_AKL_JG_JGGZ_P a JOIN BO_AKL_JG_JGGZ_CP_S s ON a.BINDID=s.BINDID WHERE a.GZBH=?";

	/**
	 * ��ȡ�ӹ������ԭ�Ϻ�����.
	 * 
	 * @param conn
	 * @param gzbh
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Integer> getProcessingMaterialForProcessRules(Connection conn, String gzbh) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		Map<String, Integer> jgMap = new HashMap<String, Integer>();
		try {
			ps = conn.prepareStatement(QUERY_JGGZ_YL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, gzbh);
			while (reset.next()) {
				jgMap.put(reset.getString("WLBH"), reset.getInt("SL"));
			}
			conn.commit();
			return jgMap;
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ��ȡ�ӹ������ԭ�Ϻ�����.
	 * 
	 * @param conn
	 * @param gzbh
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Integer> getProductForProcessRules(Connection conn, String gzbh) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		Map<String, Integer> jgMap = new HashMap<String, Integer>();
		try {
			ps = conn.prepareStatement(QUERY_JGGZ_CP);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, gzbh);
			while (reset.next()) {
				jgMap.put(reset.getString("WLBH"), reset.getInt("SL"));
			}
			conn.commit();
			return jgMap;
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ����������ʾ��Ϣ��.
	 * 
	 * @param map
	 * @return
	 * @throws SQLException
	 */
	public static String showRule(Connection conn, Map<String, Integer> map, ProductInfoBiz jgsqBiz) throws SQLException {
		if (map == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		Set<Entry<String, Integer>> entrySet = map.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			String xh = jgsqBiz.getProductInfoXH(conn, entry.getKey());
			//sb.append(entry.getKey());
			//sb.append(" = ");
			sb.append(xh);
			sb.append(" ���� ");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}
