package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo9Transaction extends WorkFlowStepRTClassA {

	public StepNo9Transaction() {
		super();
	}

	public StepNo9Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription(".���ͨ�������ݿͻ���Ϣ��ġ��Ƿ�Ԥ�ա�=���񡱣���Ӧ�ձ��������2.���ͨ��������ǩ������-ʵ��ǩ�ճ��������������0��������ǩ�ղ���������");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// ��˲˵��ж�
		boolean tgFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "���ͨ��");
		// ��ͨ������
		if(!tgFlag) return true;
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			// �Ƿ�Ԥ��Ϊ����Ӧ�ձ��в������ݡ�
			String sfys = DAOUtil.getString(conn, "SELECT SFYS FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			if("��".equals(sfys) || "025001".equals(sfys)){
				// ��ѯ�ͻ����롢���۵��š����ⵥ�š�Ӧ�ս��
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				ps = conn.prepareStatement("SELECT KH,KHMC,CKDH,RMAFXDH,YSSLHJ FROM BO_AKL_CKD_HEAD WHERE BINDID=?");
				reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
				
				if(reset.next()){
					hashtable.put("KHBM", reset.getString("KH"));
					hashtable.put("KHMC", reset.getString("KHMC"));
					hashtable.put("CKDH", reset.getString("CKDH"));
					hashtable.put("XSDH", reset.getString("RMAFXDH"));
					hashtable.put("ZT", "δ��");//״̬
					hashtable.put("LB", XSCKConstant.KH);
					hashtable.put("YSJE", reset.getBigDecimal("YSSLHJ").toString());
				}
				// ����Ӧ�ձ�
				BOInstanceAPI.getInstance().createBOData("BO_AKL_YS", hashtable, bindid, getUserContext().getUID());
			}
			
			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, ps, reset);
		}
		
	}

}
