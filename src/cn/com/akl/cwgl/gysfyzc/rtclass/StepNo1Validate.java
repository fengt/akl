package cn.com.akl.cwgl.gysfyzc.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	/**
	 * 查询重复的物料信息.
	 */
	private static final String QUERY_WLBH_REPEAR_COUNT = "SELECT WLBH FROM BO_AKL_WXB_XS_POS_BODY WHERE BINDID=? GROUP BY WLBH HAVING COUNT(*)>1";

	/**
	 * 查询物料编号和型号不匹配的型号.
	 */
	private static final String QUERY_WLXX_NOMATCH = "SELECT a.XH FROM BO_AKL_WXB_XS_POS_BODY a LEFT JOIN BO_AKL_WLXX b ON a.WLBH=b.WLBH AND a.XH=b.XH WHERE a.BINDID=? AND (b.WLBH is null OR b.XH is null)";

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("供应商费用支持申请第一节点校验事件.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			String repeatXh = DAOUtil.getStringOrNull(conn, QUERY_WLBH_REPEAR_COUNT, bindid);

			if (repeatXh == null) {
				String xh = DAOUtil.getStringOrNull(conn, QUERY_WLXX_NOMATCH, bindid);
				if (xh == null) {
					return true;
				} else {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "型号：" + xh + " 和物料编号匹配错误，请检查此型号的物料编号!");
					return false;
				}
			} else {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "物料编号：" + repeatXh + " 出现重复，请检查此型号!");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
