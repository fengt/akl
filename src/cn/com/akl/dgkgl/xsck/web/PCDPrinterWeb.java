package cn.com.akl.dgkgl.xsck.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class PCDPrinterWeb extends ActionsoftWeb{

	/**
	 * 查询运单单头.
	 */
	private static final String QUERY_YD_P = "SELECT * FROM BO_AKL_YD_P WHERE BINDID=?";
	/**
	 * 查询运单单身.
	 */
	private static final String QUERY_YD_S = "SELECT * FROM BO_AKL_YD_S WHERE BINDID=?";
	public PCDPrinterWeb(){
		super();
	}
	public PCDPrinterWeb(UserContext user){
		super(user);
	}
	public String paserHtml(String bindid){
		Connection conn = null;
		PreparedStatement bodyPs = null;
		ResultSet bodyReset = null;
		PreparedStatement headPs = null;
		ResultSet headReset = null;
		int hh = 0;
		StringBuilder htmlBuilder = new StringBuilder();
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		try {
			conn = DBSql.open();
			headPs = conn.prepareStatement(QUERY_YD_P);
			headReset = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);
			String year = "";
			String month = "";
			String day = "";
			if (headReset.next()) {
				String rq = PrintUtil.parseNull(headReset.getString("RQ"));// 日期
				if(rq.length()>10){
					year = rq.substring(0,4);
					month = rq.substring(5,7);
					day = rq.substring(8,10);
				}
				hashtable.put("year", year);// 年
				hashtable.put("month", month);// 月
				hashtable.put("day", day);// 日
				hashtable.put("CKDH", PrintUtil.parseNull(headReset.getString("CKDH")));// 出库单号
				hashtable.put("SHR", PrintUtil.parseNull(headReset.getString("SHR")));// 收货人
				hashtable.put("SHRDH", PrintUtil.parseNull(headReset.getString("SHRDH")));// 收货人电话
				hashtable.put("SHDZ", PrintUtil.parseNull(headReset.getString("SHDW")));// 收货地址
				hashtable.put("YDDH", PrintUtil.parseNull(headReset.getString("YDDH")));// 运单单号
				hashtable.put("JS", PrintUtil.parseNull(headReset.getString("JS")).equals("0")?"":PrintUtil.parseNull(headReset.getString("JS")));// 件数
				hashtable.put("TJ", PrintUtil.parseNull(headReset.getString("TJ")).equals("0.0000")?"":PrintUtil.parseNull(headReset.getString("TJ")));// 体积
				hashtable.put("ZK", PrintUtil.parseNull(headReset.getString("ZK")).equals("0.0000")?"":PrintUtil.parseNull(headReset.getString("ZK")));// 折扣

			}
			else{
				hashtable.put("RQ", "");// 日期
				hashtable.put("CKDH", "");// 出库单号
				hashtable.put("SHR", "");// 收货人
				hashtable.put("SHRDH", "");// 收货人电话
				hashtable.put("SHDZ", "");// 收货地址
				hashtable.put("YDDH", "");// 运单单号
				hashtable.put("JS", "");// 件数
				hashtable.put("TJ", "");// 体积
				hashtable.put("ZK", "");// 折扣
			}
			bodyPs = conn.prepareStatement(QUERY_YD_S);
			bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);
			while(bodyReset.next()){
				hashtable.put("YCDW", PrintUtil.parseNull(bodyReset.getString("YCDW")));// 用车单位
				hashtable.put("SCR", PrintUtil.parseNull(bodyReset.getString("SCR")));// 随车人
				hashtable.put("JSY", PrintUtil.parseNull(bodyReset.getString("JSY")));// 驾驶员
				hashtable.put("CPH", PrintUtil.parseNull(bodyReset.getString("CPH")));// 车牌号
				hashtable.put("KSSJ", PrintUtil.parseNull(bodyReset.getString("YCKSSJ")));// 用车开始时间
				hashtable.put("JSSJ", PrintUtil.parseNull(bodyReset.getString("YCJSSJ")));// 用车结束时间
				hashtable.put("QSLC", PrintUtil.parseNull(bodyReset.getString("QSLC")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("QSLC")));;// 起始里程
				hashtable.put("JSLC", PrintUtil.parseNull(bodyReset.getString("JSLC")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("JSLC")));;// 结束里程
				hashtable.put("YCXSS", PrintUtil.parseNull(bodyReset.getString("YCXSS")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("YCXSS")));// 用车小时数
				hashtable.put("QCS", PrintUtil.parseNull(bodyReset.getString("QCS")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("QCS")));;// 全程数
				hashtable.put("JYLJBS", PrintUtil.parseNull(bodyReset.getString("JYLJBS")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("JYLJBS")));// 加油量及表数
				hashtable.put("GLGQF", PrintUtil.parseNull(bodyReset.getString("GLGQF")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("GLGQF")));// 过路过桥费
				hashtable.put("TCF", PrintUtil.parseNull(bodyReset.getString("TCF")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("TCF")));// 停车费
				hashtable.put("YSXSB", PrintUtil.parseNull(bodyReset.getString("YSXSB")));// 钥匙/行驶本（是/否归还）
				hh++;
				htmlBuilder.append(getHtmlPage("派车单.html", hashtable));
			}
			if(hh==0){
				hashtable.put("YCDW", "");// 用车单位
				hashtable.put("SCR", "");// 随车人
				hashtable.put("JSY", "");// 驾驶员
				hashtable.put("CPH", "");// 车牌号
				hashtable.put("KSSJ", "");// 用车开始时间
				hashtable.put("JSSJ", "");// 用车结束时间
				hashtable.put("QSLC", "");// 起始里程
				hashtable.put("JSLC", "");// 结束里程
				hashtable.put("YCXSS", "");// 用车小时数
				hashtable.put("QCS", "");// 全程数
				hashtable.put("JYLJBS", "");// 加油量及表数
				hashtable.put("GLGQF", "");// 过路过桥费
				hashtable.put("TCF", "");// 停车费
				hashtable.put("YSXSB", "");// 钥匙/行驶本（是/否归还）
				htmlBuilder.append(getHtmlPage("派车单.html", hashtable));
			}
			hashtable.put("bindid", bindid);
			hashtable.put("sid", super.getSIDFlag());
			return htmlBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "系统出现问题,请联系管理员!");
			return "系统出现问题,请联系管理员!";
		} finally {
			DBSql.close(conn, headPs, headReset);
			DBSql.close(conn, bodyPs, bodyReset);
		}
	}
}
