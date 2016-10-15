package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsck.biz.DGOutFillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSaveCF extends WorkFlowStepRTClassA {
	/**
	 * ��ѯ���۵���.
	 */
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";

	private DGOutFillBiz fillBiz = new DGOutFillBiz();

	public StepNo1BeforeSaveCF() {
		super();
	}

	public StepNo1BeforeSaveCF(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("������ǰ�¼������뵥������");
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		if ("BO_BO_AKL_DGCK_P".equals(tablename)) {
			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();
				String xsddh = hashtable.get("XSDH");

				/** ��¼������۶���Ϊ��ֵʱ�������ӱ��ֵ����ϼ�¼ */
				if (xsddh == null || xsddh.trim().length() == 0) {
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid);
				} else {
					/** ��ȡ��һ�ε����۶�����. */
					String xsddh2 = DAOUtil.getStringOrNull(conn, queryXSDH, bindid);

					/** ���������۶�������ͬ����Ҫ�����ڲ���. */
					if (!xsddh.equals(xsddh2)) {
						/** ɾ���������� */
						BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid);
						/** ץȡ�����¼ */
						fillBiz.fetchLockMaterial(conn, bindid, uid, xsddh);
					}
				}

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
