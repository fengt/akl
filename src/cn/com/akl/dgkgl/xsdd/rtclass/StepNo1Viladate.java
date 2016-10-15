package cn.com.akl.dgkgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Viladate extends WorkFlowStepRTClassA {
	/**
	 * 查询货主的部门编号和客户编号.
	 */
	private static final String queryBMBH_KHBH_CZ = "select count(*) from BO_AKL_KH_P where SSKHBH=? AND KHID=?";
	/**
	 * 查询单身中同一客户采购单号不能用相同物料.
	 */
	private static final String queryXTWL = "SELECT COUNT(*) FROM (select WLBH, KHCGDH from BO_AKL_DGXS_S where bindid=? group by WLBH, KHCGDH HAVING count(*)>1) A";
	/**
	 * 查询每个物料的销售数量.
	 */
	private static final String QUERY_MATERIAL_SALESNUM = "SELECT WLBH, XH, SUM(ISNULL(XSSL, 0)) XSSL FROM BO_AKL_DGXS_S WHERE BINDID=? GROUP BY WLBH, XH";
	/**
	 * 查询物料的库存.
	 */
	private static final String QUERY_MATERIAL_STOCK = "SELECT SUM(ISNULL(PCSL, 0)) FROM BO_AKL_DGKC_KCHZ_P WHERE WLBH=?";

	public StepNo1Viladate(UserContext uc) {
		super(uc);
		setProvider("V1.0.0");
		setDescription("校验单身不能为空及验证单身中同一客户采购单号不能有相同物料");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> h = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGXS_P", bindid);

		Connection conn = null;
		try {
			conn = DBSql.open();
			String HZBH = PrintUtil.parseNull(h.get("HZBH"));
			String BMBH = PrintUtil.parseNull(h.get("BMBH"));

			/** 检查货主的部门 */
			String countBM = DAOUtil.getString(conn, queryBMBH_KHBH_CZ, HZBH, BMBH);
			if ((countBM == null || countBM.equals("0")) && !BMBH.equals("")) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "该货主没有此部门！", true);
				return false;
			}

			/** 检查单身中是否有相同物料的数据 */
			int count = DAOUtil.getIntOrNull(conn, queryXTWL, bindid);
			if (count > 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "单身中有同一采购单号存在相同物料的数据，请检查！", true);
				return false;
			}

			/** 单身每条物料是否库存足够. */
			DAOUtil.executeQueryForParser(conn, QUERY_MATERIAL_SALESNUM, new DAOUtil.ResultPaser() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					Integer pcsl = DAOUtil.getIntOrNull(conn, QUERY_MATERIAL_STOCK, reset.getString("WLBH"));
					if (pcsl < reset.getInt("XSSL")) {
						throw new RuntimeException("型号为：" + reset.getString("XH") + " 的物料数量不足! 销售数量为：" + reset.getInt("XSSL") + " - 库存数量为：" + pcsl);
					}
					return true;
				}
			}, bindid);

			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "系统出现问题，请联系管理员！", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
