package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMA_SH_viladate extends WorkFlowStepRTClassA {

	public RMA_SH_viladate(UserContext uc) {
		super(uc);
		setVersion("RMA�ջ�����v1.0");
		setProvider("����");
		setDescription("������֤��Ʒ�Ƿ����");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Connection conn = null;
		try {
			conn = DBSql.open();
			int stepNo = WorkFlowUtil.getProcessInstanceStepNo(conn, bindid);
			switch (stepNo) {
				case 1:
					validateStep1(conn, bindid, uid);
					break;
				case 5:
					validateStep5(conn, bindid, uid);
					break;
			}
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	public void validateStep1(Connection conn, int bindid, String uid) throws SQLException {
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);

		if (v == null || v.size() == 0) {
			throw new RuntimeException("��������Ϊ�գ�����");
		}
		if (v != null) {
			int row = 0;
			for (Hashtable<String, String> h : v) {
				// ��ѯ���������б���������Ƿ��ظ�.
				int iD = DAOUtil.getIntOrNull(conn, "select count(ID) ID from BO_AKL_WXB_XS_RMASH_S where BJTM = ? and bindid = ?", h.get("BJTM"),
						bindid);
				if (iD > 1) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�����" + row + "�У��������룺" + h.get("BJTM") + "�ظ������飡");
				}

				// ��ѯ�ͺ��Ƿ�Ϊ��.
				if (h.get("XH").toString().equals("")) {
					throw new RuntimeException("�����" + row + "�У��������룺" + h.get("BJTM") + " û�ж�Ӧ�����������ͺţ����飡");
				}

				// ��ѯ���ϱ����Ƿ��д����ϵ�������Ϣ.
				int id = DAOUtil.getIntOrNull(conn, "select count(ID) ID from BO_AKL_WLXX where WLBH = ?", h.get("YKSKU"));
				if (id == 0) {
					throw new RuntimeException("�����" + row + "�У��������룺" + h.get("BJTM") + " ������Ϣ����δ���ֹ��ڴ���Ʒ�������Ƿ��Ѿ�ά���˴����ϣ�");
				}
				row++;
			}
		}
	}

	public void validateStep5(Connection conn, int bindid, String uid) throws SQLException {
		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		String pp = ha.get("PP");

		if ("�޼�".equals(pp) || "006006".equals(pp)) {
			Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);
			for (Hashtable<String, String> hashtable : v) {
				String bjtm = hashtable.get("BJTM");
				String pjsfqq = hashtable.get("PJSFQQ");
				String jcjg = hashtable.get("JCJG");
				String cllx = hashtable.get("CLLX");
				String xiangh = hashtable.get("XIANGH");
				if (pjsfqq == null || "".equals(pjsfqq)) {
					throw new RuntimeException("�������룺" + bjtm + "������Ƿ���ȫδ��д��");
				}
				if (jcjg == null || "".equals(jcjg)) {
					throw new RuntimeException("�������룺" + bjtm + "������ԭ��δ��д��");
				}
				if (cllx == null || "".equals(cllx)) {
					throw new RuntimeException("�������룺" + bjtm + "��������δ��д��");
				}
				if (xiangh == null || "".equals(xiangh)) {
					throw new RuntimeException("�������룺" + bjtm + "�����δ��д��");
				}
			}
		}
	}

}
