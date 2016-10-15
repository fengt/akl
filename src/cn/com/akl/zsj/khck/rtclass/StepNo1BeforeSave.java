package cn.com.akl.zsj.khck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.SQLUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("У��ͻ��ֿ���");
	}

	@Override
	public boolean execute() {
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		int boid = getParameter(PARAMETER_USERDATA_ID).toInt();
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
		
		Connection conn = null;
		try{
			conn = DBSql.open();
			if("BO_AKL_KHCK".equals(tableName)){
				if(boid == 0){
					// �����ڣ���ѯ����
					int count = SQLUtil.findEqualsFieldValue(conn, "BO_AKL_KHCK", "KHCKID", hashtable.get("KHCKID"));
					if(count > 0){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͻ��ֿ���"+hashtable.get("KHCKID") +"�����ظ�", true);
						return false;
					}
				} else {
					// �Ѵ��ڣ���ѯ���˱���¼������м�¼
					int count = SQLUtil.findEqualsFieldValue(conn, "BO_AKL_KHCK", "KHCKID", hashtable.get("KHCKID"), "id<>"+boid);
					if(count > 0){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͻ��ֿ���"+hashtable.get("KHCKID") +"�����ظ�", true);
						return false;
					}
				}
			}
			return true;
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
