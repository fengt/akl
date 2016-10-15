package cn.com.akl.dgkgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsck.biz.DGOutFillBiz;
import cn.com.akl.dgkgl.xsdd.biz.DGSalesFillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class StepNo1TransactionCF extends WorkFlowStepRTClassA {

	private DGSalesFillBiz fillBiz = new DGSalesFillBiz();
	private DGOutFillBiz outFillBiz = new DGOutFillBiz();

	public StepNo1TransactionCF() {
		super();
	}

	public StepNo1TransactionCF(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("拆分库存,插入锁库表");

		/**
		 * 存在问题：如果此流程拆分过后，未点办理，另一个出库流程将此库存拿走，就存在了冲突。
		 */
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String, String> hashtable = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGXS_P", bindid);
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			String xsddh = hashtable.get("XSDDID");
			String cklx = hashtable.get("CKLX");

			/** 删除之前的锁库，并重新抓取物料进行锁库 */
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGCKSK", bindid);
			fillBiz.fetchCanUseMaterial(conn, bindid, uid, xsddh, cklx);

			/** 查询销售单号，并根据销售单号中分拣物料，插入子流程. */
			Integer subBindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_BO_AKL_DGCK_P WHERE XSDH=? ORDER BY CREATEDATE DESC", xsddh);
			if (subBindid == null || subBindid == 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "子流程未启动成功!");
			} else {
				outFillBiz.fetchLockMaterial(conn, subBindid, getUserContext().getUID(), xsddh);
			}

			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			removeSubProcessInstance(conn, hashtable.get("XSDDID"));
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "拆分出现问题，请联系管理员！", true);
			removeSubProcessInstance(conn, hashtable.get("XSDDID"));
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 流程归档错误时，删除已启动的子流程.
	 * @param conn
	 * @param xsddh
	 */
	public void removeSubProcessInstance(Connection conn, String xsddh) {
		try {
			Integer subBindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_BO_AKL_DGCK_P WHERE XSDH=? ORDER BY CREATEDATE DESC", xsddh);
			WorkflowInstanceAPI.getInstance().removeProcessInstance(subBindid);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
