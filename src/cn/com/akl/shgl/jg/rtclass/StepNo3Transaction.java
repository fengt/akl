package cn.com.akl.shgl.jg.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.jg.biz.JGBiz;
import cn.com.akl.shgl.jg.biz.JGConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	private JGBiz jgBiz = new JGBiz();

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�ۼ������棬ɾ�����⡢������.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();
		boolean flag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");
		
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			String jglx = DAOUtil.getStringOrNull(conn, JGConstant.QUERY_JGLX, bindid);
			
			if(flag){
				/**1��ɾ���ӹ����Ʒ������Ϣ.*/
				if (JGConstant.JGLX_ZCJG.equals(jglx)) {
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_JGWCHZ_S", bindid);
		        } 
				/**2�������Ŀ��.*/
				jgBiz.dealXh(conn, bindid, JGConstant.add, jglx);
			}else{
				service(conn, bindid);
			}
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
		jgBiz.removeLock(conn, bindid);
		// jgBiz.dealXh(conn, bindid);
		jgBiz.dealWc(conn, bindid, getUserContext().getUID());
		jgBiz.dealPj(conn, bindid);
	}

}
