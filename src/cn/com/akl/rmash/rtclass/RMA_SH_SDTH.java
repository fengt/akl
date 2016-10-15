package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class RMA_SH_SDTH extends WorkFlowStepJumpRuleRTClassA {

	public RMA_SH_SDTH(UserContext arg0) {
		super(arg0);
		setVersion("RMA�ջ�����v1.0");
		setProvider("����");
		setDescription("���ڴ���ڶ��ڵ���ת����");
	}

	@Override
	public int getNextNodeNo() {

		// ��ȡ����ʵ��ID
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);

		Connection conn = null;
		String sql = null;

		String pp = ha.get("PP");

		try {
			conn = DBSql.open();
			//���Ʒ��׿��
			if ("����".equals(pp) || "006001".equals(pp) || "�޼�".equals(pp) || "006006".equals(pp)||"׿��".equals(pp)||"006440".equals(pp)) {
				// ��Ʒ��Ϊ����
				boolean JS = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "����");
				// ��������������ڵ㣬ǩ�վ��߼��ڵ�.
				if (JS) {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = '����' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 3;
				} else {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = 'ǩ��' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 5;
				}
			} else {
				boolean JS = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "����");
				if (JS) {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = '����' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 1;
				} else {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = 'ǩ��' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 4;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			return 0;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	@Override
	public String getNextTaskUser() {
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();

		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		boolean QS = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ǩ��");

		Connection conn = null;
		try {
			conn = DBSql.open();

			// ����·�ɹ�ϵ���Ҷ�Ӧ��Ʒ�ư�����.
			String PP = ha.get("PP");
			String sql = "select ZH from BO_AKL_RMASH_LYGX where PP = '" + PP + "'";
			String ZH = DBSql.getString(sql, "ZH");

			// ���������.
			sql = "select createuser from BO_AKL_WXB_XS_RMASH_P where bindid = " + bindid;
			String USER = DBSql.getString(sql, "createuser");

			// ���ǩ���˾ͷ���Ʒ�ư����˴�.
			return QS ? ZH : USER;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
