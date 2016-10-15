package cn.com.akl.ccgl.wply.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	//��ѯ��Ʒ���õ���.
	private final String QUERY_WPLY_BODY = "SELECT WLBH,PCH,SL,HWDM FROM BO_AKL_WPLY_S WHERE BINDID=?";
	
	//���ó��⣬����ϸ.
	private static final String UPDATE_OUT_MX = "update BO_AKL_KC_KCMX_S set KWSL=ISNULL(KWSL, 0)-? "
			+ "where HWDM=? AND WLBH=? AND PCH=? AND KWSL>=?";
	
	//���ó��⣬������.
	private static final String UPDATE_OUT_HZ = "update BO_AKL_KC_KCHZ_P set CKSL=ISNULL(CKSL, 0)+? "
			+ "where WLBH=? AND PCH=? AND ?<=RKSL";
	
	//������»��ܱ����������.
	private static final String UPDATE_OUT_HZ_PCSL = "update BO_AKL_KC_KCHZ_P set PCSL=ISNULL(RKSL, 0)-ISNULL(CKSL, 0) "
			+ "where WLBH=? AND PCH=?";
	
	//��������Ʒ״̬
	private static final String zt0 = "����Ч";
	private static final String UPDATE_WPLY_ZT = "UPDATE BO_AKL_WPLY_P SET ZT=? WHERE BINDID=?";

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("������ת���¼�: ���¿��");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// ��ȡ�˻���˲˵�.
		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�˻�");
		
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();

			// ɾ������
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindid);
				
			// ���˻ص�ʱ��ۼ����
			if(!backFlag){
				// 2�����¿����ϸ
				// ��ѯ�ӱ��¼���ۼ����
				DAOUtil.executeQueryForParser(conn, QUERY_WPLY_BODY, new DAOUtil.ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						outOfWarehouseMX(conn, reset.getString("WLBH"), reset.getString("PCH"), reset.getString("HWDM"), reset.getInt("SL"));
						outOfWarehouseHZ(conn, reset.getString("WLBH"), reset.getString("PCH"), reset.getInt("SL"));
						return true;
					}
				}, bindid);
				//3���������õ�״̬:����Ч
				DAOUtil.executeUpdate(conn, UPDATE_WPLY_ZT, zt0,bindid);
			}
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * ���ó��⣬����ϸ.
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param kwbh
	 * @param sl
	 * @throws SQLException
	 */
	public void outOfWarehouseMX(Connection conn, String wlbh, String pch, String kwbh, Integer sl) throws SQLException {
		if (0 == DAOUtil.executeUpdate(conn, UPDATE_OUT_MX, sl, kwbh, wlbh, pch, sl)) {
			throw new RuntimeException("���Ϻ�:" + wlbh + " �ڻ�λ���룺" + kwbh + "��治��" + sl + "!");
		}
	}
	
	/**
	 * ���ó��⣬������.
	 * @param conn
	 * @param wlbh
	 * @param pch
	 * @param sl
	 * @throws SQLException
	 */
	public void outOfWarehouseHZ(Connection conn, String wlbh, String pch, Integer sl) throws SQLException {
		if (0 == DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ, sl, wlbh, pch, sl)) {
			throw new RuntimeException("���Ϻ�:" + wlbh + " ����" + pch + "���ϵĳ��������Ѿ��ﵽ���ޣ���������ܱ�");
		}
		DAOUtil.executeUpdate(conn, UPDATE_OUT_HZ_PCSL, wlbh, pch);
	}

}
