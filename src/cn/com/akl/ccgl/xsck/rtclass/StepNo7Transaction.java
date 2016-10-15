package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo7Transaction extends WorkFlowStepRTClassA {

	/**
	 * 更新签收单的实收数量为应收数量，设置默认值.
	 */
	private static final String UPDATE_QSD_SSSL = "UPDATE BO_AKL_QSD_S SET SSSL=YSSL WHERE BINDID=?";

	public StepNo7Transaction() {
		super();
	}

	public StepNo7Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("签收确认后，将出库数量默认为签收出量，将出库单推送至财务系统系统中的出库单");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		Connection conn = null;

		boolean backFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
		if (backFlag) {
			return true;
		}

		try {
			conn = DAOUtil.openConnectionTransaction();

			// 1.修改出库状态为已签收
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CKZT=? WHERE BINDID=?", XSCKConstant.CKD_CKZT_YQS, bindid);

			// 2.计算账期，并根据账期计算应收，根据客户账期字段计算账期：签收日期+客户账期天数
			/*
			 * String khid = DAOUtil.getString(conn,
			 * "SELECT KH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid); int zqts =
			 * DAOUtil.getInt(conn, "SELECT ZQTS FROM BO_AKL_KH_P WHERE KHID=?", khid); Date shrq =
			 * DAOUtil.getDate(conn, "SELECT SHRQ FROM BO_AKL_QSD_P WHERE BINDID=? ", bindid);
			 * String xsddh = DAOUtil.getString(conn,
			 * "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid); Calendar cinstance =
			 * Calendar.getInstance(); cinstance.setTime(shrq); cinstance.add(Calendar.DAY_OF_MONTH,
			 * zqts);
			 */
			// TODO 这个怎么解释
			// DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET ZQ=? WHERE DDID=?", new
			// Timestamp(cinstance.getTimeInMillis()), xsddh);

			// 3.办理后，出库数量默认为签收数量
			DAOUtil.executeUpdate(conn, UPDATE_QSD_SSSL, bindid);

			FillBiz biz = new FillBiz();
			biz.insertYS(conn, bindid, getUserContext().getUID());

			// TODO 4.并将BPM系统的亚昆出库单推送至财务系统中的出库单

			// 5.更新销售订单为已签收
			String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_QRQS, xsddh);

			conn.commit();
			return true;
		} catch (SQLException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
