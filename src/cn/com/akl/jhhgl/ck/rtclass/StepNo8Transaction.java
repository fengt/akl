package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo8Transaction extends WorkFlowStepRTClassA{

	public StepNo8Transaction() {
		super();
	}

	public StepNo8Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("流程结束，计算实际出库数量-签收出量，如果不等于0，修改出库单的订单状态为签收差异上传附件，必填，附件字段名称：签收单。");
	}

	@Override
	public boolean execute() {
		//上传附件，必填，附件字段名称：签收单。
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			int count = DAOUtil.getInt(conn, "SELECT SSSL-YSSL FROM BO_AKL_QSD_S WHERE BINDID=?", bindid);
			//计算实际出库数量-签收出量，如果不等于0，修改出库单的订单状态为签收差异
			if(count!=0){
				// 查询订单号
				String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
				
				// 更新出库单状态 -> 差异状态  如果订单状态不是在销售订单中需要修改这里
				// 更新当前出库单状态为差异状态
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 1, bindid);
			} else {
				// 更新当前出库单状态为正常状态
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CYZT=? WHERE BINDID=?", 0, bindid);
			}
			
			//上传附件，必填，附件字段名称：签收单。
			int qsdIsNull = DAOUtil.getInt(conn, "SELECT count(*) FROM BO_AKL_QSD_P WHERE BINDID=? AND QSDFJ is null",bindid);
			if(qsdIsNull < 1){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请上传签收单附件", true);
				return false;
			}
			
			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
