package cn.com.akl.ccgl.wply.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class WPLYPrinter extends ActionsoftWeb {

	private static final String QUEUE_WPLY_HEAD = "SELECT * FROM BO_AKL_WPLY_P WHERE BINDID=?";
	private static final String QUEUE_WPLY_BODY = "SELECT ROW_NUMBER() OVER(ORDER BY ID) AS NUM,* FROM BO_AKL_WPLY_S WHERE BINDID=?";
	private static final String QUEUE_WPLY_DYCS = "SELECT DYCS FROM BO_AKL_WPLY_P WHERE BINDID=?";
	private static final String UPDATE_WPLY_DYCS = "UPDATE BO_AKL_WPLY_P SET DYCS=? WHERE BINDID=?";
	private static final String QUEUE_WPLY_CKMC = "SELECT CKMC FROM BO_AKL_CK WHERE CKDM=?";
	public WPLYPrinter() {
		// TODO Auto-generated constructor stub
	}

	public WPLYPrinter(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public String parseHtml(String bindid){
		Connection conn = null;
		PreparedStatement headPs = null;
		PreparedStatement bodyPs = null;
		ResultSet headRs = null;
		ResultSet bodyRs = null;
		
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		//获取打印时的时间
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String day = format.format(date);
		
		try {
			conn = DBSql.open();
			//获取并更新打印次数
			Integer dycs = DAOUtil.getIntOrNull(conn, QUEUE_WPLY_DYCS, bindid);
			if(dycs == null){
				dycs = 0;
			}
			DAOUtil.executeUpdate(conn, UPDATE_WPLY_DYCS, ++dycs, bindid);
			
			try {
				headPs = conn.prepareStatement(QUEUE_WPLY_HEAD);
				bodyPs = conn.prepareStatement(QUEUE_WPLY_BODY);
				headRs = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);
				bodyRs = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);
				
				//获取主表数据
				if(headRs.next()){
					String lydh = parseNull(headRs.getString("DJLSH"));//领用单号
					String ckdm = parseNull(headRs.getString("LYCK"));//领用仓库
					String lyck = DAOUtil.getString(conn, QUEUE_WPLY_CKMC, ckdm);
					double fyhj = headRs.getDouble("FYHJ");//费用合计
					String sqsy = parseNull(headRs.getString("SQSY"));//申请事由
					Date sqrq = headRs.getDate("SQRQ");//申请日期
					String rq = null;
					if(sqrq != null){
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						rq = dateFormat.format(sqrq);
					}
					String sqr = parseNull(headRs.getString("DWR"));//申请人
					
					//获取子表数据
					StringBuilder sb = new StringBuilder();
					while(bodyRs.next()){
						String num = bodyRs.getString("NUM");//序号
						String wlmc = bodyRs.getString("WLMC");//物料名称
						String xh = bodyRs.getString("XH");//型号
						String pch = bodyRs.getString("PCH");//批次号
						String jldw = DictionaryUtil.parseJLDWToName(bodyRs.getString("JLDW"));//计量单位
						String dj = String.valueOf(bodyRs.getDouble("DJ"));//单价
						String sl = String.valueOf(bodyRs.getInt("SL"));//数量
						String hwdm = bodyRs.getString("HWDM");//货位代码
						String sx = DictionaryUtil.parseSXToName(bodyRs.getString("SX"));//属性
						
						sb.append("<tr class='subtable_body_tr'>");
						sb.append(formatBodyRowRecord(num,wlmc,xh,pch,jldw,dj,sl,hwdm,sx));
						sb.append("</tr>");
					}
					hashtable.put("DJH", day+lydh.substring(lydh.length()-3));
					hashtable.put("LYDH", lydh);
					hashtable.put("DYCS", String.valueOf(dycs));
					hashtable.put("LYCK", lyck);
					hashtable.put("FYHJ", String.valueOf(fyhj));
					hashtable.put("SQSY", sqsy);
					hashtable.put("SQR", sqr);
					hashtable.put("RQ", rq);
					hashtable.put("SubReport", sb.toString());
				}else{
					hashtable.put("DJH", "");
					hashtable.put("LYDH", "");
					hashtable.put("DYCS", "");
					hashtable.put("LYCK", "");
					hashtable.put("FYHJ", "");
					hashtable.put("SQSY", "");
					hashtable.put("SQR", "");
					hashtable.put("RQ", "");
					hashtable.put("SubReport", "");
				}
				hashtable.put("bindid", bindid);
				hashtable.put("sid", super.getSIDFlag());
				
			} finally{
				DBSql.close(headPs, headRs);
				DBSql.close(bodyPs, bodyRs);
			}
			return getHtmlPage("物品领用打印单.html", hashtable);
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "系统出现问题，请联系管理员！");
			return "系统出现问题，请联系管理员！";
		} finally{
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 单身字段转换表格形式
	 * @param args
	 * @return
	 */
	public String formatBodyRowRecord(String...args){
		StringBuilder sb = new StringBuilder();
		for(String field : args){
			sb.append("<td class='subtable_body_td'>").append(field).append("</td>");
		}
		return sb.toString();
	}
	
	public String parseNull(String str){
		return str == null ? "" : str;
	}
	
}
