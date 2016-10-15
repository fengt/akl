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
		setDescription("第一节点流转事件，验证完成后进行锁库");
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
			// 1、对子表物料批次进行汇总
			DAOUtil.executeQueryForParser(conn,quantityQuery,
					new ResultPaserAbs[] {
							// 2、验证库存是否充足
							getValidateRepositoryPaser(),
							// 3、插入锁库
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
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 验证库存是否充足.
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
					throw new RuntimeException("物料号:" + reset.getString("WLBH") + "， 型号:" + reset.getString("XH") + "， 批次号:" + reset.getString("PCH")
							+ "，数量不足，可能库存已被锁定！");
				}
				return true;
			}
		};
	}

	/**
	 * 插入锁库记录..
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
