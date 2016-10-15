package cn.com.akl.cwgl.gysfyzc.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class LastStepTransaction extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ��ʼ����
	 */
	private static final String QUERY_KSSJ = "SELECT KSSJ FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?";

	/**
	 * ����POS״̬
	 */
	private static final String UPDATE_ZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET POSZT=? WHERE BINDID=?";

	/**
	 * ״̬��ִ����
	 */
	private static final String POSZT_ZXZ = "030003";

	/**
	 * ���_ POS.
	 */
	private static final String LB_POS = "043028";
	/**
	 * POS���_�߼۱�.
	 */
	private static final String LB_PP = "043026";
	/**
	 * ���_������.
	 */
	private static final String LB_DD = "043027";
	/**
	 * ���_�г�����.
	 */
	private static final String LB_MDF = "043025";
	/**
	 * POS�ʽ����״̬�ֶ�- δ�ֿ�.
	 */
	private static final String POSZJC_ZT_WDK = "1";

	public LastStepTransaction() {
		super();
	}

	public LastStepTransaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���һ���ڵ㼤���¼�");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();

			String lb = DAOUtil.getStringOrNull(conn, "SELECT LB FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);

			if (lb.equals(LB_POS)) {
				Date kssj = DAOUtil.getDate(conn, QUERY_KSSJ, bindid);
				Date now = Calendar.getInstance().getTime();
				if (kssj.getTime() < now.getTime()) {
					// ����״̬
					DAOUtil.executeUpdate(conn, UPDATE_ZT, POSZT_ZXZ, bindid);
				}
			} else {
				BigDecimal poszje = DAOUtil.getBigDecimalOrNull(conn, "SELECT SUM(POSSL*POSDJ) FROM BO_AKL_WXB_XS_POS_BODY WHERE BINDID=?", bindid);
				String posbh = DAOUtil.getStringOrNull(conn, "SELECT POSBH FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);
				String tpm = DAOUtil.getStringOrNull(conn, "SELECT TPM FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);
				String gysbh = DAOUtil.getStringOrNull(conn, "SELECT GYSBH FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);

				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("LX", lb);
				hashtable.put("FABH", posbh);
				hashtable.put("TPM", tpm);
				hashtable.put("GYSBH", gysbh);
				hashtable.put("ZT", POSZJC_ZT_WDK);
				hashtable.put("YSYJE", "0");
				hashtable.put("BXSQJE", "0");
				hashtable.put("YSJE", poszje.toString());
				hashtable.put("SSJE", "0");
				hashtable.put("POSJE", poszje.toString());

				// �����ʽ��
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_MXB", hashtable, bindid, getUserContext().getUID());
			}

			conn.commit();
			return true;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
