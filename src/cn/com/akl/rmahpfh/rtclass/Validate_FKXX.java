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
		setDescription("У�鷴����Ϣ�Ƿ�Ϊ�գ��ͺ��Ƿ��ظ�����ͷ���ͳ���Ƿ�Ϊ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ȷ��")){
			//������Ϣ
			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFK_BODY", bindid);
			
			if(vc == null){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "������Ϣ��Ϊ�գ����飡");
			}
			Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
			String zetj = rkdtData.get("ZETJ") == null ?"":rkdtData.get("ZETJ").toString();//���ͳ��
			double ze = Double.parseDouble(zetj);
			if(ze == 0){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "���ͳ��Ϊ�㣬����!");
				return false;
			}
			
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			//�жϷ������ͺ��Ƿ��ظ�
			String sql = "select XH from BO_AKL_WXB_RMAFK_BODY where bindid = '"+bindid+"' group by XH having count(*)>1";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						//String wlbh = rs.getString("WLBH") == null?"":rs.getString("WLBH");
						String xh = rs.getString("XH") == null?"":rs.getString("XH");
						if(!"".equals(xh) ){
							MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "�������д����ظ��ͺ�"+xh+"������!");
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
