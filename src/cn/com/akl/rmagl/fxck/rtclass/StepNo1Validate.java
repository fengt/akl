package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA{
	
	// ��ѯ���������۶����� 
	private static final String QUERY_RMAFXDH = "SELECT RMAFXDH FROM BO_AKL_CKD_HEAD WHERE BINDID=? ";
	// ��ѯ�������۶����Ƿ����ظ���
	private static final String QUERY_RMAFXDH_COUNT = "SELECT COUNT(*) FROM BO_AKL_CKD_HEAD WHERE ISEND=0 AND RMAFXDH=?";

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
		try{
			conn = DBSql.open();
			String rmafxdh = DAOUtil.getStringOrNull(conn, QUERY_RMAFXDH, bindid);
			Integer count = DAOUtil.getIntOrNull(conn, QUERY_RMAFXDH_COUNT, rmafxdh);
			if(count!=null&&count > 1){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�������۶����Ѿ���������!");
				return false;
			} else {
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�������⣬����ϵ����Ա!");
			return true;
		}finally {
			DBSql.close(conn, null, null);
		}
	}

}
