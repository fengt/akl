package cn.com.akl.dgkgl.xsck.web;

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
public class DGCKDExcelPrinterWeb extends ActionsoftWeb {

	public DGCKDExcelPrinterWeb() {
		super();
	}

	public DGCKDExcelPrinterWeb(UserContext arg0) {
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
			tableBottomContent = DAOUtil.getStringOrNull(conn, "SELECT SUM(ISNULL(SFSL, 0)) FROM BO_BO_AKL_DGCK_S WHERE BINDID=?", bindid);
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
			ps = conn.prepareStatement("SELECT XSDH, KHMC, LXRX1, LXRDH1, SHDZ1, LXRDH1 FROM BO_BO_AKL_DGCK_P WHERE BINDID=?");
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
				qsdh = parseNull(reset.getString("XSDH"));
				shdw = parseNull(reset.getString("KHMC"));
				shfzr = parseNull(reset.getString("LXRX1"));
				shfzrdh = parseNull(reset.getString("LXRDH1"));
				shdz = parseNull(reset.getString("SHDZ1"));
				kflxr = parseNull(reset.getString("LXRX1"));
				cklxrdh = parseNull(reset.getString("LXRDH1"));
				ck = "";
			} else {
				throw new RuntimeException("��BINDIDû�ж�Ӧ�ļ�¼!");
			}

			tydw = DAOUtil.getStringOrNull(conn, "SELECT CYS FROM BO_AKL_YD_P WHERE BINDID=?", bindid);
			
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
			String khbh = DAOUtil.getStringOrNull(conn, "SELECT KHMC FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
			String fhrq = null;
			Date fhrqDate = DAOUtil.getDateOrNull(conn, "SELECT FHRQ FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			if (fhrqDate == null) {
				fhrq = dateformat.format(new Date());
			} else {
				fhrq = dateformat.format(fhrqDate);
			}

			ps = conn.prepareStatement("SELECT KHCGDH, WLMC, XH, SFSL, BZ FROM BO_BO_AKL_DGCK_S WHERE BINDID=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String[] args = new String[7];
				args[0] = reset.getString("KHCGDH");
				args[1] = khbh;
				args[2] = reset.getString("WLMC");
				args[3] = reset.getString("XH");
				args[4] = reset.getString("SFSL");
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
