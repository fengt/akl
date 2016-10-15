package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class RMA_SH_SDTH extends WorkFlowStepJumpRuleRTClassA {

	public RMA_SH_SDTH(UserContext arg0) {
		super(arg0);
		setVersion("RMA收货流程v1.0");
		setProvider("刘松");
		setDescription("用于处理第二节点跳转规则");
	}

	@Override
	public int getNextNodeNo() {

		// 获取任务实例ID
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);

		Connection conn = null;
		String sql = null;

		String pp = ha.get("PP");

		try {
			conn = DBSql.open();
			//添加品牌卓棒
			if ("闪迪".equals(pp) || "006001".equals(pp) || "罗技".equals(pp) || "006006".equals(pp)||"卓棒".equals(pp)||"006440".equals(pp)) {
				// 若品牌为闪迪
				boolean JS = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "拒收");
				// 拒收走数量差异节点，签收就走检测节点.
				if (JS) {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = '拒收' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 3;
				} else {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = '签收' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 5;
				}
			} else {
				boolean JS = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "拒收");
				if (JS) {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = '拒收' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 1;
				} else {
					sql = "update BO_AKL_WXB_XS_RMASH_P set ZT = '签收' where bindid = " + bindid;
					DBSql.executeUpdate(conn, sql);
					return 4;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
			return 0;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	@Override
	public String getNextTaskUser() {
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();

		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		boolean QS = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "签收");

		Connection conn = null;
		try {
			conn = DBSql.open();

			// 根据路由关系查找对应的品牌办理人.
			String PP = ha.get("PP");
			String sql = "select ZH from BO_AKL_RMASH_LYGX where PP = '" + PP + "'";
			String ZH = DBSql.getString(sql, "ZH");

			// 查找起草者.
			sql = "select createuser from BO_AKL_WXB_XS_RMASH_P where bindid = " + bindid;
			String USER = DBSql.getString(sql, "createuser");

			// 如果签收了就发到品牌办理人处.
			return QS ? ZH : USER;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
