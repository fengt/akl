package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;
import cn.com.akl.xsgl.xsdd.biz.SalesOrderBiz;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {
	/**
	 * 查询客户ID.
	 */
	private static final String QUERY_KHID = "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_DS = "SELECT FLFAH, FLFS, JJZE, ID, FLZCJ, WLBH, POSID, POSFALX, POSJE, POSZCSL, FLSL, PCH, CKID, DDSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	/**
	 * 更新销售订单订单状态.
	 */
	private static final String UPDATE_XSDD_DDZT = "update BO_AKL_WXB_XSDD_HEAD set ZT=?, DDZT=? where bindid=?";
	/**
	 * 查询订单号.
	 */
	private static final String QUERY_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 存放下一个节点.
	 */
	private int nextStepNo;

	private SalesOrderBiz xsddbiz = new SalesOrderBiz();

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("流程流转事件：减POS、减返利");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// 获取下个节点
		try {
			nextStepNo = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, bindid, taskId);
		} catch (AWSSDKException e) {
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			e.printStackTrace();
			return false;
		}

		// 同意标记
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "提交订单");

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			if (tyFlag) {
				auditMenuIsYes(conn, bindid, uid);
			} else {
				auditMenuIsNo(conn, bindid);
			}
			conn.commit();
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}

		if (tyFlag && nextStepNo == -1) {
			Connection conn2 = null;
			try {
				// 启动子流程
				conn2 = DBSql.open();
				xsddbiz.startCKDProcess(conn2, bindid, uid);
			} catch (RuntimeException e) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				e.printStackTrace();
			} catch (Exception e) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "子流程启动失败，请手动启动流程!", true);
				e.printStackTrace();
			} finally {
				DBSql.close(conn2, null, null);
			}
		}

		return true;
	}

	/**
	 * 审核菜单选择不同意.
	 * 
	 * @param conn
	 * @param bindId
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void auditMenuIsNo(Connection conn, int bindId) throws SQLException, AWSSDKException {
		// 审核菜单选择不同意时，根据销售订单号来删除批次锁库的记录。
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_KC_SPPCSK", bindId);
		// 删除分解出来的物料
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_WXB_XSDD_BODY", bindId);
	}

	/**
	 * 审核菜单通过.
	 * 
	 * @param conn
	 * @param bindid
	 * @param taskId
	 * @throws Exception
	 */
	public void auditMenuIsYes(Connection conn, int bindid, String uid) throws Exception {
		// 删了之前锁库的，再插入
		ProcessMaterialBiz skbiz = new ProcessMaterialBiz();
		skbiz.deleteSK(conn, bindid);

		// 流程结束后，订单生效
		if (nextStepNo == -1) {
			// 查询客户ID
			String khid = DAOUtil.getStringOrNull(conn, QUERY_KHID, bindid);
			// 更新POS和返利的已使用数量或金额
			DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, new DAOUtil.ResultPaser[] {
					// 更新POS和返利
					xsddbiz.getUpdateFLAndPOSPaser(),
					// 更新后返利
					xsddbiz.getUpdateHFLResultPaser(bindid, uid, khid),
					// 插入锁库
					skbiz.getInsertLockRepositoryPaser(bindid, uid, DAOUtil.getStringOrNull(conn, QUERY_DDID, bindid)) }, bindid);
			// 更改订单状态
			DAOUtil.executeUpdate(conn, UPDATE_XSDD_DDZT, XSDDConstant.ZT_JS, 3, bindid);
		} else {
			// 更新返利和POS
			DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, new DAOUtil.ResultPaser[] { 
					// 更新返利和POS.
					xsddbiz.getUpdateFLAndPOSPaser(), 
					// 插入锁库
					skbiz.getInsertLockRepositoryPaser(bindid, uid, DAOUtil.getStringOrNull(conn, QUERY_DDID, bindid)) 
			}, bindid);
			// 更新订单状态
			DAOUtil.executeUpdate(conn, UPDATE_XSDD_DDZT, XSDDConstant.ZT_JS, 2, bindid);
		}
	}

}
