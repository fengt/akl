package cn.com.akl.shgl.qhbh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhbh.biz.DBBiz;
import cn.com.akl.shgl.qhbh.biz.LockBiz;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	DBBiz dbBiz = new DBBiz();
	List<Integer> list = null;
	public StepNo2Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("按客服加载数据并启动多个调拨流程，插入新锁库。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "发货");
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**
			 * 1、删除锁库
			 */
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_KCSK", bindid);
			
			if(yes){
				/**
				 * 2、启动多个调拨流程并锁库
				 */
				subLoadDataAndStartProcess(conn, bindid, uid);
				
				/**
				 * 3、单据状态更新
				 */
				setStatue(conn, bindid, true);
			}else{
				DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_PCAndHWDM_TH, bindid);
				setStatue(conn, bindid, false);
			}
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			if(list !=null) removeProcess(list);
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			if(list !=null) removeProcess(list);
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 推送数据，启动多个调拨流程并进行锁库
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void subLoadDataAndStartProcess(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException{
		Hashtable<String, String> head = null;
		Vector<Hashtable<String, String>> detailBody = null;
		Vector<Hashtable<String, String>> gatherBody = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String fhr = DAOUtil.getString(conn, QHBHCnt.QUERY_USERNAME, uid);//发货人
		String db_uid = DAOUtil.getString(conn, QHBHCnt.QUERY_USERID, uid);//发货人账号
		String xmlb = DAOUtil.getString(conn, QHBHCnt.QUERY_P_XMLB, bindid);//项目类别
		String bhck = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_FHCKBM, bindid));//补货仓库
		
		try{
			ps = conn.prepareStatement(QHBHCnt.QUERY_KFFZR);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				String kfzx = StrUtil.returnStr(rs.getString(1));//客服编码
				String kfmc = StrUtil.returnStr(rs.getString(2));//客服名称
//				String fzr = StrUtil.returnStr(rs.getString(3));//责任人
//				String db_uid = dbBiz.accountParse(fzr);
				
				/**
				 * 1、启动多个调拨单
				 */
				final String dbdh = RuleAPI.getInstance().executeRuleScript("DB@replace(@date,-)@formatZero(3,@sequencefordateandkey(BO_AKL_DB_P))");//调拨单号
				head = dbBiz.getHead(conn, bindid, dbdh, xmlb, bhck, kfzx, db_uid, fhr);
//				detailBody = dbBiz.getDetailBody(conn, bindid, kfzx);
				gatherBody = dbBiz.getGatherBody(conn, bindid, kfzx, bhck);
				int sub_bindid = dbBiz.startWorkflow(conn, kfmc, db_uid, head, detailBody, gatherBody);
				
				/**
				 * 2、每个调拨单bindid集合，用于异常时删除所有调拨单
				 */
				list = new ArrayList<Integer>();
				list.add(sub_bindid);
				
				/**
				 * 3、每个调拨单进行物料锁库
				 */
				DAOUtil.executeQueryForParser(conn, QHBHCnt.QUERY_S_HZ,
						new ResultPaserAbs[]{
						LockBiz.checkInvetoryPaser(),//a、验证库存
						LockBiz.insertLockPaser(sub_bindid, uid, dbdh)//b、插入锁库
				}, bindid, kfzx);
				
				
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
	}
	
	/**
	 * 异常处理（删除所有异常产生的调拨单）
	 * @param list
	 */
	public void removeProcess(List<Integer> list){
		try {
			for(int bindid : list){
				WorkflowInstanceAPI.getInstance().removeProcessInstance(bindid);
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台", true);
		}
	}
	
	/**
	 * 单据状态更新
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setStatue(Connection conn, final int bindid, final boolean flag) throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_XMLB, bindid));//项目类别
		DAOUtil.executeQueryForParser(conn, QHBHCnt.QUERY_S,
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//送修单号
				String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//送修产品行号
				String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//交付产品行号
				String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//是否接受替换
				String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));//原产品物料编号
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//交付库房编码
				
				//更新缺货记录的状态
				int count = 0;
				if(flag){
					count = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_ZT, QHBHCnt.qhzt1, xmlb, wlbh, sx, jfkfbm, sfjsth, QHBHCnt.qhzt0, sxdh, sxcphh, jfcphh);//更新缺货记录的状态
				}else{
					count = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_ZT, QHBHCnt.qhzt2, xmlb, wlbh, sx, jfkfbm, sfjsth, QHBHCnt.qhzt0, sxdh, sxcphh, jfcphh);//更新缺货记录的状态
				}
				if(count != 1) throw new RuntimeException("缺货记录状态更新失败！");
				return true;
			}
		}, bindid);
		
		if(flag)
			DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_ZT, QHBHCnt.qhzt1, bindid);//更新补货子表的状态
		else
			DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_ZT, QHBHCnt.qhzt0, bindid);//更新补货子表的状态
		
	}
	
}
