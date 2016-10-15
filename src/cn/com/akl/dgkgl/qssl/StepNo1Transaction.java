package cn.com.akl.dgkgl.qssl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Transaction extends WorkFlowStepRTClassA{
	//查询签收数量录入单身
	private static final String QUERY_CKDH = "SELECT CKDH, SSSL, WLH FROM BO_AKL_DGCK_QSSL_S WHERE BINDID = ?";
	public StepNo1Transaction(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("流转事件：更新出库单状态");
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		PreparedStatement qsbodyPs = null;
		ResultSet qsbobyReset = null;
		
		try {
			conn = DAOUtil.openConnectionTransaction();
			qsbodyPs = conn.prepareStatement(QUERY_CKDH);
			qsbobyReset = DAOUtil.executeFillArgsAndQuery(conn, qsbodyPs, bindid);
			while(qsbobyReset.next()){
				//更新出库单状态
				DAOUtil.executeUpdate(conn, "UPDATE BO_BO_AKL_DGCK_P SET ZT=? WHERE CKDH=?", "签收确认", qsbobyReset.getString(1));
				//更新签收单签收数量
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_QSD_S SET SSSL=? from BO_AKL_QSD_S s, BO_AKL_QSD_P p WHERE p.CKDH=? AND s.bindid = p.bindid AND s.WLH=?", qsbobyReset.getInt(2), qsbobyReset.getString(1),qsbobyReset.getString(3));
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
			DBSql.close(conn, qsbodyPs, qsbobyReset);
		}
		return true;
	}

}
