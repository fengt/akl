package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo2ExcelModelUpAfter extends ExcelDownFilterRTClassA{

	public StepNo2ExcelModelUpAfter(UserContext us){
		super(us);
		setVersion("1.0.0");
		setDescription("Excel模板导入后自动去除无效行");
	}
	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		// TODO Auto-generated method stub
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;

		conn = DBSql.open();
		try {
			conn.setAutoCommit(false);
			stat = conn.createStatement();
			String sql = "select * from BO_AKL_CCB_CKD_XLH_S where bindid = "+bindid;
			rs = stat.executeQuery(sql);
			while(rs.next()){
				String xh = rs.getString("XH")==null?"":rs.getString("XH");
				if(xh.trim().equals("")){
					BOInstanceAPI.getInstance().removeBOData("BO_AKL_CCB_CKD_XLH_S", rs.getInt("ID"));
				}
			}
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "删除无效行失败", true);
			return null;
		}finally{
			DBSql.close(conn, stat, rs);
		}

		return arg0;
	}

}
