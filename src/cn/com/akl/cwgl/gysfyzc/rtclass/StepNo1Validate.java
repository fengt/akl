package cn.com.akl.cwgl.gysfyzc.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ�ظ���������Ϣ.
	 */
	private static final String QUERY_WLBH_REPEAR_COUNT = "SELECT WLBH FROM BO_AKL_WXB_XS_POS_BODY WHERE BINDID=? GROUP BY WLBH HAVING COUNT(*)>1";

	/**
	 * ��ѯ���ϱ�ź��ͺŲ�ƥ����ͺ�.
	 */
	private static final String QUERY_WLXX_NOMATCH = "SELECT a.XH FROM BO_AKL_WXB_XS_POS_BODY a LEFT JOIN BO_AKL_WLXX b ON a.WLBH=b.WLBH AND a.XH=b.XH WHERE a.BINDID=? AND (b.WLBH is null OR b.XH is null)";

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��Ӧ�̷���֧�������һ�ڵ�У���¼�.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			String repeatXh = DAOUtil.getStringOrNull(conn, QUERY_WLBH_REPEAR_COUNT, bindid);

			if (repeatXh == null) {
				String xh = DAOUtil.getStringOrNull(conn, QUERY_WLXX_NOMATCH, bindid);
				if (xh == null) {
					return true;
				} else {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͺţ�" + xh + " �����ϱ��ƥ�����������ͺŵ����ϱ��!");
					return false;
				}
			} else {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ϱ�ţ�" + repeatXh + " �����ظ���������ͺ�!");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
