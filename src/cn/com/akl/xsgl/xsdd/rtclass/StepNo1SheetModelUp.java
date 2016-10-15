package cn.com.akl.xsgl.xsdd.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.ExcelUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;

public class StepNo1SheetModelUp extends ExcelDownFilterRTClassA {

	/**
	 * ��ѯ����Ĭ�ϵ�����ָ����.
	 */
	private static final String QUERY_WLXX_XSZDJ_DEFAULT = "SELECT TOP 1 XSGHJ FROM BO_AKL_JGGL WHERE WLBH=? ORDER BY UPDATEDATE DESC";
	/**
	 * ��ѯ�ͻ�ָ������ָ����.
	 */
	private static final String QUERY_WLXX_XSZDJ_KH_DEFAULT = "SELECT TOP 1 XSGHJ FROM BO_AKL_KH_JGGL_P a JOIN BO_AKL_KH_JGGL_S b ON a.BINDID=b.BINDID AND KHBH=? WHERE WLBH=? ORDER BY b.UPDATEDATE DESC";
	/**
	 * ��ѯ���ϵ���;�����������������������.
	 */
	private static final String QUERY_WLSL = "SELECT a.WLBH,SUM(CASE WHEN ZT='042023' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as ZTSL, SUM(CASE WHEN ZT='042022' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as ZCSL FROM BO_AKL_KC_KCMX_S a, BO_AKL_KC_KCHZ_P b WHERE a.WLBH=b.WLBH AND a.PCH=b.PCH AND a.WLBH=? GROUP BY a.WLBH";
	/**
	 * ��ѯ���ϵ���������.
	 */
	private static final String QUERY_SDSL = "SELECT SUM(SDSL) AS SDSL FROM BO_AKL_KC_SPPCSK WHERE WLBH=?";
	/**
	 * ��ѯ�ͻ����ϱ�Ŷ�Ӧ�����Ϻ�.
	 */
	private static final String QUERY_WLBH = "SELECT YKSPSKU FROM BO_AKL_KHSPBMGL WHERE KHBM=? AND KHSPSKU=?";
	/**
	 * ��ѯ������Ϣ.
	 */
	private static final String QUERY_WLXX = "SELECT WLMC,GG,XH,DW,TJ,ZL FROM BO_AKL_WLXX WHERE WLBH=? AND HZBM='01065' ";
	/**
	 * ��ѯ�ͻ����.
	 */
	private static final String QUERY_KHBH = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ������Ϣͨ���ͺ�.
	 */
	private static final String QUERY_WLXX_XH = "SELECT XH, WLBH, HZBM, WLMC, GG, XH, DW, TJ, ZL FROM BO_AKL_WLXX WHERE XH=? AND HZBM='01065' ";

	public StepNo1SheetModelUp(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���۶��������ʱ�������У��У��,���۶������������뱾�����ݣ��û�ֻ����д���ͺŻ�ͻ���Ʒ���&Ѱ������&���������������ݾ��Ӻ�̨��ȡ");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		HSSFSheet sheet = arg0.getSheetAt(0);
		int maxRowNum = sheet.getLastRowNum();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		Connection conn = null;
		try {
			conn = DBSql.open();

			String khbh = DAOUtil.getStringOrNull(conn, QUERY_KHBH, bindid);
			if (khbh == null) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��ѡ��ͻ���ţ����ҵ���ݴ�!", true);
				return ExcelUtil.getClearWorkBook(arg0);
			}

			for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
				HSSFRow row = sheet.getRow(i);
				// �����к�
				row.createCell(XSDDConstant.EXCEL_COL_DH).setCellValue(i - 5);
				// ����м�¼
				fillRow(conn, row, khbh);
			}
			return arg0;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return ExcelUtil.getClearWorkBook(arg0);
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա!", true);
			return ExcelUtil.getClearWorkBook(arg0);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ����м�¼�����ݿͻ����ϱ�Ż��ͺţ�ѯ��������������.
	 * 
	 * @param row
	 * @param khspbhCell
	 * @param xdslCell
	 * @param dfslCell
	 * @throws SQLException
	 */
	public void fillRow(Connection conn, HSSFRow row, String khbh) throws SQLException {
		String khspbh = parseKHSPBH(row);
		String xdslStr = parseXDSL(row);
		String dfslStr = parseDFSL(row);
		String wlxh = parseWLXH(row);
		String wlbh = parseWLBH(row);
		String sdzt = parseSDZT(row);
		int xdsl = Integer.parseInt(xdslStr);
		int dfsl = Integer.parseInt(dfslStr);

		// ����״̬
		row.createCell(XSDDConstant.EXCEL_COL_SDZT).setCellValue(sdzt);

		// ���㶩��������
		double mzl = dfsl / xdsl * 100;
		row.createCell(XSDDConstant.EXCEL_COL_MZL).setCellValue(mzl);

		// 1��������У��
		if ((khspbh == null || "".equals(khspbh.trim())) && (wlbh == null || "".equals(wlbh.trim())) && (wlxh == null || "".equals(wlxh.trim()))) {
			throw new RuntimeException("��" + (row.getRowNum() + 1) + "����������д���ϱ�š��ͻ����ϱ�Ż����ͺ��е�һ��!");
		}

		if (wlxh != null && !"".equals(wlxh)) {
			// �����ͺ�
			wlbh = putWLXXForWLXH(conn, row, wlxh);
			if (wlbh == null || "".equals(wlbh.trim())) {
				throw new RuntimeException("��" + (row.getRowNum() + 1) + "��,�޷����������ͺ�:" + wlxh + "���ҵ����ϱ�ţ���ȷ���Ƿ���ڴ��ͺ�!");
			}
		} else if (khspbh != null && !"".equals(khspbh.trim())) {
			// �ͻ���Ʒ���
			wlbh = putKHWLXX(conn, row, khbh, khspbh);
			if (wlbh == null || "".equals(wlbh.trim())) {
				throw new RuntimeException("��" + (row.getRowNum() + 1) + "��,�޷����ݿͻ����:" + khbh + "���ͻ����ϱ��:" + khspbh + "�ҵ���Ӧ���ϱ�ţ���ȷ���Ƿ���ά���˹�ϵ!");
			}
			putWLXXForWLBH(conn, row, wlbh);
		} else if (wlbh != null && !"".equals(wlbh.trim())) {
			// ���ϱ��
			putWLXXForWLBH(conn, row, wlbh);
		}

		putWLSLCell(conn, row, wlbh);
		putPOSInfo(conn, row, wlbh);
		putFLInfo(conn, row, wlbh);

		// 4����ѯ���ϵ����ۼ۸�
		BigDecimal xszdj = DAOUtil.getBigDecimalOrNull(conn, QUERY_WLXX_XSZDJ_KH_DEFAULT, khbh, wlbh);
		if (xszdj == null) {
			xszdj = DAOUtil.getBigDecimalOrNull(conn, QUERY_WLXX_XSZDJ_DEFAULT, wlbh);
			if (xszdj == null) {
				throw new RuntimeException("���ϱ�ţ�" + wlbh + "û��ά������ָ���ۣ�");
			}
		}
		row.createCell(XSDDConstant.EXCEL_COL_XSZDJ).setCellValue(xszdj.doubleValue());
	}

	/**
	 * ���POS��Ϣ.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private void putPOSInfo(Connection conn, HSSFRow row, String wlbh) throws SQLException {
		// 1.��ȡPOS������
		String posid = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_POSID));
		if (posid == null || posid.trim().equals("")) {
			// ���POS��Ϣ
			row.createCell(XSDDConstant.EXCEL_COL_POSFALX).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_POSID).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_POSJE).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_POSMC).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_POSZCDJ).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_POSZCSL).setCellValue("");
		} else {
			String posje = null;
			String poszcdj = null;
			String poszcsl = null;
			String posmc = null;

			// 2.��ȡPOS����
			String posfalx = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_POSFALX));
			if (posfalx == null || posfalx.equals("")) {
				throw new RuntimeException("��" + (row.getRowNum() + 1) + "�У�POS��������δ��д");
			}

			// 3.�����ݿ���Ҷ�ӦPOS���������֧�ֵ��ۺͽ��
			if (posfalx.equals(XSDDConstant.POS_FALX_FA) || posfalx.startsWith("����") || posfalx.endsWith("����")) {
				posfalx = XSDDConstant.POS_FALX_FA;
				// ��POS֧����������������ʱ
				// ��ȡ��Ӧ����
				poszcsl = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_POSZCSL));
				if (poszcsl == null || poszcsl.trim().equals("")) {
					poszcsl = parseDFSL(row);
				}

				int poszcslInt;
				try {
					poszcslInt = Integer.parseInt(poszcsl);
				} catch (NumberFormatException e) {
					throw new RuntimeException((row.getRowNum() + 1) + "�У�POS֧��������ʽ�����⣬��������д����");
				}

				try {
					String dfsl = parseDFSL(row);
					if (Integer.parseInt(dfsl) < poszcslInt) {
						throw new RuntimeException((row.getRowNum() + 1) + "�У�POS֧��������ֵ�����˴�������ֵ");
					}
				} catch (NumberFormatException e) {
					throw new RuntimeException((row.getRowNum() + 1) + "�У�POS֧��������ʽ�����⣬��������д����");
				}

				PreparedStatement stat = null;
				ResultSet reset = null;
				try {
					stat = conn
							.prepareStatement("SELECT h.POSMC, b.POSDJ, b.POSSL-ISNULL(b.YSYSL,0) as POSSL FROM BO_AKL_WXB_XS_POS_HEAD h, BO_AKL_WXB_XS_POS_BODY b WHERE h.BINDID=b.BINDID AND h.POSBH=? AND b.WLBH=?");
					reset = DAOUtil.executeFillArgsAndQuery(conn, stat, posid, wlbh);
					if (reset.next()) {
						if (reset.getInt("POSSL") < poszcslInt) {
							throw new RuntimeException((row.getRowNum() + 1) + "�У���д��POS֧�������������������" + reset.getInt("POSSL"));
						}

						BigDecimal poszcdjBd = reset.getBigDecimal("POSDJ");
						BigDecimal posjeBd = poszcdjBd.multiply(new BigDecimal(poszcslInt));
						posjeBd = posjeBd.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
						posmc = reset.getString("POSMC");
						poszcdj = poszcdjBd.toString();
						posje = posjeBd.toString();
					} else {
						throw new RuntimeException((row.getRowNum() + 1) + "�У�POS�ţ�" + posid + " ������ ���� ��֧�ִ�����");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					DBSql.close(stat, reset);
				}
			} else if (posfalx.equals(XSDDConstant.POS_FALX_ZJC) || posfalx.startsWith("�ʽ��") || posfalx.endsWith("�ʽ��")) {
				// ��POS֧������Ϊ�ʽ��
				// ��ȡ��Ӧ���
				posfalx = XSDDConstant.POS_FALX_ZJC;
				posje = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_POSJE));
				poszcsl = parseDFSL(row);

				PreparedStatement stat = null;
				ResultSet reset = null;
				try {
					stat = conn.prepareStatement("SELECT FABH as POSMC, POSJE-YSYJE as POSJE FROM BO_AKL_POS_MXB WHERE FABH=?");
					reset = DAOUtil.executeFillArgsAndQuery(conn, stat, posid);
					if (reset.next()) {
						if (posje == null || posje.trim().equals("")) {
							BigDecimal posjeBd = reset.getBigDecimal("POSJE");
							BigDecimal poszcdjBd = posjeBd.divide(new BigDecimal(poszcsl), XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
							posje = posjeBd.toString();
							poszcdj = poszcdjBd.toString();
						} else {
							BigDecimal posjeBd = reset.getBigDecimal("POSJE");
							if (new BigDecimal(posje).doubleValue() > posjeBd.doubleValue()) {
								throw new RuntimeException((row.getRowNum() + 1) + "�У�POS֧�ֽ��������֧�ֽ�" + posjeBd.doubleValue());
							} else {
								BigDecimal poszcdjBd = new BigDecimal(posje).divide(new BigDecimal(poszcsl), XSDDConstant.FLOAT_SCALE,
										XSDDConstant.ROUND_MODE);
								poszcdj = poszcdjBd.toString();
							}
						}
						posmc = reset.getString("POSMC");
					} else {
						throw new RuntimeException((row.getRowNum() + 1) + "�У�POS�ţ�" + posid + " ������");
					}
				} finally {
					DBSql.close(stat, reset);
				}
			} else {
				throw new RuntimeException((row.getRowNum() + 1) + "�У�POS�������Ͳ���ʶ�𣬿���д�������������ʽ��");
			}

			row.createCell(XSDDConstant.EXCEL_COL_POSFALX).setCellValue(posfalx);
			row.createCell(XSDDConstant.EXCEL_COL_POSID).setCellValue(posid);
			row.createCell(XSDDConstant.EXCEL_COL_POSJE).setCellValue(posje);
			row.createCell(XSDDConstant.EXCEL_COL_POSMC).setCellValue(posmc);
			row.createCell(XSDDConstant.EXCEL_COL_POSZCDJ).setCellValue(poszcdj);
			row.createCell(XSDDConstant.EXCEL_COL_POSZCSL).setCellValue(poszcsl);
		}

	}

	/**
	 * ��䷵����Ϣ.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private void putFLInfo(Connection conn, HSSFRow row, String wlbh) throws SQLException {
		// 1.��ȡ����������
		String flfah = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_FLFAH));
		if (flfah == null || flfah.trim().equals("")) {
			// ��շ�����Ϣ
			row.createCell(XSDDConstant.EXCEL_COL_FLFAH).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLFAMC).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLFS).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLZCD).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLZCJ).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLSL).setCellValue("0");
		} else {
			// 2.��ȡ��Ӧ����
			String flfamc;
			String flzcd;
			String flzcj;
			String flfs = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_FLFS));
			String flsl = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_FLSL));

			if (flfs == null || flfs.trim().equals("")) {
				throw new RuntimeException((row.getRowNum() + 1) + "�У�������ʽ����Ϊ�գ�����д������ʽ");
			} else {
				// 0:�ȷ���|1:����|2:������
				if (flfs.equals("�ȷ���")) {
					flfs = "0";
				} else if (flfs.equals("����")) {
					flfs = "1";
				} else if (flfs.equals("������")) {
					flfs = "2";
				} else {
					throw new RuntimeException((row.getRowNum() + 1) + "�У�������ʽ��д��ʶ��Ӧ��д �ȷ�����������������");
				}
			}

			if (flsl == null || flsl.trim().equals("")) {
				flsl = parseDFSL(row);
			}
			int flslInt = Integer.parseInt(flsl);

			// 3.�����ݿ���Ҷ�Ӧ�������������֧�ֵ��ۺͽ��
			PreparedStatement stat = null;
			ResultSet reset = null;
			try {
				stat = conn
						.prepareStatement("SELECT FLSL,FLFAMC,FLDJ FROM BO_AKL_WXB_XS_FL_HEAD h, BO_AKL_WXB_XS_FL_BODY b WHERE h.bindid=b.bindid AND FLFABH=? AND WLBH=?");
				reset = DAOUtil.executeFillArgsAndQuery(conn, stat, flfah, wlbh);
				if (reset.next()) {
					if (flslInt > reset.getInt("FLSL")) {
						throw new RuntimeException((row.getRowNum() + 1) + "�У��������������˷���֧�ֵ����������" + reset.getInt("FLSL"));
					}

					flfamc = reset.getString("FLFAMC");
					BigDecimal flzcdBd = reset.getBigDecimal("FLDJ");
					BigDecimal flzcjBd = flzcdBd.multiply(new BigDecimal(Integer.parseInt(flsl)));
					flzcjBd = flzcjBd.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
					flzcd = flzcdBd.toString();
					flzcj = flzcjBd.toString();
				} else {
					throw new RuntimeException((row.getRowNum() + 1) + "�У����������Ų����� ���� �÷�����֧�ִ�����");
				}
			} finally {
				DBSql.close(stat, reset);
			}

			row.createCell(XSDDConstant.EXCEL_COL_FLFAH).setCellValue(flfah);
			row.createCell(XSDDConstant.EXCEL_COL_FLFAMC).setCellValue(flfamc);
			row.createCell(XSDDConstant.EXCEL_COL_FLFS).setCellValue(flfs);
			row.createCell(XSDDConstant.EXCEL_COL_FLZCD).setCellValue(flzcd);
			row.createCell(XSDDConstant.EXCEL_COL_FLZCJ).setCellValue(flzcj);
			row.createCell(XSDDConstant.EXCEL_COL_FLSL).setCellValue(flsl);
		}
	}

	/**
	 * ���������Ϣ.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private void putWLXXForWLBH(Connection conn, HSSFRow row, String wlbh) throws SQLException {
		// 3���������ϲ�ѯ������Ϣ
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_WLXX);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, wlbh);
			if (reset.next()) {
				String wlmc = reset.getString("WLMC");
				String wlgg = reset.getString("GG");
				String wlxh = reset.getString("XH");
				int tj = reset.getInt("TJ");
				int zl = reset.getInt("ZL");
				String dw = reset.getString("DW");
				row.createCell(XSDDConstant.EXCEL_COL_WLMC).setCellValue(wlmc);
				row.createCell(XSDDConstant.EXCEL_COL_WLGG).setCellValue(wlgg);
				row.createCell(XSDDConstant.EXCEL_COL_XH).setCellValue(wlxh);
				row.createCell(XSDDConstant.EXCEL_COL_TJ).setCellValue(tj);
				row.createCell(XSDDConstant.EXCEL_COL_ZL).setCellValue(zl);
				row.createCell(XSDDConstant.EXCEL_COL_JLDW).setCellValue(dw);
			} else {
				throw new RuntimeException("�޷��������ϱ�Ų�ѯ��������Ϣ����ȷ�����ϱ��" + wlbh + "����");
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ���������Ϣ.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private String putWLXXForWLXH(Connection conn, HSSFRow row, String wlxh) throws SQLException {
		// 3���������ϲ�ѯ������Ϣ
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_WLXX_XH);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, wlxh);
			if (reset.next()) {
				String wlmc = reset.getString("WLMC");
				String wlgg = reset.getString("GG");
				String wlbh = reset.getString("WLBH");
				int tj = reset.getInt("TJ");
				int zl = reset.getInt("ZL");
				String dw = reset.getString("DW");
				row.createCell(XSDDConstant.EXCEL_COL_WLBH).setCellValue(wlbh);
				row.createCell(XSDDConstant.EXCEL_COL_WLMC).setCellValue(wlmc);
				row.createCell(XSDDConstant.EXCEL_COL_WLGG).setCellValue(wlgg);
				row.createCell(XSDDConstant.EXCEL_COL_XH).setCellValue(wlxh);
				row.createCell(XSDDConstant.EXCEL_COL_TJ).setCellValue(tj);
				row.createCell(XSDDConstant.EXCEL_COL_ZL).setCellValue(zl);
				row.createCell(XSDDConstant.EXCEL_COL_JLDW).setCellValue(dw);
				return reset.getString("WLBH");
			} else {
				throw new RuntimeException("�޷��������ϱ�Ų�ѯ��������Ϣ����ȷ�������ͺ�" + wlxh + "����");
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ���ͻ�������Ϣ.
	 * 
	 * @param conn
	 * @param row
	 * @param khbh
	 * @param khspbh
	 * @return
	 * @throws SQLException
	 */
	private String putKHWLXX(Connection conn, HSSFRow row, String khbh, String khspbh) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		String wlbh = null;
		try {
			// 1�����ݿͻ����ϱ�Ų�ѯ����
			ps = conn.prepareStatement(QUERY_WLBH);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, khbh, khspbh);
			if (reset.next()) {
				wlbh = reset.getString("YKSPSKU");
				row.createCell(XSDDConstant.EXCEL_COL_WLBH).setCellValue(wlbh);
			} else {
				throw new RuntimeException("�ͻ���ţ�" + khbh + "���ͻ���Ʒ���룺" + khspbh + " �޷��ҳ���Ӧ���ϡ�");
			}
		} finally {
			DBSql.close(ps, reset);
		}
		return wlbh;
	}

	/**
	 * ������ϵĿ����Ϣ.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private void putWLSLCell(Connection conn, HSSFRow row, String wlbh) throws SQLException {
		int kcsl;
		int ztsl;
		PreparedStatement ps = null;
		ResultSet reset = null;
		// 2���������ϲ�ѯ�����ʣ�����
		try {
			ps = conn.prepareStatement(QUERY_WLSL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, wlbh);
			if (reset.next()) {
				kcsl = reset.getInt("ZCSL");
				ztsl = reset.getInt("ZTSL");
				row.createCell(XSDDConstant.EXCEL_COL_KC).setCellValue(kcsl);
				row.createCell(XSDDConstant.EXCEL_COL_ZTSL).setCellValue(ztsl);
			} else {
				throw new RuntimeException("�޷��������ϲ�ѯ�������Ϣ�����ϱ��:" + wlbh + "��");
			}
		} finally {
			DBSql.close(ps, reset);
		}
		// 5����ѯ���ϵ���������
		Integer sksl = DAOUtil.getIntOrNull(conn, QUERY_SDSL, wlbh);
		if (sksl == null) {
			sksl = Integer.valueOf(0);
		}
		row.createCell(XSDDConstant.EXCEL_COL_KYSL).setCellValue(kcsl - sksl);
	}

	/**
	 * ��ȡ����״̬.
	 * 
	 * @param row
	 * @return
	 */
	private String parseSDZT(HSSFRow row) {
		HSSFCell sdztCell = row.getCell(XSDDConstant.EXCEL_COL_SDZT);
		String sdzt = ExcelUtil.parseCellContentToString(sdztCell);
		if (sdzt == null || "".equals(sdzt.trim())) {
			return DictionaryUtil.parseYesOrNoToNo("��");
		} else {
			return DictionaryUtil.parseYesOrNoToNo(sdzt);
		}
	}

	/**
	 * ��ȡ������.
	 * 
	 * @param row
	 * @return
	 */
	private String parseDFSL(HSSFRow row) {
		HSSFCell dfslCell = row.getCell(XSDDConstant.EXCEL_COL_DFSL);
		String dfslStr = ExcelUtil.parseCellContentToString(dfslCell);
		if (dfslStr == null || "".equals(dfslStr.trim())) {
			throw new RuntimeException("��" + (row.getRowNum() + 1) + "δ��д������");
		}
		return dfslStr;
	}

	/**
	 * ��ȡ���ϱ��.
	 * 
	 * @param row
	 * @return
	 */
	private String parseWLBH(HSSFRow row) {
		HSSFCell wlbhCell = row.getCell(XSDDConstant.EXCEL_COL_WLBH);
		String wlbhStr = ExcelUtil.parseCellContentToString(wlbhCell);
		return wlbhStr;
	}

	/**
	 * ��ȡ�����ͺ�.
	 * 
	 * @param row
	 * @return
	 */
	private String parseWLXH(HSSFRow row) {
		HSSFCell wlbhCell = row.getCell(XSDDConstant.EXCEL_COL_XH);
		String wlbhStr = ExcelUtil.parseCellContentToString(wlbhCell);
		return wlbhStr;
	}

	/**
	 * ��ȡѯ������.
	 * 
	 * @param row
	 * @return
	 */
	private String parseXDSL(HSSFRow row) {
		HSSFCell xdslCell = row.getCell(XSDDConstant.EXCEL_COL_XDSL);
		String xdslStr = ExcelUtil.parseCellContentToString(xdslCell);
		if (xdslStr == null || "".equals(xdslStr.trim())) {
			throw new RuntimeException("��" + (row.getRowNum() + 1) + "δ��дѯ������");
		}
		if ("0".equals(xdslStr)) {
			throw new RuntimeException("��" + (row.getRowNum() + 1) + "ѯ������Ϊ��0��ѯ����������Ϊ��0");
		}
		return xdslStr;
	}

	/**
	 * ��ȡ�ͻ���Ʒ���.
	 * 
	 * @param row
	 * @return
	 */
	private String parseKHSPBH(HSSFRow row) {
		HSSFCell khspbhCell = row.getCell(XSDDConstant.EXCEL_COL_KHSPBH);
		String khspbh = ExcelUtil.parseCellContentToString(khspbhCell);
		return khspbh;
	}
}
