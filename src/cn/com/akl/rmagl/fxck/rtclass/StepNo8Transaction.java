package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo8Transaction extends WorkFlowStepRTClassA{

	public StepNo8Transaction() {
		super();
	}

	public StepNo8Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���̽���������ʵ�ʳ�������-ǩ�ճ��������������0���޸ĳ��ⵥ�Ķ���״̬Ϊǩ�ղ����ϴ���������������ֶ����ƣ�ǩ�յ���");
	}

	@Override
	public boolean execute() {
		//�ϴ���������������ֶ����ƣ�ǩ�յ���
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_QSD_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
			//����ʵ�ʳ�������-ǩ�ճ��������������0���޸ĳ��ⵥ�Ķ���״̬Ϊǩ�ղ���
			if(count!=0){
				// ��ѯ������
				//String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
				
				// ���³��ⵥ״̬ -> ����״̬  �������״̬���������۶�������Ҫ�޸�����
				// ���µ�ǰ���ⵥ״̬Ϊ����״̬
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 1, bindid);
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "ǩ�����������������������ǩ�ղ��������֣����������Ϻ󽫻������������̣�", true);
			} else {
				// ���µ�ǰ���ⵥ״̬Ϊ����״̬
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 0, bindid);
			}
			
			//�ϴ���������������ֶ����ƣ�ǩ�յ���
			int qsdIsNull = DAOUtil.getInt(conn, "SELECT count(*) FROM BO_AKL_CKD_HEAD WHERE BINDID=? AND QSD is null", bindid);
			if(qsdIsNull == 0){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ϴ�ǩ�յ�����", true);
				return false;
			}
			
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
