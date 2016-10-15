package cn.com.akl.rmahpfh.rtclass;

import java.sql.Connection;
import java.util.Hashtable;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Insert_YS extends WorkFlowStepRTClassA {

	public Insert_YS() {
	}

	public Insert_YS(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("插入应收表");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意")){
			//单头信息
			Hashtable headData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
			String djbh = headData.get("DJBH") == null ?"":headData.get("DJBH").toString();//坏品发货单号
			String zetj = headData.get("ZETJ") == null ?"":headData.get("ZETJ").toString();//总额统计
			String kh = headData.get("GYSMC") == null ?"":headData.get("GYSMC").toString();//供应商名称
			Double ze = Double.parseDouble(zetj);
			//反馈信息
//			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFK_BODY", bindid);
			Connection conn = DBSql.open();
			try {
				//插入应收表
				String sqlHead = "select GYSBH from BO_AKL_GYS_P where GYSMC='"+kh+"'";
				String khid = DBSql.getString(sqlHead, "GYSBH");
				khid = (khid == null ? "" : khid);//供应商编号
				Hashtable recordData = new Hashtable();
				recordData.put("YSJE", ze);//应收金额
				recordData.put("CKDH", djbh);//出库单号
				recordData.put("KHMC", kh);//客户名称
				recordData.put("KHBM", khid);//客户编码
				recordData.put("ZT", "未收");//状态
				recordData.put("LB", 1);//类别
				//插入应收表
//				int boid = BOInstanceAPI.getInstance().createBOData("BO_AKL_YS", recordData, this.getUserContext().getUID());
				BOInstanceAPI.getInstance().createBOData("BO_AKL_YS", recordData, bindid, this.getUserContext().getUID());
				return true;
			} catch (Exception e) {
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "应收数据写入失败,请通知管理员");
				e.printStackTrace(System.err);
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
}
