package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.regex.Pattern;

import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.IMAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * 销售订单操作类.
 * 
 * @author huangming
 *
 */
public class SalesOrderBiz {

	/**
	 * 查询客户采购单号是否有重复的.
	 */
	private static final String QUERY_KHCGDD_COUNT = "SELECT COUNT(*)  FROM BO_AKL_WXB_XSDD_HEAD a WHERE a.bindid<>? and a.KHCGDH=?";
	/**
	 * 查询销售订单订单号.
	 */
	private static final String QUERY_XSDD_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询签收日期超时数量.
	 */
	private static final String QUERY_KH_QSRQ_TIMEOUT = "SELECT COUNT (*) FROM BO_AKL_CKD_HEAD a LEFT JOIN BO_AKL_QSD_P b ON a.bindid = b.bindid AND a.KH = ? JOIN BO_AKL_WXB_XSDD_HEAD c ON a.XSDDH = c.DDID WHERE b.QSRQ IS NOT NULL AND b.QSRQ + ? < getdate() AND c.DDZT = ?";
	/**
	 * 销售订单查询 客户名称、客户采购单号、客户仓库. 格式：客户名称,客户采购单号,客户仓库
	 */
	private static final String QUERY_XSDD_MESSAGE_KKK = "SELECT ISNULL(KHMC, ' ')+','+ISNULL(KHCGDH, ' ')+','+ISNULL(KHCK, '') FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询销售订单单头.
	 */
	private static final String QUERY_XSDD_DT = "SELECT DDID, KHID, XDRQ,KHMC, JHRQ, XSR, ZQ, JSFS, GSTJ, GSZL, KHCGDH, XSBM, CGFZR, ZDJSHJ, CXFZRDH, CXFZRSJ, CXFZRYX, WFFZR, WFFZERDH, WFFZRSJ, WFFZRYX, KHCK, CKLXR, CKBYLXR, CKLXRDH, CKBYLXRDH, JHDZ, SHFS, ZJEHJ, SFYY, SFYS, BZ FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询销售订单的仓库代码.
	 */
	private static final String QUERY_XSDD_CKDM = "SELECT CKDM FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";

	private static ProcessRebateBiz flBiz = new ProcessRebateBiz();
	private static ProcessPOSBiz posBiz = new ProcessPOSBiz();
	private static ProcessMaterialBiz skBiz = new ProcessMaterialBiz();

	private static Pattern _name_regex = Pattern.compile("<[^><]*>");

	/**
	 * 填充出库单.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> fillCKD(Connection conn, int bindid) throws SQLException {
		Hashtable<String, String> hashtable = new Hashtable<String, String>();

		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			ps = conn.prepareStatement(QUERY_XSDD_DT);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			if (reset.next()) {
				hashtable.put("KH", parseNull(reset.getString("KHID")));
				hashtable.put("KHMC", parseNull(reset.getString("KHMC")));
				hashtable.put("TJ", parseNull(reset.getString("GSTJ")));
				hashtable.put("ZL", parseNull(reset.getString("GSZL")));
				hashtable.put("XSDDH", parseNull(reset.getString("DDID")));
				hashtable.put("KHCGDH", parseNull(reset.getString("KHCGDH")));
				hashtable.put("CXFZR", parseNull(reset.getString("CGFZR")));
				hashtable.put("CXDH", parseNull(reset.getString("CXFZRDH")));
				hashtable.put("CXSJ", parseNull(reset.getString("CXFZRSJ")));
				hashtable.put("CXEMAIL", parseNull(reset.getString("CXFZRYX")));
				hashtable.put("WFFZR", parseNull(reset.getString("WFFZR")));
				hashtable.put("CXSJ", parseNull(reset.getString("WFFZERDH")));
				hashtable.put("WFDH", parseNull(reset.getString("CXFZRSJ")));
				hashtable.put("WFSJ", parseNull(reset.getString("WFFZRSJ")));
				hashtable.put("WFEMAIL", parseNull(reset.getString("WFFZRYX")));
				hashtable.put("CK", parseNull(reset.getString("KHCK")));
				hashtable.put("KFLXR", parseNull(reset.getString("CKLXR")));
				hashtable.put("CKLXRDH", parseNull(reset.getString("CKLXRDH")));
				hashtable.put("YSHJ", parseNull(reset.getString("ZJEHJ")));
				hashtable.put("SFYS", parseNull(reset.getString("SFYS")));
				hashtable.put("SFYY", parseNull(reset.getString("SFYY")));
				hashtable.put("XDRQ", parseNull(reset.getString("XDRQ")));
				hashtable.put("JHDZ", parseNull(reset.getString("JHDZ")));
				hashtable.put("ZDJSHJ", parseNull(reset.getString("ZDJSHJ")));
				hashtable.put("BZ", parseNull(reset.getString("BZ")));
			} else {
				throw new RuntimeException("没有找到对应的销售单!");
			}
		} finally {
			DBSql.close(ps, reset);
		}

		return hashtable;
	}

	/**
	 * 获取客户采购单号重复的次数.
	 * 
	 * @param conn
	 * @param bindid
	 * @param khcgdh
	 * @return
	 * @throws SQLException
	 */
	public int getKHCGDDHRepeatCount(Connection conn, int bindid, String khcgdh) throws SQLException {
		return DAOUtil.getInt(conn, QUERY_KHCGDD_COUNT, bindid, khcgdh);
	}

	/**
	 * 获取销售出库办理人.
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public String getSalesManager(Connection conn, String ckdm) throws SQLException {
		return DAOUtil.getStringOrNull(conn, "SELECT BLRBM FROM BO_AKL_CK_CKRYDYGX WHERE CKDM=?", ckdm);
	}

	/**
	 * 开启出库流程.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws Exception
	 */
	public void startCKDProcess(Connection conn, int bindid, String uid) throws Exception {

		cn.com.akl.ccgl.xsck.biz.FillBiz ckFillBiz = new cn.com.akl.ccgl.xsck.biz.FillBiz();

		String ddid = DAOUtil.getStringOrNull(conn, QUERY_XSDD_DDID, bindid);
		String ckdm = DAOUtil.getStringOrNull(conn, QUERY_XSDD_CKDM, bindid);
		String startUids = getSalesManager(conn, ckdm);
		startUids = _name_regex.matcher(startUids).replaceAll("");

		DAOUtil.executeQueryForParser(conn, "SELECT PCH, WLBH FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?", new DAOUtil.ResultPaser() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String zt = DAOUtil.getStringOrNull(conn, "SELECT ZT FROM BO_AKL_KC_KCHZ_P WHERE PCH=? AND WLBH=?", reset.getString("PCH"),
						reset.getString("WLBH"));
				if (XSCKConstant.PC_ZT_ZT.equals(zt)) {
					throw new RuntimeException("此订单有在途物料不启动子流程!");
				}
				return true;
			}
		}, bindid);

		if (startUids == null || "".equals(startUids.trim())) {
			throw new RuntimeException("仓库编号：" + ckdm + " 未维护对应的办理人！ 子流程启动不成功！");
		}

		// 启动子流程
		int ckBindid = 0;
		try {
			Hashtable<String, String> hashtable = fillCKD(conn, bindid);

			/** 拼接标题 */
			StringBuilder titleSb = new StringBuilder();
			titleSb.append("销售出库");
			titleSb.append(" ").append(hashtable.get("KHMC"));
			titleSb.append("--").append(hashtable.get("KHCGDH"));
			titleSb.append("--").append(hashtable.get("CK"));

			/** 启动流程 */
			ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("1d67285fab7d0a0731d579105b172546", uid, titleSb.toString());
			int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
			WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, startUids, titleSb.toString());
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_HEAD", hashtable, ckBindid, uid);
			ckFillBiz.queryAllWlxx(conn, ckBindid, uid, ddid);

		} catch (RuntimeException e) {
			if (ckBindid != 0) {
				WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
			}

			String message = DAOUtil.getStringOrNull(conn, QUERY_XSDD_MESSAGE_KKK, bindid);
			String[] split = message.split("\\,");

			StringBuilder titleSb = new StringBuilder();
			titleSb.append("销售订单号:");
			titleSb.append(ddid);
			titleSb.append("--");
			titleSb.append(split[0]);
			titleSb.append("--");
			titleSb.append(split[1]);
			titleSb.append("--");
			titleSb.append(split[2]);
			titleSb.append("--出库流程启动不成功");

			StringBuilder contentSb = new StringBuilder();
			contentSb.append("您好:<br/>&nbsp;&nbsp;&nbsp;&nbsp;");
			contentSb.append("销售订单 ");
			contentSb.append(ddid);
			contentSb.append(" 的出库流程启动未成功，造成出库流程启动不成功的原因：<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			contentSb.append(e.getMessage());
			contentSb.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;此流程需要您新建出库流程，手动引入销售单号。");

			// 发送邮件
			IMAPI.getInstance().sendMail(uid, startUids, titleSb.toString(), contentSb.toString());
			throw e;
		} catch (Exception e) {
			// 如果启动失败，则创建出来的流程。
			try {
				if (ckBindid != 0) {
					WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			throw e;
		}
	}

	/**
	 * 校验物料的毛利率是否小于维护的毛利率.
	 * 
	 * @param conn
	 * @param wlbh
	 * @param gmr
	 * @return
	 * @throws SQLException
	 */
	public boolean validateMaterialGrossMarginRate(Connection conn, String wlbh, BigDecimal gmr) throws SQLException {
		/** 获取物料默认毛利率 */
		BigDecimal mll = DAOUtil.getBigDecimalOrNull(conn, "SELECT MLL FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);
		/** 若不存在默认毛利率则返回true */
		if (mll == null || mll.doubleValue() == 0) {
			return true;
		} else {
			/** 当物料销售毛利率小于默认毛利率，则返回true. */
			return mll.doubleValue() <= gmr.doubleValue();
		}
	}

	/**
	 * 验证销售订单单身数据的物料毛利率.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateSalesOrderFormBodyGrossMarginRate(Connection conn, int bindid) throws SQLException {
		DAOUtil.executeQueryForParser(conn, "SELECT WLBH, XH, JJMLL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?", new DAOUtil.ResultPaser() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				if (!validateMaterialGrossMarginRate(conn, reset.getString("WLBH"), reset.getBigDecimal("JJMLL"))) {
					throw new RuntimeException("物料：" + reset.getString("XH") + "的毛利率小于默认毛利率!");
				}
				return true;
			}
		}, bindid);
	}

	/**
	 * 逾期未付款、负毛利、销售订单下单价格与销售指导价不符、POS资金池选择、超授信额度情况判断，没有返回true，有返回false.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public void otherCaseValidate(Connection conn, int bindid) throws SQLException {
		String khid = DAOUtil.getString(conn, "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?", bindid);

		// 逾期未付款
		Integer zq = DAOUtil.getIntOrNull(conn, "SELECT ZQTS FROM BO_AKL_KH_P WHERE KHID=?", khid);
		if (zq == null||zq==0) {
			throw new RuntimeException("此客户没有维护账期!");
			// 此客户没有维护账期
		} else {
			Integer yqwfkCount;
			String zqjsfs = DAOUtil.getStringOrNull(conn, "SELECT ZQJSFS FROM BO_AKL_KH_P WHERE KHID=?", khid);
			if (zqjsfs == null || zqjsfs.trim().equals("") || zqjsfs.trim().equals(XSDDConstant.ZQJSFS_QSRQ)) {
				yqwfkCount = DAOUtil.getIntOrNull(conn, QUERY_KH_QSRQ_TIMEOUT, khid, zq, XSDDConstant.XSDD_DDZT_YQS);
			} else if (zqjsfs.trim().equals(XSDDConstant.ZQJSFS_FPRQ)) {
				yqwfkCount = DAOUtil.getIntOrNull(conn, QUERY_KH_QSRQ_TIMEOUT, khid, zq, XSDDConstant.XSDD_DDZT_YQS);
			} else {
				yqwfkCount = DAOUtil.getIntOrNull(conn, QUERY_KH_QSRQ_TIMEOUT, khid, zq, XSDDConstant.XSDD_DDZT_YQS);
			}

			if (yqwfkCount != null && yqwfkCount > 0) {
				throw new RuntimeException("此客户有逾期未付款!");
			}

		}

		// 负毛利
		Integer zqjeCount = DAOUtil.getInt(conn, "select count(*) from BO_AKL_WXB_XSDD_BODY where bindid=? AND JJMLL<0", bindid);
		if (zqjeCount > 0) {
			throw new RuntimeException("此订单有负毛利!");
		}

		// 判断销售单价为零的数量..
		Integer zeroCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND XSDJ=0", bindid);
		if (zeroCount != null && zeroCount > 0) {
			throw new RuntimeException("此订单有销售单价为0的物料，需要进入财务审核!");
		}

		// 销售订单下单价格与销售指导价不符
		Integer bfCount = DAOUtil.getInt(conn, "select count(*) from BO_AKL_WXB_XSDD_BODY where bindid=? and XSDJ<>XSZDJ", bindid);
		if (bfCount > 0) {
			throw new RuntimeException("此订单有销售订单下单价格与销售指导价不符!");
		}

		// POS资金池选择
		Integer posCount = DAOUtil.getInt(conn, "select count(*) from BO_AKL_WXB_XSDD_BODY where bindid=? and POSFALX='0'", bindid);
		if (posCount > 0) {
			throw new RuntimeException("有选择POS资金池!");
		}

		// 超授信额度
		// 1、此客户消费了多少金额
		BigDecimal zdjshj = DAOUtil.getBigDecimalOrNull(conn, "SELECT ZDJSHJ FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?", bindid);
		// 2、查询信用额度
		BigDecimal xyed = DAOUtil.getBigDecimalOrNull(conn, "SELECT XYJE FROM BO_AKL_KH_P WHERE KHID=?", khid);
		if (xyed == null) {
			xyed = new BigDecimal(0);
		}
		if (zdjshj.compareTo(xyed) == 1) {
			throw new RuntimeException("此客户超信用额度!");
		}
	}

	/**
	 * 插入锁库记录.
	 * 
	 * @param bindid
	 * @param uid
	 * @return
	 */
	public ResultPaserAbs getInsertLockResultPaser(final int bindid, final String uid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				// 锁库判断
				String sfsk = reset.getString("SDZT");
				if (sfsk != null && ("是".equals(sfsk.trim()) || XSDDConstant.YES.equals(sfsk))) {
					String ddh = reset.getString("DDID");
					String pch = reset.getString("PCH");
					String wlbh = reset.getString("WLBH");
					String ckdm = reset.getString("CKID");
					int ddsl = reset.getInt("DDSL");
					// 插入锁库
					try {
						skBiz.insertSK(conn, bindid, uid, ddh, pch, wlbh, ckdm, ddsl);
					} catch (AWSSDKException e) {
						throw new RuntimeException(e);
					}
				}
				return true;
			}
		};
	}

	/**
	 * 将后返利注入返利资金池.
	 * 
	 * @param bindid
	 * @param uid
	 * @param khid
	 * @return
	 */
	public ResultPaserAbs getUpdateHFLResultPaser(final int bindid, final String uid, final String khid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah)) {
					String flfs = reset.getString("FLFS");
					BigDecimal jjze = reset.getBigDecimal("JJZE");
					BigDecimal flzcj = reset.getBigDecimal("FLZCJ");

					// 处理后返利.
					try {
						flBiz.processHFL(conn, bindid, uid, khid, flfs, flfah, jjze, flzcj);
					} catch (AWSSDKException e) {
						throw new RuntimeException(e);
					}
				}
				return true;
			}
		};
	}

	/**
	 * 更新返利和POS.
	 * 
	 * @return
	 */
	public ResultPaserAbs getUpdateFLAndPOSPaser() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");
				// 更新POS
				String posid = reset.getString("POSID");
				if (posid != null && !"".equals(posid.trim())) {
					String posfalx = reset.getString("POSFALX");
					BigDecimal posje = reset.getBigDecimal("POSJE");
					int poszcsl = reset.getInt("POSZCSL");
					posBiz.updatePOS(conn, posid, posfalx, posje, poszcsl, wlbh);
				}

				// 更新返利
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah.trim())) {
					int flsl = reset.getInt("FLSL");
					flBiz.updateFL(conn, flfah, flsl, wlbh);
				}
				return true;
			}
		};
	}

	/**
	 * 回滚返利和POS.
	 * 
	 * @return
	 */
	public ResultPaserAbs getRollbackFLAndPOSPaser() {
		return new ResultPaserAbs() {
			private ProcessPOSBiz posBiz = new ProcessPOSBiz();

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");

				String posid = reset.getString("POSID");
				if (posid != null && !"".equals(posid.trim())) {
					String posfalx = reset.getString("POSFALX");
					BigDecimal posje = reset.getBigDecimal("POSJE");
					int poszcsl = reset.getInt("POSZCSL");
					// 回退POS
					posBiz.rollBackPOS(conn, posid, posfalx, posje, poszcsl, wlbh);
				}
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah.trim())) {
					int flsl = reset.getInt("FLSL");
					// 回退返利
					flBiz.rollbackFL(conn, flfah, flsl, wlbh);
				}
				return true;
			}
		};
	}

	/**
	 * 回滚后返利.
	 * 
	 * @param bindid
	 * @param uid
	 * @param khid
	 * @return
	 */
	public ResultPaserAbs getRollbackHFL(final int bindid, final String uid, final String khid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah)) {
					String flfs = reset.getString("FLFS");
					BigDecimal ddzje = reset.getBigDecimal("DDZJE");
					BigDecimal flzcj = reset.getBigDecimal("FLZCJ");

					// 回退返利
					try {
						flBiz.rollbackHFL(conn, khid, flfs, ddzje, flzcj);
					} catch (AWSSDKException e) {
						throw new RuntimeException(e);
					}
				}
				return true;
			}
		};
	}

	/**
	 * 转换NULL.
	 * 
	 * @param str
	 * @return
	 */
	public String parseNull(String str) {
		return str == null ? "" : str;
	}
}
