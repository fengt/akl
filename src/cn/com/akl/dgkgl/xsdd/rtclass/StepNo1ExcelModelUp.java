package cn.com.akl.dgkgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;

public class StepNo1ExcelModelUp extends ExcelDownFilterRTClassA {

	// 查询货主编号
	private static final String QUERY_HZBH = "SELECT HZBH FROM BO_AKL_DGXS_P WHERE BINDID=?";
	// 货主名称
	private static final String QUERY_HZMC = "SELECT HZMC FROM BO_AKL_DGXS_P WHERE BINDID=?";
	// 出库类型
	private static final String QUERY_CKLX = "SELECT CKLX FROM BO_AKL_DGXS_P WHERE BINDID=?";
	// 查询明细物料库存信息
	private static final String QUERY_WLKC = "SELECT a.WLBH, a.WLMC, a.GG, ISNULL(SUM(ISNULL(b.kwsl, 0))-(SELECT ISNULL(SUM(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = a.WLBH), SUM(ISNULL(b.kwsl, 0))) kysl, SUM(ISNULL(b.KWSL, 0)) KWSL, b.JLDW FROM BO_AKL_WLXX a join BO_AKL_DGKC_KCMX_S b on a.WLBH=b.WLBH WHERE a.HZBM=? AND a.XH=? AND b.JLDW=? AND b.SX in ('049088', '049090') AND a.WLZT in (0, 1, 4) GROUP BY a.WLBH, a.WLMC, a.GG, b.JLDW";
	// 其他出库
	private static final String QUERY_WLKCQT = "SELECT a.WLBH, a.WLMC, a.GG, ISNULL(SUM(ISNULL(b.KWSL, 0)), 0) kysl, SUM(ISNULL(b.KWSL, 0)) KWSL, b.JLDW FROM BO_AKL_WLXX a join BO_AKL_DGKC_KCMX_S b on a.WLBH=b.WLBH WHERE a.HZBM=? AND a.XH=? AND b.JLDW=? AND b.SX in ('049088', '049090') AND a.WLZT in (0, 1, 4) GROUP BY a.WLBH, a.WLMC, a.GG, b.JLDW";
	// 查询汇总物料库存信息
	//	private static final String QUERY_HZWLKC = "SELECT (SUM(ISNULL(RKSL, 0))-SUM(ISNULL(CKSL, 0))) as XYSL FROM BO_AKL_DGKC_KCHZ_P WHERE WLBH = ? AND DW = ?";

	// 查询物料信息
	private static final String QUERY_WLXX_COUNT = "SELECT COUNT(*) FROM BO_AKL_WLXX WHERE HZBM=? AND XH=?"; 

	public StepNo1ExcelModelUp(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("销售订单单身上传事件：根据型号和货主(单头中)来获取对应的物料号");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook wb) {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		Connection conn = null;
		try{
			conn  = DBSql.open();

			// 1、获取货主
			String hzbm = DAOUtil.getStringOrNull(conn, QUERY_HZBH, bindid);
			String hzmc = DAOUtil.getStringOrNull(conn, QUERY_HZMC, bindid);
			// 2、获取出库类型
			String cklx = DAOUtil.getStringOrNull(conn, QUERY_CKLX, bindid);
			if(hzbm==null){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请点击暂存在上传Excel!");
				return ExcelUtil.getClearWorkBook(wb);
			}

			HSSFSheet sheet = wb.getSheetAt(0);
			List<Hashtable<String,String>> list = new ArrayList<Hashtable<String,String>>();
			int lastRowNum = sheet.getLastRowNum();
			for (int i = ExcelUtil.EXCEL_START; i <= lastRowNum; i++){
				HSSFRow row = sheet.getRow(i);
				// 2、获取物料型号、客户采购单号、数量以及计量单位
				String khcgdh = parseKHCGDH(row);
				String xh = parseXH(row);
				int xssl = parseXSSL(row);
				String jldw = parseJLDW(conn, row);
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("khcgdh", khcgdh);
				hashtable.put("xh", xh);
				hashtable.put("jldw", jldw);
				hashtable.put("xssl", String.valueOf(xssl));
				list.add(hashtable);
			}
			validateKHCGDHAndWLBHAndJLDW(sheet, lastRowNum);
			for (int i = ExcelUtil.EXCEL_START; i <= lastRowNum; i++){
				HSSFRow row = sheet.getRow(i);
				processRowRecord(conn, hzbm, row, hzmc, cklx, bindid, list, i-6);
			}
			return wb;
		} catch(RuntimeException e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return ExcelUtil.getClearWorkBook(wb);
		} catch(Exception e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现错误，请联系管理员!");
			return ExcelUtil.getClearWorkBook(wb);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	private void validateKHCGDHAndWLBHAndJLDW(HSSFSheet sheet, int lastRowNum) {
		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
		// 校验提示：检查是否有相同的(物料号+单位+客户采购单号)
		for (int i = ExcelUtil.EXCEL_START; i <= lastRowNum; i++) {
			HSSFRow row = sheet.getRow(i);
			HSSFCell khcgdhCell = row.getCell(XSDDConstant.EXCEL_COL_KHCGDH);
			HSSFCell xhCell = row.getCell(XSDDConstant.EXCEL_COL_XH);
			HSSFCell jldwCell = row.getCell(XSDDConstant.EXCEL_COL_JLDW);
			String khcgdh = ExcelUtil.parseCellContentToString(khcgdhCell);
			if(khcgdh==null||"".equals(khcgdh.trim())){
				khcgdh="";
			}
			String xh = ExcelUtil.parseCellContentToString(xhCell);
			String jldw = ExcelUtil.parseCellContentToString(jldwCell);
			Map<String, Integer> jldwMap = map.get(khcgdh+xh);
			if(jldwMap==null){
				jldwMap = new HashMap<String, Integer>();
				jldwMap.put(jldw, i+1);
				map.put(khcgdh+xh, jldwMap);
			} else {
				Integer count = jldwMap.get(jldw);
				if(count==null){
					jldwMap.put(jldw, i+1);
				}
				else{
					throw new RuntimeException("EXCEL中 同一客户采购单号:"+khcgdh+" 第"+count+"行的型号: " + xh + "， 计量单位："+ jldw +" 与第"+(i+1)+"行重复，请检查！");
				}
			}
		}
	}
	/**
	 * 处理每行的记录
	 * @param conn
	 * @param hzbm
	 * @param row
	 * @throws SQLException
	 */
	private void processRowRecord(Connection conn, String hzbm, HSSFRow row, String hzmc, String cklx, int bindid, List<Hashtable<String,String>> list, int i)
			throws SQLException {
		//获取物料型号、数量以及计量单位
		String khcgdh = parseKHCGDH(row);
		String xh = parseXH(row);
		int xssl = parseXSSL(row);
		String jldw = parseJLDW(conn, row);

		int qtsl = 0;

		for(int j =0; j<=i;j++){
			Hashtable<String, String> hash = list.get(j);
			if(!hash.get("khcgdh").equals(khcgdh)&&hash.get("xh").equals(xh)&&hash.get("jldw").equals(jldw)){
				qtsl = Integer.parseInt(hash.get("xssl"))+qtsl;
			}
		}
		// 3、查询数据库中对应的物料信息
		PreparedStatement ps = null;
		ResultSet reset = null;
		try{
			if(!cklx.equals("054143")&&!cklx.equals("054144"))
				ps = conn.prepareStatement(QUERY_WLKC);
			else
				ps = conn.prepareStatement(QUERY_WLKCQT);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, hzbm, xh, DictionaryUtil.parseJLDWToNo(jldw));

			if(reset.next()) {
				// 4、将物料信息以及数量信息存入wb中
				// 根据物料表出物料基本信息
				int sksl = reset.getInt("KWSL") - reset.getInt("KYSL") + qtsl;
				int kysl = reset.getInt("KYSL") - qtsl;
				if(xssl > kysl){
					// 库位数量不足
					throw new RuntimeException("采购单号为:"+khcgdh+" 货主编码为: "+hzbm+", 货主名称为:"+hzmc+",  型号为: "+xh+", 计量单位为:"+jldw+" 的物料库存明细可用数量不足"+xssl+", 库存数量为"+reset.getInt("KWSL")+", 锁库数量+本单身中其他采购单此物料销售数量为"+sksl+", 可用数量为"+kysl+"！");
				}
				String QUERY_HZWLKC = "SELECT ISNULL(sum(ISNULL(a.PCSL, 0)), 0) PCSL FROM BO_AKL_DGKC_KCHZ_P a WHERE a.WLBH ='"+reset.getString("WLBH")+"' AND ZT='042022'";
				int xysl = DBSql.getInt(conn, QUERY_HZWLKC, "PCSL")-qtsl;
				String QUERY_HZWLKYKC = "SELECT ISNULL(SUM(ISNULL(a.PCSL, 0))-(SELECT SUM(ISNULL(XSSL, 0)) from BO_AKL_DGCKSK WHERE WLBH = a.WLBH), SUM(ISNULL(a.PCSL, 0))) kysl FROM BO_AKL_DGKC_KCHZ_P a WHERE a.WLBH ='"+reset.getString("WLBH")+"' AND ZT='042022' GROUP BY a.WLBH";
				kysl = DBSql.getInt(conn, QUERY_HZWLKYKC, "kysl")-qtsl;
				if(xssl > kysl&&!cklx.equals("054143")&&!cklx.equals("054144")){
					throw new RuntimeException("采购单号为:"+khcgdh+" 货主编码为: "+hzbm+", 货主名称为:"+hzmc+",  型号为: "+xh+", 计量单位为:"+jldw+" 的物料库存汇总可用数量不足"+xssl+", 库存数量为"+xysl+", 锁库数量+本单身中其他采购单此物料销售数量为"+sksl+", 可用数量为"+kysl+"！并且此物料在库存汇总与库存明细中数量不等！");
				}
				if(xssl> xysl){
					throw new RuntimeException("采购单号为:"+khcgdh+" 货主编码为: "+hzbm+", 货主名称为:"+hzmc+",  型号为: "+xh+", 计量单位为:"+jldw+" 的物料库存汇总库存数量不足"+xssl+", 库存数量为"+xysl+"！并且此物料在库存汇总与库存明细中数量不等！");
				}
				if(cklx.equals("054143")||cklx.equals("054144")){
					kysl = xysl;
				}
				// 获取当前能从此库存取得的最大销售数量
				//int fxssl=xssl>=0?reset.getInt("KWSL"):reset.getInt("KWSL")+xssl;
				//row.createCell(XSDDConstant.EXCEL_COL_CKBH).setCellValue(reset.getString("CKDM"));
				//row.createCell(XSDDConstant.EXCEL_COL_CKMC).setCellValue(reset.getString("CKMC"));
				row.createCell(XSDDConstant.EXCEL_COL_KHCGDH).setCellValue(khcgdh);
				row.createCell(XSDDConstant.EXCEL_COL_WLBH).setCellValue(reset.getString("WLBH"));
				row.createCell(XSDDConstant.EXCEL_COL_WLMC).setCellValue(reset.getString("WLMC"));
				row.createCell(XSDDConstant.EXCEL_COL_GG).setCellValue(reset.getString("GG"));
				row.createCell(XSDDConstant.EXCEL_COL_XH).setCellValue(xh);
				row.createCell(XSDDConstant.EXCEL_COL_XSSL).setCellValue(String.valueOf(xssl));
				row.createCell(XSDDConstant.EXCEL_COL_KYSL).setCellValue(kysl);
				row.createCell(XSDDConstant.EXCEL_COL_KCSL).setCellValue(reset.getInt("KWSL"));
				row.createCell(XSDDConstant.EXCEL_COL_JLDW).setCellValue(reset.getString("JLDW"));
			} else {
				int count = DAOUtil.getInt(conn, QUERY_WLXX_COUNT, hzbm, xh);
				if(count == 0){
					// 没有此物料
					throw new RuntimeException("货主编码为:"+hzbm+" ,货主名称为:"+hzmc+" ,没有型号为:"+xh+" ,计量单位为:"+jldw+" 的物料！");
				} else {
					// 此物料无法查询到
					throw new RuntimeException("库存中没有，货主编码为:"+hzbm+", 货主名称:"+hzmc+", 型号为:"+xh+", 计量单位为:"+jldw+" 的物料！");
				}
			}
		}finally{
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 获取型号
	 * @param row
	 * @return
	 */
	private String parseXH(HSSFRow row) {
		String xh = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_XH));
		if(xh == null || "".equals(xh.trim()))
			throw new RuntimeException("Excel文件中第"+(row.getRowNum()+1)+"行,未填写型号");
		else
			return xh;
	}

	/**
	 * 获取计量单位
	 * @param conn
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private String parseJLDW(Connection conn, HSSFRow row) throws SQLException {
		String jldw = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_JLDW));
		if(jldw == null || "".equals(jldw.trim()))
			throw new RuntimeException("Excel文件中第"+(row.getRowNum()+1)+"行，未填写计量单位!");
		return jldw;
	}

	/**
	 * 获取销售数量
	 * @param row
	 * @return
	 */
	private int parseXSSL(HSSFRow row) {
		String xssl = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_XSSL));
		if(xssl == null||"".equals(xssl.trim())){
			throw new RuntimeException("Excel文件中第"+(row.getRowNum()+1)+"行，未填写销售数量!");
		}
		return Double.valueOf(xssl).intValue();
	}

	/**
	 * 获取客户采购单号
	 * @param row
	 * @return
	 */
	private String parseKHCGDH(HSSFRow row) {
		String khcgdh = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_KHCGDH));
		if(khcgdh==null||"".equals(khcgdh.trim())){
			khcgdh="";
		}
		return khcgdh;
	}
}
