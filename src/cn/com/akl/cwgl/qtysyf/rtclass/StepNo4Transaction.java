package cn.com.akl.cwgl.qtysyf.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {
	
	private static DAOUtil.ResultPaser parser1 = null;
	
	public StepNo4Transaction() {
		super();
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("�ڶ��ڵ�����");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		
		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
		
			 String lx = DAOUtil.getString(conn, "SELECT LX FROM BO_AKL_QTYSYF_P WHERE BINDID=?", bindid);
			 
			 DAOUtil.getString(conn, "", bindid);
			 
			 String feat = lx.substring(lx.length()-2, lx.length());
		 	
			// ���̽���������Ӧ��\Ӧ�����ã�����������ϵͳ������Ӧ��\Ӧ��
				// �������ΪӦ��\Ӧ�����������̽����󣬽�����������ϵͳ��Ӧ��\����Ӧ��������
			 
			 // 7.1 �������ͻ��г�����֧�֡��ͻ����۱�֧�֣�ͬʱ���¿ͻ����Ӧ���ܶ�
			if("04".equals(feat) || "05".equals(feat) || "10".equals(feat)) {
				String khbm = DAOUtil.getString(conn, "SELECT KHBM FROM BO_AKL_QTYSYF_P WHERE BINDID=?", bindid);
				PreparedStatement ps = null;
				ResultSet reset = null; 
				try{
					ps = conn.prepareStatement("SELECT FX,KM,BZ,HL,JE,BBJE,BM,YWY,XM FROM BO_AKL_QTYSYF_S WHERE BINDID=?");
					reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
					
					while(reset.next()){
						// ���̽��������ݣ��ͻ��г�����֧�֡��ͻ����۱�֧�֡��г�֧�ֽ����뷵���ʽ���У������ʽ�ر��в������ݣ���
						BigDecimal bbje = reset.getBigDecimal("BBJE");
						Hashtable<String, String> hashtable = new Hashtable<String, String>();
						hashtable.put("FLJE", bbje.toString());
						hashtable.put("KHBM", khbm);
						hashtable.put("LX", lx);
						hashtable.put("ZT", "0");
						
						try {
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FL_MXB", hashtable, bindid, uid);
						} catch (AWSSDKException e) {
							throw new RuntimeException(e);
						}
					}	
				} finally {
					DBSql.close(ps, reset);
				}
			}
			
			//TODO 	7.2 ��Ӧ�̷���ѡ���Ӧ�̶�����ǩ���ã�ͨ����ѯ�ķ�ʽչʾ
			if("07".equals(feat) || "08".equals(feat)) {
			}
			
			// 	7.3 ����Ӧ��\Ӧ�����ã�������������ϵͳ��Ӧ��\����Ӧ��������
			// 1�����̽��������ݣ��������ͻ��г�����֧�֡��ͻ����۱�֧�֡��г�֧�ֽ����뷵���ʽ���У������ʽ�ر��в������ݣ���
			// 2�����̽���������Ӧ��\Ӧ�����ã�����������ϵͳ������Ӧ��\Ӧ��
			if("09".equals(feat)) {
				
			}
			
			// 3�����̽����󣬴������ͻ��г�����֧�֡��ͻ����۱�֧�֣�����������ϵͳ��Ӧ��
			if("04".equals(feat) || "05".equals(feat)) {
				
			}
			 
			DAOUtil.executeQueryForParser(conn, "SELECT FX,KM,BZ,HL,JE,BBJE,BM,YWY,XM FROM BO_AKL_QTYSYF_S WHERE BINDID=?", parser1, null, bindid);
			conn.commit();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * �����ʽ�أ��������۵�ʱ��ֿ�
		039004	�ͻ��г�����֧��
		039005	�ͻ����۱�֧��
		039010	�г����ã�MDF��
	
		������ͳ��
		039007	��Ӧ�̷����
		039008	��Ӧ�̶�����ǩ����

		�������ϵͳ
		039006	Ӧ�յ���
		039011	Ӧ������
		039009	����Ӧ�շ���
		039012	����Ӧ������
	 */
}
