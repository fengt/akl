package cn.com.akl.zsj.wlxx.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.zsj.util.ZSJCommonUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1AfterSave extends WorkFlowStepRTClassA {
	
	public StepNo1AfterSave() {
		super();
	}

	public StepNo1AfterSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�ڵ�һ������¼��������������ϱ��&�ͻ����&��Ӧ�̱��");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
		
		if(!"BO_AKL_WXXS_ZDRXX".equals(tableName))
			return true;
		
		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
			ZSJCommonUtil.executeSeq(conn, bindid, "BO_AKL_WLXX", "WLBH", "PPID", 5, 3);
			conn.commit();
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨��������!");
		} finally {
			DBSql.close(conn, null, null);
		}
		return true;
	}
	

}
