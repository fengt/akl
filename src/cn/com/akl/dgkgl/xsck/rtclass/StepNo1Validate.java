package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dgkgl.xsck.biz.DGOutBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ���۵���.
	 */
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	/**
	 * ����ڵ�.
	 */
	private static final int CK_STEPNO = 3;

	private DGOutBiz outBiz = new DGOutBiz();

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��һ�ڵ�У���¼�");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;

		try {
			conn = DBSql.open();

			String xsdh = DAOUtil.getString(conn, queryXSDH, bindid);

			/** ��ѯ�Ƿ�����ͬ�����۵��ŵĳ�������(λ�ڳ���ڵ�ǰ������) */
			validateRepeatSalesOrderInOut(conn, bindid, xsdh);

			/** ��֤���۶�������������������Ƿ���� */
			outBiz.validateSalesAndOutNumIsEquals(conn, bindid, uid);

			/** ��֤���ϵĿ�λ�����Ƿ���� */
			outBiz.validateMaterialAvailableAmount(conn, bindid, uid);

			/** ��ѯ����״̬ */
			outBiz.validateIsCanOut(conn, bindid, uid, xsdh);

			/** ��֤���������Ƿ񳬹����ѳ������� */
			outBiz.validateSalesOutNum(conn, bindid, xsdh);

			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * У�����֮ǰ�Ƿ�����ͬ�����۶���.
	 * 
	 * @param conn
	 * @param bindid
	 * @param xsddh
	 * @throws SQLException
	 */
	public void validateRepeatSalesOrderInOut(Connection conn, int bindid, String xsddh) throws SQLException {
		DAOUtil.executeQueryForParser(conn, "SELECT BINDID FROM BO_BO_AKL_DGCK_P WHERE XSDH=? AND ISEND=0 AND BINDID<>?", new DAOUtil.ResultPaser() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				/** ��ѯ������ͬ���۶����ĳ������̣����Ҳ��λ�ڵڼ��ڵ� */
				Integer wfsid = DAOUtil.getIntOrNull(conn, "SELECT TOP 1 WFSID FROM WF_TASK WHERE BIND_ID=? ORDER BY BEGINTIME DESC",
						reset.getInt("BINDID"));
				Integer stepNo = DAOUtil.getIntOrNull(conn, "SELECT STEPNO FROM SYSFLOWSTEP WHERE ID=?", wfsid);
				/** �ȶԽڵ��Ƿ���ڳ���ڵ� */
				if (stepNo <= CK_STEPNO) {
					throw new RuntimeException("����ͬ�����۶������ڱ�����һ��ֻ����һ����");
				}
				return true;
			}
		}, xsddh, bindid);
	}

}
