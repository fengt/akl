package cn.com.akl.ccgl.xsck.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
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
 * 打印签收单的Web处理类.
 * 
 * @author huangming
 *
 */
public class QSDPrinterWeb extends ActionsoftWeb {

	/**
	 * 查询签收单头.
	 */
	private static final String QUERY_QSD_HEAD = "SELECT * FROM BO_AKL_QSD_P WHERE BINDID=?";
	/**
	 * 查询签收单身.
	 */
	private static final String QUERY_QSD_BODY = "SELECT * FROM BO_AKL_QSD_S WHERE BINDID=?";
	/**
	 * 签收单打印次数.
	 */
	private static final String QUERY_QSD_QSDDYCS = "SELECT QSDDYCS FROM BO_AKL_QSD_P WHERE BINDID=?";
	/**
	 * 更新签收单的打印次数.
	 */
	private static final String UPDATE_QSD_QSDDYCS = "UPDATE BO_AKL_QSD_P SET QSDDYCS=? WHERE BINDID=?";

	public QSDPrinterWeb() {
		super();
	}

	public QSDPrinterWeb(UserContext arg0) {
		super(arg0);
	}

	public String paserHtml(String bindid) {

		if (bindid == null)
			return "请点击暂存，再打印";

		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(date);

		Connection conn = null;
		PreparedStatement headPs = null;
		PreparedStatement bodyPs = null;
		ResultSet headReset = null;
		ResultSet bodyReset = null;

		try {
			conn = DBSql.open();

			/**
			 * 获取打印次数并更新打印次数.
			 */
			Integer qsddycs = DAOUtil.getIntOrNull(conn, QUERY_QSD_QSDDYCS, bindid);
			if (qsddycs == null) {
				qsddycs = 0;
			}
			DAOUtil.executeUpdate(conn, UPDATE_QSD_QSDDYCS, ++qsddycs, bindid);

			headPs = conn.prepareStatement(QUERY_QSD_HEAD);
			bodyPs = conn.prepareStatement(QUERY_QSD_BODY);
			headReset = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);
			bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);

			Hashtable<String, String> hashtable = new Hashtable<String, String>();
			int sumSSSL = 0;
			// 获取主表数据
			if (headReset.next()) {
				String qsdh = headReset.getString("QSDH");
				String ckdh = headReset.getString("CKDH");
				String tydh = headReset.getString("TYDH");
				String shdw = headReset.getString("SHDW");
				String shfzr = headReset.getString("SHFZR");
				String shfzrdh = headReset.getString("SHFZRDH");
				String shdz = headReset.getString("SHDZ");
				String ysfs = headReset.getString("YSFS");
				String yfjsfs = headReset.getString("YFJSFS");
				String bm = headReset.getString("BM");
				String shdz2 = headReset.getString("SHDZ2");
				String lxrdh2 = headReset.getString("LXRDH2");
				String lxrx2 = headReset.getString("LXRX2");
				Date shrq = headReset.getDate("SHRQ");
				Date qsrq = headReset.getDate("QSRQ");
				String bzHead = headReset.getString("BZ");
				String zyHead = headReset.getString("ZY");
				String qsrqStr = "";
				String shrqStr = "";
				if (shrq != null) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					shrqStr = dateFormat.format(shrq);
				}
				if (qsrq != null) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					qsrqStr = dateFormat.format(qsrq);
				}

				int row = 1;
				StringBuilder sb = new StringBuilder();
				while (bodyReset.next()) {
					// 获取子表数据
					sb.append("<tr class='subtable_tr'>");
//					String khspbh = bodyReset.getString("KHSPBH");
					String khcgdh = bodyReset.getString("KHCGDH");
					String wlh = bodyReset.getString("WLH");
					String cpmc = bodyReset.getString("CPMC");
					String xh = bodyReset.getString("XH");
					String dw = bodyReset.getString("DW");
					String dwsql = "select XLMC from BO_AKL_DATA_DICT_S where DLBM='026' and XLBM='"+dw+"'";
					dw = DBSql.getString(dwsql, "XLMC");
					int sssl = bodyReset.getInt("SSSL");
//					String bz = bodyReset.getString("BZ");
					sumSSSL += sssl;
					sb.append(PrintUtil.formatBodyRowRecord(row++));
					sb.append(PrintUtil.formatBodyRowRecord(khcgdh, wlh, cpmc, xh, dw));
					sb.append(PrintUtil.formatBodyRowRecord(sssl));
//					sb.append(PrintUtil.formatBodyRowRecord(bz));
					sb.append("</tr>");
				}

				hashtable.put("QSDDYCS", String.valueOf(qsddycs));
				try {
					hashtable.put("ID", today + ckdh.substring(ckdh.length() - 3));
				} catch (Exception e) {
					hashtable.put("ID", "");
				}
				
				hashtable.put("BM", PrintUtil.parseNull(bm));
				hashtable.put("BZ", PrintUtil.parseNull(bzHead));
				hashtable.put("ZY", PrintUtil.parseNull(zyHead));
				hashtable.put("CKDH", PrintUtil.parseNull(ckdh));
				hashtable.put("QSRQ", PrintUtil.parseNull(qsrqStr));
				hashtable.put("FHRQ", PrintUtil.parseNull(shrqStr));
				hashtable.put("QSDH", PrintUtil.parseNull(qsdh));
				hashtable.put("TYDW", PrintUtil.parseNull(tydh));
				hashtable.put("SHDW", PrintUtil.parseNull(shdw));
				hashtable.put("SHFZR", PrintUtil.parseNull(shfzr));
				hashtable.put("YSFS", PrintUtil.parseNull(DictionaryUtil.parseNoToChinese(ysfs)));
				hashtable.put("YFJSFS", PrintUtil.parseNull(DictionaryUtil.parseNoToChinese(yfjsfs)));
				hashtable.put("SHFZRDH", PrintUtil.parseNull(shfzrdh));
				hashtable.put("SHDZ", PrintUtil.parseNull(shdz));
				hashtable.put("SHDZ2", PrintUtil.parseNull(shdz2));
				hashtable.put("LXRDH2", PrintUtil.parseNull(lxrdh2));
				hashtable.put("LXRX2", PrintUtil.parseNull(lxrx2));
				hashtable.put("HJ", String.valueOf(sumSSSL));
				hashtable.put("SubReport", sb.toString());
				hashtable.put("bindid", bindid);
				hashtable.put("sid", super.getSIDFlag());

				return getHtmlPage("签收单打印表单.html", hashtable);
			} else {
				return "请点击暂存，在打印";
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "系统出现问题,请联系管理员!");
			return "系统出现问题,请联系管理员!";
		} finally {
			DBSql.close(bodyPs, bodyReset);
			DBSql.close(conn, headPs, headReset);
		}
	}

}
