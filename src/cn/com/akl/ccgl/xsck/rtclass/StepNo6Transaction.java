package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo6Transaction extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ���ⵥ����.
	 */
	private final String QUERY_CKD_BODY = "SELECT KWBH, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	/**
	 * ��������
	 */
	private KCBiz kcbiz = new KCBiz();

	public StepNo6Transaction() {
		super();
	}

	public StepNo6Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���ǩ�յ�");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// �˻ر��
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");

		Connection conn = null;

		try {
			conn = DAOUtil.openConnectionTransaction();
			if (backFlag) {
				int nextStepNo = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, bindid, taskid);
				if (nextStepNo == 3) {
					// ��������
					kcbiz.insertLockBase(conn, bindid, uid);

					// 2�����¿����ϸ
					// ��ѯ�ӱ��¼���ۼ����
					DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new DAOUtil.ResultPaser() {
						public boolean parse(Connection conn, ResultSet reset) throws SQLException {
							kcbiz.enterWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SL"));
							kcbiz.enterWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SL"));
							return true;
						}
					}, bindid);
				}
			} else {
				FillBiz fillbiz = new FillBiz();
				fillbiz.fillQSDHead(conn, bindid, uid);
				fillbiz.fillQSDBody(conn, bindid, uid);
				
				// TODO У��������
				
			}
			conn.commit();
			return true;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
}
