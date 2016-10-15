package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class FXSLYZ extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ���ϱ�ź��ͺ�.
	 */
	private final static String QUERY_BODY_MATERIAL_INFO = "SELECT SPBH WLBH, XH FROM BO_AKL_WXB_RMAFXZX_S WHERE BINDID=?";
	private final static String QUERY_BODY_MATERIAL_INFO2 = "SELECT FXWLBH AS WBH,FXXH AS XH FROM BO_AKL_WXB_RMAFX_S WHERE BINDID=?";
	/**
	 * ��ѯ������Ϣ�����ϱ�Ŷ�Ӧ���ͺ�.
	 */
	private final static String QUERY_MATERIAL_INFO = "SELECT XH FROM BO_AKL_WLXX WHERE WLBH=?";

	public FXSLYZ() {
	}

	public FXSLYZ(UserContext uc) {
		super(uc);
		setVersion("RMA�����˻�����v1.0");
		setProvider("����");
		setDescription("������֤�˻ػ��²���Ϊ���Ϊ��");
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		// ȡ��RMA�����˻����������
		Hashtable<String, String> h = null;
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> hft = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
		Vector<Hashtable<String, String>> vft = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
		int i = 1;
		if (vft != null) {
			Iterator<Hashtable<String, String>> it = vft.iterator();
			while (it.hasNext()) {
				h = (Hashtable<String, String>) it.next();
				String THXH = h.get("THXH") == null ? "" : h.get("THXH").toString();
				String FXXH = h.get("FXXH") == null ? "" : h.get("FXXH").toString();
				String FXSL = h.get("FXSL") == null ? "0" : h.get("FXSL").toString();
				String XSDJ = h.get("XSDJ") == null ? "0" : h.get("XSDJ").toString();
				if (h.get("LX").toString().equals("��Ʒ����") && Double.parseDouble(XSDJ) <= 0) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(),
							"�����" + i + "�У��˻ؼ�����Ϊ " + h.get("THJBM") + " �ͺ�Ϊ " + THXH + " �ķ�����Ʒ�����ۼ۸���֪ͨ����ά��������");
					return false;
				} else if (h.get("LX").toString().equals("��Ʒ����") && Double.parseDouble(h.get("TKJE").toString()) <= 0) {
					if (Integer.parseInt(FXSL) <= 0 || FXXH.equals("")) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(),
								"�����" + i + "�У��˻ؼ�����Ϊ " + h.get("THJBM") + " �ͺ�Ϊ " + THXH + " �ķ�����Ʒ������Ϊ'0'��Ϊ�����ݻ��޷�����Ʒ�������˿�����飡����");
						return false;
					}
				} else if (h.get("LX").toString().equals("��Ʒ����") && Double.parseDouble(h.get("TKJE").toString()) > 0) {
					if (Integer.parseInt(FXSL) > 0) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(),
								"�����" + i + "�У��˻ؼ�����Ϊ " + h.get("THJBM") + " �ͺ�Ϊ " + THXH + " �ķ�����Ʒ��������'0'����'�˿�'�����飡����");
						return true;
					}
				} else if (h.get("LX").toString().equals("�˻�")) {
					if (Integer.parseInt(FXSL) > 0 || Double.parseDouble(h.get("TKJE").toString()) > 0) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(),
								"�����" + i + "�У��˻ؼ�����Ϊ " + h.get("THJBM") + " �ͺ�Ϊ " + THXH + " ���˻���Ʒ��������'0'����'�˿�'�����飡����");
						return false;
					}
				}
				i++;
			}
		}

		return validateMaterialInfo(bindid);
	}

	/**
	 * У�����ϱ�ź��ͺ��Ƿ��Ӧ.
	 * 
	 * @param bindid
	 * @return
	 */
	public boolean validateMaterialInfo(int bindid) {
		Connection conn = null;
		try {
			conn = DBSql.open();
			DAOUtil.executeQueryForParser(conn, QUERY_BODY_MATERIAL_INFO, validateMaterialInfoPaser, bindid);
			DAOUtil.executeQueryForParser(conn, QUERY_BODY_MATERIAL_INFO2, validateMaterialInfoPaser, bindid);
			DAOUtil.executeQueryForParser(conn, "SELECT THJBM,KHDH FROM BO_AKL_WXB_RMAFX_S WHERE BINDID=?", new DAOUtil.ResultPaser() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					String thjbm = reset.getString("THJBM");
					String khdh = reset.getString("KHDH");
					Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXB_RMAFX_S WHERE THJBM=? AND KHDH=?", thjbm, khdh);
					if (count != null && count > 1) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "����ţ�" + thjbm + "���ͻ����ţ�" + khdh + " �����ظ������飡");
					}
					return true;
				}
			}, bindid);
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣����ϵ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	private DAOUtil.ResultPaser validateMaterialInfoPaser = new DAOUtil.ResultPaser() {
		@Override
		public boolean parse(Connection conn, ResultSet reset) throws SQLException {
			String wlbh = reset.getString(1);
			String xh = reset.getString(2);

			/** ������ϲ���Ҫ���з���. */
			if ((wlbh == null || "".equals(wlbh)) && (xh == null || "".equals(xh))) {
				return true;
			}

			String rXh = DAOUtil.getStringOrNull(conn, QUERY_MATERIAL_INFO, wlbh);
			if (!xh.equals(rXh)) {
				throw new RuntimeException("���ϱ�ţ�" + wlbh + " ��Ӧ���ͺţ�" + xh + " ���� Ӧ��Ӧ�ͺţ�" + rXh + " !");
			}
			return true;
		}

	};

}
