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
 * 代管预出库单打印Web类.
 * 
 * @author huangming
 *
 */
public class DGBHDPrinterWeb extends ActionsoftWeb {

	/**
	 * 查询代管出库单头.
	 */
	private static final String QUERY_DGCK_HEAD = "SELECT * FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * 查询代管出库单身.
	 */
	private static final String QUERY_DGCK_BODY = "SELECT * FROM BO_BO_AKL_DGCK_S WHERE BINDID=? AND CKDM=? ORDER BY HWDM, XH";
	/**
	 * 查询代管出库的所有仓库.
	 */
	private static final String QUERY_DGCK_CKDM = "SELECT DISTINCT CKDM FROM BO_BO_AKL_DGCK_S WHERE BINDID=?";
	/**
	 * 查询物料的体积.
	 */
	private static final String QUERY_WLXX_TJ = "SELECT TJ FROM BO_AKL_WLXX WHERE WLBH=?";
	/**
	 * 查询物料的重量.
	 */
	private static final String QUERY_WLXX_ZL = "SELECT ZL FROM BO_AKL_WLXX WHERE WLBH=?";
	/**
	 * 代管出库预出库单打印次数.
	 */
	private static final String QUERY_DGCK_YCKDDYCS = "SELECT YCKDDYCS FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * 代管出库出库单打印次数.
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
		
		// 当前页码
		int curPageIndex = 0;

		if (bindid == null)
			return "URL不包含参数BINDID";

		StringBuilder htmlBuilder = new StringBuilder();

		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(date);

		try {
			conn = DBSql.open();

			// 打印次数
			Integer yckddycs = updatePrintCount(bindid, conn);

			// 获取仓库
			ArrayList<String> fkcklist = DAOUtil.getStringCollection(conn, QUERY_DGCK_CKDM, bindid);
			if (fkcklist.size() == 0) {
				return "请点击暂存，在打印";
			}

			// 遍历仓库
			for (String fkck : fkcklist) {
				BigDecimal tj = new BigDecimal(0);
				BigDecimal zl = new BigDecimal(0);
				double js = 0;
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				ArrayList<StringBuilder> trList = new ArrayList<StringBuilder>(50);

				// 填充主表数据
				fillMainInfo(conn, bindid, today, yckddycs, hashtable);

				try {
					bodyPs = conn.prepareStatement(QUERY_DGCK_BODY);
					bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid, fkck);

					int sl = 0;

					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					hashtable.put("SHRQ", dateFormat.format(new Date()));
					hashtable.put("RQ", dateFormat.format(new Date()));

					for (int row = 1; bodyReset.next(); row++) {
						// 加载子表单身信息
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

						hashtable.put("FHDZ", bodyReset.getString("CKMC")); // 发货地址
						sl += bodyReset.getInt("SFSL");
					}

					hashtable.put("HJ", String.valueOf(sl));
					hashtable.put("TJ", String.valueOf(tj));
					hashtable.put("ZL", String.valueOf(zl));
					hashtable.put("JS", String.valueOf(js == 0 ? "" : js));

					// 按10条记录一页的方式来做
					StringBuilder pageTrSb = new StringBuilder();
					for (int i = 0; i < trList.size(); i++) {
						if (i != 0 && i % PrintUtil.PAGE_SIZE == 0) {
							hashtable.put("SubReport", pageTrSb.toString());
							// 当前页码
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							htmlBuilder.append(getHtmlPage("代管出库备货单.html", hashtable));
							pageTrSb = new StringBuilder();
						}
						pageTrSb.append(trList.get(i));
					}

					if (pageTrSb.length() != 0) {
						hashtable.put("SubReport", pageTrSb.toString());
						// 当前页码
						hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
						htmlBuilder.append(getHtmlPage("代管出库备货单.html", hashtable));
					}

					if (htmlBuilder.length() == 0) {
						if (hashtable.size() > 10) {
							// 当前页码
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							// 如果有记录
							hashtable.put("SubReport", "");
							htmlBuilder.append(getHtmlPage("代管出库备货单.html", hashtable));
						} else {
							return "请点击暂存!";
						}
					}
				} finally {
					DBSql.close(bodyPs, bodyReset);
				}
			}
			return htmlBuilder.toString().replaceAll("\\{\\[PAGESIZE\\]\\}", String.valueOf(curPageIndex));
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "系统出现问题,请联系管理员!");
			return "系统出现问题,请联系管理员!";
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 填充主表信息.
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
				hashtable.put("KHMC", PrintUtil.parseNull(headReset.getString("KHMC")));// 客户名称
				hashtable.put("KHBH", PrintUtil.parseNull(headReset.getString("KHBH")));// 客户编号
				hashtable.put("CKDH", PrintUtil.parseNull(headReset.getString("CKDH")));// 出库单号
				hashtable.put("DJRQ", PrintUtil.parseNull(headReset.getString("DJRQ")));// 单据日期
				hashtable.put("XSRQ", PrintUtil.parseNull(headReset.getString("XSRQ")));// 销售日期
				hashtable.put("SHDZ1", PrintUtil.parseNull(headReset.getString("SHDZ1")));// 收货地址1
				hashtable.put("LXRX1", PrintUtil.parseNull(headReset.getString("LXRX1")));// 联系人1
				hashtable.put("LXRDH1", PrintUtil.parseNull(headReset.getString("LXRDH1")));// 联系人电话1
				hashtable.put("SHDZ2", PrintUtil.parseNull(headReset.getString("SHDZ2")));// 收货地址2
				hashtable.put("LXRX2", PrintUtil.parseNull(headReset.getString("LXRX2")));// 联系人2
				hashtable.put("LXRDH2", PrintUtil.parseNull(headReset.getString("LXRDH2")));// 联系人电话2
				hashtable.put("XSYWLX", PrintUtil.parseNull(headReset.getString("XSYWLX")));// 销售业务类型
				hashtable.put("XSFS", PrintUtil.parseNull(headReset.getString("XSFS")));// 销售方式
				hashtable.put("SKRQ", PrintUtil.parseNull(headReset.getString("SKRQ")));// 收款日期
				hashtable.put("YFJSFS", PrintUtil.parseNull(DictionaryUtil.parseYFJSFSToName(headReset.getString("YFJSFS"))));// 运费结算方式
				hashtable.put("YDLX", PrintUtil.parseNull(headReset.getString("YDLX")));// 源单类型
				hashtable.put("ZY", PrintUtil.parseNull(headReset.getString("ZY")));// 摘要
				hashtable.put("SKTJ", PrintUtil.parseNull(headReset.getString("SKTJ")));// 收款条件
				hashtable.put("SFYY", PrintUtil.parseNull(headReset.getString("SFYY")));// 是否预约
				hashtable.put("ZT", PrintUtil.parseNull(headReset.getString("ZT")));// 状态
				hashtable.put("QSD", PrintUtil.parseNull(headReset.getString("QSD")));// 签收单
				hashtable.put("FHR", PrintUtil.parseNull(headReset.getString("FHR")));// 发货人
				hashtable.put("BM", PrintUtil.parseNull(headReset.getString("BM")));// 客户部门
				hashtable.put("ZDR", PrintUtil.parseNull(headReset.getString("ZDR")));// 制单人
				hashtable.put("XSDH", PrintUtil.parseNull(headReset.getString("XSDH")));// 销售单号
				hashtable.put("KHBMBM", PrintUtil.parseNull(headReset.getString("KHBMBM")));// 客户部门编码
				hashtable.put("YSFS", PrintUtil.parseNull(DictionaryUtil.parseSHFSToName(headReset.getString("YSFS"))));// 运输方式
				hashtable.put("JS", ""); // 件数
				hashtable.put("SJTJ", "");// 实际体积
				hashtable.put("SJZL", "");// 实际重量
				hashtable.put("FHRDH", PrintUtil.parseNull(headReset.getString("FHRDH")));// 发货人电话
				hashtable.put("ZDR", PrintUtil.parseNull(headReset.getString("ZDR"))); // 制单人

				hashtable.put("FHGS", "北京亚昆"); // 发货公司
				hashtable.put("YCKDDYCS", String.valueOf(yckddycs)); // 已打印次数
				hashtable.put("bindid", bindid);
				hashtable.put("sid", getSIDFlag());
			}
		} finally {
			DBSql.close(headPs, headReset);
		}
	}

	/**
	 * 转换信息成TR标签.
	 * 
	 * @param bodyReset
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private StringBuilder parseTableTRHtml(ResultSet bodyReset, int row) throws SQLException {
		StringBuilder sb = new StringBuilder();
		// 获取子表数据
		sb.append("<tr class='subtable_tr'>");
		sb.append(PrintUtil.formatBodyRowRecord(row));// 行号
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("KHCGDH")));// 客户采购单号
//		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLBH")));// 物料编号
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("XH")));// 型号
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseSXToName(bodyReset.getString("SX"))));// 属性
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseJLDWToName(bodyReset.getString("DW"))));// 计量单位
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLMC")));// 物料名称
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("YFSL")));// 实发数量
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("HWDM")));// 货位代码
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("PCH")));// 批次号
		// sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("YFSL")));// 应发数量
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("SFSL")));// 实发数量
		sb.append("</tr>");
		return sb;
	}

	/**
	 * 更新并获取打印次数.
	 * 
	 * @param bindid
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private Integer updatePrintCount(String bindid, Connection conn) throws SQLException {
		/**
		 * 获取打印次数并更新打印次数.
		 */
		Integer yckddycs = DAOUtil.getIntOrNull(conn, QUERY_DGCK_YCKDDYCS, bindid);
		if (yckddycs == null) {
			yckddycs = 0;
		}
		DAOUtil.executeUpdate(conn, UPDATE_DGCK_YCKDDYCS, ++yckddycs, bindid);
		return yckddycs;
	}
}
