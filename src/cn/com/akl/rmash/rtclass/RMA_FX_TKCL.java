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
	// ��ѯ��������������Ϣ
	private static final String queryAllWlxx = "select sum(FXSL) FXSL, FXWLBH, FXXH from BO_AKL_WXB_RMAFX_S where bindid = ? and LX = '��Ʒ����' and FXSL>0 group by FXWLBH, FXXH";
	// ��ѯ�ⷿ������Ϣ
	private static final String queryKyWlxx = "select (ISNULL(sum(s.KWSL), 0)-ISNULL((select sum(SDSL) from BO_AKL_KC_SPPCSK where WLBH = s.WLBH AND PCH = s.PCH AND CKDM = s.CKDM), 0)) kysl, s.PCH, s.WLBH, s.CKDM from BO_AKL_KC_KCMX_S s where s.WLBH=? AND s.CKDM=? AND s.SX in ('049088', '049090') group by s.WLBH, s.PCH, s.CKDM ORDER BY s.PCH";

	public RMA_FX_TKCL(UserContext arg0) {
		super(arg0);

		setVersion("RMA�����˻�����1.0.0");
		setDescription("���ڽ��˿�д��Ӧ����, ����������������");
	}

	@Override
	public boolean execute() {
		// TODO Aut-generated method stub
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		boolean TY = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ͬ��");
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
				hh.put("ZT", "δ��");
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
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
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
				// ��ѯ��λ������ʱ������
				String wlbh = PrintUtil.parseNull(reset.getString("FXWLBH"));
				String xh = PrintUtil.parseNull(reset.getString("FXXH"));
				int fxsl = reset.getInt("FXSL");
				int sl = fxsl;

				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset = null;
				try {
					kywlxxPs = conn.prepareStatement(queryKyWlxx);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh, ckdm);
					// �ֻ�������� falseΪ�ѽ���
					boolean overFlag = true;
					while (overFlag && kywlxxReset.next()) {
						int kysl = kywlxxReset.getInt("kysl");
						int haveSl = kysl;
						String pch = PrintUtil.parseNull(kywlxxReset.getString("PCH"));

						// ��֤���������ε�״̬.
						String zt = DAOUtil.getStringOrNull(conn, "SELECT ZT FROM BO_AKL_KC_KCHZ_P WHERE PCH=? AND WLBH=?", pch, wlbh);
						if (!"042022".equals(zt)) {
							continue;
						}

						if (haveSl > 0) {
							sl -= haveSl;

							// Ԥ��ת����ⵥ����
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("CKDM", ckdm);
							hashtable.put("DDH", fhdh);
							hashtable.put("PCH", pch);
							hashtable.put("WLBH", wlbh);
							if (sl <= 0) {
								hashtable.put("SDSL", String.valueOf(haveSl + sl));
								// ��������
								BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", hashtable, bindid, uid);
								// �ֻ�����
								overFlag = false;
							} else {
								hashtable.put("SDSL", String.valueOf(haveSl));
								// ��������
								BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_KC_SPPCSK", hashtable, bindid, uid);
							}
						}
					}
					if (overFlag == true && sl > 0) {
						throw new RuntimeException("���۶�����" + fhdh + "�����ϱ��Ϊ" + wlbh + "�ͺ�Ϊ" + xh + "�����Ͽ����������㡣");
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
