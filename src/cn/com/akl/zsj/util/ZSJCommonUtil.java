package cn.com.akl.zsj.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.SQLUtil;

public class ZSJCommonUtil {

	/**
	 * –Ú¡–∫≈…˙≥…
	 * 
	 * @param conn
	 * @param bindid
	 * @param tableName
	 * @param seqField
	 * @param lbField
	 * @param seqLength
	 * @throws SQLException
	 */
	public static void executeSeq(Connection conn, int bindid, String tableName, String seqField, String lbField, int seqLength, int lbStartSplit)
			throws SQLException {
		StringBuilder sqlSb = new StringBuilder();
		sqlSb.append("SELECT CONVERT(VARCHAR(12), ID)+'{}'+");
		sqlSb.append(lbField).append(" FROM ").append(tableName);
		sqlSb.append(" WHERE BINDID=?");
		sqlSb.append(" AND ");
		sqlSb.append("(").append(seqField).append(" is null ");
		sqlSb.append(" OR ");
		sqlSb.append(seqField).append("='')");

		ArrayList<String> mess = DAOUtil.getStringCollection(conn, sqlSb.toString(), bindid);
		ArrayList<String> seqList = new ArrayList<String>(mess.size());
		ArrayList<String> idList = new ArrayList<String>(mess.size());
		Map<String, Integer> lbMap = new HashMap<String, Integer>();
		for (String mes : mess) {
			String[] split = mes.split("\\{\\}");
			if (split.length == 2) {
				String id = split[0];
				String lbid = split[1].substring(split[1].length() - lbStartSplit, split[1].length());
				Integer seqno = lbMap.get(lbid);
				if (seqno == null) {
					seqno = SQLUtil.sequeceGenerateGetInt(conn, tableName, seqField, lbid);
				}
				lbMap.put(lbid, ++seqno);
				seqList.add(lbid + SQLUtil.fillZero(seqno, seqLength));
				idList.add(id);
			}
		}

		Object[][] args = new Object[idList.size()][2];
		for (int i = 0; i < idList.size(); i++) {
			args[i][0] = seqList.get(i);
			args[i][1] = idList.get(i);
		}
		DAOUtil.executeBatchUpdate(conn, "UPDATE " + tableName + " SET " + seqField + "=? WHERE ID=?", args);
	}
}
