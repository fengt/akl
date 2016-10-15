package cn.com.akl.kwgl.dbcrk.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.kwgl.constant.KwglConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class DbPrinterWeb extends ActionsoftWeb {

	private static final String QUERY_DB_HEAD = "SELECT * FROM " + KwglConstant.table6 + " WHERE BINDID = ?";
	private static final String QUERY_DB_BODY = "SELECT ROW_NUMBER() OVER(ORDER BY ID) AS XH,* FROM " + KwglConstant.table7 + " WHERE BINDID = ?";
	private static final String QUERY_DB_DYCS = "SELECT DYCS FROM " + KwglConstant.table6 + " WHERE BINDID = ?";
	private static final String UPDATE_DB_DYCS = "UPDATE " + KwglConstant.table6 + " SET DYCS = ? WHERE BINDID = ?";
	public DbPrinterWeb() {
		super();
	}

	public DbPrinterWeb(UserContext arg0) {
		super(arg0);
	}

	public String parseHtml(String bindid){
		Connection conn = null;
		PreparedStatement headPs = null;
		PreparedStatement bodyPs = null;
		ResultSet headRs = null;
		ResultSet bodyRs = null;
		
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		
		//��ȡ��ӡʱ�䣬���ɵ��ݺ�
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd"); 
		String printerDate = format.format(date);
		
		try {
			conn = DBSql.open();
			//��ȡ�����´�ӡ����
			Integer dycs = DAOUtil.getIntOrNull(conn, QUERY_DB_DYCS, bindid);
			if(dycs == null){
				dycs = 0;
			}
			DAOUtil.executeUpdate(conn, UPDATE_DB_DYCS, ++dycs, bindid);
			
			try{
				headPs = conn.prepareStatement(QUERY_DB_HEAD);
				bodyPs = conn.prepareStatement(QUERY_DB_BODY);
				headRs = DAOUtil.executeFillArgsAndQuery(conn, headPs, bindid);
				bodyRs = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);
				
				//��ȡ��������
				if(headRs.next()){
					String bh = headRs.getString("DJH");
					Date rq = headRs.getDate("ZDRQ"); 
					String rqFormat = null;
					if(rq != null){
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						rqFormat = dateFormat.format(rq);
					}
					String shr = headRs.getString("SHR");
					String zdr = headRs.getString("ZDR");
					String zcck0 = headRs.getString("ZCCK");
					String zrck0 = headRs.getString("ZRCK");
					String str3 = "SELECT * FROM " + KwglConstant.table4 + " WHERE CKDM='"+zcck0+"'";
					String str4 = "SELECT * FROM " + KwglConstant.table4 + " WHERE CKDM='"+zrck0+"'";
					String zcck = DBSql.getString(str3, "CKMC");
					String zrck = DBSql.getString(str4, "CKMC");
					String bz = headRs.getString("BZ");
					
					//��ȡ�ӱ�����
					StringBuilder sb = new StringBuilder();
					while(bodyRs.next()){
						sb.append("<tr class='subtable_tr'>");
						String wlbh = bodyRs.getString("WLBH");
						String str0 = "SELECT * FROM " + KwglConstant.table9 + " WHERE WLBH='"+wlbh+"'";
						String dwbm = DBSql.getString(str0, "DW");//��λ����
						String ppid = DBSql.getString(str0, "PPID");//Ʒ�Ʊ���
						/*String str1 = "SELECT * FROM " + KwglConstant.table8 + " WHERE DLBM="+KwglConstant.dlbm1+" AND XLBM='"+dwbm+"'";
						String str2 = "SELECT * FROM " + KwglConstant.table8 + " WHERE DLBM="+KwglConstant.dlbm0+" AND XLBM='"+ppid+"'";
						String dw = DBSql.getString(str1, "XLMC");//��λ
						String pp = DBSql.getString(str2, "XLMC");//Ʒ��*/
						String dw = DictionaryUtil.parseJLDWToName(dwbm);//��λ
						String pp = DictionaryUtil.parsePPToName(ppid);//Ʒ��
						
						String tzqpc = bodyRs.getString("TZQPC");
						String tzqkw = bodyRs.getString("TZQKW");
						String tzqsl = bodyRs.getString("TZQSL");
						String tzhpc = bodyRs.getString("TZHPC");
						String tzhhw = bodyRs.getString("TZHHW");
						String tzhsl = bodyRs.getString("TZHSL");
						String xh = bodyRs.getString("XH");
						String wlxh = bodyRs.getString("WLXH");//�ͺ�
						String wlmc = bodyRs.getString("WLMC");//����
						sb.append(formatBodyRowRecord(xh,pp,wlxh,wlmc,tzqpc,tzqkw,tzqsl,dw,tzhpc,tzhhw,tzhsl));
						sb.append("</tr>");
					}
					hashtable.put("DJH", printerDate+bh.substring(bh.length()-3));
					hashtable.put("DBDH", parseNull(bh));
					hashtable.put("RQ", parseNull(rqFormat));
					hashtable.put("ZCCK", parseNull(zcck));
					hashtable.put("ZRCK", parseNull(zrck));
					hashtable.put("KGQZ", parseNull(shr));
					hashtable.put("ZDR", parseNull(zdr));
					hashtable.put("BZ", parseNull(bz));
					hashtable.put("DYCS", String.valueOf(dycs));
					hashtable.put("SubReport",sb.toString());
				}else{
					hashtable.put("DJH", "");
					hashtable.put("DBDH", "");
					hashtable.put("RQ", "");
					hashtable.put("ZCCK", "");
					hashtable.put("ZRCK", "");
					hashtable.put("KGQZ", "");
					hashtable.put("ZDR", "");
					hashtable.put("BZ", "");
					hashtable.put("DYCS", "");
					hashtable.put("SubReport", "");
				}
				hashtable.put("bindid", bindid);
				hashtable.put("sid", super.getSIDFlag());
			} finally{
				DBSql.close(headPs, headRs);
				DBSql.close(bodyPs, bodyRs);
			}
			return getHtmlPage("����������ӡ��.html",hashtable);
		} catch(SQLException e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "ϵͳ�������⣬����ϵ����Ա��");
			return "ϵͳ�������⣬����ϵ����Ա��";
		} finally{
			DBSql.close(conn, null, null);
		}
		
	}
	
	/**
	 * �����м�¼ת�����ַ���
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
