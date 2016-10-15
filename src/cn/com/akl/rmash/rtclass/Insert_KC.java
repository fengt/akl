package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Insert_KC extends WorkFlowStepRTClassA {

	public Insert_KC() {
	}

	public Insert_KC(UserContext uc) {
		super(uc);
		setVersion("RMA收货流程v1.0");
		setProvider("刘松");
		setDescription("用于写入RMA库存明细表");
	}

	@Override
	public boolean execute() {
		// 取得RMA收货申请表单数据
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		boolean QS = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "签收");

		// 写入库存明细表
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();

			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_RMA_KCMX", bindid);
			if (QS) {
				service(conn, bindid, uid);
			}

			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	public void service(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
		String CKDM = "";
		String CKMC = "";
		String LX = "";

		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);

		// 根据品牌安排放入库的名称
		if (ha.get("PP").equals("罗技")) {
			CKDM = "RMA02";
		} else if (ha.get("PP").equals("闪迪")) {
			CKDM = "RMA01";
		} else if (ha.get("PP").equals("魔声")) {
			CKDM = "JC01";
		}else if(ha.get("PP").equals("卓棒")){//添加品牌及仓库
			CKDM="RMA03";
		}

		// 获取对应仓库代码
		CKMC = DAOUtil.getStringOrNull(conn, "select CKMC from BO_AKL_CK where CKDM =?", CKDM);

		// 将RMA返货信息写入对应仓库
		for (Hashtable<String, String> h : v) {
			// 获取物料属性
			String str = DAOUtil.getStringOrNull(conn, "select XLMC from BO_AKL_DATA_DICT_S where DLBM = ? AND XLMC=?", "045", h.get("CY"));

			if (str != null) {
				LX = "退回";
			}

			Hashtable<String, String> hc = new Hashtable<String, String>();
			hc.put("WLBH", h.get("YKSKU"));
			hc.put("WLMC", h.get("SPMC"));
			hc.put("ZJM", h.get("BJTM"));
			hc.put("CKDM", CKDM);
			hc.put("CKMC", CKMC);
			hc.put("XH", h.get("XH"));
			hc.put("LX", LX);
			hc.put("KWSL", h.get("SL").toString());
			hc.put("DDH", ha.get("DJBH"));
			hc.put("CKDH", ha.get("CKDH"));
			
			packMaterialInfo(conn, h.get("YKSKU"), h.get("XH"), hc);

			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_RMA_KCMX", hc, bindid, getUserContext().getUID());
		}
	}

	/**
	 * 填充物料信息.
	 * 
	 * @param conn
	 * @param yksku
	 * @param xh
	 * @param hashtable
	 * @throws SQLException
	 */
	public void packMaterialInfo(Connection conn, String yksku, String xh, Hashtable<String, String> hashtable) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement("select GG, DW, YXQ from BO_AKL_WLXX where WLBH = ?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, yksku);
			if (reset.next()) {
				hashtable.put("BZQ", String.valueOf(reset.getInt("YXQ")));
				hashtable.put("JLDW", PrintUtil.parseNull(reset.getString("DW")));
				hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
			} else {
				throw new RuntimeException("找不到对应的物料：" + yksku);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

}
