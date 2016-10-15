package cn.com.akl.rmahpfh.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.SQLUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class HHSeq extends WorkFlowStepRTClassA {

	public HHSeq() {
	}

	public HHSeq(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("每个流程重新生成行号");
	}

	@Override
	public boolean execute() {
		Connection conn = null;
		conn = DBSql.open();
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		try {
			SQLUtil.updateRow(conn, "BO_AKL_WXB_RMAFK_BODY", "HH", bindid);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, null, null);
		}
		return true;
	}

}
