package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsck.biz.DGOutFillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	/**
	 * 查询销售订单物料的销售数量.
	 */
	private static final String QUERY_XSDD_WL_XSSL = "SELECT (ISNULL(XSSL,0)-ISNULL(YCKSL, 0)) xssl FROM BO_AKL_DGXS_S WHERE WLBH=? AND DDID=? AND ISNULL(KHCGDH, '')=?";
	/**
	 * 查看单身数据是否有需要序列号的数据
	 */
	private static final String QUERY_SFXLH = "select count(*) from BO_BO_AKL_DGCK_S s left join BO_AKL_WLXX x on s.WLBH = x.WLBH where s.bindid=? AND x.SFXLH = 1";
	/**
	 * 查询出库单单身.
	 */
	private static final String QUERY_CKD_BODY = "SELECT WLBH, sum(ISNULL(SFSL, 0)) SFSL, XH, KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID=? group by WLBH, XH, KHCGDH";
	/**
	 * 更新是否序列号.
	 */
	private static final String UPDATE_SFXLH = "update BO_BO_AKL_DGCK_P set SFXLH = ? where bindid =?";
	/**
	 * 更新出库单状态.
	 */
	private static final String UPDATE_CKD_ZT = "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?";
	/**
	 * 查询销售订单号.
	 */
	private static final String QUERY_XSDDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";

	private DGOutFillBiz fillBiz = new DGOutFillBiz();

	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("第一节点办理事件，处理销售订单的已出库数量");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet reset = null;
		int a = 0;
		try {
			conn = DAOUtil.openConnectionTransaction();

			/** 查询销售订单号 */
			String xsdh = DAOUtil.getStringOrNull(conn, QUERY_XSDDH, bindid);

			/** 更新是否序列号的状态 */
			int count = DAOUtil.getIntOrNull(conn, QUERY_SFXLH, bindid);
			DAOUtil.executeUpdate(conn, UPDATE_SFXLH, count > 0 ? "1" : "0", bindid);

			/** 遍历出库单身物料 */
			stat = conn.prepareStatement(QUERY_CKD_BODY);
			reset = DAOUtil.executeFillArgsAndQuery(conn, stat, bindid);
			while (reset.next()) {
				String xssl = DAOUtil.getStringOrNull(conn, QUERY_XSDD_WL_XSSL, reset.getString("WLBH"), xsdh, reset.getString("KHCGDH"));
				if (Integer.parseInt(xssl) < reset.getInt("SFSL")) {
					throw new RuntimeException("代管库第一节点销售出库，检测到当前订单号为：" + xsdh + ", 客户采购单号:" + PrintUtil.parseNull(reset.getString("KHCGDH"))
							+ ", 物料号为:" + reset.getString("WLBH") + ", 实发数量为:" + reset.getInt("SFSL") + ", 实发数量超过了销售数量" + xssl);
				}
				if (Integer.parseInt(xssl) > reset.getInt("SFSL")) {
					a++;
				}
			}
			if (a > 0) {
				DAOUtil.executeUpdate(conn, UPDATE_CKD_ZT, "部分出库", bindid);
			} else {
				DAOUtil.executeUpdate(conn, UPDATE_CKD_ZT, "未出库", bindid);
			}

			/** 删除锁库数据 */
			fillBiz.removeLockMaterial(conn, xsdh);
			
			/** 将单身锁库重新插入 */
			fillBiz.insertLockFromBody(conn, bindid, uid, xsdh);

			/** 将 订单中未出库的数量 进行重新锁库 */
			fillBiz.fetchCanUseMaterial(conn, bindid, uid, xsdh);

			/** 删除代管出库冗余流程 */
			/** 查询所有此销售订单的流程ID */
			//ArrayList<Integer> allBindidList = DAOUtil.getInts(conn, "select bindid from BO_BO_AKL_DGCK_P where XSDH=? AND ISEND=0", xsdh);
			//ArrayList<Integer> removeBindidList = new ArrayList<Integer>(5);
			//for (Integer bind : allBindidList) {
				/** 查询流程当前节点 */
				//int stepNo = DAOUtil.getIntOrNull(conn,
				//		"SELECT MAX(STEPNO) STEPNO FROM SYSFLOWSTEP WHERE ID in (SELECT DISTINCT WFSID FROM WF_TASK WHERE BIND_ID = ?)", bind);
				/** 如果非此流程，并且流程在第一节点或者第一节点以内则需要删除此流程，removeBindidList为待删除流程ID存放集合 */
				//if (bind != bindid && stepNo <= 1) {
				//	removeBindidList.add(bind);
				//}
			//}

			conn.commit();
			
			/** 删除流程 */
			/*
			for (Integer b : removeBindidList) {
				WorkflowInstanceAPI.getInstance().removeProcessInstance(b);
			}
			*/
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} finally {
			DBSql.close(conn, stat, reset);
		}
	}

}
