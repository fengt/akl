package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.SQLUtil;
import cn.com.akl.xsgl.xsdd.biz.InquireOrderBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1AfterSave extends WorkFlowStepRTClassA {
	/**
	 * 查询客户ID.
	 */
	private static final String QUERY_WLXX_KHID = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询销售订单的询单价格信息.
	 */
	private static final String QUERY_XSDD_XDWL = "SELECT FLFAH,FLFS,FLSL,FLZCD,XSDJ,DFSL FROm BO_AKL_WXB_XSDD_XDWL WHERE BINDID=?";
	/**
	 * 查询物料的销售指导价.
	 */
	private static final String QUERY_XSDD_WLXX_XSZDJ = "SELECT WLBH, CASE WHEN khjg IS NULL THEN mrjg ELSE khjg END AS JG FROM ( SELECT WLBH, ( SELECT TOP 1 XSGHJ FROM BO_AKL_JGGL WHERE WLBH = xsddbody.WLBH ORDER BY UPDATEDATE DESC ) AS mrjg, ( SELECT TOP 1 XSGHJ FROM BO_AKL_KH_JGGL_P a JOIN BO_AKL_KH_JGGL_S b ON a.BINDID = b.BINDID WHERE KHBH = ? AND b.WLBH = xsddbody.WLBH ORDER BY b.UPDATEDATE DESC ) AS khjg FROM BO_AKL_WXB_XSDD_BODY xsddbody WHERE BINDID =? GROUP BY WLBH ) m";

	private InquireOrderBiz ioBiz = new InquireOrderBiz();
	
	public StepNo1AfterSave() {
		super();
	}

	public StepNo1AfterSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("生成行号");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
		Connection conn = null;
		try {
			if ("BO_AKL_WXB_XSDD_HEAD".equals(tableName)) {
				conn = DAOUtil.openConnectionTransaction();
				// 生成行号
				SQLUtil.updateRow(conn, "BO_AKL_WXB_XSDD_XDWL", "DH", bindid);

				// 重新填充销售指导价
				// 计算询单系统价税合计.
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_XDWL, ioBiz.getComputeInquireOrderAmount(bindid), bindid);

				// 填充销售单价.
				String khid = DAOUtil.getString(conn, QUERY_WLXX_KHID, bindid);
				DAOUtil.executeQueryForParser(conn, QUERY_XSDD_WLXX_XSZDJ, ioBiz.getUpdateMaterialPricePaser(bindid), khid, bindid);

				conn.commit();
			}
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
