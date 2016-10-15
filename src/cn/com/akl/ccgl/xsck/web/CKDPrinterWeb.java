package cn.com.akl.ccgl.xsck.web;

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
public class CKDPrinterWeb extends ActionsoftWeb {

	/**
	 * ��ѯ���ⵥ��ͷ.
	 */
	private static final String QUERY_CKD_HEAD = "SELECT * FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ���ⵥ����.
	 */
	private static final String QUERY_CKD_BODY = "SELECT WLH, XH, WLMC, SX, JLDW, FHKFMC, SUM(ISNULL(SJSL,0)) as SJSL FROM BO_AKL_CKD_BODY WHERE BINDID=? AND FHKFBH=? GROUP BY WLH, XH, WLMC, SX, JLDW, FHKFMC ORDER BY XH";
	/**
	 * ��ѯ���ⵥ�����ֿ�.
	 */
	private static final String QUERY_CKD_FHCK = "SELECT DISTINCT FHKFBH FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	/**
	 * ��ѯ���ⵥ��Ԥ���ⵥ�Ĵ�ӡ����.
	 */
	private static final String QUERY_CKD_CKDDYCS = "SELECT CKDDYCS FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/**
	 * ���³��ⵥ�е�Ԥ���ⵥ��ӡ����.
	 */
	private static final String UPDATE_CKD_CKDDYCS = "UPDATE BO_AKL_CKD_HEAD SET CKDDYCS=? WHERE BINDID=?";

	private static final int PAGE_SIZE = 20;

	public CKDPrinterWeb() {
		super();
	}

	public CKDPrinterWeb(UserContext arg0) {
		super(arg0);
	}

	public String paserHtml(String bindid) {

		Connection conn = null;
		PreparedStatement bodyPs = null;
		ResultSet bodyReset = null;

		if (bindid == null)
			return "URL����������BINDID";

		// ��ǰҳ��
		int curPageIndex = 0;

		StringBuilder htmlBuilder = new StringBuilder();

		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(date);

		try {
			conn = DBSql.open();

			// ��ӡ����
			Integer yckddycs = updatePrintCount(bindid, conn);

			// ��ȡ�ֿ�
			ArrayList<String> fkcklist = DAOUtil.getStringCollection(conn, QUERY_CKD_FHCK, bindid);
			if (fkcklist.size() == 0) {
				return "�����ݴ棬�ڴ�ӡ";
			}

			// �����ֿ�
			for (String fkck : fkcklist) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				ArrayList<StringBuilder> trList = new ArrayList<StringBuilder>(50);

				// �����������
				fillMainInfo(conn, bindid, today, yckddycs, hashtable);

				try {
					bodyPs = conn.prepareStatement(QUERY_CKD_BODY);
					bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid, fkck);

					int sl = 0;

					for (int row = 1; bodyReset.next(); row++) {
						// �����ӱ�����Ϣ
						trList.add(parseTableTRHtml(bodyReset, row, hashtable.get("KHCGDH"), hashtable.get("KH")));

						hashtable.put("FHDZ", bodyReset.getString("FHKFMC"));
						sl += bodyReset.getInt("SJSL");
					}

					hashtable.put("HJ", String.valueOf(sl));

					// ��10����¼һҳ�ķ�ʽ����
					StringBuilder pageTrSb = new StringBuilder();
					for (int i = 0; i < trList.size(); i++) {
						if (i != 0 && i % PAGE_SIZE == 0) {
							hashtable.put("SubReport", pageTrSb.toString());
							// ��ǰҳ��
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							htmlBuilder.append(getHtmlPage("����������ⵥ.html", hashtable));
							pageTrSb = new StringBuilder();
						}
						pageTrSb.append(trList.get(i));
					}

					if (pageTrSb.length() != 0) {
						// ��ǰҳ��
						hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
						hashtable.put("SubReport", pageTrSb.toString());
						htmlBuilder.append(getHtmlPage("����������ⵥ.html", hashtable));
					}

					if (htmlBuilder.length() == 0) {
						if (hashtable.size() > PAGE_SIZE) {
							// ��ǰҳ��
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							// ����м�¼
							hashtable.put("SubReport", "");
							htmlBuilder.append(getHtmlPage("����������ⵥ.html", hashtable));
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
			headPs = conn.prepareStatement(QUERY_CKD_HEAD);
			headReset = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);

			if (headReset.next()) {
				try {
					hashtable.put("ID", today + headReset.getString("CKDH").substring(headReset.getString("CKDH").length() - 3));
				} catch (Exception e) {
					hashtable.put("ID", "");
				}
				hashtable.put("KH", PrintUtil.parseNull(headReset.getString("KH"))); // �ͻ�����
				hashtable.put("KHMC", PrintUtil.parseNull(headReset.getString("KHMC"))); // �ͻ�����
				hashtable.put("FHRQ", PrintUtil.parseNull(headReset.getString("FHRQ"))); // ��������
				hashtable.put("CJSJ", PrintUtil.parseNull(headReset.getString("CJSJ"))); // ����ʱ��
				hashtable.put("CJXM", PrintUtil.parseNull(headReset.getString("CJXM"))); // ������
				hashtable.put("TJ", PrintUtil.parseNull(headReset.getString("TJ"))); // ���
				hashtable.put("SJTJ", PrintUtil.parseNull(headReset.getString("SJTJ"))); // ʵ�����
				hashtable.put("ZL", PrintUtil.parseNull(headReset.getString("ZL"))); // ����
				hashtable.put("SJZL", PrintUtil.parseNull(headReset.getString("SJZL"))); // ʵ������
				hashtable.put("JS", PrintUtil.parseNull(headReset.getString("JS"))); // ����
				hashtable.put("BHR", PrintUtil.parseNull(headReset.getString("BHR"))); // �����ˣ�����ⷿ������Ա��
				hashtable.put("FHR", PrintUtil.parseNull(headReset.getString("FHR"))); // �����ˣ���ǰ������
				hashtable.put("ZXR", PrintUtil.parseNull(headReset.getString("ZXR"))); // װ���ˣ�����ⷿ������Ա��
				hashtable.put("XSDDH", PrintUtil.parseNull(headReset.getString("XSDDH"))); // ���۶�����
				hashtable.put("KHCGDH", PrintUtil.parseNull(headReset.getString("KHCGDH"))); // �ͻ��ɹ�����
				hashtable.put("CXFZR", PrintUtil.parseNull(headReset.getString("CXFZR"))); // ����������
				hashtable.put("CXDH", PrintUtil.parseNull(headReset.getString("CXDH"))); // ���������˵绰
				hashtable.put("CXSJ", PrintUtil.parseNull(headReset.getString("CXSJ"))); // �����������ֻ�
				hashtable.put("CXEMAIL", PrintUtil.parseNull(headReset.getString("CXEMAIL"))); // ����������E-mail
				hashtable.put("WFFZR", PrintUtil.parseNull(headReset.getString("WFFZR"))); // �ҷ�������
				hashtable.put("WFDH", PrintUtil.parseNull(headReset.getString("WFDH"))); // �ҷ������˵绰
				hashtable.put("WFSJ", PrintUtil.parseNull(headReset.getString("WFSJ"))); // �ҷ��������ֻ�
				hashtable.put("WFEMAIL", PrintUtil.parseNull(headReset.getString("WFEMAIL"))); // �ҷ�������E-mail
				hashtable.put("XDRQ", PrintUtil.parseNull(headReset.getString("XDRQ"))); // �µ�����
				hashtable.put("QWJHR", PrintUtil.parseNull(headReset.getString("QWJHR"))); // ������������
				hashtable.put("ZWJHR", PrintUtil.parseNull(headReset.getString("ZWJHR"))); // ����������
				hashtable.put("CK", PrintUtil.parseNull(headReset.getString("CK"))); // �ֿ�
				hashtable.put("JHDZ", PrintUtil.parseNull(headReset.getString("JHDZ"))); // ������ַ
				hashtable.put("KFLXR", PrintUtil.parseNull(headReset.getString("KFLXR"))); // �ֿ���ϵ��
				hashtable.put("SHFS", PrintUtil.parseNull(DictionaryUtil.parseSHFSToName(headReset.getString("SHFS")))); // �ͻ���ʽ
				hashtable.put("SFYY", PrintUtil.parseNull(headReset.getString("SFYY"))); // �Ƿ�ԤԼ
				hashtable.put("SFZX", PrintUtil.parseNull(headReset.getString("SFZX"))); // �Ƿ�װ��
				hashtable.put("CKLXRDH", PrintUtil.parseNull(headReset.getString("CKLXRDH"))); // �ֿ���ϵ�˵绰
				hashtable.put("CKLXRSJ", PrintUtil.parseNull(headReset.getString("CKLXRSJ"))); // �ֿ���ϵ���ֻ�
				hashtable.put("CKLXREMAIL", PrintUtil.parseNull(headReset.getString("CKLXREMAIL"))); // �ֿ���ϵ��E-MIAL
				hashtable.put("CKDH", PrintUtil.parseNull(headReset.getString("CKDH"))); // ���ⵥ��
				hashtable.put("BYCKDH", PrintUtil.parseNull(headReset.getString("BYCKDH"))); // ���õ���
				hashtable.put("YSHJ", PrintUtil.parseNull(headReset.getString("YSHJ"))); // Ӧ�պϼ�
				hashtable.put("CKZT", PrintUtil.parseNull(headReset.getString("CKZT"))); // ����״̬
				hashtable.put("QSD", PrintUtil.parseNull(headReset.getString("QSD"))); // ǩ�յ�
				hashtable.put("SFYS", PrintUtil.parseNull(headReset.getString("SFYS"))); // �Ƿ�Ԥ��
				hashtable.put("CYZT", PrintUtil.parseNull(headReset.getString("CYZT"))); // ����״̬
				hashtable.put("JHHDH", PrintUtil.parseNull(headReset.getString("JHHDH"))); // �軹������
				hashtable.put("JHHDB", PrintUtil.parseNull(headReset.getString("JHHDB"))); // �軹������
				hashtable.put("ZDR", this.getContext().getUserModel().getUserName()); // �Ƶ���
				hashtable.put("bindid", bindid);
				hashtable.put("sid", super.getSIDFlag());
				hashtable.put("FHGS", "��������"); // ������˾
				hashtable.put("CKDDYCS", yckddycs.toString()); // ������˾
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
	private StringBuilder parseTableTRHtml(ResultSet bodyReset, int row, String khcgdh, String khbm) throws SQLException {
		StringBuilder sb = new StringBuilder();
		// ��ȡ�ӱ�����
		sb.append("<tr class='subtable_tr'>");
		sb.append(PrintUtil.formatBodyRowRecord(row++));// �к�
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLH")));// ���Ϻ�
		sb.append(PrintUtil.formatBodyRowRecord(khbm));// �ͻ�����
		sb.append(PrintUtil.formatBodyRowRecord(khcgdh));// �ͻ��ɹ�����
		sb.append(PrintUtil.formatBodyRowRecordNoWarp(bodyReset.getString("XH")));// �ͺ�
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseSXToName(bodyReset.getString("SX"))));// ����
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseJLDWToName(bodyReset.getString("JLDW"))));// ������λ
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("SJSL")));// ʵ������
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLMC")));// ��������
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
		Integer yckddycs = DAOUtil.getIntOrNull(conn, QUERY_CKD_CKDDYCS, bindid);
		if (yckddycs == null) {
			yckddycs = 0;
		}
		DAOUtil.executeUpdate(conn, UPDATE_CKD_CKDDYCS, ++yckddycs, bindid);
		return yckddycs;
	}
}
