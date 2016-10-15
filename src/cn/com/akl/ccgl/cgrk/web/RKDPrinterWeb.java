package cn.com.akl.ccgl.cgrk.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class RKDPrinterWeb extends ActionsoftWeb {

	private static final String QUERY_RKD_HEAD = "SELECT * FROM " + CgrkCnt.tableName0 + " WHERE BINDID = ?";
	private static final String QUERY_RKD_BODY = "SELECT * FROM " + CgrkCnt.tableName1 + " WHERE BINDID = ?";
	private static final String QUERY_RKD_DYCS = "SELECT DYCS FROM " + CgrkCnt.tableName0 + " WHERE BINDID = ?";
	private static final String UPDATE_RKD_DYCS = "UPDATE " + CgrkCnt.tableName0 + " SET DYCS=? WHERE BINDID=?";
	public RKDPrinterWeb() {
		super();
	}

	public RKDPrinterWeb(UserContext arg0) {
		super(arg0);
	}
	
	public String parseHtml(String bindid){
		Connection conn = null;
		PreparedStatement headPs = null;
		PreparedStatement bodyPs = null;
		ResultSet headRs = null;
		ResultSet bodyRs = null;
		int hj = 0;
		Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
		
		try {
			conn = DBSql.open();
			//获取并更新打印次数
			Integer dycs = DAOUtil.getIntOrNull(conn, QUERY_RKD_DYCS, bindid);
			if(dycs == null){
				dycs = 0;
			}
			DAOUtil.executeUpdate(conn, UPDATE_RKD_DYCS, ++dycs, bindid);
			System.out.println(dycs);
			
			try{
			headPs = conn.prepareStatement(QUERY_RKD_HEAD);
			bodyPs = conn.prepareStatement(QUERY_RKD_BODY);
			headRs = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);
			bodyRs = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);
			
			//获取主表数据
			if(headRs.next()){
				String cgfs = headRs.getString("CGFS");
				Date rq = headRs.getDate("CJSJ"); 
				String rqFormat = null;
				if(rq != null){
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					rqFormat = dateFormat.format(rq);
				}
				String bh = headRs.getString("BH");
				String gys = headRs.getString("GYSMC");
				String rkdb0 = headRs.getString("RKDB");
				String rkdb = DBSql.getString("SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE DLBM='011' AND XLBM = '"+rkdb0+"' ", "XLMC");
				String bm = headRs.getString("BM");
				String bgr = headRs.getString("BGR");
				String ysr = headRs.getString("YZR");
				String ywy = headRs.getString("YWY");
				String shr = headRs.getString("SHR");
				String zdr = headRs.getString("CJXM");
				String sjhwtj = headRs.getString("SJHWTJ");
				String sjhwzl = headRs.getString("SJHWZL");
				
				//获取子表数据
				StringBuilder sb = new StringBuilder();
				while(bodyRs.next()){
					sb.append("<tr class='subtable_tr'>");
					String wlbh = bodyRs.getString("WLBH");
					String wlmc = bodyRs.getString("CPMC");
					String xh = bodyRs.getString("XH");
					String pch = bodyRs.getString("PCH");
					String dw = bodyRs.getString("DW");
					String yssl = bodyRs.getString("YSSL");
					String sssl = bodyRs.getString("SSSL");
					hj += Integer.parseInt(sssl);
					String ckbm = bodyRs.getString("CKBM");
					String ckmc = DBSql.getString("SELECT CKMC FROM BO_AKL_CK WHERE CKDM = '"+ckbm+"'", "CKMC");
					String cgddh = bodyRs.getString("CGDDH");
					String sflp = bodyRs.getString("SFLP");
					sb.append(formatBodyRowRecord(wlbh,wlmc,xh,pch,dw,yssl,sssl,ckmc,cgddh,sflp));
					sb.append("</tr>");
				}
				hashtable.put("CGFS", parseNull(cgfs));
				hashtable.put("RQ", parseNull(rqFormat));
				hashtable.put("BH", parseNull(bh));
				hashtable.put("GYSMC", parseNull(gys));
				hashtable.put("RKDB", parseNull(rkdb));
				hashtable.put("DYCS", String.valueOf(dycs));
				hashtable.put("BM", parseNull(bm));
				hashtable.put("BGR", parseNull(bgr));
				hashtable.put("YSR", parseNull(ysr));
				hashtable.put("YWY", parseNull(ywy));
				hashtable.put("SHR", parseNull(shr));
				hashtable.put("ZDR", parseNull(zdr));
				hashtable.put("TJ", parseNull(sjhwtj));
				hashtable.put("ZL", parseNull(sjhwzl));
				hashtable.put("HJ", hj);
				hashtable.put("SubReport", sb.toString());
			}else{
				hashtable.put("CGFS", "");
				hashtable.put("RQ", "");
				hashtable.put("BH", "");
				hashtable.put("GYS", "");
				hashtable.put("RKDB", "");
				hashtable.put("BM", "");
				hashtable.put("BGR", "");
				hashtable.put("YSR", "");
				hashtable.put("YWY", "");
				hashtable.put("SHR", "");
				hashtable.put("ZDR", "");
				hashtable.put("DYCS", "");
				hashtable.put("HJ", "");
				hashtable.put("SubReport", "");
			}
			
			hashtable.put("bindid", bindid);
			hashtable.put("sid", super.getSIDFlag());
			} finally {
				DBSql.close(headPs, headRs);
				DBSql.close(bodyPs, bodyRs);
			}
			return getHtmlPage("外购入库单.html", hashtable);
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "系统出现问题，请联系管理员！");
			return "系统出现问题，请联系管理员！";
		}finally{
			DBSql.close(conn, null, null);
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
	public String parseNull(String str){
		return str == null?"":str;
	}

}
