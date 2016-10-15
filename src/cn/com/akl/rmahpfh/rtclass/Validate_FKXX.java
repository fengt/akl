package cn.com.akl.rmahpfh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Validate_FKXX extends WorkFlowStepRTClassA {

	public Validate_FKXX() {
	}

	public Validate_FKXX(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("校验反馈信息是否为空，型号是否重复，单头金额统计是否为零");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "确认")){
			//反馈信息
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFK_BODY", bindid);
			
			if(vc == null){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "反馈信息表为空，请检查！");
			}
			Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
			String zetj = rkdtData.get("ZETJ") == null ?"":rkdtData.get("ZETJ").toString();//金额统计
			double ze = Double.parseDouble(zetj);
			if(ze == 0){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "金额统计为零，请检查!");
				return false;
			}
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			//判断反馈表型号是否重复
			String sql = "select XH from BO_AKL_WXB_RMAFK_BODY where bindid = '"+bindid+"' group by XH having count(*)>1";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						//String wlbh = rs.getString("WLBH") == null?"":rs.getString("WLBH");
						String xh = rs.getString("XH") == null?"":rs.getString("XH");
						if(!"".equals(xh) ){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "反馈表中存在重复型号"+xh+"，请检查!");
							return false;
						}
					}
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
			return false;
		}
		return true;
	}
}
