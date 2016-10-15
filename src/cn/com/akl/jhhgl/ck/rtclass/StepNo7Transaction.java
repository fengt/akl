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
		setDescription("签收确认后，将出库数量默认为签收出量，将出库单推送至财务系统系统中的出库单");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			
			// 2.计算账期，并根据账期计算应收，根据客户账期字段计算账期：签收日期+客户账期天数
			/**String khid = DAOUtil.getString(conn, "SELECT KH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			int zqts = DAOUtil.getInt(conn, "SELECT ZQTS FROM BO_AKL_KH_P WHERE KHID=?", khid);
			Date shrq = DAOUtil.getDate(conn, "SELECT SHRQ FROM BO_AKL_QSD_P WHERE BINDID=? ", bindid);
			String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD", bindid);
			Calendar cinstance = Calendar.getInstance();
			cinstance.setTime(shrq);;
			cinstance.add(Calendar.DAY_OF_MONTH, zqts);
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXB_XSDD_HEAD SET ZQ=? WHERE DDID=?", new Timestamp(cinstance.getTimeInMillis()), xsddh);
			*/
			// 3.办理后，出库数量默认为签收数量
			//int js = DAOUtil.getInt(conn, "SELECT JS FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			Vector<Hashtable<String, String>> qsdatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_QSD_S", bindid);
			for(Hashtable<String, String> qsdata: qsdatas){
				int id = Integer.parseInt(qsdata.get("ID"));
				int qssl =Integer.parseInt(qsdata.get("YSSL"));
				DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_QSD_S SET SSSL=? WHERE ID=?", qssl, id);
			}
			//更新单据状态
			//String dh = DAOUtil.getString(conn, "SELECT JHHDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			Hashtable<String, String> ckdtht = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
			String dh = ckdtht.get("JHHDH");
			int jhhdb = Integer.parseInt(ckdtht.get("JHHDB"));
			if(0==jhhdb){
				DAOUtil.executeUpdate(conn, "update BO_AKL_JHDD_HEAD set JHZT = 4,JHWCSJ = GETDATE() where JHDH = ? ",dh);
			}else if(1==jhhdb){
				DAOUtil.executeUpdate(conn, "update BO_AKL_HHDD_HEAD set ZT = 4,HHWCSJ = GETDATE() where HHDH = ? ",dh);
			}
			// TODO 4.并将BPM系统的亚昆出库单推送至财务系统中的其它出库单
			// 1.修改出库状态为已签收
			DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_CKD_HEAD SET CKZT='2' WHERE BINDID=?", bindid);
			conn.commit();
			return true;
		} catch (SQLException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage("流程向下流转出现错误，请联系管理员!", getUserContext().getUID());
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
		
	}

}
;