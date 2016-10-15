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
 * 代管预出库单打印Web类.
 * 
 * @author huangming
 *
 */
public class CKDPrinterWeb extends ActionsoftWeb {

	/**
	 * 查询出库单单头.
	 */
	private static final String QUERY_CKD_HEAD = "SELECT * FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/**
	 * 查询出库单单身.
	 */
	private static final String QUERY_CKD_BODY = "SELECT WLH, XH, WLMC, SX, JLDW, FHKFMC, SUM(ISNULL(SJSL,0)) as SJSL FROM BO_AKL_CKD_BODY WHERE BINDID=? AND FHKFBH=? GROUP BY WLH, XH, WLMC, SX, JLDW, FHKFMC ORDER BY XH";
	/**
	 * 查询出库单发货仓库.
	 */
	private static final String QUERY_CKD_FHCK = "SELECT DISTINCT FHKFBH FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	/**
	 * 查询出库单中预出库单的打印次数.
	 */
	private static final String QUERY_CKD_CKDDYCS = "SELECT CKDDYCS FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/**
	 * 更新出库单中的预出库单打印次数.
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
			return "URL不包含参数BINDID";

		// 当前页码
		int curPageIndex = 0;

		StringBuilder htmlBuilder = new StringBuilder();

		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(date);

		try {
			conn = DBSql.open();

			// 打印次数
			Integer yckddycs = updatePrintCount(bindid, conn);

			// 获取仓库
			ArrayList<String> fkcklist = DAOUtil.getStringCollection(conn, QUERY_CKD_FHCK, bindid);
			if (fkcklist.size() == 0) {
				return "请点击暂存，在打印";
			}

			// 遍历仓库
			for (String fkck : fkcklist) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				ArrayList<StringBuilder> trList = new ArrayList<StringBuilder>(50);

				// 填充主表数据
				fillMainInfo(conn, bindid, today, yckddycs, hashtable);

				try {
					bodyPs = conn.prepareStatement(QUERY_CKD_BODY);
					bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid, fkck);

					int sl = 0;

					for (int row = 1; bodyReset.next(); row++) {
						// 加载子表单身信息
						trList.add(parseTableTRHtml(bodyReset, row, hashtable.get("KHCGDH"), hashtable.get("KH")));

						hashtable.put("FHDZ", bodyReset.getString("FHKFMC"));
						sl += bodyReset.getInt("SJSL");
					}

					hashtable.put("HJ", String.valueOf(sl));

					// 按10条记录一页的方式来做
					StringBuilder pageTrSb = new StringBuilder();
					for (int i = 0; i < trList.size(); i++) {
						if (i != 0 && i % PAGE_SIZE == 0) {
							hashtable.put("SubReport", pageTrSb.toString());
							// 当前页码
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							htmlBuilder.append(getHtmlPage("网销出库出库单.html", hashtable));
							pageTrSb = new StringBuilder();
						}
						pageTrSb.append(trList.get(i));
					}

					if (pageTrSb.length() != 0) {
						// 当前页码
						hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
						hashtable.put("SubReport", pageTrSb.toString());
						htmlBuilder.append(getHtmlPage("网销出库出库单.html", hashtable));
					}

					if (htmlBuilder.length() == 0) {
						if (hashtable.size() > PAGE_SIZE) {
							// 当前页码
							hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
							// 如果有记录
							hashtable.put("SubReport", "");
							htmlBuilder.append(getHtmlPage("网销出库出库单.html", hashtable));
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
			headPs = conn.prepareStatement(QUERY_CKD_HEAD);
			headReset = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);

			if (headReset.next()) {
				try {
					hashtable.put("ID", today + headReset.getString("CKDH").substring(headReset.getString("CKDH").length() - 3));
				} catch (Exception e) {
					hashtable.put("ID", "");
				}
				hashtable.put("KH", PrintUtil.parseNull(headReset.getString("KH"))); // 客户编码
				hashtable.put("KHMC", PrintUtil.parseNull(headReset.getString("KHMC"))); // 客户名称
				hashtable.put("FHRQ", PrintUtil.parseNull(headReset.getString("FHRQ"))); // 发货日期
				hashtable.put("CJSJ", PrintUtil.parseNull(headReset.getString("CJSJ"))); // 创建时间
				hashtable.put("CJXM", PrintUtil.parseNull(headReset.getString("CJXM"))); // 创建人
				hashtable.put("TJ", PrintUtil.parseNull(headReset.getString("TJ"))); // 体积
				hashtable.put("SJTJ", PrintUtil.parseNull(headReset.getString("SJTJ"))); // 实际体积
				hashtable.put("ZL", PrintUtil.parseNull(headReset.getString("ZL"))); // 重量
				hashtable.put("SJZL", PrintUtil.parseNull(headReset.getString("SJZL"))); // 实际重量
				hashtable.put("JS", PrintUtil.parseNull(headReset.getString("JS"))); // 件数
				hashtable.put("BHR", PrintUtil.parseNull(headReset.getString("BHR"))); // 备货人（出库库房工作人员）
				hashtable.put("FHR", PrintUtil.parseNull(headReset.getString("FHR"))); // 复核人（当前操作人
				hashtable.put("ZXR", PrintUtil.parseNull(headReset.getString("ZXR"))); // 装箱人（出库库房工作人员）
				hashtable.put("XSDDH", PrintUtil.parseNull(headReset.getString("XSDDH"))); // 销售订单号
				hashtable.put("KHCGDH", PrintUtil.parseNull(headReset.getString("KHCGDH"))); // 客户采购单号
				hashtable.put("CXFZR", PrintUtil.parseNull(headReset.getString("CXFZR"))); // 采销负责人
				hashtable.put("CXDH", PrintUtil.parseNull(headReset.getString("CXDH"))); // 采销负责人电话
				hashtable.put("CXSJ", PrintUtil.parseNull(headReset.getString("CXSJ"))); // 采销负责人手机
				hashtable.put("CXEMAIL", PrintUtil.parseNull(headReset.getString("CXEMAIL"))); // 采销负责人E-mail
				hashtable.put("WFFZR", PrintUtil.parseNull(headReset.getString("WFFZR"))); // 我方负责人
				hashtable.put("WFDH", PrintUtil.parseNull(headReset.getString("WFDH"))); // 我方负责人电话
				hashtable.put("WFSJ", PrintUtil.parseNull(headReset.getString("WFSJ"))); // 我方负责人手机
				hashtable.put("WFEMAIL", PrintUtil.parseNull(headReset.getString("WFEMAIL"))); // 我方负责人E-mail
				hashtable.put("XDRQ", PrintUtil.parseNull(headReset.getString("XDRQ"))); // 下单日期
				hashtable.put("QWJHR", PrintUtil.parseNull(headReset.getString("QWJHR"))); // 期望交货日期
				hashtable.put("ZWJHR", PrintUtil.parseNull(headReset.getString("ZWJHR"))); // 最晚交货日期
				hashtable.put("CK", PrintUtil.parseNull(headReset.getString("CK"))); // 仓库
				hashtable.put("JHDZ", PrintUtil.parseNull(headReset.getString("JHDZ"))); // 交货地址
				hashtable.put("KFLXR", PrintUtil.parseNull(headReset.getString("KFLXR"))); // 仓库联系人
				hashtable.put("SHFS", PrintUtil.parseNull(DictionaryUtil.parseSHFSToName(headReset.getString("SHFS")))); // 送货方式
				hashtable.put("SFYY", PrintUtil.parseNull(headReset.getString("SFYY"))); // 是否预约
				hashtable.put("SFZX", PrintUtil.parseNull(headReset.getString("SFZX"))); // 是否装箱
				hashtable.put("CKLXRDH", PrintUtil.parseNull(headReset.getString("CKLXRDH"))); // 仓库联系人电话
				hashtable.put("CKLXRSJ", PrintUtil.parseNull(headReset.getString("CKLXRSJ"))); // 仓库联系人手机
				hashtable.put("CKLXREMAIL", PrintUtil.parseNull(headReset.getString("CKLXREMAIL"))); // 仓库联系人E-MIAL
				hashtable.put("CKDH", PrintUtil.parseNull(headReset.getString("CKDH"))); // 出库单号
				hashtable.put("BYCKDH", PrintUtil.parseNull(headReset.getString("BYCKDH"))); // 备用单号
				hashtable.put("YSHJ", PrintUtil.parseNull(headReset.getString("YSHJ"))); // 应收合计
				hashtable.put("CKZT", PrintUtil.parseNull(headReset.getString("CKZT"))); // 出库状态
				hashtable.put("QSD", PrintUtil.parseNull(headReset.getString("QSD"))); // 签收单
				hashtable.put("SFYS", PrintUtil.parseNull(headReset.getString("SFYS"))); // 是否预收
				hashtable.put("CYZT", PrintUtil.parseNull(headReset.getString("CYZT"))); // 差异状态
				hashtable.put("JHHDH", PrintUtil.parseNull(headReset.getString("JHHDH"))); // 借还货单号
				hashtable.put("JHHDB", PrintUtil.parseNull(headReset.getString("JHHDB"))); // 借还货单别
				hashtable.put("ZDR", this.getContext().getUserModel().getUserName()); // 制单人
				hashtable.put("bindid", bindid);
				hashtable.put("sid", super.getSIDFlag());
				hashtable.put("FHGS", "北京亚昆"); // 发货公司
				hashtable.put("CKDDYCS", yckddycs.toString()); // 发货公司
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
	private StringBuilder parseTableTRHtml(ResultSet bodyReset, int row, String khcgdh, String khbm) throws SQLException {
		StringBuilder sb = new StringBuilder();
		// 获取子表数据
		sb.append("<tr class='subtable_tr'>");
		sb.append(PrintUtil.formatBodyRowRecord(row++));// 行号
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLH")));// 物料号
		sb.append(PrintUtil.formatBodyRowRecord(khbm));// 客户编码
		sb.append(PrintUtil.formatBodyRowRecord(khcgdh));// 客户采购单号
		sb.append(PrintUtil.formatBodyRowRecordNoWarp(bodyReset.getString("XH")));// 型号
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseSXToName(bodyReset.getString("SX"))));// 属性
		sb.append(PrintUtil.formatBodyRowRecord(DictionaryUtil.parseJLDWToName(bodyReset.getString("JLDW"))));// 计量单位
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getInt("SJSL")));// 实际数量
		sb.append(PrintUtil.formatBodyRowRecord(bodyReset.getString("WLMC")));// 物料名称
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
		Integer yckddycs = DAOUtil.getIntOrNull(conn, QUERY_CKD_CKDDYCS, bindid);
		if (yckddycs == null) {
			yckddycs = 0;
		}
		DAOUtil.executeUpdate(conn, UPDATE_CKD_CKDDYCS, ++yckddycs, bindid);
		return yckddycs;
	}
}
