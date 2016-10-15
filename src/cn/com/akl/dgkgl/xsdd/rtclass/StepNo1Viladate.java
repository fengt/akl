package cn.com.akl.dgkgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Viladate extends WorkFlowStepRTClassA {
	/**
	 * ��ѯ�����Ĳ��ű�źͿͻ����.
	 */
	private static final String queryBMBH_KHBH_CZ = "select count(*) from BO_AKL_KH_P where SSKHBH=? AND KHID=?";
	/**
	 * ��ѯ������ͬһ�ͻ��ɹ����Ų�������ͬ����.
	 */
	private static final String queryXTWL = "SELECT COUNT(*) FROM (select WLBH, KHCGDH from BO_AKL_DGXS_S where bindid=? group by WLBH, KHCGDH HAVING count(*)>1) A";
	/**
	 * ��ѯÿ�����ϵ���������.
	 */
	private static final String QUERY_MATERIAL_SALESNUM = "SELECT WLBH, XH, SUM(ISNULL(XSSL, 0)) XSSL FROM BO_AKL_DGXS_S WHERE BINDID=? GROUP BY WLBH, XH";
	/**
	 * ��ѯ���ϵĿ��.
	 */
	private static final String QUERY_MATERIAL_STOCK = "SELECT SUM(ISNULL(PCSL, 0)) FROM BO_AKL_DGKC_KCHZ_P WHERE WLBH=?";

	public StepNo1Viladate(UserContext uc) {
		super(uc);
		setProvider("V1.0.0");
		setDescription("У�鵥����Ϊ�ռ���֤������ͬһ�ͻ��ɹ����Ų�������ͬ����");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> h = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGXS_P", bindid);

		Connection conn = null;
		try {
			conn = DBSql.open();
			String HZBH = PrintUtil.parseNull(h.get("HZBH"));
			String BMBH = PrintUtil.parseNull(h.get("BMBH"));

			/** �������Ĳ��� */
			String countBM = DAOUtil.getString(conn, queryBMBH_KHBH_CZ, HZBH, BMBH);
			if ((countBM == null || countBM.equals("0")) && !BMBH.equals("")) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�û���û�д˲��ţ�", true);
				return false;
			}

			/** ��鵥�����Ƿ�����ͬ���ϵ����� */
			int count = DAOUtil.getIntOrNull(conn, queryXTWL, bindid);
			if (count > 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��������ͬһ�ɹ����Ŵ�����ͬ���ϵ����ݣ����飡", true);
				return false;
			}

			/** ����ÿ�������Ƿ����㹻. */
			DAOUtil.executeQueryForParser(conn, QUERY_MATERIAL_SALESNUM, new DAOUtil.ResultPaser() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					Integer pcsl = DAOUtil.getIntOrNull(conn, QUERY_MATERIAL_STOCK, reset.getString("WLBH"));
					if (pcsl < reset.getInt("XSSL")) {
						throw new RuntimeException("�ͺ�Ϊ��" + reset.getString("XH") + " ��������������! ��������Ϊ��" + reset.getInt("XSSL") + " - �������Ϊ��" + pcsl);
					}
					return true;
				}
			}, bindid);

			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "ϵͳ�������⣬����ϵ����Ա��", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
