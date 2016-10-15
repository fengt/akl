package cn.com.akl.dgkgl.xsck.web;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

/**
 * ����Ԥ���ⵥ��ӡWeb��.
 * 
 * @author huangming
 *
 */
public class DGBHDPrinterWeb extends ActionsoftWeb {

	/**
	 * ��ѯ���ܳ��ⵥͷ.
	 */
	private static final String QUERY_DGCK_HEAD = "SELECT * FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * ��ѯ���ܳ��ⵥ��.
	 */
	private static final String QUERY_DGCK_BODY = "SELECT * FROM BO_BO_AKL_DGCK_S WHERE BINDID=? AND CKDM=? ORDER BY HWDM, XH";
	/**
	 * ��ѯ���ܳ�������вֿ�.
	 */
	private static final String QUERY_DGCK_CKDM = "SELECT DISTINCT CKDM FROM BO_BO_AKL_DGCK_S WHERE BINDID=?";
	/**
	 * ��ѯ���ϵ����.
	 */
	private static final String QUERY_WLXX_TJ = "SELECT TJ FROM BO_AKL_WLXX WHERE WLBH=?";
	/**
	 * ��ѯ���ϵ�����.
	 */
	private static final String QUERY_WLXX_ZL = "SELECT ZL FROM BO_AKL_WLXX WHERE WLBH=?";
	/**
	 * ���ܳ���Ԥ���ⵥ��ӡ����.
	 */
	private static final String QUERY_DGCK_YCKDDYCS = "SELECT YCKDDYCS FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * ���ܳ�����ⵥ��ӡ����.
	 */
	private static final String UPDATE_DGCK_YCKDDYCS = "UPDATE BO_BO_AKL_DGCK_P SET YCKDDYCS=? WHERE BINDID=?";

	public DGBHDPrinterWeb() {
		super();
	}

	public DGBHDPrinterWeb(UserContext arg0) {
		super(arg0);
	}

	public String paserHtml(String bindid) {

		Connection conn = null;
		PreparedStatement bodyPs = null;
		ResultSet bodyReset = null;
		
		// ��ǰҳ��
		int curPageIndex = 0;

		if (bindid == null)
			return "URL����������BINDID";

		StringBuilder htmlBuilder = new StringBuilder();

		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(date);

		try {
			conn = DBSql.open();

			// ��ӡ����
			Integer yckddycs = updatePrintCount(bindid, conn);

			// ��ȡ�ֿ�
			ArrayList<String> fkcklist = DAOUtil.getStringCollection(conn, QUERY_DGCK_CKDM, bindid);
			if (fkcklist.size() == 0) {
				return "�����ݴ棬�ڴ�ӡ";
			}

			// �����ֿ�
			for (String fkck : fkcklist) {
				BigDecimal tj = new BigDecimal(0);
				BigDecimal zl = new BigDecimal(0);
				double js = 0;
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				ArrayList<StringBuilder> trList = new ArrayList<StringBuilder>(50);

				// �����������
				fillMainInfo(conn, bindid, today, yckddycs, hashtable);

				try {
					bodyPs = conn.prepareStatement(QUERY_DGCK_BODY);
					bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid, fkck);

					int sl = 0;

					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					hashtable.put("SHRQ", dateFormat.format(new Date()));
					hashtable.put("RQ", dateFormat.format(new Date()));

					for (int row = 1; bodyReset.next(); row++) {
						// �����ӱ�����Ϣ
						trList.add(parseTableTRHtml(bodyReset, row));

						BigDecimal tjBig = DAOUtil.getBigDecimalOrNull(conn, QUERY_WLXX_TJ, bodyReset.getString("WLBH"));
						BigDecimal zlBig = DAOUtil.getBigDecimalOrNull(conn, QUERY_WLXX_ZL, bodyReset.getString("WLBH"));
						if (tjBig != null) {
							BigDecimal currentRowTjBig = tjBig.multiply(new BigDecimal(bodyReset.getString("SFSL")));
							tj = tj.add(currentRowTjBig);
						}
						if (zlBig != null) {
							BigDecimal currentRowZlBig = zlBig.multiply(new BigDecimal(bodyReset.getString("SFSL")));
							zl = zl.add(currentRowZlBig);
						}

						hashtable.put("FHDZ", bodyReset.getString("CKMC")); // ������ַ
						sl += bodyReset.getInt("SFSL");
					}

					hashtable.put("HJ", String.valueOf(sl));
					hashtable.put("TJ", String.valueOf(tj));
					hashtable.put("ZL", String.valueOf(zl));
					hashtable.put("JS", String.valueOf(js == 0 ? "" : js));

					// ��10����¼һҳ�ķ�ʽ����
					StringBuilder pageTrSb = new StringBuilder();
					for (int i = 0; i < trList.size(); i++) {
						if (i != 0 && i % PrintUtil.PAGE_SIZE == 0) {
							hashtable.put("SubReport", pageTrSb.toString());
							// ��ǰҳ��
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							htmlBuilder.append(getHtmlPage("���ܳ��ⱸ����.html", hashtable));
							pageTrSb = new StringBuilder();
						}
						pageTrSb.append(trList.get(i));
					}

					if (pageTrSb.length() != 0) {
						hashtable.put("SubReport", pageTrSb.toString());
						// ��ǰҳ��
						hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
						htmlBuilder.append(getHtmlPage("���ܳ��ⱸ����.html", hashtable));
					}

					if (htmlBuilder.length() == 0) {
						if (hashtable.size() > 10) {
							// ��ǰҳ��
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							// ����м�¼
							hashtable.put("SubReport", "");
							htmlBuilder.append(getHtmlPage("���ܳ��ⱸ����.html", hashtable));
						} else {
							return "�����ݴ�!";
						}
					}
				} finally {
					DBSql.close(bodyPs, bodyReset);
				}
			}
			return htmlBuilder.toString().replaceAll("\\{\\[PAGESIZE\\]\\}", String.valueOf(curPageIndex));
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "ϵͳ��������,����ϵ����Ա!");
			return "ϵͳ��������,����ϵ����Ա!";
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ���������Ϣ.
	 * 
	 * @param bindid
	 * @param headReset
	 * @param today
	 * @param yckddycs
	 * @param hashtable
	 * @throws SQLException
	 */
	private void fillMainInfo(Connection conn, String bindid, String today, Integer yckddycs, Hashtable<String, String> hashtable)
			throws SQLException {

		PreparedStatement headPs = null;
		ResultSet headReset = null;

		try {
			headPs = conn.prepareStatement(QUERY_DGCK_HEAD);
			headReset = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);

			if (headReset.next()) {
				try {
					hashtable.put("ID", today + headReset.getString("CKDH").substring(headReset.getString("CKDH").length() - 3));
				} catch (Exception e) {
					hashtable.put("ID", "");
				}
				hashtable.put("KHMC", PrintUtil.parseNull(headReset.getString("KHMC")));// �ͻ�����
				hashtable.put("KHBH", PrintUtil.parseNull(headReset.getString("KHBH")));// �ͻ����
				hashtable.put("CKDH", PrintUtil.parseNull(headReset.getString("CKDH")));// ���ⵥ��
				hashtable.put("DJRQ", PrintUtil.parseNull(headReset.getString("DJRQ")));// ��������
				hashtable.put("XSRQ", PrintUtil.parseNull(headReset.getString("XSRQ")));// ��������
				hashtable.put("SHDZ1", PrintUtil.parseNull(headReset.getString("SHDZ1")));// �ջ���ַ1
				hashtable.put("LXRX1", PrintUtil.parseNull(headReset.getString("LXRX1")));// ��ϵ��1
				hashtable.put("LXRDH1", PrintUtil.parseNull(headReset.getString("LXRDH1")));// ��ϵ�˵绰1
				hashtable.put("SHDZ2", PrintUtil.parseNull(headReset.getString("SHDZ2")));// �ջ���ַ2
				hashtable.put("LXRX2", PrintUtil.parseNull(headReset.getString("LXRX2")));// ��ϵ��2
				hashtable.put("LXRDH2", PrintUtil.parseNull(headReset.getString("LXRDH2")));// ��ϵ�˵绰2
				hashtable.put("XSYWLX", PrintUtil.parseNull(headReset.getString("XSYWLX")));// ����ҵ������
				hashtable.put("XSFS", PrintUtil.parseNull(headReset.getString("XSFS")));// ���۷�ʽ
				hashtable.put("SKRQ", PrintUtil.parseNull(headReset.getString("SKRQ")));// �տ�����
				hashtable.put("YFJSFS", PrintUtil.parseNull(DictionaryUtil.parseYFJSFSToName(headReset.getString("YFJSFS"))));// �˷ѽ��㷽ʽ
				hashtable.put("YDLX", PrintUtil.parseNull(headReset.getString("YDLX")));// Դ������
				hashtable.put("ZY", PrintUtil.parseNull(headReset.getString("ZY")));// ժҪ
				hashtable.put("SKTJ", PrintUtil.parseNull(headReset.getString("SKTJ")));// �տ�����
				hashtable.put("SFYY", PrintUtil.parseNull(headReset.getString("SFYY")));// �Ƿ�ԤԼ
				hashtable.put("ZT", PrintUtil.parseNull(headReset.getString("ZT")));// ״̬
				hashtable.put("QSD", PrintUtil.parseNull(headReset.getString("QSD")));// ǩ�յ�
				hashtable.put("FHR", PrintUtil.parseNull(headReset.getString("FHR")));// ������
				hashtable.put("BM", PrintUtil.parseNull(headReset.getString("BM")));// �ͻ�����
				hashtable.put("ZDR", PrintUtil.parseNull(headReset.getString("ZDR")));// �Ƶ���
				hashtable.put("XSDH", PrintUtil.parseNull(headReset.getString("XSDH")));// ���۵���
				hashtable.put("KHBMBM", PrintUtil.parseNull(headReset.getString("KHBMBM")));// �ͻ����ű���
				hashtable.put("YSFS", PrintUtil.parseNull(DictionaryUtil.parseSHFSToName(headReset.getString("YSFS"))));// ���䷽ʽ
				hashtable.put("JS", ""); // ����
				hashtable.put("SJTJ", "");// ʵ�����
				hashtable.put("SJZL", "");// ʵ������
				hashtable.put("FHRDH", PrintUtil.parseNull(headReset.getString("FHRDH")));// �����˵绰
				hashtable.put("ZDR", PrintUtil.parseNull(headReset.getString("ZDR"))); // �Ƶ���

				hashtable.put("FHGS", "��������"); // ������˾
				hashtable.put("YCKDDYCS", String.valueOf(yckddycs)); // �Ѵ�ӡ����
				hashtable.put("bindid", bindid);
				hashtable.put("sid", getSIDFlag());
			}
		} finally {
			DBSql.close(headPs, headReset);
		}
	}

	/**
	 * ת����Ϣ��TR��ǩ.
	 * 
	 * @param bodyReset
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private StringBuilder parseTableTRHtml(ResultSet bodyReset, int row) throws SQLException {
		StringBuilder sb = new StringBuilder();
		// ��ȡ�ӱ�����
		sb.append("<tr class='subtable_tr'>");
		sb.append(PrintUtil.formatBodyRowRecord(row));// �к�
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("KHCGDH")));// �ͻ��ɹ�����
//		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLBH")));// ���ϱ��
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("XH")));// �ͺ�
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseSXToName(bodyReset.getString("SX"))));// ����
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseJLDWToName(bodyReset.getString("DW"))));// ������λ
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLMC")));// ��������
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("YFSL")));// ʵ������
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("HWDM")));// ��λ����
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("PCH")));// ���κ�
		// sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("YFSL")));// Ӧ������
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("SFSL")));// ʵ������
		sb.append("</tr>");
		return sb;
	}

	/**
	 * ���²���ȡ��ӡ����.
	 * 
	 * @param bindid
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private Integer updatePrintCount(String bindid, Connection conn) throws SQLException {
		/**
		 * ��ȡ��ӡ���������´�ӡ����.
		 */
		Integer yckddycs = DAOUtil.getIntOrNull(conn, QUERY_DGCK_YCKDDYCS, bindid);
		if (yckddycs == null) {
			yckddycs = 0;
		}
		DAOUtil.executeUpdate(conn, UPDATE_DGCK_YCKDDYCS, ++yckddycs, bindid);
		return yckddycs;
	}
}
