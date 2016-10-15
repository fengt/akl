package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��������¼�����ֿ��");

		/**
		 * �������⣺��������̲�ֹ���δ�������һ���������̽��˿�����ߣ��ʹ����˳�ͻ��
		 */
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		FillBiz fillbiz = new FillBiz();

		if ("BO_AKL_CKD_HEAD".equals(tablename)) {

			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();

				String xsddh = hashtable.get("XSDDH");

				if (xsddh == null || xsddh.trim().length() == 0) {
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CKD_BODY", bindid);
					conn.commit();
					return true;
				}

				String xsddh2 = DAOUtil.getStringOrNull(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
				if (xsddh.equals(xsddh2)) {
					return true;
				}

				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CKD_BODY", bindid);
				fillbiz.queryAllWlxx(conn, bindid, uid, xsddh);

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
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��ֳ������⣬����ϵ����Ա��", true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}
}
