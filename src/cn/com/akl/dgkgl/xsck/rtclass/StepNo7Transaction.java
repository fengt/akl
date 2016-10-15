package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo7Transaction extends WorkFlowStepRTClassA {

	public StepNo7Transaction() {
		super();
	}

	public StepNo7Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("第七节点流程流转事件：改变签收差异状态值");
	}

	@Override
	public boolean execute() {
		//上传附件，必填，附件字段名称：签收单。
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		if(!th){
			if("BO_BO_AKL_DGCK_P".equals(tablename)||"BO_AKL_QSD_P".equals(tablename)){
				Connection conn = null;
				try{
					conn = DAOUtil.openConnectionTransaction();
					int count = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_QSD_S WHERE BINDID=? AND SSSL-YSSL<>0", bindid);
					//计算实际出库数量-签收出量，如果不等于0，修改出库单的订单状态为签收差异
					if(count!=0){
						// 更新出库单状态 -> 差异状态  如果订单状态不是在销售订单中需要修改这里
						// 更新当前出库单状态为差异状态
						DAOUtil.executeUpdate(conn, "UPDATE BO_BO_AKL_DGCK_P SET CYZT=? WHERE BINDID=?", "1", bindid);
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "签收数量与出库数量不符，在签收差异中体现！", true);
					} else {
						// 更新当前出库单状态为正常状态
						DAOUtil.executeUpdate(conn, "UPDATE BO_BO_AKL_DGCK_P SET CYZT=? WHERE BINDID=?", "0", bindid);
					}
					final String xsdh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
					DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DGXS_P SET ZT=? WHERE XSDDID=?", "签收", xsdh);
					conn.commit();
					return true;
				} catch(Exception e){
					DAOUtil.connectRollBack(conn);
					e.printStackTrace();
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
					return false;
				} finally {
					DBSql.close(conn, null, null);
				}
			}
		}
		return true;
	}

}
