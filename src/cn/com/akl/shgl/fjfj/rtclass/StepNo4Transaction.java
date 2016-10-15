package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.biz.FJFJBiz;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	public StepNo4Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("向总部检测库房插入记录。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**1、更新库存状态*/
			final String wlzt = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_WLZT, bindid, FJFJCnt.jlbz0, FJFJCnt.djzt0));//物流状态
			if(!wlzt.equals(FJFJCnt.wlzt)){
				throw new RuntimeException("该单还未到货，暂无法办理。");
			}else{
				setKCMXHandle(conn, bindid, uid);
			}
			
			/**2、更新单据状态*/
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_P_ZT, FJFJCnt.djzt2, bindid);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_ZT, FJFJCnt.djzt2, bindid);
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 更新库存明细和故障明细状态（在库）
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public static void setKCMXHandle(Connection conn, final int bindid, final String uid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//项目类别
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//物料编号
				String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));//物料名称
				String xh = StrUtil.returnStr(rs.getString("PN"));//型号
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String hwdm2 = StrUtil.returnStr(rs.getString("HWDM2"));//检测货位代码
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//故障条码
				int sl = rs.getInt("SL");//数量
				/**
				 * 删除在途物料，重新插入在库物料
				 */
				DAOUtil.executeUpdate(conn, FJFJCnt.DELETE_KCMX, xmlb, wlbh, sx, hwdm2, pch, FJFJCnt.zt3);
				insertHandle(conn, bindid, uid, wlbh, wlmc, xh, gztm, sx, pch, sl, xmlb);
				
				int updateCount = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX_ZT, FJFJCnt.zt4, xmlb, wlbh, pch, gztm);
				if(updateCount != 1) throw new RuntimeException("库存序列号更新失败！");
				return true;
			}
		}, bindid);
	}
	
	
	public static void insertHandle(Connection conn, int bindid, String uid,
			String wlbh, String wlmc, String xh, String gztm, String sx, String pch, int sl, String xmlb)
			throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		rec.put("XMLB", xmlb);
		rec.put("WLBH", wlbh);
		rec.put("WLMC", wlmc);
		rec.put("XH", xh);
		rec.put("PCH", pch);
		rec.put("SX", sx);
		rec.put("ZT", FJFJCnt.zt4);
		rec.put("KWSL", String.valueOf(sl));
		rec.put("GZTM", gztm);
		
		/**
		 * 获取仓库货位信息优化：根据物料库位关系带出
		 */
		String qdm = "";
		String ddm = "";
		String kwdm = "";
		String hwdm = "";
		String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_SRKF, bindid));//检测库房编码
		String ckmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKMC, ckdm));//检测库房名称
		
		Hashtable<String, String> hwdmRecord = FJFJBiz.getHWXX(conn, xmlb, wlbh, ckdm);
		if(hwdmRecord != null){
			qdm = hwdmRecord.get("qdm").toString();
			ddm = hwdmRecord.get("ddm").toString();
			kwdm = hwdmRecord.get("kwdm").toString();
			hwdm = hwdmRecord.get("hwdm").toString();
		}else{
			hwdm = ckdm;
		}
		rec.put("CKDM", ckdm);//仓库代码
		rec.put("CKMC", ckmc);//仓库名称
		rec.put("QDM", qdm);//区代码
		rec.put("DDM", ddm);//道代码
		rec.put("KWDM", kwdm);//库位代码
		rec.put("HWDM", hwdm);//货位代码
		
		try {
			/**
			 * 总部检测插入库存
			 */
			int n = DAOUtil.getInt(conn, FJFJCnt.QUERY_isExistKCMX, wlbh, sx, ckdm, pch, FJFJCnt.zt4);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", rec, bindid, uid);
			}else{
				DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX, sl, wlbh, sx, ckdm, pch, FJFJCnt.zt4);
			}
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_HWDM2, hwdm, bindid, wlbh, gztm);//更新返京子表检测货位代码(HWDM2)
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}



