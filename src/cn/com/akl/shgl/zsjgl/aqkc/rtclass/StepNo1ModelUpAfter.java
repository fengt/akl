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

	/** 项目类别列. */
	private static final int EXCEL_COL_XMLB = 0;
	/** 仓库名称列. */
	private static final int EXCEL_COL_CKMC = 2;
	private static final int EXCEL_COL_CKDM = 3;
	/** 型号列. */
	private static final int EXCEL_COL_XH = 4;
	private static final int EXCEL_COL_WLBH = 1;
	private static final int EXCEL_COL_WLMC = 7;
	/** 属性列. */
	private static final int EXCEL_COL_SX = 8;
	private static final int EXCEL_COL_SXID = 5;
	/** 库存上限列. */
//	private static final int EXCEL_COL_KCSX = 8;
	/** 库存下限列. */
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
				// 插入行号
				row.createCell(XSDDConstant.EXCEL_COL_DH).setCellValue(i - 5);
				// 填充行记录
				fillRow(conn, row, hashtable);
			}
			return arg0;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return ExcelUtil.getClearWorkBook(arg0);
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现错误，请联系管理员!", true);
			return ExcelUtil.getClearWorkBook(arg0);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 填充行记录，根据客户物料编号或型号，询单数量，答复数量.
	 * 
	 * @param row
	 * @param khspbhCell
	 * @param xdslCell
	 * @param dfslCell
	 * @throws SQLException
	 */
	private void fillRow(Connection conn, HSSFRow row, Hashtable<String, String> hashtable) throws SQLException {
		int showRowNum = row.getRowNum();

		// 匹配项目类别.
		HSSFCell xmlbCell = row.getCell(EXCEL_COL_XMLB);
		String xmlb = ExcelUtil.parseCellContentToString(xmlbCell);
		String xmlbBm = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERE DLBM='061' AND XLMC=?", xmlb);
		if (xmlbBm == null || xmlbBm.trim().equals("")) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，项目类别匹配不上.");
		} else {
			ExcelUtil.setCellValue(row, EXCEL_COL_XMLB, xmlbBm);
		}

		// 匹配仓库.
		String ckmc = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_CKMC));
		String ckbm = DAOUtil.getStringOrNull(conn, "SELECT KFCKBM FROM BO_AKL_KFCK WHERE KFCKMC=?", ckmc);
		if (ckbm == null || ckbm.trim().equals("")) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，仓库名称匹配不上.");
		} else {
			ExcelUtil.setCellValue(row, EXCEL_COL_CKDM, ckbm);
		}

		// 匹配物料.
		String xh = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_XH));
		String wlbh = DAOUtil.getStringOrNull(conn, "SELECT WLBH FROM BO_AKL_CPXX WHERE XMLB=? AND LPN8=?", xmlbBm, xh);
		if (wlbh == null || wlbh.trim().equals("")) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，物料型号匹配不上.");
		} else {
			String wlmc = DAOUtil.getStringOrNull(conn, "SELECT WLMC FROM BO_AKL_CPXX WHERE WLBH=?", wlbh);
			ExcelUtil.setCellValue(row, EXCEL_COL_WLBH, wlbh);
			ExcelUtil.setCellValue(row, EXCEL_COL_WLMC, wlmc);
		}

		// 匹配属性.
		String sx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_SX));
		String sxbm = DAOUtil.getStringOrNull(conn, "SELECT XLBM FROM BO_AKL_DATA_DICT_S WHERE DLBM='066' AND XLMC=?", sx);
		if (sxbm == null || sxbm.trim().equals("")) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，属性匹配不上.");
		} else {
			ExcelUtil.setCellValue(row, EXCEL_COL_SXID, sxbm);
		}

		// 库存上下限验证.
		int kcsxInt = 0;
		int kcxxInt = 0;
//		String kcsx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_KCSX));
		String kcxx = ExcelUtil.parseCellContentToString(row.getCell(EXCEL_COL_KCXX));
		/*if (kcsx == null || kcsx.trim().equals("")) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，库存上限不能为空.");
		}
		try {
			kcsxInt = Integer.parseInt(kcsx);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，库存上限无法转为数字，请检查是否包含除数字外的其他字符!");
		}*/
		if (kcxx == null || kcxx.trim().equals("")) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，库存上限不能为空.");
		}
		try {
			kcxxInt = Integer.parseInt(kcxx);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，库存下限无法转为数字，请检查是否包含除数字外的其他字符!");
		}
		
		/*if (kcsxInt < kcxxInt) {
			throw new RuntimeException("Excel第" + showRowNum + "行出现问题，库存上限不能小于库存下限!");
		}*/
	}
}
