package cn.com.akl.pdgl.kcpd.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DateUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class PdPrinterWeb extends ActionsoftWeb {
	/**
	 * 查询入库单的打印次数.
	 */
	private static final String QUERY_PDD_DYCS = "SELECT DYCS FROM BO_AKL_KCPD_P WHERE BINDID=?";
	/**
	 * 更新入库单的打印次数.
	 */
	private static final String UPDATE_PDD_DYCS = "UPDATE BO_AKL_KCPD_P SET DYCS=? WHERE BINDID=?";

	public PdPrinterWeb() {
	}

	public PdPrinterWeb(UserContext arg0) {
		super(arg0);
	}
	public String parseHtml(String bindid){
		PreparedStatement headPs = null;
		PreparedStatement bodyPs = null;
		ResultSet headRs = null;
		ResultSet bodyRs = null;
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		String headsql = "SELECT PDDH,CKMC,PPBH,PKRQ,CJXM,PDLX,PDFS,KFFZR,MS FROM BO_AKL_KCPD_P WHERE BINDID = '"+bindid+"'";
		String bodysql = "SELECT WLBH,WLMC,PC,KWBM,KWSL,PKSJSL,CYSL,CYYY,BZ FROM BO_AKL_KCPD_S WHERE BINDID = '"+bindid+"'";
		Connection conn = DBSql.open();
		try{
			/**
			 * 获取打印次数并更新打印次数.
			 */
			Integer dycs = DAOUtil.getIntOrNull(conn, QUERY_PDD_DYCS, bindid);
			if (dycs == null) {
				dycs = 0;
			}
			DAOUtil.executeUpdate(conn, UPDATE_PDD_DYCS, ++dycs, bindid);
		try {
			headPs = conn.prepareStatement(headsql);
			bodyPs = conn.prepareStatement(bodysql);
			headRs = headPs.executeQuery();
			bodyRs = bodyPs.executeQuery();
			
			//获取主表数据
			if(headRs.next()){
				String pddh = headRs.getString("PDDH");//盘单单号
				//生成单据号
//				String djh = String.format("%03d", rkddycs);//单据号
				String djh = pddh.substring(pddh.length()-3);
				Date now = new Date();
				djh = DateUtil.dateToLongStrBys2(now)+djh;//当天日期，如：20140724
				String ckmc = headRs.getString("CKMC");//仓库名称
				String ppbh = headRs.getString("PPBH");//品牌编号
				String rq = headRs.getString("PKRQ");//盘库日期
				//格式化盘库日期
				SimpleDateFormat f= new SimpleDateFormat("yyyy-mm-dd");
				Date rq1 = f.parse(rq);
				String pkrq = f.format(rq1);
				String zdr = headRs.getString("CJXM");//制单人
				String pdlx = headRs.getString("PDLX");//盘点类型
				String pdfs = headRs.getString("PDFS");//盘点方式
				String kffzr = headRs.getString("KFFZR");//库房负责人
				String ms = headRs.getString("MS");//描述
				String ppsql = "SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE DLBM=006 and XLBM='"+ppbh+"'";
				String pp = DBSql.getString(ppsql, "XLMC");

				int bh= 0;
				int kwhj = 0;
				//获取子表数据
				StringBuilder sb = new StringBuilder();
				while(bodyRs.next()){
					String wlbh = bodyRs.getString("WLBH");//物料编号
					String wlmc = bodyRs.getString("WLMC");//物料名称
					String pc = bodyRs.getString("PC");//批次
					String kwbm = bodyRs.getString("KWBM");//库位编码
					int kwsl = bodyRs.getInt("KWSL");//库位数量
					int rksjsl = bodyRs.getInt("PKSJSL");//盘库实际数量
					int cysl = bodyRs.getInt("CYSL");//差异数量
					String cyyy = bodyRs.getString("CYYY");//差异原因
					String bz = bodyRs.getString("BZ");//备注
					//打印单身数据
					sb.append("<tr class='subtable_tr'>");
					bh++;
					String bhxlh = String.valueOf(bh);
					//库位数量合计
					kwhj = kwhj + kwsl;
					if(pdlx.equals("暗盘") && pdfs.equals("汇总")){
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}else if(pdlx.equals("明盘") && pdfs.equals("汇总")){
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(String.valueOf(kwsl)),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}else if(pdlx.equals("暗盘") && pdfs.equals("明细")){
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(kwbm),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}else{
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(kwbm),parseNull(String.valueOf(kwsl)),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}
					sb.append("</tr>");
				}
				hashtable.put("DJH", parseNull(djh));
				hashtable.put("PDDH", parseNull(pddh));
				//打印次数
				hashtable.put("DYCS", String.valueOf(dycs));
				hashtable.put("CKMC", parseNull(ckmc));
				hashtable.put("PPBH", parseNull(pp));
				hashtable.put("PKRQ", parseNull(pkrq));
				hashtable.put("CJXM", parseNull(zdr));
				hashtable.put("PDLX", parseNull(pdlx));
				hashtable.put("PDFS", parseNull(pdfs));
				hashtable.put("KFFZR", parseNull(kffzr));
				hashtable.put("MS", parseNull(ms));
				hashtable.put("HJ", String.valueOf(kwhj));
				hashtable.put("SubReport", sb.toString());
			}else{
				hashtable.put("DJH", "");
				hashtable.put("PDDH", "");
				//打印次数
				hashtable.put("DYCS", "");
				hashtable.put("CKMC", "");
				hashtable.put("PPBH", "");
				hashtable.put("PKRQ", "");
				hashtable.put("CJXM", "");
				hashtable.put("PDLX", "");
				hashtable.put("PDFS", "");
				hashtable.put("KFFZR", "");
				hashtable.put("MS", "");
				hashtable.put("SubReport", "");
			}
			hashtable.put("bindid", bindid);
			hashtable.put("sid", super.getSIDFlag());
		}finally{
			DBSql.close( bodyPs, bodyRs);
			DBSql.close(headPs, headRs);
		}
			return getHtmlPage("盘点打印单.html", hashtable);
		} catch (Exception e) {
			e.printStackTrace();
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