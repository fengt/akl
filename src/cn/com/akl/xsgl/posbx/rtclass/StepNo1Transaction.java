package cn.com.akl.xsgl.posbx.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

	//POS����״̬
	private static final String POS_BXZT1 = "056186";//������
	
	//��ȡPOS���
	private static final String QUERY_POSBH = "SELECT POSBH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//��ȡTPM��
	private static final String QUERY_TPMH = "SELECT TPMH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//����POS����״̬
	private static final String UPDATE_POS_ZT = "UPDATE BO_AKL_POSBX_S  SET ZT='"+POS_BXZT1+"' WHERE BINDID=?";
		
	//���¹�Ӧ�̷���֧��TPMH״̬
	private static final String UPDATE_TMPH_BXZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET BXZT='"+POS_BXZT1+"' WHERE POSBH=? AND TPM=?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("����POS����״̬��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			String posbh = DAOUtil.getString(conn, QUERY_POSBH, bindid);//POS���
			String tpm = DAOUtil.getString(conn, QUERY_TPMH, bindid);//TPM��
			
			//���±���״̬
			DAOUtil.executeUpdate(conn, UPDATE_POS_ZT, bindid);
			
			//���¹�Ӧ�̷���֧��TMP��ר��
			DAOUtil.executeUpdate(conn, UPDATE_TMPH_BXZT, posbh, tpm);
			
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

}
