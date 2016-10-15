package cn.com.akl.shgl.fjfj.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class FJFJBiz {

	/**
	 * 减库存（客服或总部）
	 * @param conn
	 * @param bindid
	 * @param xmlb
	 * @throws SQLException
	 */
	public static void decreaseKCXX(Connection conn, int bindid, final int stepNo)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//项目类别
		
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//物料编号
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//故障条码
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String ejjl = StrUtil.returnStr(rs.getString("EJJL"));//二检结论
				int sl = rs.getInt("SL");//数量
				
				String hwdm = "";
				if(stepNo == 3){//客服
					hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
				}else{//检测部
					hwdm = StrUtil.returnStr(rs.getString("HWDM2"));//检测货位代码
					if(ejjl.equals(FJFJCnt.jcjg0)){//有故障，则该产品不返回给客服
						return true;
					}
				}
				
				/**1、更新故障条码状态(在途)*/
				DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX_ZT, FJFJCnt.zt3, xmlb, wlbh, pch, gztm);
				
				/**2、更新库存信息*/
				int n = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_DE, sl, xmlb, wlbh, sx, pch, hwdm);
				if(n != 1){
					throw new RuntimeException("库存扣减失败，请联系管理员！");
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 插入库存操作
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param rs
	 * @param xmlb
	 * @throws SQLException
	 */
	public static void insertXLH(Connection conn, int bindid, String uid, ResultSet rs, String xmlb)
			throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//物料编号
		String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));//物料名称
		String xh = StrUtil.returnStr(rs.getString("PN"));//型号
		String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//故障条码
		String sx = StrUtil.returnStr(rs.getString("SX"));//属性
		String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
//		String kc_hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
		int sl = rs.getInt("SL");//数量
		
//		String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, kc_hwdm, sx, FJFJCnt.zt4));//获取该物料库存批次号
		
		rec.put("XMLB", xmlb);
		rec.put("WLBH", wlbh);
		rec.put("WLMC", wlmc);
		rec.put("XH", xh);
		rec.put("PCH", pch);
		rec.put("SX", sx);
		rec.put("ZT", FJFJCnt.zt3);
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
		
		Hashtable<String, String> hwdmRecord = getHWXX(conn, xmlb, wlbh, ckdm);
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
			 * 1、总部检测插入库存
			 */
			int n = DAOUtil.getInt(conn, FJFJCnt.QUERY_isExistKCMX, wlbh, sx, ckdm, pch, FJFJCnt.zt3);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", rec, bindid, uid);
			}else{
				DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX, sl, wlbh, sx, ckdm, pch, FJFJCnt.zt3);
			}
			
			/**
			 * 2、更新返京子表检测货位代码(HWDM2)及故障条码明细(库房信息和状态)
			 */
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_HWDM2, hwdm, bindid, wlbh, gztm);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX, ckdm, ckmc, qdm, ddm, kwdm, hwdm, xmlb, wlbh, pch, gztm);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 更新送修单处理方式及库存属性
	 * @param conn
	 * @param sxBindid
	 * @param clfs
	 * @param sx 原属性
	 * @param sx2 新属性
	 * @param xmlb
	 * @param wlbh
	 * @param pch
	 * @param hwdm 客服货位代码
	 * @param hwdm2 检测货位代码
	 * @param gztm
	 * @throws SQLException
	 */
	public static void setAttribute(Connection conn, int sxBindid, String clfs, String sx, String sx2,
			String xmlb, String wlbh, String pch, String hwdm, String hwdm2, String gztm, int id, boolean flag) throws SQLException{
		
		/**1、更新送修子表(处理方式和属性)*/
		int n = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_SX_CLFS, clfs, sx2, sxBindid, wlbh, gztm, pch, hwdm, sx);
		if(n != 1) throw new RuntimeException("该单对应的送修单据（处理方式）更新失败！");
		
		/**2、客服：更新库存和序列号(属性)*/
		int updateRow1 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_SX, sx2, xmlb, wlbh, pch, hwdm, sx);
//		String xhHWDM = hwdm;//客服库<--检测库
//		if(flag) xhHWDM = hwdm2;//客服库-->检测库（序列号已在检测库）
		int updateRow2 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX_SX, sx2, xmlb, wlbh, pch, gztm, sx);
		if(updateRow1 != 1 || updateRow2 != 1){
			throw new RuntimeException("客服库存属性转换失败！");
		}
		
		/**3、检测库：更新库存(属性)*/
		int updateRow3 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_SX, sx2, xmlb, wlbh, pch, hwdm2, sx);
		if(updateRow3 != 1) throw new RuntimeException("检测库库存属性转换失败！");
		
		/**4、返京复检子表：更新(属性)*/
		int updateRow4 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_SX, sx2, id);
		if(updateRow4 != 1) throw new RuntimeException("单据子表属性转换失败！");
	}
	
	/**
	 * 获取客服仓库的库位关系信息
	 * @param conn
	 * @param xmlb
	 * @param wlbh
	 * @param ckdm
	 * @return
	 * @throws SQLException
	 */
	public static Hashtable<String, String> getHWXX(Connection conn, String xmlb, String wlbh, String ckdm)throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> rec = null;
		try{
			ps = conn.prepareStatement(SXCnt.QUERY_HWXX);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, ckdm);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String qdm = StrUtil.returnStr(rs.getString("QDM"));
				String ddm = StrUtil.returnStr(rs.getString("DDM"));
				String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
				String kwbh = StrUtil.returnStr(rs.getString("HWDM"));
				rec.put("qdm", qdm);
				rec.put("ddm", ddm);
				rec.put("kwdm", kwdm);
				rec.put("hwdm", kwbh);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return rec;
	}
}
