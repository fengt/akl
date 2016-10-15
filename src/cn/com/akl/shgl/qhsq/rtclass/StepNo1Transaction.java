package cn.com.akl.shgl.qhsq.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhsq.biz.QHSQBiz;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("更新或插入缺货记录信息。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			service(conn, bindid, uid);
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	
	public void service(Connection conn, int bindid, String uid) throws SQLException{
		String bhlx = DAOUtil.getStringOrNull(conn, QHSQCnt.QUERY_QHSQ_P_BHLX, bindid);//补货类型
		Integer highCount = DAOUtil.getIntOrNull(conn, QHSQCnt.QUERY_HIGH, bindid);//优先级高记录
		Integer isCount = DAOUtil.getIntOrNull(conn, QHSQCnt.QUERY_SFZCP, bindid);//是否主产品
		
		if(QHSQCnt.bhlx0.equals(bhlx)){//单据引发补货
			if(highCount != null && highCount > 0){
				QHSQBiz.updateStatus(conn, bindid, bhlx, QHSQCnt.zt0);
				QHSQBiz.setStatus(conn, QHSQCnt.QUERY_QHSQ_S, QHSQCnt.zt0, bindid);
			}else{
				QHSQBiz.updateStatus(conn, bindid, bhlx, QHSQCnt.zt1);
				QHSQBiz.setStatus(conn, QHSQCnt.QUERY_QHSQ_S, QHSQCnt.zt1, bindid);
			}
		}else if(QHSQCnt.bhlx1.equals(bhlx)){//特殊申请补货
			if(isCount != null && isCount > 0){
				QHSQBiz.updateStatus(conn, bindid, bhlx, QHSQCnt.zt0);
				insertQHJL(conn, bindid, uid);
			}else{
				QHSQBiz.updateStatus(conn, bindid, bhlx, QHSQCnt.zt1);
				insertQHJL(conn, bindid, uid);
				DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHJL_ZT2, QHSQCnt.zt1, bindid);
			}
		}else{//安全库存补货
			QHSQBiz.updateStatus(conn, bindid, bhlx, QHSQCnt.zt1);
			QHSQBiz.setStatus(conn, QHSQCnt.QUERY_QHSQ_S, QHSQCnt.zt1, bindid);
		}
	}
	
	/**
	 * 插入缺货记录（特殊申请）
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertQHJL(Connection conn, final int bindid, final String uid) throws SQLException{
		final String kfzx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHSQCnt.QUERY_QHSQ_P_KFZX, bindid));
		if("".equals(kfzx)) throw new RuntimeException("缺货申请的客服仓库获取失败！");
		final String kfmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHSQCnt.QUERY_KFCKMC, kfzx));
		DAOUtil.executeQueryForParser(conn, QHSQCnt.QUERY_TSSQ_S, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				QHSQBiz.insertHander(conn, rs, bindid, uid, kfzx, kfmc);
				return true;
			}
		}, bindid);
	}
	
}
