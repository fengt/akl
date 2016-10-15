/**
 * 
 */
package cn.com.akl.ccgl.xsck.qscy.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.u8.senddata.SendCunsignmentData;
import cn.com.akl.u8.senddata.SendStoreOutRedData;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * @author hzy
 *
 */
public class StepNo4TransactionAfter  extends WorkFlowStepRTClassA {

	public StepNo4TransactionAfter() {
		super();
	}

	public StepNo4TransactionAfter(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��4�ڵ������¼�����u8�д���ⷿ��������ݣ�������-->����ǩ�ղ��졢�������ⵥ-->RMAǩ�ղ��죩��");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�˻�");
		if(th)
			return true;
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_QSCY_P", bindid);
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_QSCY_S", bindid);
		SendCunsignmentData scd = new SendCunsignmentData();
		SendStoreOutRedData ssord = new SendStoreOutRedData();
		String ckdh = head.get("CKDH");
		String xsddh = "";
		String rmafxdh = "";
		//���ݳ��ⵥ�Ż�ó��ⵥ �ж�������ǩ�ղ��컹��RMAǩ�ղ���
		try{
			conn= DBSql.open();
			String sql = "select XSDDH,RMAFXDH from BO_AKL_CKD_HEAD where CKDH = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, ckdh);
			rs = ps.executeQuery();
			while(rs.next()){
				xsddh = rs.getString("XSDDH");
				rmafxdh = rs.getString("RMAFXDH");
			}
			if(!xsddh.isEmpty())
				scd.sendData(head, body);
			else if(!rmafxdh.isEmpty())
				ssord.sendData(head, body);
			else 
				throw new Exception("[������-->����ǩ�ղ��졢�������ⵥ-->RMAǩ�ղ���  �ӿڴ���]");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return true;

	}

}
