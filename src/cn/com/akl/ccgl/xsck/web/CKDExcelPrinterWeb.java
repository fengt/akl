package cn.com.akl.ccgl.xsck.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.akl.ccgl.xsck.web.excel.CKDExcelGenerator;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.form.execute.plugins.imp2exp.ImpExpUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

/**
 * ���ⵥExcel����.
 * 
 * @author huangming
 *
 */
public class CKDExcelPrinterWeb extends ActionsoftWeb {

	public CKDExcelPrinterWeb() {
		super();
	}

	public CKDExcelPrinterWeb(UserContext arg0) {
		super(arg0);
	}

	/**
	 * ת��ҳ��.
	 * 
	 * @param bindid
	 * @return
	 */
	public String parseHtml(String bindid) {

		List<String[]> formHeadContent = null;
		String[] tableHeadContent = null;
		List<String[]> tableBodyContent = null;
		String tableBottomContent = null;

		Connection conn = null;
		try {
			conn = DBSql.open();
			formHeadContent = getFormHead(conn, bindid);
			tableHeadContent = getTableHeadContent();
			tableBodyContent = getTableBodyContent(conn, bindid);
			tableBottomContent = DAOUtil.getStringOrNull(conn, "SELECT SUM(ISNULL(SJSL, 0)) FROM BO_AKL_CKD_BODY WHERE BINDID=?", bindid);
			tableBottomContent = "�ۼ�������" + parseNull(tableBottomContent);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (SQLException e) {
			e.printStackTrace();
			return "��̨�����쳣������ϵ����Ա!";
		} finally {
			DBSql.close(conn, null, null);
		}

		CKDExcelGenerator generator = new CKDExcelGenerator();
		String filename = ImpExpUtil.createExcelTmpFile(generator
				.getWorkbook(formHeadContent, tableHeadContent, tableBodyContent, tableBottomContent));

		StringBuilder fileURL = new StringBuilder();
		fileURL.append("<html><title></title><script> window.location=encodeURI('");
		fileURL.append("downfile.wf?flag1=Excel&flag2=_&sid=");
		fileURL.append(getContext().getSessionId());
		fileURL.append("&rootDir=tmp&filename=");
		fileURL.append(filename);
		fileURL.append("');</script><body><h1>������ת����ҳ��...</h1></body></html>");
		return fileURL.toString();
	}

	/**
	 * ��ȡ����ͷ.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 */
	public List<String[]> getFormHead(Connection conn, String bindid) {
		PreparedStatement ps = null;
		ResultSet reset = null;

		List<String[]> list = new ArrayList<String[]>(10);

		try {
			ps = conn.prepareStatement("SELECT XSDDH, KHMC, CXFZR, KFLXR, CXDH, JHDZ, WFFZR, CKLXRDH, CK FROM BO_AKL_CKD_HEAD WHERE BINDID=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			String qsdh = null;
			String tydw = null;
			String shdw = null;
			String shfzr = null;
			String shfzrdh = null;
			String shdz = null;
			String kflxr = null;
			String ck = null;
			String cklxrdh = null;
			if (reset.next()) {
				qsdh = parseNull(reset.getString("XSDDH"));
				// reset.getString("TYDW"); //ǩ�յ�����
				shdw = parseNull(reset.getString("KHMC"));
				shfzr = parseNull(reset.getString("CXFZR"));
				shfzrdh = parseNull(reset.getString("CXDH"));
				shdz = parseNull(reset.getString("JHDZ"));
				kflxr = parseNull(reset.getString("KFLXR"));
				cklxrdh = parseNull(reset.getString("CKLXRDH"));
				ck = parseNull(reset.getString("CK"));
			} else {
				throw new RuntimeException("��BINDIDû�ж�Ӧ�ļ�¼!");
			}

			tydw = "����������Ӧ�����޹�˾";
			//DAOUtil.getStringOrNull(conn, "SELECT CYS FROM BO_AKL_YD_P WHERE BINDID=?", bindid);
			list.add(packStrings("ǩ�յ��ţ�", qsdh));
			list.add(packStrings("���˵�λ��", parseNull(tydw)));
			list.add(packStrings("�ջ���λ��", shdw));
			list.add(packStrings("�ջ������ˣ�", shfzr + "   �绰��" + shfzrdh));
			list.add(packStrings("�ջ���ַ��", shdz));
			list.add(packStrings(ck + "�ⷿ�����ˣ�", kflxr + "   �绰��" + cklxrdh));
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ��ȡ��ͷ����.
	 * 
	 * @return
	 */
	public String[] getTableHeadContent() {
		return new String[] { "�ɹ�����", "�ͻ����	", "��Ʒ����", "�ͺ�", "����", "����", "��ע" };
	}

	public List<String[]> getTableBodyContent(Connection conn, String bindid) {
		PreparedStatement ps = null;
		ResultSet reset = null;

		List<String[]> list = new ArrayList<String[]>(30);
		try {
			String khcgdh = DAOUtil.getStringOrNull(conn, "SELECT KHCGDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			String khbh = DAOUtil.getStringOrNull(conn, "SELECT KH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			String fhrq = null;
			Date fhrqDate = DAOUtil.getDateOrNull(conn, "SELECT FHRQ FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			if (fhrqDate == null) {
				fhrq = dateformat.format(new Date());
			} else {
				fhrq = dateformat.format(fhrqDate);
			}

			ps = conn.prepareStatement("SELECT WLMC, XH, SJSL, BZ FROM BO_AKL_CKD_BODY WHERE BINDID=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String[] args = new String[7];
				args[0] = khcgdh;
				args[1] = khbh;
				args[2] = reset.getString("WLMC");
				args[3] = reset.getString("XH");
				args[4] = reset.getString("SJSL");
				args[5] = fhrq;
				args[6] = reset.getString("BZ");
				list.add(args);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			DBSql.close(ps, reset);
		}
	}

	public String[] packStrings(String field, String value) {
		return new String[] { field, value };
	}

	public String parseNull(String value) {
		return value == null ? "" : value;
	}

}
