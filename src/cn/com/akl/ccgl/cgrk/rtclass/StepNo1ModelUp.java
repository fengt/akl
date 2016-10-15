package cn.com.akl.ccgl.cgrk.rtclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.ExcelUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;

public class StepNo1ModelUp extends ExcelDownFilterRTClassA {

	public StepNo1ModelUp(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("Execl上传数据校验。");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String rkdb = DBSql.getString("SELECT RKDB FROM BO_AKL_CCB_RKD_HEAD WHERE BINDID="+bindid, "RKDB");
		HSSFSheet sheet = arg0.getSheetAt(0);
		int maxRowNum = sheet.getLastRowNum();
		try {
			if(CgrkCnt.rkdb0.equals(rkdb)){//闪迪采购入库
				checkKHDDHAndLH(sheet, maxRowNum);
			}else if(CgrkCnt.rkdb3.equals(rkdb)){//其他入库
				checkWlxx(sheet, maxRowNum);
			}else if(CgrkCnt.rkdb5.equals(rkdb)){//旺店通入库
				checkWlxx(sheet, maxRowNum);
			}
			
		} catch(RuntimeException e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return null;
			//return ExcelUtil.getClearWorkBook(arg0);
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现错误，请联系管理员!");
			return null;
			//return ExcelUtil.getClearWorkBook(arg0);
		}
		return arg0;
	}
	
	/**
	 * 其他入库校验型号是否存在
	 * @param sheet
	 * @param maxRowNum
	 */
	private void checkWlxx(HSSFSheet sheet, int maxRowNum){
		List<String> list = new ArrayList<String>();
		for(int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++){
			HSSFRow row = sheet.getRow(i);
			String xh = parseXH(row);
			String queryXH = "SELECT COUNT(1)n FROM BO_AKL_WLXX WHERE XH='"+xh+"' AND HZBM='01065'";
			int n = DBSql.getInt(queryXH, "n");
			if(n == 0){
				list.add(xh);
			}
		}
		if(list.size() > 0){
			throw new RuntimeException("该型号"+list.toString()+"在系统中不存在，请先维护物料信息。");
		}
	}
	
	/**
	 * 转仓校验同一采购订单不能有重复的型号。
	 * @param sheet
	 * @param maxRowNum
	 */
	private void checkKHDDHAndLH(HSSFSheet sheet, int maxRowNum){
		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
		for(int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++){
			HSSFRow row = sheet.getRow(i);
			String khddh = parseKHDDH(row);
			String xh = parseLH(row);
			Map<String, Integer> xhMap = map.get(khddh);
			if(xhMap == null){
				xhMap = new HashMap<String, Integer>();
				xhMap.put(xh, i+1);
				map.put(khddh, xhMap);
			}else{
				Integer count = xhMap.get(xh);
				if(count == null){
					xhMap.put(xh, i+1);
				}else{
					throw new RuntimeException("EXCEL中同一客户订单号("+khddh+")，第"+count+"行与第"+(i+1)+"行的型号("+xh+")重复，请检查！");
				}
			}
		}
	}
	
	//获取客户订单号
	private String parseKHDDH(HSSFRow row){
		String khddh = ExcelUtil.parseCellContentToString(row.getCell(CgrkCnt.EXCEL_COL_KHDDH));
		if(khddh == null || "".equals(khddh.trim())){
			khddh = "";
		}
		return khddh;
	}
	//获取型号
	private String parseLH(HSSFRow row){
		String xh = ExcelUtil.parseCellContentToString(row.getCell(CgrkCnt.EXCEL_COL_LH));
		if(xh == null || "".equals(xh.trim())){
			xh = "";
		}
		return xh;
	}
	
	//获取（其他入库）型号
	private String parseXH(HSSFRow row){
		String xh = ExcelUtil.parseCellContentToString(row.getCell(CgrkCnt.EXCEL_COL_XH));
		if(xh == null || "".equals(xh.trim())){
			xh = "";
		}
		return xh;
	}

}
