package cn.com.akl.cggl.cgdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	private static final String QueryGYSMC = "SELECT GYSMC FROM BO_AKL_GYS_P WHERE GYSBH=?";
	
	private UserContext uc;
	private Connection conn = null;
	public StepNo3Transaction() {
	}

	public StepNo3Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("根据是否预付插入应付记录。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int tastid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean agree = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, "同意");//获取审核菜单
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_CGDD_HEAD", bindid);
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CGDD_BODY", bindid);
		String sfyf = head.get("SFYF").toString();//是否预付
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			if(agree){//同意
				//1、更新采购单状态
				String update_head_zt = "UPDATE " +CgddConstant.tableName0+ " SET DZT='"+CgddConstant.dzt1+"',DDZT='"+CgddConstant.zt+"' WHERE BINDID="+bindid;
				String update_body_zt = "UPDATE " +CgddConstant.tableName1+ " SET ZT='"+CgddConstant.zt+"' WHERE BINDID="+bindid;
				DBSql.executeUpdate(conn, update_head_zt);
				DBSql.executeUpdate(conn, update_body_zt);
				//2、是否插入应付记录
				if(sfyf.equals(CgddConstant.sfyf)){
					insertYF(conn, bindid, uc, head, body);
				}
			}else{//不同意
				String update_head_zt = "UPDATE " +CgddConstant.tableName0+ " SET DZT='"+CgddConstant.dzt2+"',DDZT='"+CgddConstant.dzt2+"',JSHJ="+CgddConstant.jshj+" WHERE BINDID="+bindid;
				String update_body_zt = "UPDATE " +CgddConstant.tableName1+ " SET ZT='"+CgddConstant.dzt2+"' WHERE BINDID="+bindid;
				DBSql.executeUpdate(conn, update_head_zt);
				DBSql.executeUpdate(conn, update_body_zt);
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
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台。");
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 是否预付
	 * @param conn
	 * @param bindid
	 * @param uc
	 * @param pTable
	 * @param sVector
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertYF(Connection conn, int bindid,UserContext uc,
			Hashtable<String, String> pTable,Vector<Hashtable<String, String>> sVector) throws SQLException, AWSSDKException{
		
		Hashtable<String, String> recordData = new Hashtable<String, String>();
		String gysbh = pTable.get("GYSID").toString();
		String gysmc = DAOUtil.getString(conn, QueryGYSMC, gysbh);
		recordData.put("GYSBM", StrUtil.returnStr(gysbh));
		recordData.put("GYSMC", StrUtil.returnStr(gysmc));
		recordData.put("RKDH", pTable.get("DDID").toString());
		recordData.put("ZCRQ", pTable.get("CGRQ").toString());
		double wsje = 0.0000d;
		double hsje = 0.0000d;
		double wsAccount = 0.0000d;
		double hsAccount = 0.0000d;
		Hashtable<String, String> resTable = null;
		for (int i = 0; i < sVector.size(); i++) {
			resTable = sVector.get(i);
			double sl = Double.parseDouble(resTable.get("SL").toString());//税率
			double dj = Double.parseDouble(resTable.get("CGDJ").toString());//采购单价
			int cgsl = Integer.parseInt(resTable.get("CGSL").toString());//采购数量
			wsje = cgsl*dj;//未税金额
			hsje = cgsl*dj*(sl+1.0);//含税金额
			wsAccount += wsje;
			hsAccount += hsje;
		}
		recordData.put("WSYFJE", String.valueOf(wsAccount));
		recordData.put("HSYFJE", String.valueOf(hsAccount));
		recordData.put("ZT",CgrkCnt.zt);
		recordData.put("LB",CgrkCnt.lb1);
		
		BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName11, recordData, bindid, uc.getUID());
	}
	

}
