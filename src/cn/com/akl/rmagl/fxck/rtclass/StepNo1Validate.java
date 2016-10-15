package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA{
	
	// 查询本单的销售订单号 
	private static final String QUERY_RMAFXDH = "SELECT RMAFXDH FROM BO_AKL_CKD_HEAD WHERE BINDID=? ";
	// 查询本单销售订单是否有重复的
	private static final String QUERY_RMAFXDH_COUNT = "SELECT COUNT(*) FROM BO_AKL_CKD_HEAD WHERE ISEND=0 AND RMAFXDH=?";

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setDescription("用于校验销售订单是否已下单");
		setVersion("1.0.0");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try{
			conn = DBSql.open();
			String rmafxdh = DAOUtil.getStringOrNull(conn, QUERY_RMAFXDH, bindid);
			Integer count = DAOUtil.getIntOrNull(conn, QUERY_RMAFXDH_COUNT, rmafxdh);
			if(count!=null&&count > 1){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "这张销售订单已经被办理了!");
				return false;
			} else {
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现问题，请联系管理员!");
			return true;
		}finally {
			DBSql.close(conn, null, null);
		}
	}

}
