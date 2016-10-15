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

	/** ����������� */
	private int EXCEL_COL_BJTM = 1;
	/** ��Ʒ������ */
	private int EXCEL_COL_KHSPBM = 2;
	/** �ͺ��� */
	private int EXCEL_COL_XH = 4;
	/** ����ԭ���� */
	private int EXCEL_COL_JCJG = 6;
	/** ������ */
	private int EXCEL_COL_JIEGUO = 7;
	/** �����Ϻ� */
	private int EXCEL_COL_XYLIAOH = 8;
	/** �����Ϻ����� */
	private int EXCEL_COL_XYLIAOHN = 9;
	/** �Ƿ���ȫ */
	private int EXCEL_COL_YORN = 10;
	/** ��� */
	private int EXCEL_COL_XIANGH = 13;

	// /�����Ƿ���ȫ\������\�����Ϻ�\�����Ϻ����Ƶ�,qjc 2015-02-17

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

		if ("�޼�".equals(pp) || "006006".equals(pp)) {
			Connection conn = null;
			try {
				conn = DBSql.open();

				// ��ʼ��������˳��.
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
				MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			} finally {
				DBSql.close(conn, null, null);
			}
		} else {
			MessageQueue.getInstance().putMessage(uid, "��Ʒ�����޵��빦��.");
		}
		return ExcelUtil.getClearWorkBook(wb);
	}

	/**
	 * ����ÿ������. ���¶�Ӧ�ļ����.
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

		// ����У��.
		if (validateEmtpy(jcjg, xianghao)) {
			throw new RuntimeException("�������ԭ������Ƿ���д���ڵ�" + (row.getRowNum() + 1) + "��!");
		}

		// ��ѯ��Ӧ�ļ�¼�Ƿ����.
		int id = DAOUtil.getIntOrNull(conn, "SELECT ID FROM BO_AKL_WXB_XS_RMASH_S WHERE BJTM=? AND XH=? AND BINDID=?", zjtm, xh, bindid);
		if (id == 0) {
			throw new RuntimeException("�����˲�ƥ��ļ�¼���ڵ�" + (row.getRowNum() + 1) + "��!");
		} else {
			// �����������Ϣ.�����Ƿ���ȫ\������\�����Ϻ�\�����Ϻ����Ƶ�,qjc 2015-02-17
			String jcjgNo = matchJcjg(conn, jcjg);
			if (jcjgNo == null) {
				throw new RuntimeException("�����˲���ʶ��ļ������" + jcjg + "���ڵ�" + (row.getRowNum() + 1) + "��!");
			} else {
				// �������ͺ�����
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
	 * ƥ����������.
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
	 * ƥ������.
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
	 * ��֤�Ƿ�Ϊ�ա�
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
