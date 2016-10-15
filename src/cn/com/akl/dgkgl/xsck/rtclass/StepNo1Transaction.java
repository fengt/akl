package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsck.biz.DGOutFillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	/**
	 * ��ѯ���۶������ϵ���������.
	 */
	private static final String QUERY_XSDD_WL_XSSL = "SELECT (ISNULL(XSSL,0)-ISNULL(YCKSL, 0)) xssl FROM BO_AKL_DGXS_S WHERE WLBH=? AND DDID=? AND ISNULL(KHCGDH, '')=?";
	/**
	 * �鿴���������Ƿ�����Ҫ���кŵ�����
	 */
	private static final String QUERY_SFXLH = "select count(*) from BO_BO_AKL_DGCK_S s left join BO_AKL_WLXX x on s.WLBH = x.WLBH where s.bindid=? AND x.SFXLH = 1";
	/**
	 * ��ѯ���ⵥ����.
	 */
	private static final String QUERY_CKD_BODY = "SELECT WLBH, sum(ISNULL(SFSL, 0)) SFSL, XH, KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID=? group by WLBH, XH, KHCGDH";
	/**
	 * �����Ƿ����к�.
	 */
	private static final String UPDATE_SFXLH = "update BO_BO_AKL_DGCK_P set SFXLH = ? where bindid =?";
	/**
	 * ���³��ⵥ״̬.
	 */
	private static final String UPDATE_CKD_ZT = "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?";
	/**
	 * ��ѯ���۶�����.
	 */
	private static final String QUERY_XSDDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";

	private DGOutFillBiz fillBiz = new DGOutFillBiz();

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("��һ�ڵ�����¼����������۶������ѳ�������");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet reset = null;
		int a = 0;
		try {
			conn = DAOUtil.openConnectionTransaction();

			/** ��ѯ���۶����� */
			String xsdh = DAOUtil.getStringOrNull(conn, QUERY_XSDDH, bindid);

			/** �����Ƿ����кŵ�״̬ */
			int count = DAOUtil.getIntOrNull(conn, QUERY_SFXLH, bindid);
			DAOUtil.executeUpdate(conn, UPDATE_SFXLH, count > 0 ? "1" : "0", bindid);

			/** �������ⵥ������ */
			stat = conn.prepareStatement(QUERY_CKD_BODY);
			reset = DAOUtil.executeFillArgsAndQuery(conn, stat, bindid);
			while (reset.next()) {
				String xssl = DAOUtil.getStringOrNull(conn, QUERY_XSDD_WL_XSSL, reset.getString("WLBH"), xsdh, reset.getString("KHCGDH"));
				if (Integer.parseInt(xssl) < reset.getInt("SFSL")) {
					throw new RuntimeException("���ܿ��һ�ڵ����۳��⣬��⵽��ǰ������Ϊ��" + xsdh + ", �ͻ��ɹ�����:" + PrintUtil.parseNull(reset.getString("KHCGDH"))
							+ ", ���Ϻ�Ϊ:" + reset.getString("WLBH") + ", ʵ������Ϊ:" + reset.getInt("SFSL") + ", ʵ��������������������" + xssl);
				}
				if (Integer.parseInt(xssl) > reset.getInt("SFSL")) {
					a++;
				}
			}
			if (a > 0) {
				DAOUtil.executeUpdate(conn, UPDATE_CKD_ZT, "���ֳ���", bindid);
			} else {
				DAOUtil.executeUpdate(conn, UPDATE_CKD_ZT, "δ����", bindid);
			}

			/** ɾ���������� */
			fillBiz.removeLockMaterial(conn, xsdh);
			
			/** �������������²��� */
			fillBiz.insertLockFromBody(conn, bindid, uid, xsdh);

			/** �� ������δ��������� ������������ */
			fillBiz.fetchCanUseMaterial(conn, bindid, uid, xsdh);

			/** ɾ�����ܳ����������� */
			/** ��ѯ���д����۶���������ID */
			//ArrayList<Integer> allBindidList = DAOUtil.getInts(conn, "select bindid from BO_BO_AKL_DGCK_P where XSDH=? AND ISEND=0", xsdh);
			//ArrayList<Integer> removeBindidList = new ArrayList<Integer>(5);
			//for (Integer bind : allBindidList) {
				/** ��ѯ���̵�ǰ�ڵ� */
				//int stepNo = DAOUtil.getIntOrNull(conn,
				//		"SELECT MAX(STEPNO) STEPNO FROM SYSFLOWSTEP WHERE ID in (SELECT DISTINCT WFSID FROM WF_TASK WHERE BIND_ID = ?)", bind);
				/** ����Ǵ����̣����������ڵ�һ�ڵ���ߵ�һ�ڵ���������Ҫɾ�������̣�removeBindidListΪ��ɾ������ID��ż��� */
				//if (bind != bindid && stepNo <= 1) {
				//	removeBindidList.add(bind);
				//}
			//}

			conn.commit();
			
			/** ɾ������ */
			/*
			for (Integer b : removeBindidList) {
				WorkflowInstanceAPI.getInstance().removeProcessInstance(b);
			}
			*/
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} finally {
			DBSql.close(conn, stat, reset);
		}
	}

}
