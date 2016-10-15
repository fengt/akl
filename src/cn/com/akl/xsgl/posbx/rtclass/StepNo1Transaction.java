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

	//POS报销状态
	private static final String POS_BXZT1 = "056186";//报销中
	
	//获取POS编号
	private static final String QUERY_POSBH = "SELECT POSBH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//获取TPM号
	private static final String QUERY_TPMH = "SELECT TPMH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//更新POS报销状态
	private static final String UPDATE_POS_ZT = "UPDATE BO_AKL_POSBX_S  SET ZT='"+POS_BXZT1+"' WHERE BINDID=?";
		
	//更新供应商费用支出TPMH状态
	private static final String UPDATE_TMPH_BXZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET BXZT='"+POS_BXZT1+"' WHERE POSBH=? AND TPM=?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("更新POS报销状态。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			String posbh = DAOUtil.getString(conn, QUERY_POSBH, bindid);//POS编号
			String tpm = DAOUtil.getString(conn, QUERY_TPMH, bindid);//TPM号
			
			//更新报销状态
			DAOUtil.executeUpdate(conn, UPDATE_POS_ZT, bindid);
			
			//更新供应商费用支持TMP号专题
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
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现错误，请检查控制台。");
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
	}

}
