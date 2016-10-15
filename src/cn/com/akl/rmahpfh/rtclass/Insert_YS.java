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
		setDescription("����Ӧ�ձ�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		if(WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ͬ��")){
			//��ͷ��Ϣ
			Hashtable headData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAHPFH_HEAD", bindid);
			String djbh = headData.get("DJBH") == null ?"":headData.get("DJBH").toString();//��Ʒ��������
			String zetj = headData.get("ZETJ") == null ?"":headData.get("ZETJ").toString();//�ܶ�ͳ��
			String kh = headData.get("GYSMC") == null ?"":headData.get("GYSMC").toString();//��Ӧ������
			Double ze = Double.parseDouble(zetj);
			//������Ϣ
//			Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFK_BODY", bindid);
			Connection conn = DBSql.open();
			try {
				//����Ӧ�ձ�
				String sqlHead = "select GYSBH from BO_AKL_GYS_P where GYSMC='"+kh+"'";
				String khid = DBSql.getString(sqlHead, "GYSBH");
				khid = (khid == null ? "" : khid);//��Ӧ�̱��
				Hashtable recordData = new Hashtable();
				recordData.put("YSJE", ze);//Ӧ�ս��
				recordData.put("CKDH", djbh);//���ⵥ��
				recordData.put("KHMC", kh);//�ͻ�����
				recordData.put("KHBM", khid);//�ͻ�����
				recordData.put("ZT", "δ��");//״̬
				recordData.put("LB", 1);//���
				//����Ӧ�ձ�
//				int boid = BOInstanceAPI.getInstance().createBOData("BO_AKL_YS", recordData, this.getUserContext().getUID());
				BOInstanceAPI.getInstance().createBOData("BO_AKL_YS", recordData, bindid, this.getUserContext().getUID());
				return true;
			} catch (Exception e) {
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "Ӧ������д��ʧ��,��֪ͨ����Ա");
				e.printStackTrace(System.err);
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
}
