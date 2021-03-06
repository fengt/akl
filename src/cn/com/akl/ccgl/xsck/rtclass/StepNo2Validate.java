package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo2Validate extends WorkFlowStepRTClassA {

	/**
	 * 查询型号数量不相等的数量.
	 */
	private final static String QUERY_XH_SL_NOEQUALS_COUNT = "SELECT COUNT(*) FROM (SELECT COUNT(*) a FROM BO_AKL_CCB_CKD_ZXD_S a, BO_AKL_CKD_BODY b WHERE a.bindid=b.bindid AND a.bindid=? AND a.XH=b.XH GROUP BY a.XH HAVING SUM(ISNULL(a.SL, 0))<>SUM(ISNULL(b.SL, 0))) a";
	/**
	 * 查询型号数量相等的型号.
	 */
	private final static String QUERY_XH_SL_NOEQUALS_MESSAGE = "SELECT a.XH+'-'+STR(SUM(ISNULL(a.SL, 0)))+'-'+STR(SUM(ISNULL(b.SL, 0))) FROM BO_AKL_CCB_CKD_ZXD_S a, BO_AKL_CKD_BODY b WHERE a.bindid=b.bindid AND a.XH=b.XH AND a.bindid=? GROUP BY a.XH HAVING SUM(ISNULL(a.SL, 0))<>SUM(ISNULL(b.SL, 0))";

	public StepNo2Validate() {
		super();
	}

	public StepNo2Validate(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("装箱节点，数据校验事件：验证装箱数量的正确性。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();

			Integer count = DAOUtil.getIntOrNull(conn, QUERY_XH_SL_NOEQUALS_COUNT, bindid);
			if (count != null && count > 0) {
				String message = DAOUtil.getStringOrNull(conn, QUERY_XH_SL_NOEQUALS_MESSAGE, bindid);
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "物料型号数量不一致" + message);
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
