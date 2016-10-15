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
	 * ��ѯ��ⵥ�Ĵ�ӡ����.
	 */
	private static final String QUERY_PDD_DYCS = "SELECT DYCS FROM BO_AKL_KCPD_P WHERE BINDID=?";
	/**
	 * ������ⵥ�Ĵ�ӡ����.
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
			 * ��ȡ��ӡ���������´�ӡ����.
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
			
			//��ȡ��������
			if(headRs.next()){
				String pddh = headRs.getString("PDDH");//�̵�����
				//���ɵ��ݺ�
//				String djh = String.format("%03d", rkddycs);//���ݺ�
				String djh = pddh.substring(pddh.length()-3);
				Date now = new Date();
				djh = DateUtil.dateToLongStrBys2(now)+djh;//�������ڣ��磺20140724
				String ckmc = headRs.getString("CKMC");//�ֿ�����
				String ppbh = headRs.getString("PPBH");//Ʒ�Ʊ��
				String rq = headRs.getString("PKRQ");//�̿�����
				//��ʽ���̿�����
				SimpleDateFormat f= new SimpleDateFormat("yyyy-mm-dd");
				Date rq1 = f.parse(rq);
				String pkrq = f.format(rq1);
				String zdr = headRs.getString("CJXM");//�Ƶ���
				String pdlx = headRs.getString("PDLX");//�̵�����
				String pdfs = headRs.getString("PDFS");//�̵㷽ʽ
				String kffzr = headRs.getString("KFFZR");//�ⷿ������
				String ms = headRs.getString("MS");//����
				String ppsql = "SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE DLBM=006 and XLBM='"+ppbh+"'";
				String pp = DBSql.getString(ppsql, "XLMC");

				int bh= 0;
				int kwhj = 0;
				//��ȡ�ӱ�����
				StringBuilder sb = new StringBuilder();
				while(bodyRs.next()){
					String wlbh = bodyRs.getString("WLBH");//���ϱ��
					String wlmc = bodyRs.getString("WLMC");//��������
					String pc = bodyRs.getString("PC");//����
					String kwbm = bodyRs.getString("KWBM");//��λ����
					int kwsl = bodyRs.getInt("KWSL");//��λ����
					int rksjsl = bodyRs.getInt("PKSJSL");//�̿�ʵ������
					int cysl = bodyRs.getInt("CYSL");//��������
					String cyyy = bodyRs.getString("CYYY");//����ԭ��
					String bz = bodyRs.getString("BZ");//��ע
					//��ӡ��������
					sb.append("<tr class='subtable_tr'>");
					bh++;
					String bhxlh = String.valueOf(bh);
					//��λ�����ϼ�
					kwhj = kwhj + kwsl;
					if(pdlx.equals("����") && pdfs.equals("����")){
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}else if(pdlx.equals("����") && pdfs.equals("����")){
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(String.valueOf(kwsl)),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}else if(pdlx.equals("����") && pdfs.equals("��ϸ")){
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(kwbm),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}else{
						sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(wlbh),parseNull(wlmc),parseNull(pc),parseNull(kwbm),parseNull(String.valueOf(kwsl)),parseNull(String.valueOf(rksjsl)),parseNull(String.valueOf(cysl)),parseNull(cyyy),parseNull(bz)));
					}
					sb.append("</tr>");
				}
				hashtable.put("DJH", parseNull(djh));
				hashtable.put("PDDH", parseNull(pddh));
				//��ӡ����
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
				//��ӡ����
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
			return getHtmlPage("�̵��ӡ��.html", hashtable);
		} catch (Exception e) {
			e.printStackTrace();
			return "ϵͳ�������⣬����ϵ����Ա��";
		}finally{
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