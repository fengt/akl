package cn.com.akl.ccgl.cgrk.rtclass;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.biz.RKDBiz;
import cn.com.akl.ccgl.cgrk.biz.YFBiz;
import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA{

	private static final String UPDATE_CGDD_P_ZT = "UPDATE BO_AKL_CGDD_HEAD SET DDZT =? WHERE DDID =?";
	private static final String UPDATE_CGDD_S_ZT = "UPDATE BO_AKL_CGDD_BODY SET ZT=?,YRKSL=ISNULL(YRKSL,0)+?,ZTSL=ISNULL(ZTSL,0)-? WHERE DDID=? AND XH=? ";
	private static final String UPDATE_RKRQ = "UPDATE "+CgrkCnt.tableName0+" SET RKRQ=? WHERE BINDID=?";
	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo4Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("办理完毕后，更新采购订单的订单状态为：已入库/部分入库!");
	}
	
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//入库单头集合
		Vector<Hashtable<String, String>> sVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);
		String rkdb = pTable.get("RKDB").toString();//入库单别
		
		Date date = Calendar.getInstance().getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String now = df.format(date);//获取当前时间
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			DAOUtil.executeUpdate(conn,UPDATE_RKRQ,now,bindid);//更新入库日期为当前时间
			
			/**
			 * 根据客户代码判断是否对库存汇总、明细和采购信息操作
			 */
			String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//获取客户代码
			if("".equals(khdm) || khdm.equals(CgrkCnt.khdm0)){
				
				dealStatusForKCHZ(conn,sVector,bindid,CgrkCnt.tableName3);//根据物料编号和批次号更新库存汇总表的"入库数量"及状态
				if(!CgrkCnt.rkdb3.equals(rkdb) && !CgrkCnt.rkdb5.equals(rkdb)){//除其他入库、旺电通入库，即为(闪迪采购入库，回采入库，其他采购入库，BG采购入库)
					UpdateCgdd(conn,bindid);//更新采购订单状态、已入库数量、在途数量
					YFBiz.insertInfoToYf(conn,bindid,uc,pTable,sVector);//向应付信息表中插入应付记录
				}			
				deleteAndInsertUtil(conn,uc,pTable,bindid);//删除库存明细表转仓时生成的数据和插入入库时新的数据
				
			}else{
				dealStatusForKCHZ(conn,sVector,bindid,CgrkCnt.tableName12);//根据物料编号和批次号更新代管库存汇总表的"入库数量"及状态
				Vector<Hashtable<String, String>> tmpSvector = RKDBiz.RKDPackageV2(conn, pTable);
				Vector<Hashtable<String, String>> reSvector = RKDBiz.getSvectorV2(tmpSvector);
				BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName13, reSvector, bindid, getUserContext().getUID());//向代管库存明细表中插入数据
				conn.commit();
			}
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}
	
	
	
	
	/**
	 * 删除转仓时此次入库（同一批次）中明细表中所有数据和向明细表插入入库时新的数据
	 * @param vector
	 * @param bindid
	 */
	private void deleteAndInsertUtil(Connection conn, UserContext uc, Hashtable<String, String> pTable,int bindid) throws SQLException,AWSSDKException{
		String str = "select pch from " + CgrkCnt.tableName1 + " where bindid = " + bindid;
		String pch1 = DBSql.getString(str, "pch");
		String str2 = "delete from " + CgrkCnt.tableName4 + " where pch = '"+pch1+"'";
		DBSql.executeUpdate(conn, str2);
		
		Vector<Hashtable<String, String>> tmpSvector = RKDBiz.RKDPackageV2(conn, pTable);
		Vector<Hashtable<String, String>> recordDatas = RKDBiz.getSvectorV2(tmpSvector);
		
		int processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance(CgrkCnt.uuid, uc.getUID(), "入库维护");
		int[] processTaskInstanceId = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uc.getUID(), processInstanceId, 1, uc.getUID(), "入库维护");
		BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName4, recordDatas, processInstanceId, uc.getUID());
		conn.commit();
		WorkflowInstanceAPI.getInstance().closeProcessInstance(uc.getUID(), processInstanceId, processTaskInstanceId[0]);
	}
	
	
	/**
	 * 更新库存汇总表中的状态为"正常"和"入库数量"
	 * @param xh
	 * @param pch
	 */
	private void dealStatusForKCHZ(Connection conn, Vector<Hashtable<String, String>> sVector,int bindid,String tablename) throws SQLException{
		for (int i = 0; i < sVector.size(); i++) {
			Hashtable<String, String> rec = sVector.get(i);
			String wlbh = rec.get("WLBH").toString();
			String pch = rec.get("PCH").toString();
			String xh = rec.get("XH").toString();
			String str3 = "select sum(sssl) sssl from " + CgrkCnt.tableName1 + " where wlbh = '"+wlbh+"' and pch = '"+pch+"' and bindid = " + bindid;
			int sssl = DBSql.getInt(str3, "sssl");
			String sql = "update " + tablename +" set rksl = "+sssl+" , pcsl = "+sssl+", zt = '" + CgrkCnt.kczt2 + "' where wlbh = '"+wlbh+"' and PCH = '" + pch +"' and xh = '" + xh + "'";
			int updateCount = DBSql.executeUpdate(conn, sql);
			if(updateCount != 1) throw new RuntimeException("库存汇总状态更新失败，请联系管理员！");
		}
	}
	
	
	/**
	 * 更新采购订单的状态和已入库数量
	 * @param bindid
	 */
	private void UpdateCgdd(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select cgddh,xh,sssl,pch from " + CgrkCnt.tableName1 + " where bindid = " + bindid;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					String aklOrderId = StrUtil.returnStr(rs.getString("cgddh"));
					String xh = StrUtil.returnStr(rs.getString("xh"));
					String pch = StrUtil.returnStr(rs.getString("pch"));
					int sssl = rs.getInt("sssl");
					if(StrUtil.isNotNull(aklOrderId) && StrUtil.isNotNull(xh)){
						String zt1 = judege(conn, aklOrderId, xh, sssl);
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_S_ZT,zt1,sssl,sssl,aklOrderId,xh);//更新单身状态和在途数量
						String zt0 = judge(conn, aklOrderId);
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_P_ZT, zt0, aklOrderId);//更新单头状态
					}
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
	}
	
	/**
	 * 判断是否已转仓、还是部分转仓(针对单头状态)
	 * @param conn
	 * @param khddh
	 * @return
	 * @throws SQLException
	 */
	public String judge(Connection conn, String khddh) throws SQLException{
		String str = "SELECT SUM(CGSL)AS CGSL,SUM(YRKSL)AS YRKSL FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"'";
		int cgsl = DBSql.getInt(conn, str, "CGSL");//采购总数量
		int yrksl = DBSql.getInt(conn, str, "YRKSL");//已入库总数量
		if(cgsl == yrksl){
			return CgrkCnt.ddzt4;//已入库
		}else{
			return CgrkCnt.ddzt5;//部分入库
		}
	}
	
	/**
	 * 判断是否已入库、还是部分入库(针对单身状态)
	 * @param conn
	 * @param khddh
	 * @param lh
	 * @param chsl
	 * @return
	 */
	private String judege(Connection conn,String khddh,String xh,int sssl) throws SQLException{
		String sql = "select * from " + CgrkCnt.tableName7 + " where ddid = '" + khddh + "' and xh = '" + xh + "'";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			int wrksl = 0;
			if(rs!=null){
				while(rs.next()){
					int cgsl = rs.getInt("cgsl");
					int yrksl = rs.getInt("yrksl");
					wrksl = cgsl - yrksl;
					if(wrksl==sssl){
						return CgrkCnt.ddzt4;//已入库
					}else if(sssl<wrksl){
						return CgrkCnt.ddzt5;//部分入库
					}else{
						return CgrkCnt.ddzt6;//入库数量异常
					}
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return CgrkCnt.ddzt6;//入库数量异常
	}
	
}
