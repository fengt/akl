package cn.com.akl.ccgl.qssl;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA{

	public StepNo1Validate(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("�ж�������ֲ��죬�����ϴ����츽��");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		Connection conn = null;

		
		try {
			conn = DAOUtil.openConnectionTransaction();
			int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_DGCK_QSSL_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
			int counts = DAOUtil.getInt(conn, "SELECT COUNT(*) SL FROM (SELECT CKDH FROM BO_AKL_DGCK_QSSL_S WHERE BINDID = ? GROUP BY CKDH) A", bindid);
			if(count!=0&&counts==1){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ϴ�ǩ�յ�����", true);
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return true;
	}
}
