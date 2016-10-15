package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo9Transaction extends WorkFlowStepRTClassA {

	public StepNo9Transaction() {
		super();
	}

	public StepNo9Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription(".审核通过，根据客户信息表的“是否预收”=“否”，向应收表插入数据2.审核通过，计算签收数量-实际签收出量，如果不等于0，则启动签收差异子流程");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		// 审核菜单判断
		boolean tgFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "审核通过");
		// 不通过返回
		if(!tgFlag) return true;
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			// 是否预收为否，向应收表中插入数据。
			String sfys = DAOUtil.getString(conn, "SELECT SFYS FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			if("否".equals(sfys) || "025001".equals(sfys)){
				// 查询客户编码、销售单号、出库单号、应收金额
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				ps = conn.prepareStatement("SELECT KH,KHMC,CKDH,RMAFXDH,YSSLHJ FROM BO_AKL_CKD_HEAD WHERE BINDID=?");
				reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
				
				if(reset.next()){
					hashtable.put("KHBM", reset.getString("KH"));
					hashtable.put("KHMC", reset.getString("KHMC"));
					hashtable.put("CKDH", reset.getString("CKDH"));
					hashtable.put("XSDH", reset.getString("RMAFXDH"));
					hashtable.put("ZT", "未收");//状态
					hashtable.put("LB", XSCKConstant.KH);
					hashtable.put("YSJE", reset.getBigDecimal("YSSLHJ").toString());
				}
				// 存入应收表
				BOInstanceAPI.getInstance().createBOData("BO_AKL_YS", hashtable, bindid, getUserContext().getUID());
			}
			
			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, ps, reset);
		}
		
	}

}
