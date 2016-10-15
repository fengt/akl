package cn.com.akl.xsgl.xsdd.ddbg.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {
	/**
	 * 查询客户ID.
	 */
	private static final String QUERY_KHID = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_DS = "SELECT ID, WLBH, PCH, DDID, POSZCSL, CKID, DDZJE, FLSL, DDSL, POSID, POSFALX, POSJE, FLFAH, FLFAMC, FLFALX, FLFS, JJZE, FLZCJ, FLZCD, FLHJ, DFSL, SDZT  FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	/**
	 * 查询销售订单差异单头.
	 */
	private static final String QUERY_DDBG_DT = "SELECT DBID, DDID, DJRQ, JHRQ, XSR, FP, ZQ, JSFS, ZDR, SHR, DDZT, JSHJ, ZDJSHJ, GSTJ, GSZL, ZT, KHID, KHMC, XSBM, CGFZR, CXFZRDH, CXFZRSJ, CXFZRYX, WFFZR, WFFZERDH, WFFZRSJ, WFFZRYX, XDRQ, QWJHRQ, ZWJHRQ, KHCK, CKLXR, CKBYLXR, CKLXRDH, CKBYLXRDH, JHDZ, SHFS, YFTK, SFYY, ZJEHJ, XDSLHJ, DDSLHJ, DFSLHJ, BZ, SFYS, KHCGDH, XDJEHJ, DYCS FROM BO_WXB_XSDD_BG_P WHERE BINDID=?";
	/**
	 * 查询销售订单差异单身.
	 */
	private static final String QUERY_DDBG_DS = "SELECT DDID, DH, CKID, CKMC, WLBH, PCH, WLMC, WLGG, XH, ZL, TJ, XSDJ, KHSPBH, XSZDJ, WOS, KYSL, ZTSL, KC, XSSL, XDSL, DDSL, DFSL, MZL, SDZT, JLDW, POSFALX, POSID, POSMC, POSZCDJ, POSJE, POSZCSL, FLFALX, FLFS, FLFAH, FLFAMC, FLZCJ, FLZCD, FLHJ, FLSL, JJMLL, JJZE, CBZE, YSJE, DDZJE, PCCBJ, ZT, WLGDMLL, BZ, SL FROM BO_WXB_XSDD_BG_S WHERE BINDID=?";
	/**
	 * 查询库存不足的物料数量.
	 */
	private static final String QUERY_KC_BZ = "SELECT COUNT(*) FROM BO_AKL_KC_KCMX_S WHERE PCH=? AND WLBH=? AND CKDM=? GROUP BY PCH, WLBH, CKDM HAVING SUM(KWSL)<?";
	/**
	 * 查询订单变更订单ID.
	 */
	private static final String QUERY_DDBG_DDID = "SELECT DDID FROM BO_WXB_XSDD_BG_P WHERE BINDID=?";
	/**
	 * 查询销售订单BINDID.
	 */
	private static final String QUERY_XSDD_BINDID = "SELECT BINDID FROM BO_AKL_WXB_XSDD_HEAD WHERE DDID=?";

	private SalesOrderBiz xsddbiz = new SalesOrderBiz();
	private ProcessMaterialBiz skbiz = new ProcessMaterialBiz();

	private String ddid;

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第一节点流转事件，用于更新出库单，返利资金池，并校验库存。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// 同意标记
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "提交订单");
		tyFlag = true;

		int xsddbindid = 0;

		Connection conn = null;
		try {
			if (tyFlag) {
				conn = DAOUtil.openConnectionTransaction();
				ddid = DAOUtil.getStringOrNull(conn, QUERY_DDBG_DDID, bindid);
				xsddbindid = DAOUtil.getIntOrNull(conn, QUERY_XSDD_BINDID, ddid);
				String khid = DAOUtil.getStringOrNull(conn, QUERY_KHID, bindid);

				// 删除锁库
				skbiz.deleteSK(conn, xsddbindid);

				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, new DAOUtil.ResultPaser[] {
						// 回滚原来的POS与返利
						xsddbiz.getRollbackFLAndPOSPaser(),
						// 回滚后返利.
						xsddbiz.getRollbackHFL(xsddbindid, uid, khid) }, ddid);

				// 更新子表记录.
				DAOUtil.executeQueryForParser(conn, QUERY_DDBG_DS, new DAOUtil.ResultPaser[] {
						// 校验库存
						validateRepostiory,
						// 更新销售订单单身
						fillBodyPaser,
						// 更新返利和POS
						xsddbiz.getUpdateFLAndPOSPaser(),
						// 插入锁库记录
						xsddbiz.getInsertLockResultPaser(xsddbindid, uid),
						// 插入后返利
						xsddbiz.getUpdateHFLResultPaser(xsddbindid, uid, khid) }, bindid);

				// 重计算主表价格.
				DAOUtil.executeQueryForParser(conn, QUERY_DDBG_DT, fillHeadPaser, bindid);

				conn.commit();
			}

			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}

	}

	/**
	 * 校验库存.
	 */
	private DAOUtil.ResultPaser validateRepostiory = new DAOUtil.ResultPaser() {
		@Override
		public boolean parse(Connection conn, ResultSet reset) throws SQLException {
			String wlbh = reset.getString("WLBH");
			String pch = reset.getString("PCH");
			String ckdm = reset.getString("CKDM");
			int sl = reset.getInt("SL");
			Integer count = DAOUtil.getIntOrNull(conn, QUERY_KC_BZ, pch, wlbh, ckdm, sl);
			if (count == null || count == 0) {
				// 库存充足
				return true;
			} else {
				// 库存不足
				throw new RuntimeException("物料编号：" + wlbh + " ，批次号：" + pch + " ，仓库代码：" + ckdm + " ，物料数量不足：" + sl);
			}
		}
	};

	/**
	 * 填充单头.
	 */
	private DAOUtil.ResultPaser fillHeadPaser = new DAOUtil.ResultPaser() {
		@Override
		public boolean parse(Connection conn, ResultSet reset) throws SQLException {
			Hashtable<String, String> hashtable = new Hashtable<String, String>();
			hashtable.put("DBID", parseNull(reset.getString("DBID")));
			hashtable.put("DDID", parseNull(reset.getString("DDID")));
			hashtable.put("KHID", parseNull(reset.getString("KHID")));
			hashtable.put("DJRQ", parseNull(reset.getString("DJRQ")));
			hashtable.put("JHRQ", parseNull(reset.getString("JHRQ")));
			hashtable.put("XSR", parseNull(reset.getString("XSR")));
			hashtable.put("FP", parseNull(reset.getString("FP")));
			hashtable.put("ZQ", parseNull(reset.getString("ZQ")));
			hashtable.put("JSFS", parseNull(reset.getString("JSFS")));
			hashtable.put("ZDR", parseNull(reset.getString("ZDR")));
			hashtable.put("SHR", parseNull(reset.getString("SHR")));
			hashtable.put("DDZT", parseNull(reset.getString("DDZT")));
			hashtable.put("BZ", parseNull(reset.getString("BZ")));
			hashtable.put("JSHJ", parseNull(reset.getString("JSHJ")));
			hashtable.put("ZDJSHJ", parseNull(reset.getString("ZDJSHJ")));
			hashtable.put("GSTJ", parseNull(reset.getString("GSTJ")));
			hashtable.put("GSZL", parseNull(reset.getString("GSZL")));
			hashtable.put("ZT", parseNull(reset.getString("ZT")));
			hashtable.put("KHMC", parseNull(reset.getString("KHMC")));
			hashtable.put("XSBM", parseNull(reset.getString("XSBM")));
			hashtable.put("CGFZR", parseNull(reset.getString("CGFZR")));
			hashtable.put("CXFZRDH", parseNull(reset.getString("CXFZRDH")));
			hashtable.put("CXFZRSJ", parseNull(reset.getString("CXFZRSJ")));
			hashtable.put("CXFZRYX", parseNull(reset.getString("CXFZRYX")));
			hashtable.put("WFFZR", parseNull(reset.getString("WFFZR")));
			hashtable.put("WFFZERDH", parseNull(reset.getString("WFFZERDH")));
			hashtable.put("WFFZRSJ", parseNull(reset.getString("WFFZRSJ")));
			hashtable.put("WFFZRYX", parseNull(reset.getString("WFFZRYX")));
			hashtable.put("XDRQ", parseNull(reset.getString("XDRQ")));
			hashtable.put("QWJHRQ", parseNull(reset.getString("QWJHRQ")));
			hashtable.put("ZWJHRQ", parseNull(reset.getString("ZWJHRQ")));
			hashtable.put("KHCK", parseNull(reset.getString("KHCK")));
			hashtable.put("CKLXR", parseNull(reset.getString("CKLXR")));
			hashtable.put("CKBYLXR", parseNull(reset.getString("CKBYLXR")));
			hashtable.put("CKLXRDH", parseNull(reset.getString("CKLXRDH")));
			hashtable.put("CKBYLXRDH", parseNull(reset.getString("CKBYLXRDH")));
			hashtable.put("JHDZ", parseNull(reset.getString("JHDZ")));
			hashtable.put("SHFS", parseNull(reset.getString("SHFS")));
			hashtable.put("YFTK", parseNull(reset.getString("YFTK")));
			hashtable.put("SFYY", parseNull(reset.getString("SFYY")));
			hashtable.put("ZJEHJ", parseNull(reset.getString("ZJEHJ")));
			hashtable.put("XDSLHJ", parseNull(reset.getString("XDSLHJ")));
			hashtable.put("DDSLHJ", parseNull(reset.getString("DDSLHJ")));
			hashtable.put("DFSLHJ", parseNull(reset.getString("DFSLHJ")));
			hashtable.put("SFYS", parseNull(reset.getString("SFYS")));
			hashtable.put("KHCGDH", parseNull(reset.getString("KHCGDH")));
			hashtable.put("XDJEHJ", parseNull(reset.getString("XDJEHJ")));
			hashtable.put("DYCS", parseNull(reset.getString("DYCS")));
			try {
				Integer boId = DAOUtil.getIntOrNull(conn, "SELECT ID FROM BO_AKL_WXB_XSDD_HEAD WHERE DDID=?", reset.getString("DDID"));
				BOInstanceAPI.getInstance().updateBOData(conn, "BO_AKL_WXB_XSDD_HEAD", hashtable, boId);
			} catch (AWSSDKException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
	};

	/**
	 * 填充单身.
	 */
	private DAOUtil.ResultPaser fillBodyPaser = new DAOUtil.ResultPaser() {
		@Override
		public boolean parse(Connection conn, ResultSet reset) throws SQLException {
			Hashtable<String, String> hashtable = new Hashtable<String, String>();
			hashtable.put("DDID", parseNull(reset.getString("DDID")));
			hashtable.put("DH", parseNull(reset.getString("DH")));
			hashtable.put("CKID", parseNull(reset.getString("CKID")));
			hashtable.put("CKMC", parseNull(reset.getString("CKMC")));
			hashtable.put("WLBH", parseNull(reset.getString("WLBH")));
			hashtable.put("WLMC", parseNull(reset.getString("WLMC")));
			hashtable.put("WLGG", parseNull(reset.getString("WLGG")));
			hashtable.put("XH", parseNull(reset.getString("XH")));
			hashtable.put("XSDJ", parseNull(reset.getString("XSDJ")));
			hashtable.put("XSSL", parseNull(reset.getString("XSSL")));
			hashtable.put("ZT", parseNull(reset.getString("ZT")));
			hashtable.put("BZ", parseNull(reset.getString("BZ")));
			hashtable.put("POSID", parseNull(reset.getString("POSID")));
			hashtable.put("POSMC", parseNull(reset.getString("POSMC")));
			hashtable.put("WOS", parseNull(reset.getString("WOS")));
			hashtable.put("KYSL", parseNull(reset.getString("KYSL")));
			hashtable.put("ZTSL", parseNull(reset.getString("ZTSL")));
			hashtable.put("XSZDJ", parseNull(reset.getString("XSZDJ")));
			hashtable.put("MZL", parseNull(reset.getString("MZL")));
			hashtable.put("SDZT", parseNull(reset.getString("SDZT")));
			hashtable.put("KC", parseNull(reset.getString("KC")));
			hashtable.put("KHSPBH", parseNull(reset.getString("KHSPBH")));
			hashtable.put("XDSL", parseNull(reset.getString("XDSL")));
			hashtable.put("DDSL", parseNull(reset.getString("DDSL")));
			hashtable.put("DFSL", parseNull(reset.getString("DFSL")));
			hashtable.put("JLDW", parseNull(reset.getString("JLDW")));
			hashtable.put("POSZCDJ", parseNull(reset.getString("POSZCDJ")));
			hashtable.put("POSJE", parseNull(reset.getString("POSJE")));
			hashtable.put("FLZCJ", parseNull(reset.getString("FLZCJ")));
			hashtable.put("FLZCD", parseNull(reset.getString("FLZCD")));
			hashtable.put("FLHJ", parseNull(reset.getString("FLHJ")));
			hashtable.put("JJMLL", parseNull(reset.getString("JJMLL")));
			hashtable.put("DDZJE", parseNull(reset.getString("DDZJE")));
			hashtable.put("JJZE", parseNull(reset.getString("JJZE")));
			hashtable.put("WLGDMLL", parseNull(reset.getString("WLGDMLL")));
			hashtable.put("PCCBJ", parseNull(reset.getString("PCCBJ")));
			hashtable.put("PCH", parseNull(reset.getString("PCH")));
			hashtable.put("POSFALX", parseNull(reset.getString("POSFALX")));
			hashtable.put("FLFAH", parseNull(reset.getString("FLFAH")));
			hashtable.put("FLFAMC", parseNull(reset.getString("FLFAMC")));
			hashtable.put("FLFALX", parseNull(reset.getString("FLFALX")));
			hashtable.put("FLFS", parseNull(reset.getString("FLFS")));
			hashtable.put("YSJE", parseNull(reset.getString("YSJE")));
			hashtable.put("FLSL", parseNull(reset.getString("FLSL")));
			hashtable.put("TJ", parseNull(reset.getString("TJ")));
			hashtable.put("ZL", parseNull(reset.getString("ZL")));
			hashtable.put("CBZE", parseNull(reset.getString("CBZE")));
			hashtable.put("POSZCSL", parseNull(reset.getString("POSZCSL")));
			hashtable.put("SL", parseNull(reset.getString("SL")));
			try {
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_WXB_XSDD_BODY", hashtable, getParameter(PARAMETER_INSTANCE_ID).toInt(),
						getUserContext().getUID());
			} catch (AWSSDKException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
	};

	/**
	 * 转置NULL.
	 * 
	 * @param str
	 * @return
	 */
	public String parseNull(String str) {
		if (str == null) {
			return "";
		} else {
			return str;
		}
	}
}
