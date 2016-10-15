package cn.com.akl.shgl.sx.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class SXDPrinterWeb extends ActionsoftWeb {

	private static final String QUERY_SX_P = "SELECT xmlb.XLMC AS XMMC,ywlx.XLMC,kf.KFCKMC,kf.DHQH+'-'+kf.DH AS KFDH,p.* FROM BO_AKL_SX_P p LEFT JOIN BO_AKL_DATA_DICT_S xmlb ON xmlb.XLBM=p.XMLB LEFT JOIN BO_AKL_DATA_DICT_S ywlx ON ywlx.XLBM=p.YWLX LEFT JOIN BO_AKL_KFCK kf  ON kf.KFCKBM=p.XMKF WHERE p.BINDID = ?";
	private static final String QUERY_SX_S = "SELECT ROW_NUMBER() OVER(ORDER BY s.ID)RowNum,clfs.XLMC,s.* FROM BO_AKL_SX_S s LEFT JOIN BO_AKL_DATA_DICT_S clfs ON clfs.XLBM=s.CLFS WHERE s.BINDID = ?";
//	private static final String QUERY_RKD_DYCS = "SELECT DYCS FROM " + CgrkCnt.tableName0 + " WHERE BINDID = ?";
//	private static final String UPDATE_RKD_DYCS = "UPDATE " + CgrkCnt.tableName0 + " SET DYCS=? WHERE BINDID=?";
	
	private static final String is = "是";
	private static final int PAGE_SIZE = 18;
	private static final String blank = "&nbsp;";
	
	public SXDPrinterWeb() {
		super();
	}

	public SXDPrinterWeb(UserContext arg0) {
		super(arg0);
	}
	
	public String parseHtml(String bindid){
		int curPageIndex = 0;// 当前页码
		StringBuilder htmlBuilder = new StringBuilder();
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		Connection conn = null;
		try {
			conn = DBSql.open();
			fillMainInfo(conn, bindid, hashtable);//填充主表数据
			ArrayList<StringBuilder> trList = fillSubInfo(conn, bindid);//填充子表数据
			
			StringBuilder trPage = new StringBuilder();
			for (int i = 0; i < trList.size(); i++) {
				if(i != 0 && i % PAGE_SIZE == 0){
					hashtable.put("SubReport", trPage.toString());
					//当前页码
					hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
					htmlBuilder.append(getHtmlPage("客服中心专用送修单.html", hashtable));
					trPage = new StringBuilder();
				}
				trPage.append(trList.get(i));
			}
			if(trPage.length() != 0){
				int rowNum = PAGE_SIZE - trList.size() % PAGE_SIZE;
				hashtable.put("SubReport", fillBlankTr(rowNum, trPage).toString());
				hashtable.put("CURPAGE", String.valueOf(++curPageIndex));
				htmlBuilder.append(getHtmlPage("客服中心专用送修单.html", hashtable));
			}
			
			return htmlBuilder.toString().replaceAll("\\{\\[PAGESIZE\\]\\}", String.valueOf(curPageIndex));
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "系统出现问题，请联系管理员！");
			return "系统出现问题，请联系管理员！";
		}finally{
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 填充子表数据
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	private ArrayList<StringBuilder> fillSubInfo(Connection conn, String bindid) throws SQLException{
		ArrayList<StringBuilder> trList = new ArrayList<StringBuilder>();
		PreparedStatement bodyPs = null;
		ResultSet bodyRs = null;
		try{
			bodyPs = conn.prepareStatement(QUERY_SX_S);
			bodyRs = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);
			while(bodyRs.next()){
				StringBuilder sb = new StringBuilder();
				sb.append("<tr class='subtable_body_tr'>");
				String rowNum = bodyRs.getString("RowNum");
				String wlmc = parseNull(bodyRs.getString("WLMC"));
				String xh = bodyRs.getString("XH");
				String sn = bodyRs.getString("SN");
				String sl = bodyRs.getString("SL");
				String clfs = bodyRs.getString("XLMC");
				String gzyy = parseNull(bodyRs.getString("GZYY"));
				sb.append(formatBodyRowRecord(rowNum,wlmc,xh,parseNull(sn),sl, parseNull(clfs),gzyy));
				sb.append("</tr>");
				trList.add(sb);
			}
		} finally{
			DBSql.close(bodyPs, bodyRs);
		}
		return trList;
	}
	
	/**
	 * 填充主表信息
	 * @param conn
	 * @param bindid
	 * @param hashtable
	 * @throws SQLException
	 */
	private void fillMainInfo(Connection conn, String bindid, Hashtable<String, String> hashtable) throws SQLException{
		PreparedStatement headPs = null;
		ResultSet headRs = null;
		try {
			//获取并更新打印次数
//			Integer dycs = DAOUtil.getIntOrNull(conn, QUERY_RKD_DYCS, bindid);
//			if(dycs == null){
//				dycs = 0;
//			}
//			DAOUtil.executeUpdate(conn, UPDATE_RKD_DYCS, ++dycs, bindid);
			
			headPs = conn.prepareStatement(QUERY_SX_P);
			headRs = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);
			//获取主表数据
			if(headRs.next()){
				String kfzx = headRs.getString("KFCKMC");
				String zdr = headRs.getString("ZDR");
				String kfdh = headRs.getString("KFDH");
				Date rq = headRs.getDate("ZDRQ"); 
				String sxrq = null;
				if(rq != null){
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					sxrq = dateFormat.format(rq);
				}
				String sxdh = headRs.getString("SXDH");
				String ywlx = headRs.getString("XLMC");
				String xmmc = headRs.getString("XMMC");
				
				String khmc = headRs.getString("KHMC");
				String sxr = headRs.getString("SXR");
				String sxyy = headRs.getString("SXYY");
				String dhqh = headRs.getString("DHQH");
				String dh = headRs.getString("DH");
				String sjh = headRs.getString("SJH");
				
				String s = headRs.getString("S");
				String shi = headRs.getString("SHI");
				String qx = headRs.getString("QX");
				String xxdz = headRs.getString("XXDZ");
				
				String pjcb = headRs.getString("YSPJCB");
				String wxf = headRs.getString("YSWXF");
				String yjf = headRs.getString("YSYJF");
				String yj = headRs.getString("YSYJ");
				String qtfy = headRs.getString("YSQTF");
				String fyhj = headRs.getString("YSFYHJ");
				
//				hashtable.put("DYCS", String.valueOf(dycs));
				hashtable.put("KFZX", parseNull(kfzx));
				hashtable.put("SXDH", parseNull(sxdh));
				hashtable.put("YWLX", parseNull(ywlx));
				hashtable.put("XMLB", parseNull(xmmc));
				hashtable.put("KHDH", parseNull(dhqh)+parseNull(dh));
				hashtable.put("KHSJ", parseNull(sjh));
				hashtable.put("KHMC", parseNull(khmc));
				hashtable.put("SXR", parseNull(sxr));
				hashtable.put("SXYY", parseNull(sxyy));
				hashtable.put("DZ", parseNull(xxdz));//parseNull(s)+parseNull(shi)+parseNull(qx)+
				hashtable.put("PJCB", parseNull(pjcb));
				hashtable.put("WXF", parseNull(wxf));
				hashtable.put("YJF", parseNull(yjf));
				hashtable.put("YJ", parseNull(yj));
				hashtable.put("QT", parseNull(qtfy));
				hashtable.put("HJ", parseNull(fyhj));
				
				hashtable.put("ZDR", parseNull(zdr));
				hashtable.put("SXRQ", parseNull(sxrq));
				hashtable.put("KFDH", parseNull(kfdh));
				hashtable.put("bindid", bindid);
				hashtable.put("sid", super.getSIDFlag());
			}
		} finally{
			DBSql.close(headPs, headRs);
		}
	}
	
	
	/**
	 * 单身行记录转换成字符串
	 * @param args
	 * @return
	 */
	public String formatBodyRowRecord(String...args){
		StringBuilder sb = new StringBuilder();
		for(String string : args){
			sb.append("<td class='subtable_body_td'>").append(string).append("</td>");
		}
		return sb.toString();
	}
	
	/**
	 * 填充空白行
	 * @param rowNum
	 * @param sb
	 * @return
	 */
	public StringBuilder fillBlankTr(int rowNum, StringBuilder sb){
		StringBuilder tr = null;
		for (int i = 0; i < rowNum; i++) {
			tr = new StringBuilder();
			tr.append("<tr class='subtable_body_tr'>");
			tr.append(formatBodyRowRecord(blank,blank,blank,blank,blank, blank,blank));
			tr.append("</tr>");
			sb.append(tr);
		}
		return sb;
	}
	public String parseNull(String str){
		return str == null?"":str;
	}

}
