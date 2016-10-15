package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo7Transaction extends WorkFlowStepRTClassA {

	public StepNo7Transaction() {
		super();
	}

	public StepNo7Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���߽ڵ�������ת�¼����ı�ǩ�ղ���״ֵ̬");
	}

	@Override
	public boolean execute() {
		//�ϴ���������������ֶ����ƣ�ǩ�յ���
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�˻�");
		if(!th){
			if("BO_BO_AKL_DGCK_P".equals(tablename)||"BO_AKL_QSD_P".equals(tablename)){
				Connection conn = null;
				try{
					conn = DAOUtil.openConnectionTransaction();
					int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_QSD_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
					//����ʵ�ʳ�������-ǩ�ճ��������������0���޸ĳ��ⵥ�Ķ���״̬Ϊǩ�ղ���
					if(count!=0){
						// ���³��ⵥ״̬ -> ����״̬  �������״̬���������۶�������Ҫ�޸�����
						// ���µ�ǰ���ⵥ״̬Ϊ����״̬
						DAOUtil.executeUpdate(conn, "UPDATE BO_BO_AKL_DGCK_P SET CYZT=? WHERE BINDID=?", "1", bindid);
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "ǩ�����������������������ǩ�ղ��������֣�", true);
					} else {
						// ���µ�ǰ���ⵥ״̬Ϊ����״̬
						DAOUtil.executeUpdate(conn, "UPDATE BO_BO_AKL_DGCK_P SET CYZT=? WHERE BINDID=?", "0", bindid);
					}
					final String xsdh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGXS_P SET ZT=? WHERE XSDDID=?", "ǩ��", xsdh);
					conn.commit();
					return true;
				} catch(Exception e){
					DAOUtil.connectRollBack(conn);
					e.printStackTrace();
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
					return false;
				} finally {
					DBSql.close(conn, null, null);
				}
			}
		}
		return true;
	}

}
