package cn.com.akl.shgl.qhsq.rtclass;

import java.sql.Connection;

import cn.com.akl.shgl.qhsq.biz.QHSQBiz;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	public StepNo2Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("����ȱ����¼���״̬��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ͬ��");
		
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			String bhlx = DAOUtil.getStringOrNull(conn, QHSQCnt.QUERY_QHSQ_P_BHLX, bindid);//��������
			
			if(yes){
				if(QHSQCnt.bhlx0.equals(bhlx)){//������������
					QHSQBiz.updateStatus(conn, bindid, bhlx, QHSQCnt.zt1);
					QHSQBiz.setStatus(conn, QHSQCnt.QUERY_QHSQ_S, QHSQCnt.zt1, bindid);
				}else if(QHSQCnt.bhlx1.equals(bhlx)){//�������벹��
					QHSQBiz.updateStatus(conn, bindid, bhlx, QHSQCnt.zt1);
					DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHJL_ZT2, QHSQCnt.zt1, bindid);
				}
			}else{
				if(QHSQCnt.bhlx1.equals(bhlx)){//�������벹��
					DAOUtil.executeUpdate(conn, QHSQCnt.DELETE_QHJL, bindid);
				}
			}
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
}
