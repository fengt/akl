package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

public class StepNo6Transaction extends WorkFlowStepRTClassA{
	
	private static final String QUERY_CKD_WLSL = "SELECT WLH, WLMC, XH, JLDW, KHCPBM, SUM(ISNULL(SJSL, 0)) as YSSL FROM BO_AKL_CKD_BODY WHERE BINDID=? GROUP BY WLH, WLMC, XH, JLDW, KHCPBM";
	private static final String QUERY_CKD_HEAD = "SELECT JHDZ,CKDH,KHCGDH,KHMC,CXFZR,CXDH,CK,YSHJ,KFLXR,CKLXRDH,CKLXRSJ,CKLXREMAIL FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	
	
	public StepNo6Transaction() {
		super();
	}

	public StepNo6Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("填充签收单");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet reset = null;
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			Hashtable<String, String> qsData = new Hashtable<String, String>();
			
			// 出库单填充签收单
			PreparedStatement ckPs = null;
			ResultSet ckReset = null;
			try {
				ckPs = conn.prepareStatement(QUERY_CKD_HEAD);
				ckReset = DAOUtil.executeFillArgsAndQuery(conn, ckPs, bindid);
				if(ckReset.next()){
					qsData.put("SHDZ", parseNull(ckReset.getString("JHDZ")));
					qsData.put("CKDH", parseNull(ckReset.getString("CKDH")));
					qsData.put("KHCGDH", parseNull(ckReset.getString("KHCGDH")));
					qsData.put("SHDW", parseNull(ckReset.getString("KHMC")));
					qsData.put("SHFZR", parseNull(ckReset.getString("CXFZR")));
					qsData.put("SHFZRDH", parseNull(ckReset.getString("CXDH")));
					qsData.put("SHKF", parseNull(ckReset.getString("CK")));
					qsData.put("YSSLHJ", parseNull(ckReset.getString("YSHJ")));
					qsData.put("SHFZR", parseNull(ckReset.getString("KFLXR")));
					qsData.put("SHFZRDH", parseNull(ckReset.getString("CKLXRDH")));
				}
				
				if(qsData.size() != 0)
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_P", qsData, bindid, getUserContext().getUID());
			} finally {
				DBSql.close(ckPs, ckReset);
			}
			
			fillWLSL(conn, bindid);

			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return true;
		} finally {
			DBSql.close(conn, ps, reset);
		}
	}
	
	/**
	 * 填充签收单
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException 
	 */
	public void fillWLSL(Connection conn, int bindid) throws SQLException, AWSSDKException{
		PreparedStatement ps = null;
		ResultSet reset = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		try{
			ps = conn.prepareStatement(QUERY_CKD_WLSL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(reset.next()){
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				String wlbh = reset.getString("WLH");
				String wlmc = reset.getString("WLMC");
				String xh = reset.getString("XH");
				String jldw = reset.getString("JLDW");
				String khcpbm = reset.getString("KHCPBM");
				String wlsl = reset.getString("YSSL");
				hashtable.put("WLH", wlbh);
				hashtable.put("CPMC", wlmc);
				hashtable.put("XH", xh);
				hashtable.put("DW", jldw);
				hashtable.put("KHSPBH", khcpbm);
				hashtable.put("YSSL", wlsl);
				hashtable.put("SSSL", wlsl);
				vector.add(hashtable);
			}
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_S", vector, bindid, getUserContext().getUID());
		}finally{
			DBSql.close(ps, reset);
		}
	}

	public String parseNull(Object obj){
		if(obj == null)
			return "";
		else 
			return obj.toString();
	}
	
}
