package cn.com.akl.ccgl.thck.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

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

		try {
			conn = DAOUtil.openConnectionTransaction();
			// 1�����ӱ��������ν��л���
			DAOUtil.executeQueryForParser(conn,
					"SELECT WLH, PC, XH, FHKFBH CKDM, SUM(SJSL) as CKSL FROM BO_AKL_CKD_BODY WHERE BINDID=? GROUP BY WLH, PC, FHKFBH, XH",
					new ResultPaserAbs[] {
							// 2����֤����Ƿ����
							getValidateRepositoryPaser(),
							// 3����������
							getInsertLockPaser(bindid, uid) }, bindid);
			
			// 2�������ӱ����ϵĳɱ�
			/*DAOUtil.executeQueryForParser(conn, "SELECT WLH, PC, XH, ID FROM BO_AKL_CKD_BODY WHERE BINDID=?", new ResultPaserAbs() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					BigDecimal cbdj = DAOUtil.getBigDecimalOrNull(conn, "SELECT DJ FROM BO_AKL_KC_KCHZ_P WHERE PCH=? AND WLBH=?", reset.getString("PC"), reset.getString("WLH"));
					if(cbdj == null){
						cbdj = new BigDecimal(0);
					}
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_BODY SET CBDJ=? WHERE ID=?", cbdj, reset.getInt("ID"));
					return true;
				}
			}, bindid);*/
			
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
						reset.getString("PC"), reset.getString("CKDM"), reset.getString("WLH"));
				Integer kwsl = DAOUtil.getIntOrNull(conn, "SELECT SUM(KWSL) KWSL FROM BO_AKL_KC_KCMX_S WHERE PCH=? AND CKDM=? AND WLBH=?",
						reset.getString("PC"), reset.getString("CKDM"), reset.getString("WLH"));
				int cksl = reset.getInt("CKSL");
				if (kwsl - sdsl < cksl) {
					throw new RuntimeException("���Ϻ�:" + reset.getString("WLH") + "�� �ͺ�:" + reset.getString("XH") + "�� ���κ�:" + reset.getString("PC")
							+ "���������㣬���ܿ���ѱ�������");
				}
				return true;
			}
		};
	}

	/**
	 * ���������¼..
	 */
	public ResultPaserAbs getInsertLockPaser(final int bindid, final String uid) {
		return new ResultPaserAbs() {
			private ProcessMaterialBiz biz = new ProcessMaterialBiz();

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				try {
					biz.insertSK(conn, bindid, uid, "", reset.getString("PC"), reset.getString("WLH"), reset.getString("CKDM"), reset.getInt("CKSL"));
				} catch (AWSSDKException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
		};
	}
}
