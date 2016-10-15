package cn.com.akl.cwgl.jkfk.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	/**
	 * Ӧ��״̬�Ѹ���
	 */
	private static final String YF_ZT_YFK = "�Ѹ�";
	/**
	 * ��ⵥ�Ѹ���״̬
	 */
	private static final String RKD_DDZT_YFK = "�Ѹ�";
	/**
	 * �ɹ����Ѹ���״̬
	 */
	private static final String CGD_DDZT_YFK = "�Ѹ�";
	/**
	 * POS�ʽ���ѵֿ�״̬
	 */
	private static final String POSZJC_YDK = "0";
	
	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		
		setVersion("1.0.0");
		setDescription("���ɸ���ڵ�������ת�¼�");
	}

	/**
	 * 4.���ɸ���ڵ㣬�������ĳ����¼Ϊ��δ���״̬�������̲�������������ת�����ɸ������������м�¼��Ϊ�Ѹ���״̬�������̽���
	 * 5.�����Ѹ���״̬�ļ�¼����������󣬱�����¼���ɱ༭
	 * 8.���ɽڵ��������״̬���ѵֿ�\�Ѹ���=1��δ�ֿ�\δ����=0�����״̬=1��
	 * 	  ������¼���ɱ༭�����״̬=0��������¼�ɱ༭��
	 *   ��������и���״̬ȫ��Ϊ�Ѹ�������̽����������δ����״̬�������̼����ȴ��ڱ��ڵ�
	 */
	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			// ����δ�����¼������ȴ�
			//int countFk = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_HKJK_S WHERE BINDID=? AND FKLX=?", bindid, 0);
			//int countDk = DAOUtil.getInt(conn, "SELECT COUNT(*) FROM BO_AKL_HKJK_DK_S WHERE BINDID=? AND DKZT=?", bindid, 0);
				
			// 1.���̽��������Ӧ����Ϣ���и�״̬���ֶΣ��ѵֿ�\δ�ֿۣ�
			DAOUtil.executeQueryForParser(conn, "SELECT RKDH, CGDH FROM BO_AKL_HKJK_S WHERE BINDID=?", new ResultPaser() {
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					if(reset.getString("CGDH") != null && !reset.getString("CGDH").trim().equals("")){
						// �����Ѹ���
						DAOUtil.executeUpdate(conn, "update BO_AKL_CCB_RKD_HEAD set FKZT=? where RKDH=?", RKD_DDZT_YFK, reset.getString("RKDH"));
//						DAOUtil.executeUpdate(conn, "update BO_AKL_CGDD_HEAD set FKZT=? where DDID=?", CGD_DDZT_YFK, reset.getString("CGDH"));
					}
					// TODO �ֿ�״̬�����
					DAOUtil.executeUpdate(conn, "update BO_AKL_YF set zt=? where RKDH=?", YF_ZT_YFK, reset.getString("RKDH"));
					return true;
				}
			}, bindid);
			
			// 2.���̽����󣬸���POS�ʽ�ر��ж�ӦTPM�ŵ�״̬���ѵֿ�\δ�ֿۣ�
			DAOUtil.executeQueryForParser(conn, "SELECT POSZJCID FROM BO_AKL_HKJK_DK_S WHERE BINDID=?", new ResultPaser() {
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					// TODO �ֿ�״̬�����
					DAOUtil.executeUpdate(conn, "update BO_AKL_POS_MXB set zt=?, SSJE=POSJE where ID=?", POSZJC_YDK, reset.getInt("POSZJCID"));
					return true;
				}
			}, bindid);
			
			// TODO 3.������=Ӧ�����-��Ʊ�����������<>0�����̽����������ϵͳ����Ӧ��������
			conn.commit();
			return true;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	

}
