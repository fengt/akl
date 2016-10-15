package cn.com.akl.shgl.qhbh.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class LockBiz {

	/**
	 * 验证库存是否充足.
	 */
	public static ResultPaserAbs checkInvetoryPaser() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet rs) throws SQLException {
				String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//项目类别
				String wlbh = StrUtil.returnStr(rs.getString("SQCPWLBH"));//物料编号
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
				String sx = StrUtil.returnStr(rs.getString("PHSX"));//配货属性
				
				Integer sdsl = DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_SK_SUM, xmlb, wlbh, pch, hwdm, sx);//锁库数量
				Integer kwsl = DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_KCMX_SUM, xmlb, wlbh, pch, hwdm, sx);//库位数量
				int sl = rs.getInt("SL");
				if (kwsl - sdsl < sl) {
					throw new RuntimeException("物料号:" + rs.getString("WLBH") + "， 型号:" + rs.getString("XH") + "， 批次号:" + rs.getString("PCH")
							+ "，数量不足，可能库存已被锁定！");
				}
				return true;
			}
		};
	}
	
	
	/**
	 * 插入锁库记录..
	 */
	public static ResultPaserAbs insertLockPaser(final int bindid, final String uid, final String dbdh) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet rs) throws SQLException {
				try {
					insertSK(conn, bindid, uid, dbdh, rs.getString("XMLB"), rs.getString("SQCPWLBH"), rs.getString("SQCPPN"), rs.getString("PCH"), rs.getString("JFKFBM"), rs.getString("HWDM"), rs.getString("PHSX"), rs.getInt("SL"));
				} catch (AWSSDKException e) {
					throw new RuntimeException(e);
				}
				return true;
			}
		};
	}
	
	
	/**
	 * 插入锁库记录.
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ddh
	 * @param pch
	 * @param wlbh
	 * @param ckdm
	 * @param ddsl
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public static void insertSK(Connection conn, int bindid, String uid, String ydh, String xmlb, String wlbh, String xh, String pch, String ckdm, String hwdm, String sx, int ddsl) 
			throws SQLException, AWSSDKException {
		// 插入记录
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		hashtable.put("YDH", ydh);
		hashtable.put("XMLB", xmlb);
		hashtable.put("WLBH", wlbh);
		hashtable.put("XH", xh);
		hashtable.put("PCH", pch);
		hashtable.put("CKDM", ckdm);
		hashtable.put("HWDM", hwdm);
		hashtable.put("SX", sx);
		hashtable.put("SDSL", String.valueOf(ddsl));
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_KCSK", hashtable, bindid, uid);
	}
	
	public static int nullParse(Integer i){
		return i == null ? 0 : i.intValue();
	}
}
