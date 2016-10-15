package cn.com.akl.ccgl.thck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {
	
	//���²ɹ�������״̬
	private final String UPDATE_CGDD = "UPDATE BO_AKL_CGDD_HEAD SET DZT='���˵�' WHERE DDID=?";
	
	//���ⵥ������
	private final String QUERY_CKD_JE = "SELECT SUM(SJSL*CBDJ)WSYFJE, SUM(SJSL*DJ)HSYFJE,YDH FROM BO_AKL_CKD_BODY WHERE BINDID=? GROUP BY YDH";
	
	//����Ӧ����¼
	private final String UPDATE_YF = "UPDATE BO_AKL_YF SET WSYFJE=ISNULL(WSYFJE,0)-?,HSYFJE=ISNULL(HSYFJE,0)-?,LB=2 WHERE RKDH=?";//LB=2��ʾ�ɹ��˻��ۼ�Ӧ��
	
	//��ѯ���ⵥ����.
	private final String QUERY_CKD_BODY = "SELECT KWBH, SJSL, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	
	//��������.
	private KCBiz kcbiz = new KCBiz();
	
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
			String ddid = DBSql.getString(conn, "SELECT THDH FROM BO_AKL_CKD_HEAD WHERE BINDID="+bindid, "THDH");//�ɹ�������

			// ɾ������
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindid);
				
			// ���˻ص�ʱ��ۼ����
			if(!backFlag){
				// 2�����¿����ϸ
				// ��ѯ�ӱ��¼���ۼ����
				DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						kcbiz.outOfWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SJSL"));
						kcbiz.outOfWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SJSL"));
						return true;
					}
				}, bindid);
				
				// 3��������ⵥ�ſۼ�Ӧ�����
				DAOUtil.executeQueryForParser(conn, QUERY_CKD_JE, new ResultPaser(){
					public boolean parse(Connection conn, ResultSet rs) throws SQLException{
						int count = DAOUtil.executeUpdate(conn, UPDATE_YF, rs.getDouble(1), rs.getDouble(2), rs.getString(3));
						if(count == 0){
							throw new RuntimeException("��ⵥ��Ϊ"+rs.getString(3)+"��Ӧ��������ʧ�ܣ����飡");
						}
						
						return true;
					}
				}, bindid);
				
				//4�����²ɹ�����״̬�����˵�
				DAOUtil.executeUpdate(conn, UPDATE_CGDD, ddid);
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
	
}
