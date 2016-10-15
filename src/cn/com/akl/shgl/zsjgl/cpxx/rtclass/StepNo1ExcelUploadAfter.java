package cn.com.akl.shgl.zsjgl.cpxx.rtclass;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.ExcelUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.RuleAPI;

public class StepNo1ExcelUploadAfter extends ExcelDownFilterRTClassA {

	private static final int EXCEL_WLBH_COL = 6;
	private static final int EXCEL_JLDW_COL = 6;
	private static final int EXCEL_LBID_COL = 0;

	public StepNo1ExcelUploadAfter(UserContext arg0) {
		super(arg0);
		setDescription("产品信息流程第一节点上传后事件，处理物料编码生成，导入基础信息中文转换.");
		setVersion("1.0.0");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook wb) {
		HSSFSheet sheet = wb.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();

		// 遍历所有内容行.
		for (int i = ExcelUtil.EXCEL_START; i < lastRowNum; i++) {
			parseRow(sheet.getRow(i));
		}

		return wb;
	}

	/**
	 * 对行进行处理.
	 * 
	 * @param row
	 */
	private void parseRow(HSSFRow row) {
		// 获取计量单位.
		HSSFCell jldwCell = row.getCell(EXCEL_JLDW_COL);
		jldwCell.setCellValue(DictionaryUtil.parseJLDWToNo(ExcelUtil.parseCellContentToString(jldwCell)));

		// 获取类别编号.
		HSSFCell lbCell = row.getCell(EXCEL_LBID_COL);
		String lbStr = DictionaryUtil.parseChineseToNo(ExcelUtil.parseCellContentToString(lbCell));
		lbCell.setCellValue(lbStr);

		// 设置物料编号.
		HSSFCell wlbhCell = row.getCell(EXCEL_WLBH_COL);
		wlbhCell.setCellValue(RuleAPI.getInstance().executeRuleScript("WL@formatZero(5,@sequence:no)"));
	}

}
