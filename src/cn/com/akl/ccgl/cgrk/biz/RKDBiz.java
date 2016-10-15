package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;

public class RKDBiz {

	/**
	 * 封装库存汇总表数据
	 * @param pTable
	 * @param vector
	 * @return
	 */
	public static Vector<Hashtable<String, String>> getPvectorV2(
			Hashtable<String, String> pTable, Vector<Hashtable<String, String>> vector,String rkdb){
		
		Vector<Hashtable<String, String>> rceVector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rceHash = null;
		String zt = CgrkCnt.kczt;
		if(rkdb.equals(CgrkCnt.rkdb3)){
			zt = CgrkCnt.kczt3;
		}
		for (int i = 0; i < vector.size(); i++) {
			rceHash = new Hashtable<String, String>();
			Hashtable<String, String> rec = vector.get(i);
			rceHash.put("WLBH", rec.get("wlbh"));
			rceHash.put("WLMC", rec.get("cpmc"));
			rceHash.put("XH", rec.get("xh"));
			rceHash.put("PCH", rec.get("pch"));
			rceHash.put("DW", rec.get("dw"));
			rceHash.put("RKSL", rec.get("sssl"));
			rceHash.put("PCSL", rec.get("sssl"));
			rceHash.put("DJ", rec.get("wsjg"));
			rceHash.put("RKDH", pTable.get("RKDH"));
			rceHash.put("RKRQ", pTable.get("RKRQ"));
			rceHash.put("ZT",zt);
			
			rceVector.add(rceHash);
		}
		return rceVector;
	}
	
	/**
	 * 封装库存明细表数据
	 * @param pTable
	 * @param vector
	 * @return
	 */
	public static Vector<Hashtable<String, String>> getSvectorV2(Vector<Hashtable<String, String>> vector){
		Vector<Hashtable<String, String>> rceVector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rceHash = null;
		for (int i = 0; i < vector.size(); i++) {
			rceHash = new Hashtable<String, String>();
			Hashtable<String, String> rec = vector.get(i);
			String wlbh = rec.get("wlbh").toString();
			String str0 = "SELECT * FROM " + CgrkCnt.tableName8 + " WHERE WLBH='"+wlbh+"'";
			String gg = DBSql.getString(str0, "GG");
			rceHash.put("WLBH", wlbh);//物料编号
			rceHash.put("WLMC", rec.get("cpmc"));//物料名称
			rceHash.put("PCH", rec.get("pch"));//批次号
			rceHash.put("XH", rec.get("xh"));//型号
			rceHash.put("GG", gg);//规格
			
			rceHash.put("FZSX", rec.get("fzsx"));//辅助属性
			rceHash.put("SCRQ", rec.get("scrq"));//生产日期
			rceHash.put("JLDW", rec.get("dw"));//计量单位
			rceHash.put("KWSL", rec.get("sssl"));//库位数量
			
			String ckbm = rec.get("ckbm").toString();
			rceHash.put("CKDM",ckbm);//仓库代码
			String ckmc = DBSql.getString("select CKMC from BO_AKL_CK where CKDM='"+ckbm+"'", "ckmc");
			rceHash.put("CKMC",ckmc);//仓库名称
			
			rceHash.put("QDM", rec.get("kfqbm"));//区代码
			rceHash.put("DDM", rec.get("kfdbm"));//道代码
			rceHash.put("KWDM", rec.get("kfkwdm"));//库位代码
			rceHash.put("HWDM", rec.get("kwbh"));//货位代码
			rceHash.put("SX", CgrkCnt.sx0);
			rceVector.add(rceHash);
		}
		return rceVector;
	}
	
	/**
	 * 入库单身数据汇总(不针对货位：用于汇总插入时)
	 * @param conn
	 * @param pTable
	 * @return
	 */
	public static Vector<Hashtable<String, String>> RKDPackageV1(Connection conn, Hashtable<String, String> pTable) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		int bindid = Integer.parseInt(pTable.get("BINDID").toString());
		String sql = "SELECT wlbh,cpmc,xh,pch,ISNULL(wsjg, 0) as dj,sum(sssl) as sssl,dw FROM " + CgrkCnt.tableName1 + " WHERE bindid="+bindid+" GROUP BY wlbh,cpmc,xh,pch,wsjg,dw";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					rec = new Hashtable<String, String>();
					rec.put("wlbh", rs.getString("wlbh"));
					rec.put("cpmc",StrUtil.returnStr(rs.getString("cpmc")));
					rec.put("xh", rs.getString("xh"));
					rec.put("pch", rs.getString("pch"));
					rec.put("wsjg", String.valueOf((rs.getDouble("dj"))));
					rec.put("sssl", String.valueOf(rs.getInt("sssl")));
					rec.put("dw",StrUtil.returnStr(rs.getString("dw")));
					vector.add(rec);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
	/**
	 * 入库单身数据汇总(针对货位：可能会存在同一货位的重复物料,但来源单号不同)
	 * @param conn
	 * @param pTable
	 * @return
	 */
	public static Vector<Hashtable<String, String>> RKDPackageV2(Connection conn,Hashtable<String, String> pTable) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		int bindid = Integer.parseInt(pTable.get("BINDID").toString());
		String sql = "SELECT WLBH,CPMC,PCH,XH,FZSX,SCRQ,DW,CKBM,KFQBM,KFDBM,KFKWDM,KWBH,sum(SSSL)as SSSL FROM " + CgrkCnt.tableName1 + " WHERE bindid="+bindid+" GROUP BY WLBH,CPMC,PCH,XH,FZSX,SCRQ,DW,CKBM,KFQBM,KFDBM,KFKWDM,KWBH";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					rec = new Hashtable<String, String>();
					rec.put("wlbh", rs.getString("wlbh"));//物料编号
					rec.put("cpmc",StrUtil.returnStr(rs.getString("cpmc")));//物料名称
					rec.put("pch", rs.getString("pch"));//批次号
					rec.put("xh", rs.getString("xh"));//型号
					rec.put("fzsx", StrUtil.returnStr(rs.getString("fzsx")));//辅助属性
					rec.put("scrq", StrUtil.returnStr(rs.getString("SCRQ")));//生产日期
					rec.put("dw",StrUtil.returnStr(rs.getString("dw")));//单位
					rec.put("ckbm", StrUtil.returnStr(rs.getString("ckbm")));//仓库编码
					rec.put("kfqbm", StrUtil.returnStr(rs.getString("kfqbm")));//区编码
					rec.put("kfdbm", StrUtil.returnStr(rs.getString("kfdbm")));//道编码
					rec.put("kfkwdm", StrUtil.returnStr(rs.getString("kfkwdm")));//库位编码
					rec.put("kwbh", StrUtil.returnStr(rs.getString("kwbh")));//货位编号
					rec.put("sssl", String.valueOf(rs.getInt("sssl")));//库位数量
					vector.add(rec);
				}
			}
		}finally{
			DBSql.close(null, ps, rs);
		}
		return vector;
	}
	
	
	public static Vector<Hashtable<String, String>> getRKDVector(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> rkdVector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> hs = null;
		String query_rkd = "SELECT * FROM BO_AKL_CCB_RKD_BODY WHERE BINDID="+bindid; 
		try{
			ps = conn.prepareStatement(query_rkd);
			rs = ps.executeQuery();
			while(rs.next()){
				hs = new Hashtable<String, String>();
				hs.put("CGDDH",StrUtil.returnStr(rs.getString("CGDDH")));//采购订单号
				hs.put("LYDH",StrUtil.returnStr(rs.getString("LYDH")));//来源单号
				hs.put("WLBH",StrUtil.returnStr(rs.getString("WLBH")));//物料编号
				hs.put("CPMC",StrUtil.returnStr(rs.getString("CPMC")));//物料名称
				hs.put("XH",StrUtil.returnStr(rs.getString("XH")));//型号
				hs.put("PCH",StrUtil.returnStr(rs.getString("PCH")));//批次号
				hs.put("SCRQ",StrUtil.returnStr(rs.getString("SCRQ")));//生产日期
				hs.put("YSSL",String.valueOf(rs.getInt("YSSL")));//应收数量
				hs.put("SSSL",String.valueOf(rs.getInt("SSSL")));//实收数量
				hs.put("WSJG",String.valueOf(rs.getDouble("WSJG")));//未税价格
				hs.put("HSJG",String.valueOf(rs.getDouble("HSJG")));//含税价格
				hs.put("WSJE",String.valueOf(rs.getDouble("WSJE")));//未税金额
				hs.put("HSJE",String.valueOf(rs.getDouble("HSJE")));//含税金额
				hs.put("FZSX",StrUtil.returnStr(rs.getString("FZSX")));//辅助属性
				hs.put("SFLP",StrUtil.returnStr(rs.getString("SFLP")));//是否良品
				hs.put("DW",StrUtil.returnStr(rs.getString("DW")));//单位
				hs.put("CKBM",StrUtil.returnStr(rs.getString("CKBM")));//仓库编码
				hs.put("KFQBM",StrUtil.returnStr(rs.getString("KFQBM")));//区编码
				hs.put("KFDBM",StrUtil.returnStr(rs.getString("KFDBM")));//道编码
				hs.put("KFKWDM",StrUtil.returnStr(rs.getString("KFKWDM")));//库位代码
				hs.put("KWBH",StrUtil.returnStr(rs.getString("KWBH")));//货位编号
				hs.put("RKDH",StrUtil.returnStr(rs.getString("RKDH")));//入库单号
				rkdVector.add(hs);
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return rkdVector;
	}
	
}
