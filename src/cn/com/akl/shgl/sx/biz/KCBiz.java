package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class KCBiz {

	/**
	 * 库存汇总插入或更新
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param rs
	 * @param xmlb
	 * @throws SQLException
	 */
	public void insertKCHZ(Connection conn, int bindid, String uid, ResultSet rs, String xmlb, String pch) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		int rksl = rs.getInt("SL");
		rec.put("XMLB", xmlb);//项目类别
		rec.put("WLBH", wlbh);//物料编号
		rec.put("WLMC", wlmc);//物料名称
		rec.put("XH", xh);//型号
		rec.put("PCH", pch);//批次号
		rec.put("RKSL", String.valueOf(rksl));//入库数量
		rec.put("PCSL", String.valueOf(rksl));//批次数量
		
		try {
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCHZ, xmlb, wlbh, pch);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_P", rec, bindid, uid);//插入库存汇总
			}else{
				int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCHZ, rksl, rksl, xmlb, wlbh, xh, pch);//更新库存汇总
				if(updateCount != 1) throw new RuntimeException("库存汇总更新失败！"); 
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 库存明细更新（即属性变化的更新）
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param body
	 * @throws SQLException
	 */
	public void insertKCMX(Connection conn, int bindid, String uid, Hashtable<String, String> body, String xmlb, String clfs) throws SQLException{
		String wlbh = body.get("WLBH");
		String sx = body.get("SX");
		String pch = body.get("PCH");
		String hwdm = body.get("HWDM");
		int sl = Integer.parseInt(body.get("KWSL"));
		
		/**
		 * 1、减（第一节点插入或更新）的待检品库存；
		 * 注：（废弃思路）核查（第一节点插入或更新）的待检品库存，如果库存值 = 送修数量，则删除此库存记录，否则，则减该库存;（但此操作较复杂）
		 */
		int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_KWSL, -sl, xmlb, wlbh, hwdm, pch, SXCnt.sx2);
		if(updateCount != 1) throw new RuntimeException("待检品库存更新失败！");
		int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCMX, xmlb, wlbh, sx, hwdm, pch, SXCnt.zt0);
		if(!SXCnt.clfs8.equals(clfs)){//除（处理方式：无实物更换）不插入库存，其他都插入库存
			if (n == 0) {
				//a、如果库存没有：插入新库存
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", body, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				//b、如果库存已有：加库存数量
				int updateCount1 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1) throw new RuntimeException("库存明细更新失败！");
			}
		}
	}
	
	/**
	 * 插入故障明细
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param body
	 */
	public void insertXLHMX(Connection conn, int bindid, String uid, Hashtable<String, String> body, String xmlb, String gztm) throws SQLException{
		String wlbh = body.get("WLBH");
		String sx = body.get("SX");
		String pch = body.get("PCH");
		String hwdm = body.get("HWDM");
		try {
			/**1、插入或更新库存明细*/
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistGZMX, xmlb, wlbh, sx, hwdm, pch, SXCnt.zt0, gztm);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_XLH_S", body, bindid, uid);
			}else{
				throw new RuntimeException("该故障条码【"+gztm+"】重复录入，请核查！");
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 删除临时库存明细
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void removeKCMX(Connection conn, int bindid) throws SQLException{
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SHKC_S", bindid);
	}
	
	/**
	 * 库存明细数据封装
	 * @param conn
	 * @param rs
	 * @param ckdm
	 * @param xmlb
	 * @param pch
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String,String> getKCMX(Connection conn, ResultSet rs, String ckdm, String xmlb, String ywlx, String pch, String sx)
			throws SQLException{
		
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		String syrlx = StrUtil.returnStr(rs.getString("SYRLX"));
		String gztm = StrUtil.returnStr(rs.getString("GZTM"));
		int id = rs.getInt("ID");
		int sl = rs.getInt("SL");
		double jg = rs.getDouble("JG");
		
		String ckmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKMC, ckdm));
		rec.put("KEYID", String.valueOf(id));
		rec.put("XMLB", xmlb);//项目类别
		rec.put("WLBH", wlbh);//物料编号
		rec.put("WLMC", wlmc);//物料名称
		rec.put("SXRLX", syrlx);//使用人类型
		rec.put("XH", xh);//型号
		rec.put("SX", sx);//属性
		rec.put("PCH", pch);//批次号
		rec.put("ZT", SXCnt.zt0);//状态
		rec.put("KWSL", String.valueOf(sl));//库位数量
		rec.put("DJ", String.valueOf(jg));//单价
		rec.put("GZTM", gztm);//故障条码
		
		/**
		 * 获取仓库货位信息优化：根据物料库位关系带出
		 */
		Hashtable<String, String> hwdmRecord = getHWXX(conn, xmlb, wlbh, ckdm);
		String hwdm = "";
		rec.put("CKDM", ckdm);//客服仓库编码
		rec.put("CKMC", ckmc);//客服仓库名称
		if(hwdmRecord != null){
			hwdm = hwdmRecord.get("hwdm").toString();
			rec.put("QDM", hwdmRecord.get("qdm").toString());//区代码
			rec.put("DDM", hwdmRecord.get("ddm").toString());//道代码
			rec.put("KWDM", hwdmRecord.get("kwdm").toString());//库位代码
			rec.put("HWDM", hwdm);//货位代码
		}else{
			hwdm = ckdm;
			rec.put("HWDM", ckdm);//货位代码
		}
		return rec;
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
	public Hashtable<String, String> getHWXX(Connection conn, String xmlb, String wlbh, String ckdm)throws SQLException{
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
