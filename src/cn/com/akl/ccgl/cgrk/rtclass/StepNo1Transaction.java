package cn.com.akl.ccgl.cgrk.rtclass;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.biz.CreatePCHBiz;
import cn.com.akl.ccgl.cgrk.biz.RKDBiz;
import cn.com.akl.ccgl.cgrk.biz.ZCXXBiz;
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

public class StepNo1Transaction extends WorkFlowStepRTClassA{
	
	private static final String UPDATE_PCH = "UPDATE BO_AKL_CCB_RKD_BODY SET PCH=? WHERE id=?";
	private static final String UPDATE_CGDD_P_ZT = "UPDATE BO_AKL_CGDD_HEAD SET DDZT =? WHERE DDID =?";
	private static final String UPDATE_CGDD_S_ZT = "UPDATE BO_AKL_CGDD_BODY SET ZT =?,ZTSL=ISNULL(ZTSL,0)+? WHERE DDID =? AND XH =?";
	
	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";
	
	
	private Connection conn = null;
	private UserContext uc;
	
	public StepNo1Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("办理完毕后，更新采购订单的订单状态为：已转仓、插入库存汇总及明细");
	}
	
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//入库单头集合
		Vector<Hashtable<String, String>> sVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);//入库单身集合
		String rkdb = pTable.get("RKDB").toString();//入库单别
		String rkdh = pTable.get("RKDH").toString();//入库单号
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());//转仓日期
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			/**
			 * 根据入库单别判断是否转仓
			 */
			if(rkdb.equals(CgrkCnt.rkdb0)){
				dealDatas(conn,uc,bindid);//将转仓信息回填至入库单身
				//sVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);
				sVector = RKDBiz.getRKDVector(conn, bindid);
				updateZcWlbh(conn,sVector,bindid);//将入库单身数据的物料编号再反写到转仓明细中
			}else{//不走转仓信息的入库，生成批次号
				if(sVector != null){
					for(int i=0; i < sVector.size(); i++){
						Hashtable<String, String> record = sVector.get(i);
						int id = Integer.parseInt(record.get("ID").toString());
						String pch = CreatePCHBiz.createPCH(zcrq);
						int n = DAOUtil.executeUpdate(conn, UPDATE_PCH, pch, id);
						if(n != 1) throw new RuntimeException("系统未能产生该单批次号，请联系管理员。");
					}
				}
			}
			
			/**
			 * 根据客户代码判断是否对库存汇总、明细和采购信息操作
			 * 1：将入库单头、单身相关数据信息插入至库存汇总表、库存明细表
			 * 2：根据转仓表中的客户订单号，更新采购订单订单状态。 
			 */
			String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//获取客户代码
			if("".equals(khdm) || khdm.equals(CgrkCnt.khdm0)){
				if(!CgrkCnt.rkdb3.equals(rkdb) && !CgrkCnt.rkdb5.equals(rkdb)){//除其他入库、旺电通入库，即为(闪迪采购入库，回采入库，其他采购入库，BG采购入库)
					UpdateCgddZT(conn, bindid);//更新采购单状态和采购在途数量
				}
				dealDatas(conn, uc, pTable, sVector,rkdb);
			}else{
				/*向代管库存汇总表插入数据*/
				Vector<Hashtable<String, String>> tmpPvector = RKDBiz.RKDPackageV1(conn, pTable);
				Vector<Hashtable<String, String>> rePvector = RKDBiz.getPvectorV2(pTable, tmpPvector,rkdb);
				BOInstanceAPI.getInstance().createBOData(conn,CgrkCnt.tableName12, rePvector, bindid, getUserContext().getUID());
				conn.commit();
			}
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台");
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 更新采购订单状态
	 * @param bindid
	 */
	public void UpdateCgddZT(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select cgddh,xh,sssl from " + CgrkCnt.tableName1 + " where bindid = " + bindid;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					String aklOrderId = StrUtil.returnStr(rs.getString("cgddh"));
					String lh = StrUtil.returnStr(rs.getString("xh"));
					int sssl = rs.getInt("sssl");
					if(StrUtil.isNotNull(aklOrderId)){
						String zt0 = judge(conn,aklOrderId,bindid);
						String zt1 = judge(conn,aklOrderId,lh,sssl);
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_P_ZT, zt0, aklOrderId);//更新单头状态
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_S_ZT, zt1, sssl, aklOrderId, lh);//更新单身状态和在途数量
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
			return CgrkCnt.ddzt0;//已转仓
		}else{
			return CgrkCnt.ddzt1;//部分转仓
		}
	}
	
	/**
	 * 判断是否已转仓、还是部分转仓(针对单身状态)
	 * @param conn
	 * @param khddh
	 * @param lh
	 * @param chsl
	 * @return
	 */
	public String judge(Connection conn,String khddh,String xh,int sssl) throws SQLException{
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
					int yrksl = rs.getInt("YRKSL");
					wrksl = cgsl - yrksl;
					if(wrksl==sssl){
						return CgrkCnt.ddzt0;//已转仓
					}else if(sssl<wrksl){
						return CgrkCnt.ddzt1;//部分转仓
					}
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return CgrkCnt.ddzt3;//转仓数量异常
	}
	
	
	public void dealDatas(Connection conn, UserContext uc, int bindid) throws Exception{
		/**第一步：将转仓明细插入至入库单身**/
		Vector<Hashtable<String, String>> zcVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName2, bindid);
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);
		ZCXXBiz zxUtil = new ZCXXBiz();
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());
		/**第二步：封装入库单身数据并写入**/
		Vector<Hashtable<String, String>> reZcVector = zxUtil.getZcxx(conn,pTable,zcVector,zcrq);
		BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName1, reZcVector, bindid, uc.getUID());
	}
	
	/**
	 * 将入库单身数据的物料编号再反写到转仓明细中
	 * @param sVector
	 * @param bindid
	 */
	public void updateZcWlbh(Connection conn,Vector sVector,int bindid) throws Exception{
		String str = "update " +CgrkCnt.tableName2+ " set wlbh=? where lh=? and khddh=? and bindid =?";
		if(sVector != null){
			for (int i = 0; i < sVector.size(); i++) {
				Hashtable rec = (Hashtable)sVector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String xh = rec.get("XH").toString();
				String cgddh = rec.get("CGDDH").toString();
				DAOUtil.executeUpdate(conn, str, wlbh,xh,cgddh,bindid);
			}
		}
	}
	
	public void dealDatas(Connection conn, UserContext uc, 
			Hashtable<String,String> pTable,Vector<Hashtable<String,String>> sVector,String rkdb) throws SQLException,AWSSDKException{
		int processInstanceId = 0;
		//写入库存汇总表数据
		Vector<Hashtable<String,String>> rePvector = new Vector<Hashtable<String,String>>();
		Vector<Hashtable<String,String>> tmpPvector = new Vector<Hashtable<String,String>>();
		tmpPvector = RKDBiz.RKDPackageV1(conn, pTable);
		rePvector = RKDBiz.getPvectorV2(pTable,tmpPvector,rkdb);
		//写入库存明细子表数据
		Vector<Hashtable<String,String>> reSvector = new Vector<Hashtable<String,String>>();
		Vector<Hashtable<String,String>> tmpSvector = new Vector<Hashtable<String,String>>();
		tmpSvector = RKDBiz.RKDPackageV2(conn, pTable);
		reSvector = RKDBiz.getSvectorV2(tmpSvector);
		processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance(CgrkCnt.uuid, uc.getUID(), "入库维护");
		int[] processTaskInstanceIds = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uc.getUID(), processInstanceId,  1, uc.getUID(), "入库维护" ); 
		int[] boIds = BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName3, rePvector, processInstanceId, uc.getUID());
		int[] boIds2 = BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName4, reSvector, processInstanceId, uc.getUID()); 
		conn.commit();
		WorkflowInstanceAPI.getInstance().closeProcessInstance(uc.getUID(), processInstanceId, processTaskInstanceIds[0]);
	}
	
	
}
