package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsck.biz.DGOutFillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSaveCF extends WorkFlowStepRTClassA {
	/**
	 * 查询销售单号.
	 */
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";

	private DGOutFillBiz fillBiz = new DGOutFillBiz();

	public StepNo1BeforeSaveCF() {
		super();
	}

	public StepNo1BeforeSaveCF(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("表单保存前事件：插入单身数据");
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		if ("BO_BO_AKL_DGCK_P".equals(tablename)) {
			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();
				String xsddh = hashtable.get("XSDH");

				/** 当录入的销售订单为空值时，清理子表拆分的物料记录 */
				if (xsddh == null || xsddh.trim().length() == 0) {
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid);
				} else {
					/** 获取上一次的销售订单号. */
					String xsddh2 = DAOUtil.getStringOrNull(conn, queryXSDH, bindid);

					/** 若两次销售订单号相同则不需要重新在插入. */
					if (!xsddh.equals(xsddh2)) {
						/** 删除单身数据 */
						BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid);
						/** 抓取锁库记录 */
						fillBiz.fetchLockMaterial(conn, bindid, uid, xsddh);
					}
				}

				conn.commit();
				return true;
			} catch (RuntimeException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "拆分出现问题，请联系管理员！", true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}
}
