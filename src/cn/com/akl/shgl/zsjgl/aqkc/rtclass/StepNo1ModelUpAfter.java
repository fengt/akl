package cn.com.akl.shgl.zsjgl.aqkc.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;

public class StepNo1ModelUpAfter extends ExcelDownFilterRTClassA {

	/** ��Ŀ�����. */
	private static final int EXCEL_COL_XMLB = 0;
	/** �ֿ�������. */
	private static final int EXCEL_COL_CKMC = 2;
	private static final int EXCEL_COL_CKDM = 3;
	/** �ͺ���. */
	private static final int EXCEL_COL_XH = 4;
	private static final int EXCEL_COL_WLBH = 1;
	private static final int EXCEL_COL_WLMC = 7;
	/** ������. */
	private static final int EXCEL_COL_SX = 8;
	private static final int EXCEL_COL_SXID = 5;
	/** ���������. */
//	private static final int EXCEL_COL_KCSX = 8;
	/** ���������. */
	private static final int EXCEL_COL_KCXX = 9;

	public StepNo1ModelUpAfter(UserContext arg0) {
		super(arg0);
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		HSSFSheet sheet = arg0.getSheetAt(0);
		int maxRowNum = sheet.getLastRowNum();

		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		Connection conn = null;
		try {
			conn = DBSql.open();
			for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
				HSSFRow row = sheet.getRow(i);
				// �����к�
				row.createCell(XSDDConstant.EXCEL_COL_DH).setCellValue(i - 5);
				// ����м�¼
				fillRow(conn, row, hashtable);
			}
			return arg0;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return ExcelUtil.getClearWorkBook(arg0);
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա!", true);
			return ExcelUtil.getClearWorkBook(arg0);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ����м�¼�����ݿͻ����ϱ�Ż��ͺţ�ѯ��������������.
	 * 
	 * @param row
	 * @param khspbhCell
	 * @param xdslCell
	 * @param dfslCell
	 * @throws SQLException
	 */
	private void fillRow(Connection conn, HSSFRow row, Hashtable<String, String> hashtable) throws SQLException {
		int showRowNum = row.getRowNum();

		// ƥ����Ŀ���.
		HSSFCell xmlbCell = row.getCell(EXCEL_COL_XMLB);
		String xmlb = ExcelUtil.parseCellContentToString(xmlbCell);
		String xmlbBm = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERE DLBM='061' AND XLMC=?", xmlb);
		if (xmlbBm == null || xmlbBm.trim().equals("")) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬��Ŀ���ƥ�䲻��.");
		} else {
			ExcelUtil.setCellValue(row, EXCEL_COL_XMLB, xmlbBm);
		}

		// ƥ��ֿ�.
		String ckmc = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CKMC));
		String ckbm = DAOUtil.getStringOrNull(conn, "SELECT KFCKBM FROM BO_AKL_KFCK WHERE KFCKMC=?", ckmc);
		if (ckbm == null || ckbm.trim().equals("")) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬�ֿ�����ƥ�䲻��.");
		} else {
			ExcelUtil.setCellValue(row, EXCEL_COL_CKDM, ckbm);
		}

		// ƥ������.
		String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_XH));
		String wlbh = DAOUtil.getStringOrNull(conn, "SELECT WLBH FROM BO_AKL_CPXX WHERE XMLB=? AND LPN8=?", xmlbBm, xh);
		if (wlbh == null || wlbh.trim().equals("")) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬�����ͺ�ƥ�䲻��.");
		} else {
			String wlmc = DAOUtil.getStringOrNull(conn, "SELECT WLMC FROM BO_AKL_CPXX WHERE WLBH=?", wlbh);
			ExcelUtil.setCellValue(row, EXCEL_COL_WLBH, wlbh);
			ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, wlmc);
		}

		// ƥ������.
		String sx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SX));
		String sxbm = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERE DLBM='066' AND XLMC=?", sx);
		if (sxbm == null || sxbm.trim().equals("")) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬����ƥ�䲻��.");
		} else {
			ExcelUtil.setCellValue(row, EXCEL_COL_SXID, sxbm);
		}

		// �����������֤.
		int kcsxInt = 0;
		int kcxxInt = 0;
//		String kcsx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_KCSX));
		String kcxx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_KCXX));
		/*if (kcsx == null || kcsx.trim().equals("")) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬������޲���Ϊ��.");
		}
		try {
			kcsxInt = Integer.parseInt(kcsx);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬��������޷�תΪ���֣������Ƿ������������������ַ�!");
		}*/
		if (kcxx == null || kcxx.trim().equals("")) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬������޲���Ϊ��.");
		}
		try {
			kcxxInt = Integer.parseInt(kcxx);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬��������޷�תΪ���֣������Ƿ������������������ַ�!");
		}
		
		/*if (kcsxInt < kcxxInt) {
			throw new RuntimeException("Excel��" + showRowNum + "�г������⣬������޲���С�ڿ������!");
		}*/
	}
}
