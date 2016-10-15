package cn.com.akl.shgl.zsjgl.cpxx.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.shgl.zsjgl.biz.MaterialBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1AfterSave extends WorkFlowStepRTClassA {

	public StepNo1AfterSave() {
		super();
	}

	public StepNo1AfterSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第一节点保存后事件：生成物料编号.");
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		if ("BO_AKL_CPXX".equals(tablename)) {
			generatMaterialNumber(bindid);
		}
		return true;
	}

	/**
	 * 生成物料编号.
	 * 
	 * @param bindid
	 */
	public void generatMaterialNumber(int bindid) {
		MaterialBiz mBiz = new MaterialBiz();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			conn = DBSql.open();
			ps = conn.prepareStatement("SELECT LBID, ID FROM BO_AKL_CPXX WHERE BINDID=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = mBiz.addWlbh(reset.getString("LBID"));
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CPXX SET WLBH=? WHERE ID=?", wlbh, reset.getInt("id"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}

}
