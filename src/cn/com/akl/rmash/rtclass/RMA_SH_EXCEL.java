package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;

public class RMA_SH_EXCEL extends ExcelDownFilterRTClassA {

	public RMA_SH_EXCEL(UserContext arg0) {
		super(arg0);
		setVersion("RMA收货流程v1.0");
		setProvider("刘松");
		setDescription("EXCEL导入前验证");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		Statement state = null;
		String sql = null;
		try {
			conn = DBSql.open();
			state = conn.createStatement();
			sql = "select count(ID) id from BO_AKL_WXB_XS_RMASH_P where bindid =" + bindid;
			int id = DBSql.getInt(conn, sql, "id");
			if (id == 0) {
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "导入前请点击暂存", true);
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(conn, state, null);
		}

		return arg0;
	}

}
