package cn.com.akl.shgl.fjjh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class FJJHBiz {

	//查询复检计划子表(客服中心过滤)
	private static final String QUERY_FJJH = "SELECT * FROM BO_AKL_FJJH_S WHERE BINDID=? AND KFBM=?";
	
	//查询确认的复检计划子表
	private static final String QUERY_FJ = "SELECT WLBH,WLMC,XH,SX,SUM(FJJHSL)FJJHSL FROM BO_AKL_FJJH_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH,SX";
	
	//查询汇总的复检流程子表
	private static final String QUERY_FJLC = "SELECT CPLH,CPMC,CPSX,SN,COUNT(1)SL FROM BO_AKL_FJ_S WHERE BINDID=? GROUP BY CPLH,CPMC,CPSX,SN";
	
	/**
	 * 筛选被选择的客服中心的物料信息
	 * @param conn
	 * @param kfckbm
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public Vector<Hashtable<String, String>> queryByKfzx(Connection conn, String kfckbm, int bindid) throws SQLException{
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			String[] ckbm = kfckbm.split("\\|");//客服仓库编码
			for (int i = 0; i < ckbm.length; i++) {
				String kfzx = ckbm[i];
				ps = conn.prepareStatement(QUERY_FJJH);
				rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, kfzx);
				while(rs.next()){
					rec = new Hashtable<String, String>();
					String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
					String dbdh = StrUtil.returnStr(rs.getString("DBDH"));//调拨单号
					String sx = StrUtil.returnStr(rs.getString("SX"));//属性
					int fjsl = rs.getInt("FJSL");//返京数量
					
					rec.put("WLBH", wlbh);
					rec.put("DBDH", dbdh);
					rec.put("SX", sx);
					rec.put("FJSL", String.valueOf(fjsl));
					vector.add(rec);
				}
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
	/**
	 * 复检流程子表汇总
	 * 注：该方法暂时停用，若以后检测库-->返货库的调拨数据从复检子流程抓取时，再启用该方法
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> queryByCplh(Connection conn, int bindid) throws SQLException{
		String jcck = DAOUtil.getStringOrNull(conn, FJJHCnt.QUERY_FJJH_P_JCCKBM, bindid);//检测库
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(QUERY_FJLC);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String cplh = StrUtil.returnStr(rs.getString("CPLH"));//产品料号
				String cpmc = StrUtil.returnStr(rs.getString("CPMC"));//产品名称
				String cpsx = StrUtil.returnStr(rs.getString("CPSX"));//产品属性
				String pn = StrUtil.returnStr(rs.getString("PN"));//型号
				int sl = rs.getInt("SL");//数量
				
				rec.put("WLBH", cplh);//物料编号
				rec.put("WLMC", cpmc);//物料名称
				rec.put("XH", pn);//型号
				rec.put("CPSX", cpsx);//属性
				rec.put("SJFHSL", String.valueOf(sl));//实际返货数量
				rec.put("CKSL", String.valueOf(sl));//入库数量
				rec.put("CKCKDM", jcck);//出库仓库编码
				vector.add(rec);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
	/**
	 * 复检计划子表汇总
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> queryByWlbh(Connection conn, int bindid, String ckdm) throws SQLException{
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(QUERY_FJ);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
				String pn = StrUtil.returnStr(rs.getString("XH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				int fjjhsl = rs.getInt("FJJHSL");
				
				rec.put("WLBH", wlbh);//物料编号
				rec.put("WLMC", wlmc);//物料名称
				rec.put("XH", pn);//PN
				rec.put("CPSX", sx);//属性
				rec.put("SJFHSL", String.valueOf(fjjhsl));//实际返货数量
				rec.put("CKSL", String.valueOf(fjjhsl));//入库数量
				rec.put("CKCKDM", ckdm);//出库仓库编码
				vector.add(rec);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
}




