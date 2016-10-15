package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;

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
		setDescription("��һ���ڵ��У���¼������ͻ��������ظ�����������ʾ��");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		String table = getParameter(PARAMETER_TABLE_NAME).toString();

		if (table.equals("BO_AKL_WXB_XSDD_HEAD")) {
			Connection conn = null;
			try {
				conn = DAOUtil.openConnection();

				// ��ѯ���������ͬ�Ŀͻ��ɹ���
				String khcgdh = hashtable.get("KHCGDH");
				// δ��д�ͻ��ɹ����Ų������ж�
				if (khcgdh == null || "".equals(khcgdh)) {
					return true;
				}
				// ��ѯ���ƵĲɹ�����
				SalesOrderBiz biz = new SalesOrderBiz();
				int count = biz.getKHCGDDHRepeatCount(conn, bindid, khcgdh);
				if (count > 0) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "ϵͳ�д�����ͬ�Ŀͻ��ɹ���ţ�����Ȼ���Լ�������", true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}

}
