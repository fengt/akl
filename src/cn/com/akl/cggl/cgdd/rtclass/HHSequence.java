package cn.com.akl.cggl.cgdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.SQLUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class HHSequence extends WorkFlowStepRTClassA {

	public HHSequence() {
	}

	public HHSequence(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("按每个流程重新生成行号。");
	}

	@Override
	public boolean execute() {
		Connection conn = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		try {
			SQLUtil.updateRow(conn, CgddConstant.tableName1, "HH", bindid);
			String update_zt = "UPDATE " +CgddConstant.tableName0+ " SET DZT='"+CgddConstant.dzt0+"' WHERE BINDID="+bindid;
			DBSql.executeUpdate(update_zt);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, null, null);
		}
		return true;
	}

}
