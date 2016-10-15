package cn.com.akl.posbg.rtclass;

import java.sql.Connection;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.posbg.biz.POSModifyBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	/**
	 * 遍历POS变更的单身.
	 */
	private static final String QUERY_POSBG_DS = "SELECT POSBH, TPM, WLBH, WLMC, XH, GG, POSSL, CURRENCY, POSDJ, YJG, ZCHJJBHS, ZCHJJHS, SL, QRRQ, ZT, YSYSL FROM BO_AKL_POS_BG_S WHERE BINDID=?";
	/**
	 * 遍历POS变更的单头.
	 */
	private static final String QUERY_POSBG_DT = "SELECT KSSJ, JSSJ, POSMC, POSBH FROM BO_AKL_POS_BG_P WHERE BINDID=?";
	/**
	 * 查询POS编号.
	 */
	private static final String QUERY_POSBG_POSBH = "SELECT POSBH FROM BO_AKL_POS_BG_P WHERE BINDID=?";

	/**
	 * POS修改业务类.
	 */
	private POSModifyBiz posBiz = new POSModifyBiz();

	public StepNo1Transaction(UserContext uc) {
		super(uc);
		setVersion("1.0.0");
		setDescription("修改后反写pos申请表");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			// 查询POS编号.
			String posid = DAOUtil.getStringOrNull(conn, QUERY_POSBG_POSBH, bindid);
			// 更新POS修改单头.
			DAOUtil.executeQueryForParser(conn, QUERY_POSBG_DT, posBiz.getUpdatePOSHead(), bindid);
			// 更新POS修改单身. 对销售订单进行更新.
			DAOUtil.executeQueryForParser(conn, QUERY_POSBG_DS, new ResultPaserAbs[] {
					// 更新POS的单身.
					posBiz.getUpdatePOSBody(posid),
					// 更新销售订单中的数据.
					posBiz.getUpdateSalerOrder() }, bindid);
			conn.commit();
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
		return true;
	}
}
