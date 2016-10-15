package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class DGRK_Step2 extends WorkFlowStepRTClassA {

	public DGRK_Step2() {
	}

	public DGRK_Step2(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("�ж���˲˵������ȼ�������;�˻ط�дԤ�����������Ĳɹ���ͷ������ɹ�״̬��ɾ����ⵥ�����κš�������");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		//��ⵥͷ��Ϣ
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rkdh = rkdtData.get("RKDH") == null ?"":rkdtData.get("RKDH").toString();//��ⵥ��
		String ydh = rkdtData.get("YDH") == null ?"":rkdtData.get("YDH").toString();//�ɹ�����
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//�������

		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "����")){
			return true;
		}else if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�")){
			//ɾ��������
			String hzsql = "delete from BO_AKL_DGKC_KCHZ_P where RKDH='"+rkdh+"'";
			DBSql.executeUpdate(hzsql);
			//���������Ͳ����������
			if(!rklx.equals("�������")){
				//���²ɹ�����ɹ�״̬
				String cgdssql = "update BO_AKL_DGCG_S set CGZT='���ɹ�' where DDBH='"+ydh+"'";
				DBSql.executeUpdate(cgdssql);
				//���²ɹ���ͷ
				String cgdtsql = "update BO_AKL_DGCG_P set CGZT='���ɹ�' where DDBH='"+ydh+"'";
				DBSql.executeUpdate(cgdtsql);
			}
			//ɾ����ⵥ�����κ�
			String pchsql = "update BO_AKL_DGRK_S set PCH='' where bindid='"+bindid+"'";
			DBSql.executeUpdate(pchsql);
			return true;
		}
		return false;
	}
}
