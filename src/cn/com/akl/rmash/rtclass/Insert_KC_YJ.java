package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Insert_KC_YJ extends WorkFlowStepRTClassA {
	
	public Insert_KC_YJ(UserContext uc) {
		super(uc);
		setVersion("RMA�ջ�����v1.0");
		setProvider("����");
		setDescription("���ڼ����޸Ŀ����ϸ��");
	}

	@Override
	public boolean execute() {
		// ȡ��RMA�ջ��������
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;

		boolean readyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "������");

		if (readyFlag) {
			try {
				conn = DAOUtil.openConnectionTransaction();
				service(conn, bindid, uid);
				conn.commit();
			} catch (SQLException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣������ϵ����Ա!", true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}

	/**
	 * ���¿����ϸ�����ⷿ��Ϣ����Ʒд�����Ʒ�⣬�˻�д������˻��⣬���������շ���Ʒ������
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void service(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {

		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);

		if (v == null || v.size() == 0) {
			throw new RuntimeException("��������Ϊ�գ�����");
		}

		// ��⵱ǰ����״̬.
		if (ha.get("TP").toString().equals("�ȴ�����")) {
			throw new RuntimeException("�ȴ������в������������");
		}

		String pp = ha.get("PP");

		for (Hashtable<String, String> h : v) {
			// ��ü�����ж��Ƿ��뵽������
			String JCJG = h.get("JCJG").toString();

			// ������
			String cljg = h.get("CLLX");
			String tp = ha.get("TP");
			String tph = h.get("TPH");
			String bjtm = h.get("BJTM");
			String djbh = h.get("DJBH");

			/**
			 * ���ݴ��������¿���еĴ�������<br/>
			 * 1.���ݴ����� �˻�->�˻أ�����->��Ʒ����<br/>
			 * 2.�����Ϊ�� ��Ʒ����<br/>
			 * 3.����������Ʒ����<br/>
			 * */
			/** �����ϲ�Ʒ���²ֿ� */
			if ("����".equals(pp) || "006001".equals(pp)||"׿��".equals(pp)||"006440".equals(pp)) {//���Ʒ��׿��
				// ���ϴ���ʽ
				sdDealWay(conn, JCJG, cljg, tp, tph, bjtm, djbh);
			} else if ("�޼�".equals(pp) || "006006".equals(pp)) {
				// �޼�����ʽ
				ljDealWay(conn, JCJG, cljg, tp, tph, bjtm, djbh);
			} else {
				// Ĭ�ϲ����޼�����ʽ
				ljDealWay(conn, JCJG, cljg, tp, tph, bjtm, djbh);
			}
		}
	}

	/**
	 * �޼�����ʽ.
	 * 
	 * @param conn
	 * @param JCJG
	 * @param cljg
	 * @param tp
	 * @param tph
	 * @param bjtm
	 * @param djbh
	 * @throws SQLException
	 */
	private void ljDealWay(Connection conn, String JCJG, String cljg, String tp, String tph, String bjtm, String djbh) throws SQLException {
		String lx = "";

		if ("".equals(cljg)) {
			throw new RuntimeException("��������:" + bjtm + "��δ��д������!");
		}

		if ("����".equals(cljg) || "��Ʒ����".equals(cljg)) {
			lx = "��Ʒ����";
		} else if ("�˻�".equals(cljg)) {
			lx = "�˻�";
		} else {
			throw new RuntimeException("��������:" + bjtm + "����������" + cljg + " ����ʶ��!");
		}

		// ��ȡ������ֿ����
		String CKDM = "FH01";
		String CKMC = "�ۺ󷵻���";
		DAOUtil.executeUpdate(conn, "update BO_AKL_RMA_KCMX set LX=?, CKDM=?, CKMC=? where  ZJM = ? and DDH = ?", lx, CKDM, CKMC, bjtm, djbh);
	}

	/**
	 * ���ϴ���ʽ.
	 * 
	 * @param conn
	 * @param JCJG
	 * @param cljg
	 * @param tp
	 * @param tph
	 * @param bjtm
	 * @param djbh
	 * @throws SQLException
	 */
	private void sdDealWay(Connection conn, String JCJG, String cljg, String tp, String tph, String bjtm, String djbh) throws SQLException {
		String jljccy = DAOUtil.getStringOrNull(conn, "select XLMC from BO_AKL_DATA_DICT_S where DLBM = ? AND XLMC=?", "046", JCJG);
		if (jljccy != null) {
			if (!JCJG.equals("Z02���޼���Ʒ") && !JCJG.equals("NPF����޹���")) {
				JCJG = "";
			}
		}

		// ���ϴ���ʽ.
		String lx = "";
		if ("�˻�".equals(cljg)) {
			lx = "�˻�";
		} else {
			lx = "��Ʒ����";
		}

		// ��������Ϊ�գ���Ϊ ��Ʒ����
		if ("".equals(JCJG)) {
			lx = "��Ʒ����";
		}

		// ���Ϊ��������Ϊ ��Ʒ����
		if (!"".equals(tph) || "��������".equals(tp)) {
			lx = "��Ʒ����";
		}

		// ���´���ʽ
		DAOUtil.executeUpdate(conn, "update BO_AKL_RMA_KCMX set LX=? where ZJM = ? and DDH = ?", lx, bjtm, djbh);
	}

}
