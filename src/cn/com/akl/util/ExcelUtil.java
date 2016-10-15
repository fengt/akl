package cn.com.akl.util;

import java.text.DecimalFormat;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class ExcelUtil {

	/** Excel内容起始行 */
	public static final int EXCEL_START = 6;

	/**
	 * 转换单元格内容为字符串.
	 * 
	 * @param cell
	 * @return
	 */
	public static String parseCellContentToString(Cell cell) {
		DecimalFormat df_int = new DecimalFormat("#");
		if (cell == null)
			return null;

		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				return "";
			case Cell.CELL_TYPE_BOOLEAN:
				return cell.getBooleanCellValue() ? "true" : "false";
			case Cell.CELL_TYPE_FORMULA:
				return cell.getCellFormula();
			case Cell.CELL_TYPE_NUMERIC:
				return df_int.format(cell.getNumericCellValue());
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			default:
				return null;
		}
	}

	/**
	 * 转换单元格内容为Double.
	 * 
	 * @param cell
	 * @return
	 */
	public static Double parseCellContentToDouble(Cell cell) {
		if (cell == null)
			return null;

		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				return null;
				// case Cell.CELL_TYPE_FORMULA: return cell.getCellFormula();
			case Cell.CELL_TYPE_NUMERIC:
				return cell.getNumericCellValue();
			default:
				return null;
		}
	}

	/**
	 * 清空模板中的所有记录
	 * 
	 * @param wb
	 * @return
	 */
	public static HSSFWorkbook getClearWorkBook(HSSFWorkbook wb) {
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFWorkbook wb2 = new HSSFWorkbook();
		HSSFSheet sheet2 = wb2.createSheet();
		for(int i=0; i<ExcelUtil.EXCEL_START; i++){
			HSSFRow row = sheet.getRow(i);
			HSSFRow row2 = sheet2.createRow(i);
			for(int j=0; j<row.getLastCellNum(); j++){
				row2.createCell(j).setCellValue(ExcelUtil.parseCellContentToString(row.getCell(j)));
			}
		}
		return wb2;
	}

	/**
	 * 快速设置单元格内容.
	 * 
	 * @param row
	 * @param col
	 * @param str
	 */
	public static void setCellValue(HSSFRow row, int col, String str) {
		if (row == null) {
			return;
		}

		HSSFCell cell = row.getCell(col);
		if (cell == null) {
			cell = row.createCell(col);
		}
		cell.setCellValue(str);
	}

	/**
	 * 从标题行的所有列中找到对应的字符串，并返回列号。
	 * 
	 * @param sheet
	 * @param columnContent
	 * @return
	 */
	public static int findColumnNum(HSSFSheet sheet, String columnContent) {
		HSSFRow row = sheet.getRow(EXCEL_START - 1);
		short lastCellNum = row.getLastCellNum();
		for (int i = 0; i < lastCellNum; i++) {
			HSSFCell cell = row.getCell(i);
			String text = parseCellContentToString(cell);
			if (text != null) {
				if (text.startsWith(columnContent)) {
					return i;
				}
			}
		}
		return -1;
	}

}
