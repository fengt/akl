package cn.com.akl.xsgl.xsdd.ddbg.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ComputeBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_BODY = "SELECT DDID, DH, CKID, CKMC, WLBH, WLMC, WLGG, XH, XSDJ, XSSL, ZT, BZ, POSID, POSMC, WOS, KYSL, ZTSL, XSZDJ, MZL, SDZT, KC, KHSPBH, XDSL, DDSL, DFSL, JLDW, POSZCDJ, POSJE, FLZCJ, FLZCD, FLHJ, JJMLL, DDZJE, JJZE, WLGDMLL, PCCBJ, PCH, POSFALX, FLFAH, FLFAMC, FLFALX, FLFS, YSJE, FLSL, TJ, ZL, CBZE, POSZCSL, SL FROM BO_AKL_WXB_XSDD_BODY WHERE DDID=?";
	/**
	 * 查询销售订单单头.
	 */
	private static final String QUERY_XSDD_HEAD = "SELECT DBID, DDID, KHID, DJRQ, JHRQ, XSR, FP, ZQ, JSFS, ZDR, SHR, DDZT, BZ, JSHJ, ZDJSHJ, GSTJ, GSZL, ZT, KHMC, XSBM, CGFZR, CXFZRDH, CXFZRSJ, CXFZRYX, WFFZR, WFFZERDH, WFFZRSJ, WFFZRYX, XDRQ, QWJHRQ, ZWJHRQ, KHCK, CKLXR, CKBYLXR, CKLXRDH, CKBYLXRDH, JHDZ, SHFS, YFTK, SFYY, ZJEHJ, XDSLHJ, DDSLHJ, DFSLHJ, SFYS, KHCGDH, XDJEHJ, DYCS FROM BO_AKL_WXB_XSDD_HEAD WHERE DDID=?";

	public Hashtable<String, String> hashtable = null;

	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第一节点保存前事件，用于计算子表每条记录的金额.");
	}

	@Override
	public boolean execute() {
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();

		if ("BO_WXB_XSDD_BG_S".equals(tableName)) {
			// 单身触发此事件，计算单身的金额信息.
			ComputeBiz fillbiz = new ComputeBiz();
			fillbiz.computePOS(hashtable);
			fillbiz.computeFL(hashtable);
			fillbiz.computeChengben(hashtable);

		} else if ("BO_WXB_XSDD_BG_P".equals(tableName)) {
			// 引用销售订单.
			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();

				String xsddh = hashtable.get("DDID");
				if (xsddh == null || xsddh.trim().length() == 0) {
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_WXB_XSDD_BG_P", bindid);
					conn.commit();
					return true;
				}
				String xsddh2 = DAOUtil.getStringOrNull(conn, "SELECT DDID FROM BO_WXB_XSDD_BG_P WHERE BINDID=?", bindid);
				if (xsddh.equals(xsddh2)) {
					return true;
				}

				// 填充销售变更订单单头.
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_HEAD, fillHeadPaser, xsddh);
				// 删除现有的销售变更订单单身，并重新填充销售变更订单单身.
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_WXB_XSDD_BG_S", bindid);
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_BODY, fillBodyPaser, xsddh);

				conn.commit();
			} catch (RuntimeException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现问题，请联系管理员！", true);
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}

	/**
	 * 填充单头.
	 */
	private DAOUtil.ResultPaser fillHeadPaser = new DAOUtil.ResultPaser() {

		@Override
		public boolean parse(Connection conn, ResultSet reset) throws SQLException {
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
				BOInstanceAPI.getInstance().createBOData(conn, "BO_WXB_XSDD_BG_S", hashtable, getParameter(PARAMETER_INSTANCE_ID).toInt(),
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
