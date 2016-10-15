package cn.com.akl.dgrk;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.ExcelUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;

public class StepNo1ExcelFilter extends ExcelDownFilterRTClassA {

	private final static int START_ROW = 6;
	
	public StepNo1ExcelFilter(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("第一节点Excel过滤：转换计量单位");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook wb) {
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row = sheet.getRow(START_ROW);
		int lastRowNum = sheet.getLastRowNum();
		short lastCellNum = row.getLastCellNum();
		int dw = -1;
		for (int i = 0; i < lastCellNum; i++) {
			HSSFCell cell = row.getCell(i);
			String title = ExcelUtil.parseCellContentToString(cell);
			if(title.startsWith("DW<")){
				dw = i;
			}
		}
		if(dw != -1){
			for (int i = START_ROW+1; i <lastRowNum ; i++) {
				HSSFRow curRow = sheet.getRow(i);
				HSSFCell cell = curRow.getCell(dw);
				String dwStr = ExcelUtil.parseCellContentToString(cell);
				cell.setCellValue(DictionaryUtil.parseJLDWToNo(dwStr));
			}
		}
		return wb;
	}

}
