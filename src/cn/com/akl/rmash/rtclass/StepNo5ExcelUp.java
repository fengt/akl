package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo5ExcelUp extends ExcelDownFilterRTClassA {

	/** 备件条码号列 */
	private int EXCEL_COL_BJTM = 1;
	/** 商品编码列 */
	private int EXCEL_COL_KHSPBM = 2;
	/** 型号列 */
	private int EXCEL_COL_XH = 4;
	/** 故障原因列 */
	private int EXCEL_COL_JCJG = 6;
	/** 处理结果 */
	private int EXCEL_COL_JIEGUO = 7;
	/** 差异料号 */
	private int EXCEL_COL_XYLIAOH = 8;
	/** 差异料号名称 */
	private int EXCEL_COL_XYLIAOHN = 9;
	/** 是否齐全 */
	private int EXCEL_COL_YORN = 10;
	/** 箱号 */
	private int EXCEL_COL_XIANGH = 13;

	// /加入是否齐全\处理结果\差异料号\差异料号名称等,qjc 2015-02-17

	public StepNo5ExcelUp(UserContext arg0) {
		super(arg0);
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook wb) {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		String userName = getUserContext().getUserModel().getUserName();

		HSSFSheet sheet = wb.getSheetAt(0);
		int lastRowNum = sheet.getLastRowNum();

		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		String pp = ha.get("PP");

		if ("罗技".equals(pp) || "006006".equals(pp)) {
			Connection conn = null;
			try {
				conn = DBSql.open();

				// 初始化所有列顺序.
				EXCEL_COL_BJTM = ExcelUtil.findColumnNum(sheet, "BJTM");
				EXCEL_COL_KHSPBM = ExcelUtil.findColumnNum(sheet, "KHSPBH");
				EXCEL_COL_XH = ExcelUtil.findColumnNum(sheet, "XH");
				EXCEL_COL_JCJG = ExcelUtil.findColumnNum(sheet, "JCJG");
				EXCEL_COL_JIEGUO = ExcelUtil.findColumnNum(sheet, "CLLX");
				EXCEL_COL_XYLIAOH = ExcelUtil.findColumnNum(sheet, "CYLH");
				EXCEL_COL_XYLIAOHN = ExcelUtil.findColumnNum(sheet, "CYLHMC");
				EXCEL_COL_YORN = ExcelUtil.findColumnNum(sheet, "PJSFQQ");
				EXCEL_COL_XIANGH = ExcelUtil.findColumnNum(sheet, "XIANGH");

				for (int i = ExcelUtil.EXCEL_START; i <= lastRowNum; i++) {
					HSSFRow row = sheet.getRow(i);
					dealRow(conn, row, bindid, uid, userName);
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(uid, e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
			} finally {
				DBSql.close(conn, null, null);
			}
		} else {
			MessageQueue.getInstance().putMessage(uid, "此品牌暂无导入功能.");
		}
		return ExcelUtil.getClearWorkBook(wb);
	}

	/**
	 * 处理每行数据. 更新对应的检测结果.
	 * 
	 * @param conn
	 * @param row
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void dealRow(Connection conn, HSSFRow row, int bindid, String uid, String userName) throws SQLException {
		HSSFCell zjtmCell = row.getCell(EXCEL_COL_BJTM);
		HSSFCell khspbmCell = row.getCell(EXCEL_COL_KHSPBM);
		HSSFCell xhCell = row.getCell(EXCEL_COL_XH);
		HSSFCell jcjgCell = row.getCell(EXCEL_COL_JCJG);
		HSSFCell xianghCell = row.getCell(EXCEL_COL_XIANGH);
		HSSFCell YORN = row.getCell(EXCEL_COL_YORN);
		HSSFCell JIEGUO = row.getCell(EXCEL_COL_JIEGUO);
		HSSFCell XYLIAOH = row.getCell(EXCEL_COL_XYLIAOH);
		HSSFCell XYLIAOHN = row.getCell(EXCEL_COL_XYLIAOHN);

		String zjtm = ExcelUtil.parseCellContentToString(zjtmCell);
		String khspbm = ExcelUtil.parseCellContentToString(khspbmCell);
		String xh = ExcelUtil.parseCellContentToString(xhCell);
		String jcjg = ExcelUtil.parseCellContentToString(jcjgCell);
		String xianghao = ExcelUtil.parseCellContentToString(xianghCell);

		String yn = ExcelUtil.parseCellContentToString(YORN);
		String jg = ExcelUtil.parseCellContentToString(JIEGUO);
		String cyxh = ExcelUtil.parseCellContentToString(XYLIAOH);
		String cyxhn = ExcelUtil.parseCellContentToString(XYLIAOHN);

		// 必填校验.
		if (validateEmtpy(jcjg, xianghao)) {
			throw new RuntimeException("请检查故障原因、箱号是否填写，在第" + (row.getRowNum() + 1) + "行!");
		}

		// 查询对应的记录是否存在.
		int id = DAOUtil.getIntOrNull(conn, "SELECT ID FROM BO_AKL_WXB_XS_RMASH_S WHERE BJTM=? AND XH=? AND BINDID=?", zjtm, xh, bindid);
		if (id == 0) {
			throw new RuntimeException("出现了不匹配的记录，在第" + (row.getRowNum() + 1) + "行!");
		} else {
			// 存在则更新信息.更新是否齐全\处理结果\差异料号\差异料号名称等,qjc 2015-02-17
			String jcjgNo = matchJcjg(conn, jcjg);
			if (jcjgNo == null) {
				throw new RuntimeException("出现了不能识别的检测结果：" + jcjg + "，在第" + (row.getRowNum() + 1) + "行!");
			} else {
				// 填充差异型号名称
				if (cyxh != null && !cyxh.equals("")) {
					if (cyxhn == null || "".equals(cyxhn.trim())) {
						cyxhn = matchMaterialName(conn, cyxh);
					}
				}

				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XS_RMASH_S SET JCJG=?, XIANGH=?, JCR=?,PJSFQQ=?,CLLX=?,CYLH=?,CYLHMC=? WHERE ID=?",
						jcjgNo, xianghao, userName, DictionaryUtil.parseYesOrNoToNo(yn), jg, cyxh, cyxhn, id);
			}
		}
	}

	/**
	 * 匹配物料名称.
	 * 
	 * @param conn
	 * @param xh
	 * @return
	 * @throws SQLException
	 */
	public String matchMaterialName(Connection conn, String xh) throws SQLException {
		return DAOUtil.getStringOrNull(conn, "SELECT WLMC FROM BO_AKL_WLXX WHERE XH=?", xh);
	}

	/**
	 * 匹配检测结果.
	 * 
	 * @param conn
	 * @param str
	 * @return
	 * @throws SQLException
	 */
	public String matchJcjg(Connection conn, String str) throws SQLException {
		return DAOUtil.getStringOrNull(conn, "select XLBM from BO_AKL_DATA_DICT_S where (DLBM = '046' OR DLBM='048') AND ( XLMC=? OR XLBM=?)", str,
				str);
	}

	/**
	 * 验证是否为空。
	 * 
	 * @param str
	 * @return
	 */
	public boolean validateEmtpy(String... strs) {
		for (String str : strs) {
			if (str == null || str.trim().length() == 0) {
				return true;
			}
		}
		return false;
	}
}
