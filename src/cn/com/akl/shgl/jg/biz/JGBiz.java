package cn.com.akl.shgl.jg.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.kc.biz.RepositoryConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class JGBiz {

	private RepositoryBiz repositoryBiz = new RepositoryBiz();

	public void removeLock(Connection conn, int bindid) {
		repositoryBiz.removeLock(conn, bindid);
	}

	/**
	 * 待加工产品插入锁库.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param xmlb
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertXhLock(Connection conn, int bindid, String uid, String xmlb) throws SQLException, AWSSDKException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_DJG);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String pch = reset.getString("PCH");
				String ckdm = reset.getString("CKDM");
				String hwdm = reset.getString("HWDM");
				String sx = reset.getString("CPSX");
				int sl = reset.getInt("JGSL");
				int hasNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);
				if (hasNum >= sl) {
					repositoryBiz.insertLock(conn, bindid, uid, xmlb, wlbh, xh, pch, ckdm, hwdm, sx, sl);
				} else {
					throw new RuntimeException("型号：" + xh + "库存物料不足!");
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 配件插入锁库.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param xmlb
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertPjLock(Connection conn, int bindid, String uid, String xmlb) throws SQLException, AWSSDKException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_PJ);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String pch = reset.getString("PCH");
				String ckdm = reset.getString("CKDM");
				String hwdm = reset.getString("HWDM");
				String sx = reset.getString("CPSX");
				int sl = reset.getInt("SJSL");
				int hasNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);
				if (hasNum > sl) {
					repositoryBiz.insertLock(conn, bindid, uid, xmlb, wlbh, xh, pch, ckdm, hwdm, sx, sl);
				} else {
					throw new RuntimeException("型号：" + xh + "库存物料不足!");
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 插入加工完成汇总子表.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param xmlb
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertHz(Connection conn, int bindid, String uid, String xmlb) throws SQLException, AWSSDKException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		Hashtable<String, String> rec = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_JGGZ_HZ);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(reset.getString("WLBH"));
				String wlmc = StrUtil.returnStr(reset.getString("WLMC"));
				String xh = StrUtil.returnStr(reset.getString("XH"));
				String gg = StrUtil.returnStr(reset.getString("GG"));
				String sx = StrUtil.returnStr(reset.getString("CPSX"));
				int sl = reset.getInt("SL");
				
				rec.put("WLBH", wlbh);
				rec.put("WLMC", wlmc);
				rec.put("XH", xh);
				rec.put("GG", gg);
				rec.put("CPSX", sx);
				rec.put("SL", String.valueOf(sl));
				
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_JGWCHZ_S", rec, bindid, uid);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}
	
	
	/**
	 * 处理消耗.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void dealXh(Connection conn, int bindid, String option, String jglx) throws SQLException {
		String xmlb = DAOUtil.getStringOrNull(conn, JGConstant.QUERY_XMLB, bindid);
		String sxdh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, JGConstant.QUERY_SXDH, bindid));

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_DJG);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String pch = reset.getString("PCH");
				String ckdm = reset.getString("CKDM");
				String hwdm = reset.getString("HWDM");
				String sx = reset.getString("CPSX");
				String tph = reset.getString("TPH");
//				int sl = reset.getInt("SL");
				int jgsl = reset.getInt("JGSL");
				
				if(option.equals(JGConstant.subtract)){//非正常加工
					jgsl = -jgsl;
					if(JGConstant.JGLX_FZCJG.equals(jglx)){
						int num = DAOUtil.executeUpdate(conn, JGConstant.UPDATE_SX_SFYJG, sxdh, wlbh, pch, hwdm, sx, tph);//更新送修单是否已加工
						if(num != 1) throw new RuntimeException("送修单（是否已加工）更新失败！");
					}
				}
				int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlb, wlbh, pch, hwdm, sx, RepositoryConstant.WL_ZT_ZK, jgsl);
				if (updateCount != 1) {
					throw new RuntimeException("型号：" + xh + "库存更新失败!");
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 处理完成的物料，插入库存.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void dealWc(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
		String xmlb = DAOUtil.getStringOrNull(conn, JGConstant.QUERY_XMLB, bindid);
		String pch = RepositoryBiz.getPCH(conn, xmlb);

		DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SH_JGWC_S SET PCH=? WHERE BINDID=?", pch, bindid);

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_JGWC);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String ckdm = reset.getString("CKDM");
				String ckmc = reset.getString("CKMC");
				String hwdm = reset.getString("HWDM");
				String sx = reset.getString("CPSX");
				int sl = reset.getInt("SL");

				int queryCount = repositoryBiz.queryRecordCount(conn, xmlb, wlbh, pch, hwdm, sx, RepositoryConstant.WL_ZT_ZK);
				if (queryCount == 0) {
					Hashtable<String, String> hashtable = new Hashtable<String, String>();
					mapMaterialInfo(reset, xmlb, hashtable);
					hashtable.put("PCH", pch);
					hashtable.put("CKDM", ckdm);
					hashtable.put("CKMC", ckmc);
					hashtable.put("HWDM", hwdm);
					hashtable.put("KWSL", String.valueOf(sl));
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", hashtable, bindid, uid);
				} else {
					int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlb, wlbh, pch, hwdm, sx, RepositoryConstant.WL_ZT_ZK, sl);
					if (updateCount != 1) {
						throw new RuntimeException("型号：" + xh + "库存更新失败!");
					}
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * 更新配件库存.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void dealPj(Connection conn, int bindid) throws SQLException {
		String xmlb = DAOUtil.getStringOrNull(conn, JGConstant.QUERY_XMLB, bindid);

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_PJ);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String pch = reset.getString("PCH");
				String ckdm = reset.getString("CKDM");
				String hwdm = reset.getString("HWDM");
				String sx = reset.getString("CPSX");
				int sl = reset.getInt("SJSL");

				int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlb, wlbh, pch, hwdm, sx, RepositoryConstant.WL_ZT_ZK, -sl);
				if (updateCount != 1) {
					throw new RuntimeException("型号：" + xh + "库存更新失败!");
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}
	
	/**
	 * 非正常加工取送修数据
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param xmlb
	 * @param wlbh
	 * @param sxdh
	 * @param ckdm
	 * @param sx
	 * @param sl
	 * @param hashtable
	 * @return
	 * @throws SQLException
	 */
	public boolean autoFetchNoLock(Connection conn, int bindid, String uid, String xmlb, String wlbh, String sxdh,
			String ckdm, String sx, int sl, Hashtable<String, String > hashtable) throws SQLException{
		PreparedStatement ps = null;
		ResultSet reset = null;
		int total = sl;
		
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_JGLX_FZCJG);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, sxdh, RepositoryConstant.WL_ZT_ZK, wlbh, sx);
			while (total > 0 && reset.next()) {
				String pch = reset.getString("PCH");
				String hwdm = reset.getString("HWDM");
				String tph = reset.getString("TPH");
				int hasSl = reset.getInt("SL");
				
				/*Integer lockNum = DAOUtil.getIntOrNull(conn, RepositoryConstant.QUERY_MATERIAL_REMAINING_NUMBER_LOCK, xmlb, wlbh, pch, hwdm, sx);
				if(lockNum.equals(null))lockNum = 0;
				else lockNum.intValue();
				if (hasSl > lockNum) {
					int useSl = hasSl - lockNum;
					if (useSl < total) {
						continue;
					}
				}*/
				
				if(total > hasSl){
					throw new RuntimeException("非正常加工的加工数量只能为1！");
				}else{
					hashtable.put("KWSL", String.valueOf(hasSl));
					hashtable.put("PCH", PrintUtil.parseNull(pch));
					hashtable.put("HWDM", PrintUtil.parseNull(hwdm));
					hashtable.put("TPH", PrintUtil.parseNull(tph));
				}
				total = 0;
				break;
			}
			return total == 0;
		} finally {
			DBSql.close(ps, reset);
		}
	}
	

	/**
	 * 填充物料信息.
	 * 
	 * @param reset
	 * @param hashtable
	 * @throws SQLException
	 */
	public void mapMaterialInfo(ResultSet reset, String xmlx, Hashtable<String, String> hashtable) throws SQLException {
		hashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
		hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
		hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
		hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
		// hashtable.put("PCH", PrintUtil.parseNull(reset.getString("PCH")));
		hashtable.put("CKDM", "");
		hashtable.put("DJ", PrintUtil.parseNull(reset.getString("ZDCB")));
		hashtable.put("CKMC", "");
		hashtable.put("QDM", "");
		hashtable.put("DDM", "");
		hashtable.put("KWDM", "");
		hashtable.put("HWDM", PrintUtil.parseNull(reset.getString("HWDM")));
		hashtable.put("KWSL", PrintUtil.parseNull(reset.getString("SL")));
		hashtable.put("ZJM", "");
		hashtable.put("BZQ", "");
		hashtable.put("FZSX", "");
		hashtable.put("SCRQ", "");
		hashtable.put("JLDW", "");
		hashtable.put("SX", PrintUtil.parseNull(reset.getString("CPSX")));
		hashtable.put("XMLB", xmlx);
		hashtable.put("ZT", RepositoryConstant.WL_ZT_ZK);
	}

}
