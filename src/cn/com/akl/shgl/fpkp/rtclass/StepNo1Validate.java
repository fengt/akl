package cn.com.akl.shgl.fpkp.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setDescription("校验子表中的发票是否被其他开票流程办理了，检验开票状态是否可用.");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		try {
			conn = DBSql.open();
			validate(conn, bindid);
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 校验发票记录是否可用.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validate(Connection conn, int bindid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(FpkpConstant.QUERY_FPKP_FORM_BODY);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String fpsqdh = reset.getString("FPSQDH");

				// 1、 校验发票的状态.
				String fpzt = DAOUtil.getStringOrNull(conn, FpkpConstant.QUERY_FPSQ_FPZT, fpsqdh);
				if (!FpkpConstant.FPZT_WKP.equals(fpzt)) {
					throw new RuntimeException("当前发票不能被办理! 此发票状态为" + fpzt);
				}

				// 2、校验发票是否被其他流程办理了.
				Integer otherBindid = DAOUtil.getIntOrNull(conn, FpkpConstant.QUERY_FPKP_FORM_BODY_FPSQDH, fpsqdh, bindid);
				if (otherBindid != null && otherBindid > 0) {
					Integer otherStepNo = WorkFlowUtil.getProcessInstanceStepNo(conn, otherBindid);
					if (otherStepNo > 1) {
						// 2.1 查询另一个单子是否办理过了第一节点.
						throw new RuntimeException("当前发票不能被办理! 此发票状态为" + fpzt);
					}
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}
}
