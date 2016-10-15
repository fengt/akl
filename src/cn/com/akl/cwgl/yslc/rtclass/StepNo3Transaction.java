package cn.com.akl.cwgl.yslc.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {
	
	/**
	 * Ӧ��״̬�Ѹ���
	 */
	private static final String YS_ZT_YFK = "�Ѹ�";

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("�����ڵ�ڵ�����¼�");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(PARAMETER_TASK_ID).toInt();
		
		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
//			int count = DAOUtil.getInt(conn, "SELECT count(*) FROM BO_AKL_YS_S WHERE bindid=?", bindid);
			// �����δ����״̬�������̼����ȴ��ڱ��ڵ�
			//��������и���״̬ȫ��Ϊ�Ѹ�������̽�����
			
			boolean kpFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "��Ʊ");
			// TODO Other: ���״̬=1��������¼���ɱ༭�����״̬=0��������¼�ɱ༭��
			if(kpFlag){
				// TODO ��Ʊ
				//4.��Ʊ����ȡ����ϵͳ���ⵥ�����ݿͻ��ɹ������ţ�ȡ�ط�Ʊ�ţ�������������еķ�Ʊ���ֶ�
			} else{
				// ���տ�
				DAOUtil.executeQueryForParser(conn, "SELECT KHBH, XSDH, CKDH, YSJE, SSJE FROM BO_AKL_YS_P p, BO_AKL_YS_S s WHERE p.BINDID=s.BINDID AND p.BINDID=?", 
						new DAOUtil.ResultPaser(){
							@Override
							public boolean parse(Connection conn, ResultSet reset) throws SQLException {
								//���տ�������۶�����������״̬Ϊ���տ�
								//���̽��������Ӧ����Ϣ���и�״̬���ֶΣ��ѵֿ�\δ�ֿۣ���
								//	����Ӧ��	
								DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_YS SET ZT=? WHERE KHBM=? AND CKDH=?", YS_ZT_YFK, reset.getString("KHBH"), reset.getString("CKDH"));
								
								if(reset.getString("XSDH") != null && !reset.getString("XSDH").trim().equals("")) {
									// ���¶���
									DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_YFK, reset.getString("XSDH"));
									// ���³���
									// DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CGDD_HEAD SET DDZT=? WHERE DDID=?", "���տ�", reset.getString("CKDH"));
								}
								return true;
							}
				}, bindid);
				
				// ���·����ʽ����״̬�ֶ�(�ѵֿ�\δ�ֿ�)
				/*
				DAOUtil.executeQueryForParser(conn, "SELECT FLZJCID FROM BO_AKL_YS_DK_S WHERE BINDID=?", new DAOUtil.ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						// TODO �ֿ�״̬�����
						DAOUtil.executeUpdate(conn, "update BO_AKL_FL_MXB set zt=? where ID=?", "�ѵֿ�", reset.getInt("FLZJCID"));
						return true;
					}
				}, bindid);
				*/
				// ��ȡ�ͻ������¿ͻ��ķ����ֿ�
				
			}
			
			conn.commit();
			return true;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
	}

}
