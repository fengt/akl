package cn.com.akl.ccgl.wply.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	private static final String quantityQuery = "SELECT WLBH, PCH, XH, SUM(SL) as CKSL,LYCK as CKDM FROM BO_AKL_WPLY_P a,BO_AKL_WPLY_S b " +
			"WHERE a.BINDID=b.BINDID AND a.BINDID=? GROUP BY WLBH, PCH, XH,LYCK";
	
	public StepNo1Transaction() {
		super();
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��һ�ڵ���ת�¼�����֤��ɺ��������");
	}

	@Override
	public boolean execute() {

		Connection conn = null;
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable head = BOInstanceAPI.getInstance().getBOData("BO_AKL_WPLY_P", bindid);
		String ddh = head.get("DJLSH").toString();

		try {
			conn = DAOUtil.openConnectionTransaction();
			// 1�����ӱ��������ν��л���
			DAOUtil.executeQueryForParser(conn,quantityQuery,
					new ResultPaserAbs[] {
							// 2����֤����Ƿ����
							getValidateRepositoryPaser(),
							// 3����������
							getInsertLockPaser(bindid, uid, ddh) }, bindid);
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
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ��֤����Ƿ����.
	 */
	public ResultPaserAbs getValidateRepositoryPaser() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				Integer sdsl = DAOUtil.getIntOrNull(conn, "SELECT SUM(SDSL) SDSL FROM BO_AKL_KC_SPPCSK WHERE PCH=? AND CKDM=? AND WLBH=?",
						reset.getString("PCH"), reset.getString("CKDM"), reset.getString("WLBH"));
				Integer kwsl = DAOUtil.getIntOrNull(conn, "SELECT SUM(KWSL) KWSL FROM BO_AKL_KC_KCMX_S WHERE PCH=? AND CKDM=? AND WLBH=?",
						reset.getString("PCH"), reset.getString("CKDM"), reset.getString("WLBH"));
				int cksl = reset.getInt("CKSL");
				if (kwsl - sdsl < cksl) {
					throw new RuntimeException("���Ϻ�:" + reset.getString("WLBH") + "�� �ͺ�:" + reset.getString("XH") + "�� ���κ�:" + reset.getString("PCH")
							+ "���������㣬���ܿ���ѱ�������");
				}
				return true;
			}
		};
	}

	/**
	 * ���������¼..
	 */
	public ResultPaserAbs getInsertLockPaser(final int bindid, final String uid, final String ddh) {
		return new ResultPaserAbs() {
			private ProcessMaterialBiz biz = new ProcessMaterialBiz();

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				try {
					biz.insertSK(conn, bindid, uid, ddh, reset.getString("PCH"), reset.getString("WLBH"), reset.getString("CKDM"), reset.getInt("CKSL"));
				} catch (AWSSDKException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
		};
	}
}
