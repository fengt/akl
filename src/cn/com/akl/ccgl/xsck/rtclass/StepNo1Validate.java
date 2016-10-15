package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ���������۶�����
	 */
	private static final String queryXSDDH = "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=? ";
	/**
	 * ��ѯ�������۶����Ƿ����ظ���
	 */
	private static final String queryXSDDHCount = "SELECT COUNT(*) FROM BO_AKL_CKD_HEAD WHERE ISEND=0 AND XSDDH=?";
	/**
	 * ������۶����Ķ��������ϼ�.
	 */
	private static final String QUERY_XSDD_DDSLHJ = "SELECT SUM(DDSL) FROM BO_AKL_WXB_XSDD_BODY WHERE DDID=?";
	/**
	 * ��ȡ���ⵥ�������ϼ�.
	 */
	private static final String QUERY_CKD_SLHJ = "SELECT SUM(SJSL) FROM BO_AKL_CKD_BODY WHERE BINDID=?";

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setDescription("����У�����۶����Ƿ����µ�");
		setVersion("1.0.0");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			String xsddh = DAOUtil.getString(conn, queryXSDDH, bindid);

			Integer xsddsl = DAOUtil.getIntOrNull(conn, QUERY_XSDD_DDSLHJ, xsddh);
			Integer ckdsl = DAOUtil.getIntOrNull(conn, QUERY_CKD_SLHJ, bindid);
			if (!xsddsl.equals(ckdsl)) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�������������������������!");
				return false;
			}

			int count = DAOUtil.getInt(conn, queryXSDDHCount, xsddh);
			if (count > 1) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�������۶����Ѿ���������!");
				return false;
			} else {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�������⣬����ϵ����Ա!");
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
