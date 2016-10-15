package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
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

public class RMA_FX_TKCL extends WorkFlowStepRTClassA {
	// 查询单身所有物料信息
	private static final String queryAllWlxx = "select sum(FXSL) FXSL, FXWLBH, FXXH from BO_AKL_WXB_RMAFX_S where bindid = ? and LX = '坏品返新' and FXSL>0 group by FXWLBH, FXXH";
	// 查询库房可用信息
	private static final String queryKyWlxx = "select (ISNULL(sum(s.KWSL), 0)-ISNULL((select sum(SDSL) from BO_AKL_KC_SPPCSK where WLBH = s.WLBH AND PCH = s.PCH AND CKDM = s.CKDM), 0)) kysl, s.PCH, s.WLBH, s.CKDM from BO_AKL_KC_KCMX_S s where s.WLBH=? AND s.CKDM=? AND s.SX in ('049088', '049090') group by s.WLBH, s.PCH, s.CKDM ORDER BY s.PCH";

	public RMA_FX_TKCL(UserContext arg0) {
		super(arg0);

		setVersion("RMA返新退货流程1.0.0");
		setDescription("用于将退款写入应付表, 并向锁库表插入数据");
	}

	@Override
	public boolean execute() {
		// TODO Aut-generated method stub
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		boolean TY = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
		if (TY) {
			Connection conn = null;
			Statement stat = null;
			ResultSet rs = null;
			Hashtable ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
			Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
			Double je = 0.0;
			Double wsje = 0.0;
			Hashtable h = null;

			if (v != null) {
				Iterator it = v.iterator();
				while (it.hasNext()) {
					h = (Hashtable) it.next();
					je += Double.parseDouble(h.get("TKJE").toString());
					wsje += je / (1 + Double.parseDouble(h.get("SL").toString()));
				}
				Hashtable hh = new Hashtable();
				hh.put("GYSBM", ha.get("KHBH"));
				hh.put("GYSMC", ha.get("KHMC"));
				hh.put("LB", 0);
				hh.put("RKDH", ha.get("FHDH"));
				hh.put("HSYFJE", je);
				hh.put("WSYSJE", wsje);
				hh.put("ZT", "未付");
				try {
					conn = DAOUtil.openConnectionTransaction();
					stat = conn.createStatement();
					if (je > 0) {
						BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_YF", bindid);
						BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YF", hh, bindid, getUserContext().getUID());
					}
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindid);
					queryAllWlxx(conn, bindid, uid, ha.get("FHDH").toString(), ha.get("CHCKDM").toString());
					conn.commit();
					return true;
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					DAOUtil.connectRollBack(conn);
					e.printStackTrace();
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
					return false;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					DAOUtil.connectRollBack(conn);
					e.printStackTrace();
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
					return false;
				} catch (Exception e) {
					DAOUtil.connectRollBack(conn);
					e.printStackTrace();
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
					return false;
				} finally {
					DBSql.close(conn, stat, null);
				}
			}
		}
		return true;
	}

	public void queryAllWlxx(Connection conn, int bindid, String uid, String fhdh, String ckdm) throws SQLException, AWSSDKException {
		PreparedStatement ps = conn.prepareStatement(queryAllWlxx);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
		int rowNum = 1;
		try {
			while (reset.next()) {
				// 查询库位，根据时间排序
				String wlbh = PrintUtil.parseNull(reset.getString("FXWLBH"));
				String xh = PrintUtil.parseNull(reset.getString("FXXH"));
				int fxsl = reset.getInt("FXSL");
				int sl = fxsl;

				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset = null;
				try {
					kywlxxPs = conn.prepareStatement(queryKyWlxx);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh, ckdm);
					// 分货结束标记 false为已结束
					boolean overFlag = true;
					while (overFlag && kywlxxReset.next()) {
						int kysl = kywlxxReset.getInt("kysl");
						int haveSl = kysl;
						String pch = PrintUtil.parseNull(kywlxxReset.getString("PCH"));

						// 验证该物料批次的状态.
						String zt = DAOUtil.getStringOrNull(conn, "SELECT ZT FROM BO_AKL_KC_KCHZ_P WHERE PCH=? AND WLBH=?", pch, wlbh);
						if (!"042022".equals(zt)) {
							continue;
						}

						if (haveSl > 0) {
							sl -= haveSl;

							// 预备转存入库单身中
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("CKDM", ckdm);
							hashtable.put("DDH", fhdh);
							hashtable.put("PCH", pch);
							hashtable.put("WLBH", wlbh);
							if (sl <= 0) {
								hashtable.put("SDSL", String.valueOf(haveSl + sl));
								// 插入数据
								BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", hashtable, bindid, uid);
								// 分货结束
								overFlag = false;
							} else {
								hashtable.put("SDSL", String.valueOf(haveSl));
								// 插入数据
								BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", hashtable, bindid, uid);
							}
						}
					}
					if (overFlag == true && sl > 0) {
						throw new RuntimeException("销售订单：" + fhdh + "中物料编号为" + wlbh + "型号为" + xh + "的物料可用数量不足。");
					}
				} finally {
					DBSql.close(kywlxxPs, kywlxxReset);
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}
}
