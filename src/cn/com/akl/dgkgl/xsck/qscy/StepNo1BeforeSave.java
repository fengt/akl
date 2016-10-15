package cn.com.akl.dgkgl.xsck.qscy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	/**
	 * 查询签收单单身.
	 */
	private static final String QUERY_QSD_BODY = "SELECT WLH, XH, SUM(ISNULL(YSSL, 0)) YSSL, SUM(ISNULL(SSSL, 0)) SSSL, CPMC FROM BO_AKL_QSD_P p, BO_AKL_QSD_S s WHERE p.BINDID=s.BINDID AND CKDH=? GROUP BY WLH, XH, CPMC";
	/**
	 * 查询差异单单头信息来源.
	 */
	private static final String QUERY_CYD_HEAD = "SELECT SFZ, DZ, CYS, LXR, LXFS, YSFS, WLDH, XSDDH, b.CKDH, b.KHMC,KFLXR,FHRQ,CKLXRDH FROM BO_AKL_YD_P a, BO_BO_AKL_DGCK_P b WHERE a.bindid=b.bindid AND b.CKDH=?";

	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("签收差异，第一节点表单保存事件.");
	}

	@Override
	public boolean execute() {
		// 查询父流程与子流程
		String uid = getUserContext().getUID();
		final int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
		
		if(!"BO_AKL_QSCY_P".equals(tableName))
			return true;
		
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			String ckdh = hashtable.get("CKDH");

			if (ckdh == null || ckdh.trim().length() == 0) {
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGCK_QSCY_S", bindid);
				conn.commit();
				return true;
			}

			String ckdh2 = DAOUtil.getStringOrNull(conn, "SELECT CKDH FROM BO_AKL_DGCK_QSCY_P WHERE BINDID=?", bindid);
			if (ckdh.equals(ckdh2)) {
				return true;
			}

			getFormHead(conn, hashtable, ckdh);

			DAOUtil.executeQueryForParser(conn, QUERY_QSD_BODY, new DAOUtil.ResultPaser() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					int yssl = reset.getInt("YSSL");
					int sssl = reset.getInt("SSSL");
					if (yssl > sssl) {
						String wlbh = reset.getString("WLH");
						String xh = reset.getString("XH");
						String cpmc = reset.getString("CPMC");

						Hashtable<String, String> hashtable = new Hashtable<String, String>();
						hashtable.put("WLH", wlbh);
						hashtable.put("SL", String.valueOf(yssl));
						hashtable.put("QSSL", String.valueOf(sssl));
						hashtable.put("CYSL", String.valueOf(yssl - sssl));
						hashtable.put("CYXH", xh);
						try {
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSCY_S", hashtable, bindid, getUserContext().getUID());
						} catch (AWSSDKException e) {
							throw new SQLException(e);
						}
					}
					return true;
				}
			}, ckdh);
			conn.commit();
			return true;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	public Hashtable<String, String> getFormHead(Connection conn, Hashtable<String, String> hashtable, String CKDH) {
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
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hashtable;
	}

	public String parseNull(String str) {
		return str == null ? "" : str;
	}

}
