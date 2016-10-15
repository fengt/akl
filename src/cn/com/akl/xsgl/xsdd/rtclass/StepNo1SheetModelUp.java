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
	 * 查询物料默认的销售指导价.
	 */
	private static final String QUERY_WLXX_XSZDJ_DEFAULT = "SELECT TOP 1 XSGHJ FROM BO_AKL_JGGL WHERE WLBH=? ORDER BY UPDATEDATE DESC";
	/**
	 * 查询客户指定销售指导价.
	 */
	private static final String QUERY_WLXX_XSZDJ_KH_DEFAULT = "SELECT TOP 1 XSGHJ FROM BO_AKL_KH_JGGL_P a JOIN BO_AKL_KH_JGGL_S b ON a.BINDID=b.BINDID AND KHBH=? WHERE WLBH=? ORDER BY b.UPDATEDATE DESC";
	/**
	 * 查询物料的在途数量、库存数量、可用数量.
	 */
	private static final String QUERY_WLSL = "SELECT a.WLBH,SUM(CASE WHEN ZT='042023' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as ZTSL, SUM(CASE WHEN ZT='042022' THEN ISNULL(a.KWSL, 0) ELSE 0 END) as ZCSL FROM BO_AKL_KC_KCMX_S a, BO_AKL_KC_KCHZ_P b WHERE a.WLBH=b.WLBH AND a.PCH=b.PCH AND a.WLBH=? GROUP BY a.WLBH";
	/**
	 * 查询物料的锁定数量.
	 */
	private static final String QUERY_SDSL = "SELECT SUM(SDSL) AS SDSL FROM BO_AKL_KC_SPPCSK WHERE WLBH=?";
	/**
	 * 查询客户物料编号对应的物料号.
	 */
	private static final String QUERY_WLBH = "SELECT YKSPSKU FROM BO_AKL_KHSPBMGL WHERE KHBM=? AND KHSPSKU=?";
	/**
	 * 查询物料信息.
	 */
	private static final String QUERY_WLXX = "SELECT WLMC,GG,XH,DW,TJ,ZL FROM BO_AKL_WLXX WHERE WLBH=? AND HZBM='01065' ";
	/**
	 * 查询客户编号.
	 */
	private static final String QUERY_KHBH = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询物料信息通过型号.
	 */
	private static final String QUERY_WLXX_XH = "SELECT XH, WLBH, HZBM, WLMC, GG, XH, DW, TJ, ZL FROM BO_AKL_WLXX WHERE XH=? AND HZBM='01065' ";

	public StepNo1SheetModelUp(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("销售订单导入的时候进行有校性校验,销售订单交付，导入本地数据，用户只需填写，型号或客户商品编号&寻单数量&答复数量，其他数据均从后台获取");
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
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请选择客户编号，并且点击暂存!", true);
				return ExcelUtil.getClearWorkBook(arg0);
			}

			for (int i = ExcelUtil.EXCEL_START; i <= maxRowNum; i++) {
				HSSFRow row = sheet.getRow(i);
				// 插入行号
				row.createCell(XSDDConstant.EXCEL_COL_DH).setCellValue(i - 5);
				// 填充行记录
				fillRow(conn, row, khbh);
			}
			return arg0;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return ExcelUtil.getClearWorkBook(arg0);
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现错误，请联系管理员!", true);
			return ExcelUtil.getClearWorkBook(arg0);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 填充行记录，根据客户物料编号或型号，询单数量，答复数量.
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

		// 锁定状态
		row.createCell(XSDDConstant.EXCEL_COL_SDZT).setCellValue(sdzt);

		// 计算订单满足率
		double mzl = dfsl / xdsl * 100;
		row.createCell(XSDDConstant.EXCEL_COL_MZL).setCellValue(mzl);

		// 1、必填项校验
		if ((khspbh == null || "".equals(khspbh.trim())) && (wlbh == null || "".equals(wlbh.trim())) && (wlxh == null || "".equals(wlxh.trim()))) {
			throw new RuntimeException("第" + (row.getRowNum() + 1) + "行请至少填写物料编号、客户物料编号或者型号中的一个!");
		}

		if (wlxh != null && !"".equals(wlxh)) {
			// 物料型号
			wlbh = putWLXXForWLXH(conn, row, wlxh);
			if (wlbh == null || "".equals(wlbh.trim())) {
				throw new RuntimeException("第" + (row.getRowNum() + 1) + "行,无法根据物料型号:" + wlxh + "，找到物料编号，请确认是否存在此型号!");
			}
		} else if (khspbh != null && !"".equals(khspbh.trim())) {
			// 客户商品编号
			wlbh = putKHWLXX(conn, row, khbh, khspbh);
			if (wlbh == null || "".equals(wlbh.trim())) {
				throw new RuntimeException("第" + (row.getRowNum() + 1) + "行,无法根据客户编号:" + khbh + "，客户物料编号:" + khspbh + "找到对应物料编号，请确认是否已维护此关系!");
			}
			putWLXXForWLBH(conn, row, wlbh);
		} else if (wlbh != null && !"".equals(wlbh.trim())) {
			// 物料编号
			putWLXXForWLBH(conn, row, wlbh);
		}

		putWLSLCell(conn, row, wlbh);
		putPOSInfo(conn, row, wlbh);
		putFLInfo(conn, row, wlbh);

		// 4、查询物料的销售价格
		BigDecimal xszdj = DAOUtil.getBigDecimalOrNull(conn, QUERY_WLXX_XSZDJ_KH_DEFAULT, khbh, wlbh);
		if (xszdj == null) {
			xszdj = DAOUtil.getBigDecimalOrNull(conn, QUERY_WLXX_XSZDJ_DEFAULT, wlbh);
			if (xszdj == null) {
				throw new RuntimeException("物料编号：" + wlbh + "没有维护销售指导价！");
			}
		}
		row.createCell(XSDDConstant.EXCEL_COL_XSZDJ).setCellValue(xszdj.doubleValue());
	}

	/**
	 * 填充POS信息.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private void putPOSInfo(Connection conn, HSSFRow row, String wlbh) throws SQLException {
		// 1.获取POS方案号
		String posid = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_POSID));
		if (posid == null || posid.trim().equals("")) {
			// 清空POS信息
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

			// 2.获取POS类型
			String posfalx = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_POSFALX));
			if (posfalx == null || posfalx.equals("")) {
				throw new RuntimeException("第" + (row.getRowNum() + 1) + "行，POS方案类型未填写");
			}

			// 3.从数据库查找对应POS，并计算出支持单价和金额
			if (posfalx.equals(XSDDConstant.POS_FALX_FA) || posfalx.startsWith("方案") || posfalx.endsWith("方案")) {
				posfalx = XSDDConstant.POS_FALX_FA;
				// 当POS支持类型是正常方案时
				// 获取相应数量
				poszcsl = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_POSZCSL));
				if (poszcsl == null || poszcsl.trim().equals("")) {
					poszcsl = parseDFSL(row);
				}

				int poszcslInt;
				try {
					poszcslInt = Integer.parseInt(poszcsl);
				} catch (NumberFormatException e) {
					throw new RuntimeException((row.getRowNum() + 1) + "行，POS支持数量格式有问题，请重新填写数量");
				}

				try {
					String dfsl = parseDFSL(row);
					if (Integer.parseInt(dfsl) < poszcslInt) {
						throw new RuntimeException((row.getRowNum() + 1) + "行，POS支持数量的值大于了答复数量的值");
					}
				} catch (NumberFormatException e) {
					throw new RuntimeException((row.getRowNum() + 1) + "行，POS支持数量格式有问题，请重新填写数量");
				}

				PreparedStatement stat = null;
				ResultSet reset = null;
				try {
					stat = conn
							.prepareStatement("SELECT h.POSMC, b.POSDJ, b.POSSL-ISNULL(b.YSYSL,0) as POSSL FROM BO_AKL_WXB_XS_POS_HEAD h, BO_AKL_WXB_XS_POS_BODY b WHERE h.BINDID=b.BINDID AND h.POSBH=? AND b.WLBH=?");
					reset = DAOUtil.executeFillArgsAndQuery(conn, stat, posid, wlbh);
					if (reset.next()) {
						if (reset.getInt("POSSL") < poszcslInt) {
							throw new RuntimeException((row.getRowNum() + 1) + "行，填写的POS支持数量超过了最大数量" + reset.getInt("POSSL"));
						}

						BigDecimal poszcdjBd = reset.getBigDecimal("POSDJ");
						BigDecimal posjeBd = poszcdjBd.multiply(new BigDecimal(poszcslInt));
						posjeBd = posjeBd.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
						posmc = reset.getString("POSMC");
						poszcdj = poszcdjBd.toString();
						posje = posjeBd.toString();
					} else {
						throw new RuntimeException((row.getRowNum() + 1) + "行，POS号：" + posid + " 不存在 或者 不支持此物料");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					DBSql.close(stat, reset);
				}
			} else if (posfalx.equals(XSDDConstant.POS_FALX_ZJC) || posfalx.startsWith("资金池") || posfalx.endsWith("资金池")) {
				// 当POS支持类型为资金池
				// 获取相应金额
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
								throw new RuntimeException((row.getRowNum() + 1) + "行，POS支持金额超过了最大支持金额：" + posjeBd.doubleValue());
							} else {
								BigDecimal poszcdjBd = new BigDecimal(posje).divide(new BigDecimal(poszcsl), XSDDConstant.FLOAT_SCALE,
										XSDDConstant.ROUND_MODE);
								poszcdj = poszcdjBd.toString();
							}
						}
						posmc = reset.getString("POSMC");
					} else {
						throw new RuntimeException((row.getRowNum() + 1) + "行，POS号：" + posid + " 不存在");
					}
				} finally {
					DBSql.close(stat, reset);
				}
			} else {
				throw new RuntimeException((row.getRowNum() + 1) + "行，POS方案类型不被识别，可填写：正常方案、资金池");
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
	 * 填充返利信息.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private void putFLInfo(Connection conn, HSSFRow row, String wlbh) throws SQLException {
		// 1.获取返利方案号
		String flfah = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_FLFAH));
		if (flfah == null || flfah.trim().equals("")) {
			// 清空返利信息
			row.createCell(XSDDConstant.EXCEL_COL_FLFAH).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLFAMC).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLFS).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLZCD).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLZCJ).setCellValue("");
			row.createCell(XSDDConstant.EXCEL_COL_FLSL).setCellValue("0");
		} else {
			// 2.获取相应数量
			String flfamc;
			String flzcd;
			String flzcj;
			String flfs = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_FLFS));
			String flsl = ExcelUtil.parseCellContentToString(row.getCell(XSDDConstant.EXCEL_COL_FLSL));

			if (flfs == null || flfs.trim().equals("")) {
				throw new RuntimeException((row.getRowNum() + 1) + "行，返利方式不能为空，请填写返利方式");
			} else {
				// 0:先返利|1:后返利|2:不返利
				if (flfs.equals("先返利")) {
					flfs = "0";
				} else if (flfs.equals("后返利")) {
					flfs = "1";
				} else if (flfs.equals("不返利")) {
					flfs = "2";
				} else {
					throw new RuntimeException((row.getRowNum() + 1) + "行，返利方式填写不识别，应填写 先返利、后返利、不返利");
				}
			}

			if (flsl == null || flsl.trim().equals("")) {
				flsl = parseDFSL(row);
			}
			int flslInt = Integer.parseInt(flsl);

			// 3.从数据库查找对应返利并进计算出支持单价和金额
			PreparedStatement stat = null;
			ResultSet reset = null;
			try {
				stat = conn
						.prepareStatement("SELECT FLSL,FLFAMC,FLDJ FROM BO_AKL_WXB_XS_FL_HEAD h, BO_AKL_WXB_XS_FL_BODY b WHERE h.bindid=b.bindid AND FLFABH=? AND WLBH=?");
				reset = DAOUtil.executeFillArgsAndQuery(conn, stat, flfah, wlbh);
				if (reset.next()) {
					if (flslInt > reset.getInt("FLSL")) {
						throw new RuntimeException((row.getRowNum() + 1) + "行，返利数量大于了返利支持的最大数量：" + reset.getInt("FLSL"));
					}

					flfamc = reset.getString("FLFAMC");
					BigDecimal flzcdBd = reset.getBigDecimal("FLDJ");
					BigDecimal flzcjBd = flzcdBd.multiply(new BigDecimal(Integer.parseInt(flsl)));
					flzcjBd = flzcjBd.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
					flzcd = flzcdBd.toString();
					flzcj = flzcjBd.toString();
				} else {
					throw new RuntimeException((row.getRowNum() + 1) + "行，返利方案号不存在 或者 该返利不支持此物料");
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
	 * 填充物料信息.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private void putWLXXForWLBH(Connection conn, HSSFRow row, String wlbh) throws SQLException {
		// 3、根据物料查询物料信息
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
				throw new RuntimeException("无法根据物料编号查询到物料信息，请确认物料编号" + wlbh + "存在");
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 填充物料信息.
	 * 
	 * @param conn
	 * @param row
	 * @param wlbh
	 * @throws SQLException
	 */
	private String putWLXXForWLXH(Connection conn, HSSFRow row, String wlxh) throws SQLException {
		// 3、根据物料查询物料信息
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
				throw new RuntimeException("无法根据物料编号查询到物料信息，请确认物料型号" + wlxh + "存在");
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 填充客户物料信息.
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
			// 1、根据客户物料编号查询物料
			ps = conn.prepareStatement(QUERY_WLBH);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, khbh, khspbh);
			if (reset.next()) {
				wlbh = reset.getString("YKSPSKU");
				row.createCell(XSDDConstant.EXCEL_COL_WLBH).setCellValue(wlbh);
			} else {
				throw new RuntimeException("客户编号：" + khbh + "，客户商品编码：" + khspbh + " 无法找出对应物料。");
			}
		} finally {
			DBSql.close(ps, reset);
		}
		return wlbh;
	}

	/**
	 * 填充物料的库存信息.
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
		// 2、根据物料查询库存中剩余多少
		try {
			ps = conn.prepareStatement(QUERY_WLSL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, wlbh);
			if (reset.next()) {
				kcsl = reset.getInt("ZCSL");
				ztsl = reset.getInt("ZTSL");
				row.createCell(XSDDConstant.EXCEL_COL_KC).setCellValue(kcsl);
				row.createCell(XSDDConstant.EXCEL_COL_ZTSL).setCellValue(ztsl);
			} else {
				throw new RuntimeException("无法根据物料查询到库存信息，物料编号:" + wlbh + "。");
			}
		} finally {
			DBSql.close(ps, reset);
		}
		// 5、查询物料的锁库数量
		Integer sksl = DAOUtil.getIntOrNull(conn, QUERY_SDSL, wlbh);
		if (sksl == null) {
			sksl = Integer.valueOf(0);
		}
		row.createCell(XSDDConstant.EXCEL_COL_KYSL).setCellValue(kcsl - sksl);
	}

	/**
	 * 获取锁定状态.
	 * 
	 * @param row
	 * @return
	 */
	private String parseSDZT(HSSFRow row) {
		HSSFCell sdztCell = row.getCell(XSDDConstant.EXCEL_COL_SDZT);
		String sdzt = ExcelUtil.parseCellContentToString(sdztCell);
		if (sdzt == null || "".equals(sdzt.trim())) {
			return DictionaryUtil.parseYesOrNoToNo("是");
		} else {
			return DictionaryUtil.parseYesOrNoToNo(sdzt);
		}
	}

	/**
	 * 获取答复数量.
	 * 
	 * @param row
	 * @return
	 */
	private String parseDFSL(HSSFRow row) {
		HSSFCell dfslCell = row.getCell(XSDDConstant.EXCEL_COL_DFSL);
		String dfslStr = ExcelUtil.parseCellContentToString(dfslCell);
		if (dfslStr == null || "".equals(dfslStr.trim())) {
			throw new RuntimeException("第" + (row.getRowNum() + 1) + "未填写答复数量");
		}
		return dfslStr;
	}

	/**
	 * 获取物料编号.
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
	 * 获取物料型号.
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
	 * 获取询单数量.
	 * 
	 * @param row
	 * @return
	 */
	private String parseXDSL(HSSFRow row) {
		HSSFCell xdslCell = row.getCell(XSDDConstant.EXCEL_COL_XDSL);
		String xdslStr = ExcelUtil.parseCellContentToString(xdslCell);
		if (xdslStr == null || "".equals(xdslStr.trim())) {
			throw new RuntimeException("第" + (row.getRowNum() + 1) + "未填写询单数量");
		}
		if ("0".equals(xdslStr)) {
			throw new RuntimeException("第" + (row.getRowNum() + 1) + "询单数量为：0，询单数量不能为：0");
		}
		return xdslStr;
	}

	/**
	 * 获取客户商品编号.
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
