package cn.com.akl.shgl.jg.rtclass;

import java.sql.Connection;
import java.util.ArrayList;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private static final String QUERY_isRepeatGZBH = "SELECT GZBH FROM BO_AKL_SH_JG_GZ_S WHERE BINDID=? GROUP BY GZBH HAVING COUNT(1)>1";
	
	private static final String QUERY_isExsit = "SELECT COUNT(1)N FROM BO_AKL_SH_DJG_S WHERE BINDID=?";
	
	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("加工信息校验。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		
		Connection conn = null;
		try {
			conn = DBSql.open();
			
			ArrayList<String> gzbhList = DAOUtil.getStringCollection(conn, QUERY_isRepeatGZBH, bindid);
			int isExsit = DAOUtil.getInt(conn, QUERY_isExsit, bindid);
			if(gzbhList.size() > 0){
				throw new RuntimeException("加工规则"+gzbhList.toString()+"录入重复，请检查！");
			}
			if(isExsit == 0){
				throw new RuntimeException("待加工信息不能为空！");
			}
			
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

}
