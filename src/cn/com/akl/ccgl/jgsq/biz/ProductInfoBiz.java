package cn.com.akl.ccgl.jgsq.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DateUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;

public class ProductInfoBiz {

	/**
	 * ������Ϣ����
	 */
	private Map<String, Hashtable<String, String>> wlxxCacheMap = new HashMap<String, Hashtable<String, String>>();

	private JGSQBiz jgsqBiz = null;

	public ProductInfoBiz() {
		super();
	}

	public ProductInfoBiz(JGSQBiz jgsqBiz) {
		super();
		this.jgsqBiz = jgsqBiz;
	}

	/**
	 * ��ȡ������Ϣ.
	 * 
	 * @param conn
	 * @param wlbh
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getProductInfo(Connection conn, String wlbh) throws SQLException {
		if (jgsqBiz != null) {
			jgsqBiz.getProductInfo(wlbh);
		}

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement("SELECT WLMC, XH, GG, DW FROM BO_AKL_WLXX WHERE WLBH=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, wlbh);
			if (reset.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("WLBH", wlbh);
				hashtable.put("WLMC", reset.getString("WLMC"));
				hashtable.put("XH", reset.getString("XH"));
				hashtable.put("GG", reset.getString("GG"));
				hashtable.put("DW", reset.getString("DW"));
				wlxxCacheMap.put(wlbh, hashtable);
				return hashtable;
			} else {
				return null;
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ��ȡ�����ͺ�.
	 * 
	 * @param conn
	 * @param wlbh
	 * @return
	 * @throws SQLException
	 */
	public String getProductInfoXH(Connection conn, String wlbh) throws SQLException {
		String xh = getProductInfo(conn, wlbh).get("XH");
		if (xh == null) {
			xh = DAOUtil.getStringOrNull(conn, "SELECT XH FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);
		}
		return xh;
	}

	/**
	 * ��ȡ�����ܴ��ɱ�..
	 * 
	 * @param conn
	 * @param wlbh
	 * @return
	 * @throws SQLException
	 */
	public BigDecimal getProductInfoCost(Connection conn, String pch, String wlbh) throws SQLException {
		BigDecimal productCost = jgsqBiz.getProductCost(wlbh);
		if (productCost == null) {
			return DAOUtil.getBigDecimalOrNull(conn, "SELECT DJ FROM BO_AKL_KC_KCHZ_P WHERE WLBH=? AND PCH=?", wlbh, pch);
		} else {
			return productCost;
		}
	}

	/**
	 * ��ȡ���ϱ��.
	 * 
	 * @param conn
	 * @param wlbh
	 * @return
	 * @throws SQLException
	 */
	public String getProductInfoUnit(Connection conn, String wlbh) throws SQLException {
		return DAOUtil.getString(conn, "SELECT DW FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);
	}

	/**
	 * 1����Ӫ������κ����ɹ���
	 * 
	 * @throws SQLException
	 **/
	public static String createPCH(Connection conn, Date rkrq) throws SQLException {
		// �������κŹ��򣬵�ǰ�������2014-07-24ת��Ϊ20140724 + 001���к�
		String pch1 = DateUtil.dateToLongStrBys2(rkrq);// ǰ׺���磺20140724
		String pch = pch1 + judgeRKRQ(conn, pch1);
		return pch;
	}

	/**
	 * �ж��Ƿ���ڵ�ǰ������ڵ����κţ���������ܱ����Ѵ��ڵ�ǰ������ڣ������κţ���׺��λ�����ۼӣ����򣬵�ǰ�������+001
	 * 
	 * @param pch1
	 * @return
	 * @throws SQLException
	 */
	private static String judgeRKRQ(Connection conn, String pch1) throws SQLException {
		String sql = "select max(SUBSTRING(pch,9,3)) pch2 from " + CgrkCnt.tableName3 + " where SUBSTRING(pch,1,8) = '" + pch1 + "'";
		String pch2 = "";
		pch2 = StrUtil.returnStr(DBSql.getString(conn, sql, "pch2"));
		if (StrUtil.isNotNull(pch2)) {
			return String.format("%03d", Integer.parseInt(pch2) + 1);
		}
		return "001";
	}

}
