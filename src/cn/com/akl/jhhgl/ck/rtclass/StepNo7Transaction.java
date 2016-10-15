package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo7Transaction extends WorkFlowStepRTClassA{

	public StepNo7Transaction() {
		super();
	}

	public StepNo7Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("ǩ��ȷ�Ϻ󣬽���������Ĭ��Ϊǩ�ճ����������ⵥ����������ϵͳϵͳ�еĳ��ⵥ");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			
			// 2.�������ڣ����������ڼ���Ӧ�գ����ݿͻ������ֶμ������ڣ�ǩ������+�ͻ���������
			/**String khid = DAOUtil.getString(conn, "SELECT KH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			int zqts = DAOUtil.getInt(conn, "SELECT ZQTS FROM BO_AKL_KH_P WHERE KHID=?", khid);
			Date shrq = DAOUtil.getDate(conn, "SELECT SHRQ FROM BO_AKL_QSD_P WHERE BINDID=? ", bindid);
			String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD", bindid);
			Calendar cinstance = Calendar.getInstance();
			cinstance.setTime(shrq);;
			cinstance.add(Calendar.DAY_OF_MONTH, zqts);
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET ZQ=? WHERE DDID=?", new Timestamp(cinstance.getTimeInMillis()), xsddh);
			*/
			// 3.����󣬳�������Ĭ��Ϊǩ������
			//int js = DAOUtil.getInt(conn, "SELECT JS FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			Vector<Hashtable<String, String>> qsdatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_QSD_S", bindid);
			for(Hashtable<String, String> qsdata: qsdatas){
				int id = Integer.parseInt(qsdata.get("ID"));
				int qssl =Integer.parseInt(qsdata.get("YSSL"));
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_QSD_S SET SSSL=? WHERE ID=?", qssl, id);
			}
			//���µ���״̬
			//String dh = DAOUtil.getString(conn, "SELECT JHHDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			Hashtable<String, String> ckdtht = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
			String dh = ckdtht.get("JHHDH");
			int jhhdb = Integer.parseInt(ckdtht.get("JHHDB"));
			if(0==jhhdb){
				DAOUtil.executeUpdate(conn, "update BO_AKL_JHDD_HEAD set JHZT = 4,JHWCSJ = GETDATE() where JHDH = ? ",dh);
			}else if(1==jhhdb){
				DAOUtil.executeUpdate(conn, "update BO_AKL_HHDD_HEAD set ZT = 4,HHWCSJ = GETDATE() where HHDH = ? ",dh);
			}
			// TODO 4.����BPMϵͳ���������ⵥ����������ϵͳ�е��������ⵥ
			// 1.�޸ĳ���״̬Ϊ��ǩ��
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CKZT='2' WHERE BINDID=?", bindid);
			conn.commit();
			return true;
		} catch (SQLException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage("����������ת���ִ�������ϵ����Ա!", getUserContext().getUID());
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
		
	}

}
;