package cn.com.akl.zsj.kh.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.zsj.util.ZSJCommonUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

/**
 * ����ǰ�¼�.
 * @since 2014/09/16
 * @author huangming
 */
public class StepNo1AfterSave extends WorkFlowStepRTClassA {

	/**
	 * 	�޲ι��캯��.
	 */
	public StepNo1AfterSave() {
	super();
	}

	/**
	 * ���캯��.
	 * @param uc
	 */
	public StepNo1AfterSave(UserContext uc) {
		super(uc);
		setVersion("1.0.0");
		setDescription("����ǰ�¼�");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		String tableName = getParameter(PARAMETER_TABLE_NAME)
				.toString();

		if ("BO_AKL_WXXS_ZDRXX".equals(tableName)) {
			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();
				//�ͻ���� = ������λ + 3λ���к�
				ZSJCommonUtil.executeSeq(conn, bindid
						, "BO_AKL_KH_P", "KHID"
						, "LBID", 3, 2);
				conn.commit();
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance()
					.putMessage(uid, "��̨��������!");
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}
}
