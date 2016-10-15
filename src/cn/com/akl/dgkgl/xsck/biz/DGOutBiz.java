package cn.com.akl.dgkgl.xsck.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

public class DGOutBiz {
	/**
	 * ��ѯ���۶�������.
	 */
	private static final String QUERY_XSDD_BODY = "SELECT XSSL, WLBH, YCKSL, KHCGDH FROM BO_AKL_DGXS_S where DDID=?";
	/**
	 * ��ѯ���������Ƿ񳬳����۶����е�����
	 */
	private static final String queryKCKSLNoEnoughWLBH = "SELECT s1.WLBH+'����Ӧ����'+CONVERT(VARCHAR(16), SUM(ISNULL(s2.XSSL, 0))-SUM(ISNULL(s2.YCKSL, 0))) FROM  BO_BO_AKL_DGCK_S s1, (SELECT p.XSDDID as DDID,s.WLBH,s.XSSL,s.YCKSL,s.JLDW FROM BO_AKL_DGXS_P p, BO_AKL_DGXS_S s WHERE p.BINDID=s.BINDID AND p.BINDID=?) as s2 WHERE s1.DDH = s2.DDID AND s1.WLBH=s2.WLBH AND s1.DW=s2.JLDW AND s1.DDH=? GROUP BY s1.WLBH HAVING SUM(ISNULL(s1.SFSL, 0))>SUM(ISNULL(s2.XSSL, 0))-SUM(ISNULL(s2.YCKSL, 0))";
	/**
	 * ��ѯ�������۵���������.
	 */
	private static final String QUERY_DGXS_WLSL = "SELECT SUM(ISNULL(s.XSSL, 0)) FROM BO_AKL_DGXS_P p, BO_AKL_DGXS_S s WHERE p.bindid=s.bindid AND p.XSDDID=?";
	/**
	 * ��ѯ���ܳ������������.
	 */
	private static final String QUERY_DGCK_WLSL = "SELECT SUM(ISNULL(SFSL, 0)) FROM BO_BO_AKL_DGCK_S WHERE BINDID=?";
	/**
	 * ��ѯ���۶���.
	 */
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * ��ѯ�����������Ϣ +
	 */
	private static final String queryWLXX = "SELECT s.WLBH,s.PCH,sum(s.SFSL) sfsl,s.HWDM,s.SX,p.XSDH, s.KHCGDH FROM BO_BO_AKL_DGCK_S s left join BO_BO_AKL_DGCK_P p on s.bindid = p.bindid WHERE s.BINDID=? group by s.WLBH,s.PCH,s.HWDM,s.SX, p.XSDH,s.KHCGDH";
	/**
	 * ��ѯ���ܿ����ϸ�д˻�λ��������������������� +
	 */
	private static final String queryWLSLNoEnoughMXHW = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCMX_S s WHERE s.HWDM=? AND s.WLBH=? AND s.PCH=? AND ISNULL(s.KWSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = s.WLBH and HWDM = s.HWDM AND PCH = s.PCH AND (XSDH<>? or ISNULL(KHCGDH, '')<>?)), s.KWSL)>=? AND SX=?";
	/**
	 * ��ѯ���ܿ������д������������������ +
	 */
	private static final String queryWLSLNoEnoughHZPC = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCHZ_P p WHERE p.WLBH=? AND p.PCH=? AND ISNULL(p.PCSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = p.WLBH AND PCH = p.PCH AND (XSDH<>? or ISNULL(KHCGDH, '')<>?)), p.PCSL)>=? AND ZT='042022'";
	/**
	 * ��������+
	 */
	private static final String queryWLSLNoEnoughMXHWQT = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCMX_S s WHERE s.HWDM=? AND s.WLBH=? AND s.PCH=? AND ISNULL(s.KWSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = s.WLBH AND HWDM = s.HWDM AND PCH = s.PCH AND XSDH=? AND ISNULL(KHCGDH, '')<>?), s.KWSL)>=? AND SX=?";
	/**
	 * ��������+
	 */
	private static final String queryWLSLNoEnoughHZPCQT = "SELECT COUNT(*) FROM BO_AKL_DGKC_KCHZ_P p WHERE p.WLBH=? AND p.PCH=? AND ISNULL(p.PCSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = p.WLBH AND PCH = p.PCH AND XSDH=? AND ISNULL(KHCGDH, '')<>?), p.PCSL)>=? AND ZT='042022'";
	/**
	 * ��������.
	 */
	private static final String QUERY_CKD_CKLX = "SELECT CKLX FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * ��ѯ���ⵥ�Ƿ��ѳ�
	 */
	private static final String queryXSDDCKSL = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a join BO_AKL_DGXS_S b on a.BINDID=b.BINDID WHERE a.XSDDID=? GROUP BY a.XSDDID HAVING SUM(ISNULL(YCKSL, 0))<>SUM(ISNULL(XSSL, 0))";
	/**
	 * ��ѯ���ⵥ�Ƿ��Ѱ���
	 */
	private static final String queryYBXSDH = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a left join BO_BO_AKL_DGCK_P b on a.XSDDID = b.XSDH where a.ZT <> '���ֳ���' and b.XSDH=? and b.bindid<>? and b.ZT = 'δ����'";

	/**
	 * ��֤���۶��������۳��������Ƿ�һ��.
	 * 
	 * @throws SQLException
	 */
	public void validateSalesAndOutNumIsEquals(Connection conn, int bindid, String uid) throws SQLException {
		String xsdh = DAOUtil.getString(conn, queryXSDH, bindid);

		// ����������ʾ.
		Integer xsWlsl = DAOUtil.getIntOrNull(conn, QUERY_DGXS_WLSL, xsdh);
		Integer ckWlsl = DAOUtil.getIntOrNull(conn, QUERY_DGCK_WLSL, bindid);
		if (xsWlsl == null) {
			if (!ckWlsl.equals(xsWlsl)) {
				MessageQueue.getInstance().putMessage(uid, "�������������������������ע�⣡");
			}
		} else {
			if (!xsWlsl.equals(ckWlsl)) {
				MessageQueue.getInstance().putMessage(uid, "�������������������������ע�⣡");
			}
		}
	}

	/**
	 * ��֤���Ͽ�������.
	 * 
	 * @throws SQLException
	 */
	public void validateMaterialAvailableAmount(Connection conn, int bindid, final String uid) throws SQLException {
		/** �������� */
		final String cklx = DAOUtil.getString(conn, QUERY_CKD_CKLX, bindid);

		// ��֤ÿ����λ�Ļ����Ƿ����
		DAOUtil.executeQueryForParser(conn, queryWLXX, new DAOUtil.ResultPaser() {
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				if (!cklx.equals("054143") && !cklx.equals("054144")) {
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughMXHW, reset.getString("HWDM"), reset.getString("WLBH"), reset.getString("PCH"),
							reset.getString("XSDH"), reset.getString("KHCGDH"), reset.getInt("SFSL"), reset.getString("SX"))) {
						MessageQueue.getInstance().putMessage(
								uid,
								"���κţ�" + reset.getString("PCH") + " ���Ϻ�:" + reset.getString("WLBH") + " �ڻ�λ���룺" + reset.getString("HWDM") + "������������"
										+ reset.getInt("SFSL") + "!", true);
						throw new RuntimeException("���κţ�" + reset.getString("PCH") + " ���Ϻ�:" + reset.getString("WLBH") + " �ڻ�λ���룺"
								+ reset.getString("HWDM") + "������������" + reset.getInt("SFSL") + "!");
					}
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughHZPC, reset.getString("WLBH"), reset.getString("PCH"), reset.getString("XSDH"),
							reset.getString("KHCGDH"), reset.getInt("SFSL"))) {
						MessageQueue.getInstance().putMessage(uid,
								"���Ϻ�:" + reset.getString("WLBH") + " ����" + reset.getString("PCH") + "���ϵĳ��������Ѿ��ﵽ���ޣ���������ܱ�", true);
						throw new RuntimeException("���Ϻ�:" + reset.getString("WLBH") + " ����" + reset.getString("PCH") + "���ϵĳ��������Ѿ��ﵽ���ޣ���������ܱ�");
					}
				} else {
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughMXHWQT, reset.getString("HWDM"), reset.getString("WLBH"), reset.getString("PCH"),
							reset.getString("XSDH"), reset.getString("KHCGDH"), reset.getInt("SFSL"), reset.getString("SX"))) {
						MessageQueue.getInstance().putMessage(
								uid,
								"���κţ�" + reset.getString("PCH") + " ���Ϻ�:" + reset.getString("WLBH") + " �ڻ�λ���룺" + reset.getString("HWDM") + "������������"
										+ reset.getInt("SFSL") + "!", true);
						throw new RuntimeException("���κţ�" + reset.getString("PCH") + " ���Ϻ�:" + reset.getString("WLBH") + " �ڻ�λ���룺"
								+ reset.getString("HWDM") + "��λ��������" + reset.getInt("SFSL") + "!");
					}
					if (0 == DAOUtil.getInt(conn, queryWLSLNoEnoughHZPCQT, reset.getString("WLBH"), reset.getString("PCH"), reset.getString("XSDH"),
							reset.getString("KHCGDH"), reset.getInt("SFSL"))) {
						MessageQueue.getInstance().putMessage(uid,
								"���Ϻ�:" + reset.getString("WLBH") + " ����" + reset.getString("PCH") + "���ϵĳ��������Ѿ��ﵽ���ޣ���������ܱ�", true);
						throw new RuntimeException("���Ϻ�:" + reset.getString("WLBH") + " ����" + reset.getString("PCH") + "���ϵĳ��������Ѿ��ﵽ���ޣ���������ܱ�");
					}
				}
				return true;
			}
		}, bindid);
	}

	/**
	 * ��֤�˳��ⵥ�Ƿ��ܹ���<br/>
	 * 1����֤���۵��Ƿ������.<br/>
	 * 2����֤���۵���״̬.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param xsdh
	 * @throws SQLException
	 */
	public void validateIsCanOut(Connection conn, int bindid, String uid, String xsdh) throws SQLException {
		/** 1.��֤���۶���״̬ */
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(QUERY_XSDD_BODY);
			rs = DAOUtil.executeFillArgsAndQuery(conn, stat, xsdh);
			while (rs.next()) {
				// ��ѯ����ĺ������Ӧ������֮��
				String WLBH = rs.getString(2);
				String KHCGDH = PrintUtil.parseNull(rs.getString(4));
				int YFSL = DAOUtil.getIntOrNull(conn,
						"SELECT sum(YFSL) YFSL FROM BO_BO_AKL_DGCK_S where bindid=? and WLBH=? AND ISNULL(KHCGDH, '')=?", bindid, WLBH, KHCGDH);
				int SYXSSL = rs.getInt(1) - rs.getInt(3);
				if (YFSL != SYXSSL) {
					throw new RuntimeException("�ͻ��ɹ�����Ϊ��" + KHCGDH + "���ϱ��Ϊ��" + WLBH + "�ĸĺ�ʵ�������ܼƣ�" + YFSL + "�������Ӧ�������ܼƣ�" + SYXSSL + "����");
				}
			}
		} finally {
			DBSql.close(stat, rs);
		}

		/** 2.��֤���۶����ѳ������� */
		String YB = DAOUtil.getStringOrNull(conn, queryYBXSDH, xsdh, bindid);
		if (YB != null && Integer.parseInt(YB) > 0) {
			throw new RuntimeException("�����۶����Ѱ���");
		}
		String YC = DAOUtil.getStringOrNull(conn, queryXSDDCKSL, xsdh);
		if (YC == null || "0".equals(YC)) {
			throw new RuntimeException("�����۶����Ѱ���");
		}
	}

	/**
	 * ��֤���ⵥ�ĳ��������Ƿ񳬹����۶�����������
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateSalesOutNum(Connection conn, final int bindid, String xsdh) throws SQLException {
		Integer xsddbindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_DGXS_P WHERE XSDDID=?", xsdh);
		DAOUtil.executeQueryForParser(conn,
				"SELECT WLBH, XH, SUM(ISNULL(XSSL, 0)) XSSL, SUM(ISNULL(YCKSL, 0)) YCKSL FROM BO_AKL_DGXS_S WHERE BINDID=? GROUP BY WLBH, XH",
				new DAOUtil.ResultPaser() {
					@Override
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						Integer sfsl = DAOUtil.getIntOrNull(conn, "SELECT SUM(ISNULL(SFSL, 0)) SFSL FROM BO_BO_AKL_DGCK_S WHERE BINDID=? AND WLBH=?",
								bindid, reset.getString("WLBH"));
						int xssl = reset.getInt("XSSL");
						int ycksl = reset.getInt("YCKSL");
						int kysl = xssl - ycksl;

						if (sfsl != kysl) {
							throw new RuntimeException("�����ͺţ�" + reset.getString("XH") + "�ĳ������� ������ ����������" + kysl + "��");
						}

						return true;
					}
				}, xsddbindid);

	}

}
