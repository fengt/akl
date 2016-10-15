package cn.com.akl.ccgl.xsck.web.excel;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * ���ⵥExcel������.
 * 
 * @author huangming
 *
 */
public class CKDExcelGenerator {

	/**
	 * �����и߶�.
	 */
	private static float TITLE_ROW_HEIGHT = 25.25f;
	/**
	 * ��ͷ�и߶�.
	 */
	private static float HEAD_ROW_HEIGHT = 17.75f;
	/**
	 * ��ͷ�·��ո��и߶�.
	 */
	private static float HEAD_ROW_DOWN_SPACE = 12.75f;
	/**
	 * ��ͷ�и߶�.
	 */
	private static float TABLE_HEAD_ROW_HEIGHT = 17.75f;
	/**
	 * ��������и߶�.
	 */
	private static float TABLE_BODY_ROW_HEIGHT = 23.75f;
	/**
	 * ���ײ��и߶�.
	 */
	private static float TABLE_BOTTOM_ROW_HEIGHT = 12.75f;

	
	public CKDExcelGenerator() {
		super();
	}

	/**
	 * ��ȡExcel.
	 * 
	 * @return
	 */
	public HSSFWorkbook getWorkbook(List<String[]> formHeadContent, String[] tableHeadContent, List<String[]> tableBodyContent, String tableBottomContent) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		HSSFRow titleRow = sheet.createRow(0);
		HSSFCell titleCell = titleRow.createCell(0);

		setSheetColumnWidth(sheet);

		int currentRowNum = 0;
		currentRowNum = setSheetTitle(wb, sheet, titleRow, titleCell, "����������Ӧ������˾���ⵥ");

		HSSFRow row = sheet.createRow(++currentRowNum);
		HSSFCellStyle headCellStyle = getFormHeadCellStyle(wb);

		for (String[] strings : formHeadContent) {
			HSSFCell fieldCell = row.createCell(0);
			HSSFCell valueCell = row.createCell(1);

			fieldCell.setCellStyle(headCellStyle);
			valueCell.setCellStyle(headCellStyle);

			fieldCell.setCellValue(strings[0]);
			valueCell.setCellValue(strings[1]);

			row.setHeightInPoints(HEAD_ROW_HEIGHT);
			row = sheet.createRow(++currentRowNum);
		}

		row.setHeightInPoints(HEAD_ROW_DOWN_SPACE);
		row = sheet.createRow(++currentRowNum);
		row.setHeightInPoints(HEAD_ROW_DOWN_SPACE);

		HSSFCellStyle tableHeadCellStyle = getTableHeadCellStyle(wb);
		row = sheet.createRow(++currentRowNum);
		row.setHeightInPoints(TABLE_HEAD_ROW_HEIGHT);
		for (int i = 0; i < tableHeadContent.length; i++) {
			HSSFCell tableHeadCell = row.createCell(i);
			tableHeadCell.setCellStyle(tableHeadCellStyle);
			tableHeadCell.setCellValue(tableHeadContent[i]);
		}

		row = sheet.createRow(++currentRowNum);
		HSSFCellStyle tableBodyCellStyle = getTableBodyCellStyle(wb);
		for (String[] strings : tableBodyContent) {
			for (int i = 0; i < tableHeadContent.length; i++) {
				HSSFCell tableBodyCell = row.createCell(i);
				tableBodyCell.setCellStyle(tableBodyCellStyle);
				tableBodyCell.setCellValue(strings[i]);
			}
			row.setHeightInPoints(TABLE_BODY_ROW_HEIGHT);
			row = sheet.createRow(++currentRowNum);
		}

		row.setHeightInPoints(TABLE_BOTTOM_ROW_HEIGHT);
		HSSFCellStyle tableBottomCellStyle = getTableBottomCellStyle(wb);
		HSSFCell tableBottomCell = row.createCell(4);
		tableBottomCell.setCellStyle(tableBottomCellStyle);
		tableBottomCell.setCellValue(tableBottomContent);

		return wb;
	}

	/**
	 * ��ȡ���ײ���ʽ.
	 * 
	 * @param wb
	 * @return
	 */
	private HSSFCellStyle getTableBottomCellStyle(HSSFWorkbook wb) {
		short FONT_SIZE = 10;
		HSSFFont font = wb.createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints(FONT_SIZE);

		HSSFCellStyle style = wb.createCellStyle();
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);

		style.setFont(font);
		return style;
	}

	/**
	 * ��ȡ��������ʽ.
	 * 
	 * @param wb
	 * @return
	 */
	private HSSFCellStyle getTableBodyCellStyle(HSSFWorkbook wb) {
		short FONT_SIZE = 10;
		HSSFFont font = wb.createFont();
		font.setFontName("����");
		font.setFontHeightInPoints(FONT_SIZE);

		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
		style.setWrapText(true);
		style.setFont(font);

		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);

		return style;
	}

	/**
	 * ��ȡ��ͷ���ֶ���ʽ.
	 * 
	 * @param wb
	 * @return
	 */
	private HSSFCellStyle getTableHeadCellStyle(HSSFWorkbook wb) {
		short FONT_SIZE = 12;
		HSSFFont font = wb.createFont();
		font.setFontName("����");
		font.setFontHeightInPoints(FONT_SIZE);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);

		style.setFont(font);
		return style;
	}

	/**
	 * ���ñ���п�.
	 * 
	 * @param sheet
	 */
	public void setSheetColumnWidth(HSSFSheet sheet) {
		sheet.setColumnWidth(0, 24 * 270);
		sheet.setColumnWidth(1, 11 * 270);
		sheet.setColumnWidth(2, 15 * 270);
		sheet.setColumnWidth(3, 19 * 270);
		sheet.setColumnWidth(4, 7 * 270);
		sheet.setColumnWidth(5, 15 * 270);
		sheet.setColumnWidth(6, 9 * 270);
	}

	/**
	 * ���ñ�����.
	 * 
	 * @param workbook
	 * @param sheet
	 * @param row
	 * @param cell
	 * @param title
	 * @return
	 */
	public int setSheetTitle(HSSFWorkbook workbook, HSSFSheet sheet, HSSFRow row, HSSFCell cell, String title) {

		int height = 0;
		int width = 4;
		short FONT_SIZE = 16;
		int rowNum = row.getRowNum();
		if (cell == null) {
			cell = row.createCell(0);
		}

		HSSFFont font = workbook.createFont();
		font.setFontName("����");
		font.setFontHeightInPoints(FONT_SIZE);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setFont(font);

		row.setHeightInPoints(TITLE_ROW_HEIGHT);

		CellRangeAddress titleRange = new CellRangeAddress(rowNum, rowNum + height, cell.getColumnIndex(), cell.getColumnIndex() + width);
		sheet.addMergedRegion(titleRange);

		cell.setCellStyle(style);
		cell.setCellValue("����������Ӧ������˾���ⵥ");

		return row.getRowNum() + height;
	}

	/**
	 * ��ȡ��ͷ��һ�е�CellStyle.
	 * 
	 * @param workbook
	 * @return
	 */
	private HSSFCellStyle getFormHeadCellStyle(HSSFWorkbook workbook) {
		short FONT_SIZE = 12;
		HSSFFont font = workbook.createFont();
		font.setFontName("����");
		font.setFontHeightInPoints(FONT_SIZE);

		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);

		style.setFont(font);
		return style;
	}

}
