package cn.com.akl.ccgl.xsck.qscy.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class SubProcessStartAfterEvent extends SubWorkflowEventClassA {

	private static final String QUERY_XSDD_WLXX = "SELECT WLMC, XH, SL,XSDJ FROM BO_AKL_WXB_XSDD_HEAD h LEFT JOIN BO_AKL_WXB_XSDD_BODY b ON h.bindid=b.bindid WHERE h.DDID=? AND b.WLBH=?";
	/**
	 * 查询签收单单身.
	 */
	private static final String QUERY_QSD_BODY = "SELECT WLH, XH, YSSL, SSSL, CPMC, s.BZ FROM BO_AKL_QSD_P p, BO_AKL_QSD_S s WHERE p.BINDID=s.BINDID AND CKDH=?";
	/**
	 * 查询差异单单头信息来源.
	 */
	private static final String QUERY_CYD_HEAD = "SELECT SFZ, DZ, CYS, LXR, LXFS, YSFS, WLDH, XSDDH, b.CKDH, b.KHMC,KFLXR,FHRQ,CKLXRDH,b.KH FROM BO_AKL_YD_P a, BO_AKL_CKD_HEAD b WHERE a.bindid=b.bindid AND b.CKDH=?";

	public SubProcessStartAfterEvent() {
		super();
	}

	public SubProcessStartAfterEvent(UserContext arg0) {
		super(arg0);
		setDescription("子流程启动后事件，用于过滤签收差异.");
		setVersion("1.0.0");
	}

	@Override
	public boolean execute() {
		// 查询父流程与子流程
		int parentBindid = getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();
		Hashtable<Integer, Integer> hashtable = getParameter(PARAMETER_SUB_PROCESS_INSTANCE_ID).toHashtable();
		String uid = getUserContext().getUID();
		final int bindid = hashtable.get(0);
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			// 查询差异单的出库单号
			String ckdh = DAOUtil.getStringOrNull(conn, "SELECT CKDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", parentBindid);

			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_QSCY_S", bindid);
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_QSCY_P", bindid);
			Hashtable<String, String> head = new Hashtable<String, String>();
			dealHead(conn, head, ckdh);
			dealBody(conn, bindid, head.get("DDH"), ckdh);
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSCY_P", head, bindid, uid);
			conn.commit();
			return true;
		} catch (SQLException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	public void dealBody(Connection conn, int bindid, String xsddh, String ckdh) throws SQLException {
		String ckdm = DAOUtil.getStringOrNull(conn, "SELECT CKDM FROM BO_AKL_WXB_XSDD_HEAD WHERE DDID=?", xsddh);
		String ckmc = DAOUtil.getStringOrNull(conn, "SELECT CKMC FROM BO_AKL_WXB_XSDD_HEAD WHERE DDID=?", xsddh);

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_QSD_BODY);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, ckdh);
			while (reset.next()) {
				int yssl = reset.getInt("YSSL");
				int sssl = reset.getInt("SSSL");
				String wlbh = reset.getString("WLH");
				String xh = reset.getString("XH");
				String bz = reset.getString("BZ");

				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("WLH", wlbh);
				hashtable.put("SL", String.valueOf(yssl));
				hashtable.put("QSSL", String.valueOf(sssl));
				hashtable.put("CYSL", String.valueOf(yssl - sssl));
				hashtable.put("CYXH", xh);
				hashtable.put("CYYY", bz);
				hashtable.put("FHKFDM", ckdm);
				hashtable.put("FHKFMC", ckmc);

				packMaterialInfo(conn, xsddh, wlbh, hashtable);

				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSCY_S", hashtable, bindid, getUserContext().getUID());
				} catch (AWSSDKException e) {
					throw new SQLException(e);
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	public void packMaterialInfo(Connection conn, String ddid, String wlbh, Hashtable<String, String> hashtable) {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_XSDD_WLXX);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, ddid, wlbh);
			if (reset.next()) {
				hashtable.put("CPXH", parseNull(reset.getString("XH")));
				hashtable.put("CPMC", parseNull(reset.getString("WLMC")));
				hashtable.put("XSDJ", parseNull(reset.getString("XSDJ")));
				hashtable.put("SHUIL", parseNull(reset.getString("SL")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 自动从出库单抓取数据.
	 * 
	 * @param conn
	 * @param hashtable
	 * @param CKDH
	 * @return
	 */
	public Hashtable<String, String> dealHead(Connection conn, Hashtable<String, String> hashtable, String CKDH) {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_CYD_HEAD);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, CKDH);
			if (reset.next()) {
				hashtable.put("CKDH", parseNull(reset.getString("CKDH")));
				// hashtable.put("WLDH", parseNull(reset.getString("WLDH")));
				hashtable.put("DDH", parseNull(reset.getString("XSDDH")));
				hashtable.put("SHDW", parseNull(reset.getString("KHMC")));
				hashtable.put("SHR", parseNull(reset.getString("KFLXR")));
				hashtable.put("SHRLXFS", parseNull(reset.getString("CKLXRDH")));
				hashtable.put("FHRQ", parseNull(reset.getString("FHRQ")));
				hashtable.put("SFZ", parseNull(reset.getString("SFZ")));
				hashtable.put("MDZ", parseNull(reset.getString("DZ")));
				hashtable.put("CYS", parseNull(reset.getString("CYS")));
				hashtable.put("CYSLXR", parseNull(reset.getString("LXR")));
				hashtable.put("CYSLXFS", parseNull(reset.getString("LXFS")));
				hashtable.put("YSFS", parseNull(reset.getString("YSFS")));
				hashtable.put("KHBM", parseNull(reset.getString("KH")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(ps, reset);
		}
		return hashtable;
	}

	public String parseNull(String str) {
		return str == null ? "" : str;
	}
}
