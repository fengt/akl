package cn.com.akl.ccgl.cgrk.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo2And3Transaction extends WorkFlowStepRTClassA{

	private static final String UPDATE_CGDD_P_ZT = "UPDATE BO_AKL_CGDD_HEAD SET DDZT =? WHERE DDID =?";
	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo2And3Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("办理完毕后，更新采购订单的订单状态为：已提货!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//入库单头集合
		String rkdb = pTable.get("RKDB").toString();//入库单别
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			/**
			 * 根据客户代码判断是否更新采购订单状态
			 */
			String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//获取客户代码
			if("".equals(khdm) || khdm.equals(CgrkCnt.khdm0)){
				if(!CgrkCnt.rkdb3.equals(rkdb) && !CgrkCnt.rkdb5.equals(rkdb)){//除其他入库、旺电通入库，即为(闪迪采购入库，回采入库，其他采购入库，BG采购入库)
					updateCgddZT(conn,bindid);
				}
			}
			
			conn.commit();
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
	 * 更新采购订单的提货状态
	 * @param bindid
	 */
	private void updateCgddZT(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select cgddh,xh,sssl from " + CgrkCnt.tableName1 + " where bindid = " + bindid;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					String aklOrderId = StrUtil.returnStr(rs.getString("cgddh"));
					String xh = StrUtil.returnStr(rs.getString("xh"));
					int sssl = rs.getInt("sssl");
					if(StrUtil.isNotNull(aklOrderId) && StrUtil.isNotNull(xh)){
						String zt0 = judge(conn, aklOrderId,bindid);
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_P_ZT, zt0, aklOrderId);//更新单头状态
						updateDatas(conn,aklOrderId,xh,sssl);//更新单身状态
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
	public String judge(Connection conn, String khddh, int bindid) throws SQLException{
		String str0 = "SELECT SUM(CGSL)AS CGSL,SUM(YRKSL)AS YRKSL FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"'";
		String str1 = "SELECT SUM(SSSL)AS SSSL FROM BO_AKL_CCB_RKD_BODY WHERE CGDDH='"+khddh+"' AND BINDID="+bindid;
		int cgsl = DBSql.getInt(conn, str0, "CGSL");//采购总数量
		int yrksl = DBSql.getInt(conn, str0, "YRKSL");//已入库总数量
		int sssl = DBSql.getInt(conn, str1, "SSSL");//正在入库的实收总数量
		if(cgsl-yrksl == sssl ){
			return CgrkCnt.ddzt2;//已提货
		}else{
			return CgrkCnt.ddzt7;//部分提货
		}
	}
	
	/***
	 * 根据采购订单号，更新采购订单订单状态为已提货
	 * @param aklOrderId
	 * @return
	 */
	private void updateDatas(Connection conn,String aklOrderId,String xh,int sssl) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select * from " + CgrkCnt.tableName7 + " where ddid = '" + aklOrderId + "' and xh = '" + xh + "'"; 
		String sql3 = "";
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
						/**已提货**/
						sql3 = "update " + CgrkCnt.tableName7 + " set zt = '" + CgrkCnt.ddzt2 + "' where ddid = '" + aklOrderId + "' and xh = '" + xh +"'";
					}else if(sssl<wrksl){
						/**部分提货**/
						sql3 = "update " + CgrkCnt.tableName7 + " set zt = '" + CgrkCnt.ddzt7 + "' where ddid = '" + aklOrderId + "' and xh = '" + xh +"'";
					}else{
						/**提货数量异常**/
						sql3 = "update " + CgrkCnt.tableName7 + " set zt = '" + CgrkCnt.ddzt8 + "' where ddid = '" + aklOrderId + "' and xh = '" + xh +"'";
					}
				}
			}
			DBSql.executeUpdate(conn,sql3);
		}finally{
			DBSql.close(ps, rs);
		}
	}
	
}
