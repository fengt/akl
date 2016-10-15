package cn.com.akl.dgrk;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DateUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public class DGRKDPrinterWeb extends ActionsoftWeb {
	/**
	 * ��ѯ��ⵥ�Ĵ�ӡ����.
	 */
	private static final String QUERY_RKD_RKDDYCS = "SELECT DYCS FROM BO_AKL_DGRK_P WHERE BINDID=?";
	/**
	 * ������ⵥ�Ĵ�ӡ����.
	 */
	private static final String UPDATE_RKD_RKDDYCS = "UPDATE BO_AKL_DGRK_P SET DYCS=? WHERE BINDID=?";
	/**
	 * ��ӡֽÿҳ����.
	 */
	public static final int PAGE_SIZE = 10;
	
	public DGRKDPrinterWeb() {
		super();
	}

	public DGRKDPrinterWeb(UserContext arg0) {
		super(arg0);
	}
	
	public String parseHtml(String bindid){
		PreparedStatement bodyPs = null;
		ResultSet bodyRs = null;
		String pch = "";
		StringBuilder htmlBuilder = new StringBuilder();
		
		String bodysql = "SELECT WLBH,WLMC,XH,PCH,DW,SX,SSSL,HWDM FROM BO_AKL_DGRK_S WHERE BINDID = '"+bindid+"' order by XH";
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		Connection conn = DBSql.open();
		try{
			/**
			 * ��ȡ��ӡ���������´�ӡ����.
			 */
			Integer rkddycs = DAOUtil.getIntOrNull(conn, QUERY_RKD_RKDDYCS, bindid);
			if (rkddycs == null) {
				rkddycs = 0;
			}
			DAOUtil.executeUpdate(conn, UPDATE_RKD_RKDDYCS, ++rkddycs, bindid);
			try {
				bodyPs = conn.prepareStatement(bodysql);
				bodyRs = bodyPs.executeQuery();
	
				//�ֱ����ݼ���
				ArrayList<StringBuilder> trList = new ArrayList<StringBuilder>(50);
	
				// �����������
				fillMainInfo(conn, bindid, rkddycs, hashtable);
				
				int zs = 0;
				double ztj=0;
				double zzl=0;
				for(int bh = 1; bodyRs.next(); bh++){
					// �����ӱ�����Ϣ
					trList.add(parseTableTRHtml(bodyRs, bh));
					
					String wlbh = parseNull(bodyRs.getString("WLBH"));
					String wlsql = "select TJ,ZL FROM BO_AKL_WLXX WHERE WLBH='"+wlbh+"'";
					Double tj = DBSql.getDouble(wlsql, "TJ");
					Double zl = DBSql.getDouble(wlsql, "ZL");
					pch = bodyRs.getString("PCH");
					String sssl = bodyRs.getString("SSSL");
					if(sssl!=null){
						int sl = Integer.parseInt(sssl);
						//���
						tj = tj * sl;
						ztj = ztj + tj;
						//����
						zl = zl * sl;
						zzl = zzl + zl;
						//�ϼ�
						zs=zs+sl;
					}
				}
				String hj = String.valueOf(zs);
				BigDecimal bd=new BigDecimal(ztj); 
				bd=bd.setScale(4, BigDecimal.ROUND_HALF_UP);
				String hjtj = String.valueOf(bd);
				bd=new BigDecimal(zzl); 
				bd=bd.setScale(4, BigDecimal.ROUND_HALF_UP);
				String hjzl = String.valueOf(bd);
				hashtable.put("PCH", parseNull(pch));
				
				hashtable.put("HJ", parseNull(hj));
				hashtable.put("TJ", parseNull(hjtj));
				hashtable.put("ZL", parseNull(hjzl));
				
				// ��10����¼һҳ�ķ�ʽ����
				StringBuilder pageTrSb = new StringBuilder();
				for (int i = 0; i < trList.size(); i++) {
					/*if (i != 0 && i % PAGE_SIZE == 0) {
						hashtable.put("SubReport", pageTrSb.toString());
						htmlBuilder.append(getHtmlPage("������ⵥ.html", hashtable));
						pageTrSb = new StringBuilder();
					}*/
					pageTrSb.append(trList.get(i));
				}
	
				if (pageTrSb.length() != 0) {
					hashtable.put("SubReport", pageTrSb.toString());
					htmlBuilder.append(getHtmlPage("������ⵥ.html", hashtable));
				}
				
				if (htmlBuilder.length() == 0) {
					hashtable.put("SubReport", "");
					htmlBuilder.append(getHtmlPage("������ⵥ.html", hashtable));
				}
			}finally{
				DBSql.close( bodyPs, bodyRs);
			}
			return htmlBuilder.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "ϵͳ�������⣬����ϵ����Ա��");
			return "ϵͳ�������⣬����ϵ����Ա��";
		}finally{
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ���������Ϣ.
	 * 
	 * @param bindid
	 * @param headReset
	 * @param today
	 * @param yckddycs
	 * @param hashtable
	 * @throws SQLException
	 */
	private void fillMainInfo(Connection conn, String bindid, Integer rkddycs, Hashtable<String, String> hashtable)
			throws SQLException {

		PreparedStatement headPs = null;
		ResultSet headRs = null;
		String headsql = "SELECT RKDH,KHBH,BZ FROM BO_AKL_DGRK_P WHERE BINDID = '"+bindid+"'";

		try {
			headPs = conn.prepareStatement(headsql);
			headRs = headPs.executeQuery();

			if (headRs.next()) {
				String rkdh = headRs.getString("RKDH");//��ⵥ��
				
				//���ɵ��ݺ�
//				String djh = String.format("%03d", yrkddycs);//���ݺ�
				String djh = rkdh.substring(rkdh.length()-3);
				Date now = new Date();
				djh = DateUtil.dateToLongStrBys2(now)+djh;//�������ڣ��磺20140724
				String khbh = headRs.getString("KHBH");//�ͻ����
//				String shck = headRs.getString("SHCK");//�ջ��ֿ�
				String bz = headRs.getString("BZ");//��ע
				String fhsql = "SELECT NAME,LXR,LXDH,LXDZ FROM BO_AKL_KH_P WHERE KHID='"+khbh+"'";
				String shsql = "SELECT NAME,LXR,LXDH,LXDZ FROM BO_AKL_KH_P WHERE KHID='01065'";
				//������˾
				String fhgs = DBSql.getString(fhsql, "NAME");
				String fhr = DBSql.getString(fhsql, "LXR");
				String fhdh = DBSql.getString(fhsql, "LXDH");
				String fhdz = DBSql.getString(fhsql, "LXDZ");
				//�ջ���˾
				String shgs = DBSql.getString(shsql, "NAME");
				String shr = DBSql.getString(shsql, "LXR");
				String shdh = DBSql.getString(shsql, "LXDH");
				String shdz = DBSql.getString(shsql, "LXDZ");

				hashtable.put("DYCS", String.valueOf(rkddycs));
				hashtable.put("DJH", parseNull(djh));
				hashtable.put("RKDH", parseNull(rkdh));
				hashtable.put("FHGS", parseNull(fhgs));
				hashtable.put("SHGS", parseNull(shgs));
				hashtable.put("FHDZ", parseNull(fhdz));
				hashtable.put("SHDZ", parseNull(shdz));
				hashtable.put("FHR", parseNull(fhr));
				hashtable.put("LXR", parseNull(shr));
//				hashtable.put("LXR", this.getContext().getUserModel().getUserName());
				hashtable.put("FHDH", parseNull(fhdh));
				hashtable.put("SHDH", parseNull(shdh));
//				hashtable.put("SHDH", this.getContext().getUserModel().getMobile());
				hashtable.put("BZ", parseNull(bz));
				hashtable.put("JS", "");
				hashtable.put("KGQZ", "");
				hashtable.put("SHRQZ", "");
				hashtable.put("ZDR", this.getContext().getUserModel().getUserName());
			}else{
				hashtable.put("DJH", "");
				hashtable.put("RKDH", "");
				hashtable.put("DYCS", "");
				hashtable.put("FHGS", "");
				hashtable.put("SHGS", "");
				hashtable.put("FHDZ", "");
				hashtable.put("SHDZ", "");
				hashtable.put("FHR", "");
				hashtable.put("LXR", "");
				hashtable.put("FHDH", "");
				hashtable.put("SHDH", "");
				hashtable.put("BZ", "");
				hashtable.put("JS", "");
//				hashtable.put("HJ", "");
//				hashtable.put("TJ", "");
//				hashtable.put("ZL", "");
				hashtable.put("KGQZ", "");
				hashtable.put("SHRQZ", "");
				hashtable.put("ZDR", "");
				hashtable.put("SubReport", "");
			}
			hashtable.put("bindid", bindid);
			hashtable.put("sid", super.getSIDFlag());
		} finally {
			DBSql.close(headPs, headRs);
		}
	}

	/**
	 * ת����Ϣ��TR��ǩ.
	 * 
	 * @param bodyReset
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private StringBuilder parseTableTRHtml(ResultSet bodyRs, int bh) throws SQLException {
		StringBuilder sb = new StringBuilder();
		// ��ȡ�ӱ�����
		sb.append("<tr class='subtable_tr'>");
//		bh++;
		String bhxlh = String.valueOf(bh);
		String xh = bodyRs.getString("XH");
		String sx = bodyRs.getString("SX");
		String sxsql = "SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE DLBM='049' AND XLBM='"+sx+"'";
		String sxmc = DBSql.getString(sxsql, "XLMC");
		String dw = bodyRs.getString("DW");
		String dwsql = "SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE DLBM='026' AND XLBM='"+dw+"'";
		String dwmc = DBSql.getString(dwsql, "XLMC");
		String sssl = bodyRs.getString("SSSL");
		String hwdm = bodyRs.getString("HWDM");
		String wlmc = bodyRs.getString("WLMC");
		sb.append(formatBodyRowRecord(parseNull(bhxlh),parseNull(xh),parseNull(wlmc),parseNull(sxmc),parseNull(dwmc),parseNull(sssl),parseNull(hwdm)));
		sb.append("</tr>");
		return sb;
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
