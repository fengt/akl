package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1_2AfterSave extends WorkFlowStepRTClassA{
	public StepNo1_2AfterSave() {
		super();
	}

	public StepNo1_2AfterSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第一二节点：保存后事件，更新锁库表");
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
//		int parentBindid = getParameter(PARAMETER_PARENT_WORKFLOW_INSTANCE_ID).toInt();// 父类bindid
		if("BO_BO_AKL_DGCK_P".equals(tablename)){
			Connection conn = null;
			Statement stat = null;
			ResultSet rs = null;
			try {
				conn = DAOUtil.openConnectionTransaction();
				stat = conn.createStatement();
				
				final String xsdh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
				final int parentBindid = DAOUtil.getInt(conn, "SELECT bindid FROM BO_AKL_DGXS_P WHERE XSDDID=?", xsdh);
				//删除锁库表中的数量
				String sql = "delete from BO_AKL_DGCKSK where XSDH = '"+xsdh+"'";
				stat.executeUpdate(sql);
				//遍历所有的单身物料信息
				sql = "SELECT WLBH,HWDM,PCH,HWKYSL,YFSL,KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID="+bindid;
				rs = stat.executeQuery(sql);
				while(rs.next()){
					Hashtable<String, String> hashtable = new Hashtable<String, String>();
					hashtable.put("HWDM", rs.getString(2)==null?"":rs.getString(2));
					hashtable.put("KHCGDH", rs.getString(6)==null?"":rs.getString(6));
					hashtable.put("PCH", rs.getString(3)==null?"":rs.getString(3));
					hashtable.put("HWKYSL", String.valueOf(rs.getInt(4)));
					hashtable.put("XSDH", xsdh);
					hashtable.put("WLBH", rs.getString(1)==null?"":rs.getString(1));
					hashtable.put("XSSL", String.valueOf(rs.getInt(5)));
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCKSK", hashtable, parentBindid, getUserContext().getUID());
				}
				conn.commit();
			} catch(RuntimeException e){
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch(Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，无法出库，请检查控制台", true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
			
		}
		return true;
	}

}
