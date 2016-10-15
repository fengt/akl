package cn.com.akl.dgkgl.xsdd.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA{
	
	public StepNo1Transaction(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("º”»ÎÀ¯ø‚±Ì");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;
		String sql = null;
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			stat = conn.createStatement();
			HSSFWorkbook hss = new HSSFWorkbook();
			sql = "select DDID, WLBH, ISNULL(XSSL, 0) XSSL from BO_AKL_DGXS_S where bindid ="+bindid;
			rs = stat.executeQuery(sql);
			while(rs.next()){
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("XSDH", rs.getString(1)==null?"":rs.getString(1));
				hashtable.put("WLBH", rs.getString(2)==null?"":rs.getString(2));
				hashtable.put("XSSL", String.valueOf(rs.getInt(3)));
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCKSK", hashtable, bindid, getUserContext().getUID());
			}
			conn.commit();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (AWSSDKException e) {
			// TODO Auto-generated catch block
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		}
		finally{
			DBSql.close(conn, stat, rs);
		}
	}

}
