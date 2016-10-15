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
	 * ����POS����ĵ���.
	 */
	private static final String QUERY_POSBG_DS = "SELECT POSBH, TPM, WLBH, WLMC, XH, GG, POSSL, CURRENCY, POSDJ, YJG, ZCHJJBHS, ZCHJJHS, SL, QRRQ, ZT, YSYSL FROM BO_AKL_POS_BG_S WHERE BINDID=?";
	/**
	 * ����POS����ĵ�ͷ.
	 */
	private static final String QUERY_POSBG_DT = "SELECT KSSJ, JSSJ, POSMC, POSBH FROM BO_AKL_POS_BG_P WHERE BINDID=?";
	/**
	 * ��ѯPOS���.
	 */
	private static final String QUERY_POSBG_POSBH = "SELECT POSBH FROM BO_AKL_POS_BG_P WHERE BINDID=?";

	/**
	 * POS�޸�ҵ����.
	 */
	private POSModifyBiz posBiz = new POSModifyBiz();

	public StepNo1Transaction(UserContext uc) {
		super(uc);
		setVersion("1.0.0");
		setDescription("�޸ĺ�дpos�����");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			// ��ѯPOS���.
			String posid = DAOUtil.getStringOrNull(conn, QUERY_POSBG_POSBH, bindid);
			// ����POS�޸ĵ�ͷ.
			DAOUtil.executeQueryForParser(conn, QUERY_POSBG_DT, posBiz.getUpdatePOSHead(), bindid);
			// ����POS�޸ĵ���. �����۶������и���.
			DAOUtil.executeQueryForParser(conn, QUERY_POSBG_DS, new ResultPaserAbs[] {
					// ����POS�ĵ���.
					posBiz.getUpdatePOSBody(posid),
					// �������۶����е�����.
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
