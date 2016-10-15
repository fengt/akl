package cn.com.akl.zsj.gys.rtclass;

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
				//客户编号 = 类别后两位 + 3位序列号
				ZSJCommonUtil.executeSeq(conn, bindid
						, "BO_AKL_GYS_P", "GYSBH"
						, "LBID", 3, 2);
				conn.commit();
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance()
					.putMessage(uid, "后台出现问题!");
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}

}
