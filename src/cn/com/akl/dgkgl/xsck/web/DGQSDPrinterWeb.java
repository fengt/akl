package cn.com.akl.dgkgl.xsck.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class DGQSDPrinterWeb extends ActionsoftWeb {

	private static final String QUERY_QSD_HEAD = "SELECT QSDH,TYDH,SHDW,SHFZR,SHFZRDH,SHDZ, KHCGDH,SHRQ FROM BO_AKL_QSD_P WHERE BINDID=?";
	private static final String QUERY_QSD_BODY = "SELECT KHSPBH,CPMC,XH,SSSL,BZ FROM BO_AKL_QSD_S WHERE BINDID=?";

	public DGQSDPrinterWeb() {
		super();
	}

	public DGQSDPrinterWeb(UserContext arg0) {
		super(arg0);
	}

	public String paserHtml(String bindid) {

		Connection conn = null;
		PreparedStatement headPs = null;
		PreparedStatement bodyPs = null;
		ResultSet headReset = null;
		ResultSet bodyReset = null;

		try {
			conn = DBSql.open();
			headPs = conn.prepareStatement(QUERY_QSD_HEAD);
			bodyPs = conn.prepareStatement(QUERY_QSD_BODY);
			headReset = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);
			bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);

			Hashtable<String, String> hashtable = new Hashtable<String, String>();
			int sumSSSL = 0;
			// 获取主表数据
			if (headReset.next()) {
				String qsdh = headReset.getString("QSDH");
				String tydh = headReset.getString("TYDH");
				String shdw = headReset.getString("SHDW");
				String shfzr = headReset.getString("SHFZR");
				String shfzrdh = headReset.getString("SHFZRDH");
				String shdz = headReset.getString("SHDZ");
				String khcgdh = headReset.getString("KHCGDH");
				Date shrq = headReset.getDate("SHRQ");
				String shrqStr = "";
				if (shrq != null) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					shrqStr = dateFormat.format(shrq);
				}
				StringBuilder sb = new StringBuilder();
				while (bodyReset.next()) {
					// 获取子表数据
					sb.append("<tr class='subtable_tr'>");
					String khspbh = bodyReset.getString("KHSPBH");
					String cpmc = bodyReset.getString("CPMC");
					String xh = bodyReset.getString("XH");
					int sssl = bodyReset.getInt("SSSL");
					String bz = bodyReset.getString("BZ");
					sumSSSL += sssl;
					sb.append(formatBodyRowRecord(khcgdh, khspbh, cpmc, xh, String.valueOf(sssl), shrqStr, bz));
					sb.append("</tr>");
				}

				hashtable.put("QSDH", parseNull(qsdh));
				hashtable.put("TYDW", parseNull(tydh));
				hashtable.put("SHDW", parseNull(shdw));
				hashtable.put("SHFZR", parseNull(shfzr));
				hashtable.put("SHFZRDH", parseNull(shfzrdh));
				hashtable.put("SHDZ", parseNull(shdz));
				hashtable.put("LJSL", String.valueOf(sumSSSL));
				hashtable.put("SubReport", sb.toString());
			} else {
				hashtable.put("QSDH", "");
				hashtable.put("TYDW", "");
				hashtable.put("SHDW", "");
				hashtable.put("SHFZR", "");
				hashtable.put("SHFZRDH", "");
				hashtable.put("SHDZ", "");
				hashtable.put("LJSL", "");
				hashtable.put("SubReport", "");
			}
			hashtable.put("bindid", bindid);
			hashtable.put("sid", super.getSIDFlag());

			return getHtmlPage("代管出库签收单.html", hashtable);
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "系统出现问题,请联系管理员!");
			return "系统出现问题,请联系管理员!";
		} finally {
			DBSql.close(bodyPs, bodyReset);
			DBSql.close(conn, headPs, headReset);
		}
	}

	/**
	 * 单身行记录装换成字符串
	 * 
	 * @param cgdh
	 * @param khbh
	 * @param cpmc
	 * @param xh
	 * @param sl
	 * @param rq
	 * @param bz
	 * @return
	 */
	public String formatBodyRowRecord(String... args) {
		StringBuilder sb = new StringBuilder();
		for (String string : args) {
			if (string != null)
				sb.append("<td class='subtable_body_td'>").append(string).append("</td>");
			else
				sb.append("<td class='subtable_body_td'></td>");
		}
		return sb.toString();
	}

	public String parseNull(String str) {
		return str == null ? "" : str;
	}

}
