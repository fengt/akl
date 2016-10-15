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
		setDescription("��Ʒ��Ϣ���̵�һ�ڵ��ϴ����¼����������ϱ������ɣ����������Ϣ����ת��.");
		setVersion("1.0.0");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook wb) {
		HSSFSheet sheet = wb.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();

		// ��������������.
		for (int i = ExcelUtil.EXCEL_START; i < lastRowNum; i++) {
			parseRow(sheet.getRow(i));
		}

		return wb;
	}

	/**
	 * ���н��д���.
	 * 
	 * @param row
	 */
	private void parseRow(HSSFRow row) {
		// ��ȡ������λ.
		HSSFCell jldwCell = row.getCell(EXCEL_JLDW_COL);
		jldwCell.setCellValue(DictionaryUtil.parseJLDWToNo(ExcelUtil.parseCellContentToString(jldwCell)));

		// ��ȡ�����.
		HSSFCell lbCell = row.getCell(EXCEL_LBID_COL);
		String lbStr = DictionaryUtil.parseChineseToNo(ExcelUtil.parseCellContentToString(lbCell));
		lbCell.setCellValue(lbStr);

		// �������ϱ��.
		HSSFCell wlbhCell = row.getCell(EXCEL_WLBH_COL);
		wlbhCell.setCellValue(RuleAPI.getInstance().executeRuleScript("WL@formatZero(5,@sequence:no)"));
	}

}
