package cn.com.akl.xsgl.xsdd.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;
import cn.com.akl.xsgl.xsdd.biz.ProcessPOSBiz;
import cn.com.akl.xsgl.xsdd.biz.ProcessRebateBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Validate extends WorkFlowStepRTClassA {
	/**
	 * 查询除指定客户之外其他客户订单的物料可用数量
	 */
	private static final String QUERY_KYWLXX = "SELECT COUNT (*) FROM BO_AKL_WXB_XSDD_BODY s LEFT JOIN BO_AKL_WXB_XSDD_HEAD p ON s.bindid = p.bindid WHERE s.KYSL < s.DDSL AND p.KHID NOT IN (SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE DLBM='055') AND p.bindid = ?";
	/**
	 * 查询销售订单号.
	 */
	private static final String QUERY_XSDD_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * 查询销售订单单身.
	 */
	private static final String QUERY_XSDD_DS = "SELECT WLBH, SUM(DDSL) as DDSL, PCH FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? GROUP BY PCH, WLBH";
	
	ProcessPOSBiz posbiz = new ProcessPOSBiz();
	ProcessRebateBiz flbiz = new ProcessRebateBiz();
	ProcessMaterialBiz pmBiz = new ProcessMaterialBiz();

	public StepNo2Validate() {
		super();
	}

	public StepNo2Validate(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("第二个节点流程节点办理前校验事件: 处理POS是否结束");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		// 同意标记
		boolean tyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "提交订单");
		if (!tyFlag) {
			return true;
		}

		// 校验POS的截止日期>=当前日期或POS方案为已结束的，提示：该POS方案XXX已过期。流程不向下办理。
		// 1.查出本流程拥有的所有POS方案
		// 2. 检查是否有过期的
		// 3. 检查是否有结束的
		Connection conn = null;
		try {
			conn = DBSql.open();

			posbiz.validatePOSFAEqualsFA(conn, bindid);
			posbiz.validatePOSFAEqualsZJC(conn, bindid);
			posbiz.validatePOSTimeOut(conn, bindid);

			flbiz.validateFLTimeOut(conn, bindid);
			flbiz.validateFLSL(conn, bindid);

			String ddid = DAOUtil.getStringOrNull(conn, QUERY_XSDD_DDID, bindid);

			// 校验物料批次数量是否充足.
			DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, pmBiz.getValidateRepository(ddid), bindid);

			int count = DAOUtil.getInt(conn, QUERY_KYWLXX, bindid);
			if (count > 0) {
				MessageQueue.getInstance().putMessage(uid, "此销售订单中有订单数量大于可用数量数据存在，请检查！", true);
				return false;
			}
			// 提交时校验填写的返利支持金额，如果大于返利方案表中的返利支持金额，则禁止提交，并提示返利方案表中的返利支持金额值

			return true;
		} catch (RuntimeException e) {
			MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			MessageQueue.getInstance().putMessage(uid, "后台出现异常，请检查控制台", true);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
