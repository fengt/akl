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
	 * ��ѯ�˵���ͷ.
	 */
	private static final String QUERY_YD_P = "SELECT * FROM BO_AKL_YD_P WHERE BINDID=?";
	/**
	 * ��ѯ�˵�����.
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
				String rq = PrintUtil.parseNull(headReset.getString("RQ"));// ����
				if(rq.length()>10){
					year = rq.substring(0,4);
					month = rq.substring(5,7);
					day = rq.substring(8,10);
				}
				hashtable.put("year", year);// ��
				hashtable.put("month", month);// ��
				hashtable.put("day", day);// ��
				hashtable.put("CKDH", PrintUtil.parseNull(headReset.getString("CKDH")));// ���ⵥ��
				hashtable.put("SHR", PrintUtil.parseNull(headReset.getString("SHR")));// �ջ���
				hashtable.put("SHRDH", PrintUtil.parseNull(headReset.getString("SHRDH")));// �ջ��˵绰
				hashtable.put("SHDZ", PrintUtil.parseNull(headReset.getString("SHDW")));// �ջ���ַ
				hashtable.put("YDDH", PrintUtil.parseNull(headReset.getString("YDDH")));// �˵�����
				hashtable.put("JS", PrintUtil.parseNull(headReset.getString("JS")).equals("0")?"":PrintUtil.parseNull(headReset.getString("JS")));// ����
				hashtable.put("TJ", PrintUtil.parseNull(headReset.getString("TJ")).equals("0.0000")?"":PrintUtil.parseNull(headReset.getString("TJ")));// ���
				hashtable.put("ZK", PrintUtil.parseNull(headReset.getString("ZK")).equals("0.0000")?"":PrintUtil.parseNull(headReset.getString("ZK")));// �ۿ�

			}
			else{
				hashtable.put("RQ", "");// ����
				hashtable.put("CKDH", "");// ���ⵥ��
				hashtable.put("SHR", "");// �ջ���
				hashtable.put("SHRDH", "");// �ջ��˵绰
				hashtable.put("SHDZ", "");// �ջ���ַ
				hashtable.put("YDDH", "");// �˵�����
				hashtable.put("JS", "");// ����
				hashtable.put("TJ", "");// ���
				hashtable.put("ZK", "");// �ۿ�
			}
			bodyPs = conn.prepareStatement(QUERY_YD_S);
			bodyReset = DAOUtil.executeFillArgsAndQuery(conn, bodyPs, bindid);
			while(bodyReset.next()){
				hashtable.put("YCDW", PrintUtil.parseNull(bodyReset.getString("YCDW")));// �ó���λ
				hashtable.put("SCR", PrintUtil.parseNull(bodyReset.getString("SCR")));// �泵��
				hashtable.put("JSY", PrintUtil.parseNull(bodyReset.getString("JSY")));// ��ʻԱ
				hashtable.put("CPH", PrintUtil.parseNull(bodyReset.getString("CPH")));// ���ƺ�
				hashtable.put("KSSJ", PrintUtil.parseNull(bodyReset.getString("YCKSSJ")));// �ó���ʼʱ��
				hashtable.put("JSSJ", PrintUtil.parseNull(bodyReset.getString("YCJSSJ")));// �ó�����ʱ��
				hashtable.put("QSLC", PrintUtil.parseNull(bodyReset.getString("QSLC")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("QSLC")));;// ��ʼ���
				hashtable.put("JSLC", PrintUtil.parseNull(bodyReset.getString("JSLC")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("JSLC")));;// �������
				hashtable.put("YCXSS", PrintUtil.parseNull(bodyReset.getString("YCXSS")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("YCXSS")));// �ó�Сʱ��
				hashtable.put("QCS", PrintUtil.parseNull(bodyReset.getString("QCS")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("QCS")));;// ȫ����
				hashtable.put("JYLJBS", PrintUtil.parseNull(bodyReset.getString("JYLJBS")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("JYLJBS")));// ������������
				hashtable.put("GLGQF", PrintUtil.parseNull(bodyReset.getString("GLGQF")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("GLGQF")));// ��·���ŷ�
				hashtable.put("TCF", PrintUtil.parseNull(bodyReset.getString("TCF")).equals("0.0000")?"":PrintUtil.parseNull(bodyReset.getString("TCF")));// ͣ����
				hashtable.put("YSXSB", PrintUtil.parseNull(bodyReset.getString("YSXSB")));// Կ��/��ʻ������/��黹��
				hh++;
				htmlBuilder.append(getHtmlPage("�ɳ���.html", hashtable));
			}
			if(hh==0){
				hashtable.put("YCDW", "");// �ó���λ
				hashtable.put("SCR", "");// �泵��
				hashtable.put("JSY", "");// ��ʻԱ
				hashtable.put("CPH", "");// ���ƺ�
				hashtable.put("KSSJ", "");// �ó���ʼʱ��
				hashtable.put("JSSJ", "");// �ó�����ʱ��
				hashtable.put("QSLC", "");// ��ʼ���
				hashtable.put("JSLC", "");// �������
				hashtable.put("YCXSS", "");// �ó�Сʱ��
				hashtable.put("QCS", "");// ȫ����
				hashtable.put("JYLJBS", "");// ������������
				hashtable.put("GLGQF", "");// ��·���ŷ�
				hashtable.put("TCF", "");// ͣ����
				hashtable.put("YSXSB", "");// Կ��/��ʻ������/��黹��
				htmlBuilder.append(getHtmlPage("�ɳ���.html", hashtable));
			}
			hashtable.put("bindid", bindid);
			hashtable.put("sid", super.getSIDFlag());
			return htmlBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getContext().getUID(), "ϵͳ��������,����ϵ����Ա!");
			return "ϵͳ��������,����ϵ����Ա!";
		} finally {
			DBSql.close(conn, headPs, headReset);
			DBSql.close(conn, bodyPs, bodyReset);
		}
	}
}
