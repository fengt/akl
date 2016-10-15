package cn.com.akl.xsgl.posbx.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	//POS����֮��������
	private static final String POSLX_FA = "035001";
	
	private static final float SL = 0.17f;
	
	//POS����״̬
	private static final String POS_BXZT0 = "056185";//δ����
	private static final String POS_BXZT2 = "056187";//�ѱ���
	
	
	//��ȡPOS���
	private static final String QUERY_POSBH = "SELECT POSBH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//��ȡTPM��
	private static final String QUERY_TPMH = "SELECT TPMH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//��ȡ�ܷ���֧������
	//private static final String QUERY_FLSL = "SELECT SUM(ISNULL(FLZCSL,0)) AS FLSL FROM BO_AKL_WXB_XSDD_BODY WHERE POSID=? AND XH=?";
	
	//��ѯ�����ܽ��
	private static final String QUERY_PROCESS_FLJE = 
			"SELECT SUM(ISNULL(FLZCJ,0)) as FLJE FROM BO_AKL_WXB_XSDD_HEAD head,BO_AKL_WXB_XSDD_BODY body "
			+ "WHERE head.ISEND=1 AND head.bindid=body.bindid AND body.POSFALX=? AND body.POSID=? GROUP BY body.POSID";
	
	//��ѯPOS����ʵ���ܽ��
	private static final String QUERY_POS_SPZJE = "SELECT SUM(ISNULL(SPPOSZE,0)) AS SPZJE FROM BO_AKL_POSBX_S WHERE BINDID=?";
	
	//��ѯPOS����Ԥ���ܽ��
	private static final String QUERY_POS_YPZJE = "SELECT SUM(ISNULL(YPPOSZE,0)) AS YPZJE FROM BO_AKL_POSBX_S WHERE BINDID=?";
	
	//��ѯ��Ӧ�̱�ź�����
	private static final String QUERY_GYSBH = "SELECT GYSBH FROM BO_AKL_WXB_XS_POS_HEAD where POSBH=?";
	private static final String QUERY_GYSMC = "SELECT GYSMC FROM BO_AKL_WXB_XS_POS_HEAD where POSBH=?";	

	//����POS����״̬
	private static final String UPDATE_POS_ZT = "UPDATE BO_AKL_POSBX_S  SET ZT=? WHERE BINDID=?";
	
	//���¹�Ӧ�̷���֧��TPMH״̬
	private static final String UPDATE_TMPH_BXZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET BXZT=? WHERE POSBH=? AND TPM=?";
	
	
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo2Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo2Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("��POS�ʽ����ϸ���в���TPM�ŵ�POS������Ӧ�ս�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ͬ��");
//		Vector<Hashtable<String, String>> vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_POSBX_S", bindid);
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			String posbh = DAOUtil.getString(conn, QUERY_POSBH, bindid);//POS���
			String tpm = DAOUtil.getString(conn, QUERY_TPMH, bindid);//TPM��
			
			if(yes){
				//POS�ʽ��ע��
				DataHander(conn, uc, bindid, posbh, tpm);
				//����POS״̬
				updateZT(conn, POS_BXZT2, bindid, posbh, tpm);
			}else{
				updateZT(conn, POS_BXZT0, bindid, posbh, tpm);
			}
			
			conn.commit();
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨���ִ����������̨��");
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
	}
	
	
	/**
	 * POS�����ʽ�ز���
	 * @param vector
	 * @param bindid
	 */
	public void DataHander(Connection conn, UserContext uc,
			int bindid, String posbh, String tpm) throws SQLException, AWSSDKException{
		
		String gysbh = DAOUtil.getString(conn, QUERY_GYSBH, posbh);//��Ӧ�̱��
		String gysmc = DAOUtil.getString(conn, QUERY_GYSMC, posbh);//��Ӧ������
		BigDecimal ypzje = DAOUtil.getBigDecimal(conn, QUERY_POS_YPZJE, bindid);//Ԥ��POS�ܶ�
		BigDecimal spzje = DAOUtil.getBigDecimal(conn, QUERY_POS_SPZJE, bindid);//ʵ��POS�ܶ�
		BigDecimal flje = DAOUtil.getBigDecimal(conn, QUERY_PROCESS_FLJE, POSLX_FA, posbh);//�����ܽ��
		
		BigDecimal sl = new BigDecimal(1);
		sl = sl.add(new BigDecimal(SL)).setScale(4, 4);
		BigDecimal ypzje2 = ypzje.multiply(sl);//ʵ��POS�ܶ�(��˰)
		BigDecimal spzje2 = spzje.multiply(sl);//ʵ��POS�ܶ�(��˰)
		BigDecimal syje = spzje2.subtract(flje);//ʣ����(pos���)
		
		/**
		 * POS�ʽ����ϸ���װ
		 */
		String a = "043028";//POS
		String b = "1";//δ�ֿ�
		Hashtable<String, String> record = new Hashtable<String, String>();
		record.put("LX", a);
		record.put("TPM", tpm);
		record.put("FABH", posbh);
		record.put("YSYJE", "0");//��ʹ�ý��
		record.put("POSJE", syje.toString());//POS���
		record.put("BXSQJE", ypzje2.toString());//����������
		record.put("YSJE", ypzje2.toString());//Ӧ�ս��
		record.put("SSJE", spzje2.toString());//ʵ�ս��
		record.put("GYSBH", gysbh);//��Ӧ�̱���
		record.put("ZT", b);
		
		// ���ʣ����Ϊ��������Ϊ��
		if(syje != null && syje.doubleValue() > 0){
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_MXB", record, bindid, uc.getUID());
		}
		
		/**
		 * POS�ʽ�ػ��ܱ��װ
		 */
		String query_zjc = "SELECT COUNT(1)N FROM BO_AKL_POS_HZB WHERE GYSBH=?";
		Integer n = DAOUtil.getIntOrNull(conn, query_zjc, gysbh);
		if(n != null && n > 0){
			String update_zjc = "UPDATE BO_AKL_POS_HZB SET POSZJE=ISNULL(POSZJE,0) + ?, SPZJE=ISNULL(SPZJE,0) + ?, SSZJE=ISNULL(SSZJE,0) + ? WHERE GYSBH=?";
			int updateCount = DAOUtil.executeUpdate(conn, update_zjc, syje.toString(), ypzje2.toString(), spzje2.toString(), gysbh);
			if(updateCount != 1) throw new RuntimeException("POS�ʽ�ظ���ʧ�ܣ�����ϵ����Ա��");
		}else{
			Hashtable<String, String> Precord = new Hashtable<String, String>();
			Precord.put("GYSBH", gysbh);//��Ӧ�̱���
			Precord.put("GYSMC", gysmc);//��Ӧ������
			Precord.put("YSYZJE", "0");//��ʹ���ܽ��
			Precord.put("POSZJE", syje.toString());//POS�ܽ��
			Precord.put("SPZJE", ypzje2.toString());//Ӧ���ܽ��
			Precord.put("SSZJE", spzje2.toString());//ʵ���ܽ��
			
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_HZB", Precord, bindid, uc.getUID());
		}
	}
	
	public static void updateZT(Connection conn, String zt, int bindid, String posbh, String tpm) throws SQLException{
		DAOUtil.executeUpdate(conn, UPDATE_POS_ZT, zt, bindid);//���±���״̬
		DAOUtil.executeUpdate(conn, UPDATE_TMPH_BXZT, zt, posbh, tpm);//���¹�Ӧ�̷���֧��TMP��ר��
	}
	
}
