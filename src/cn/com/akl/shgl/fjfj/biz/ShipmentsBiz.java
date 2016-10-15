package cn.com.akl.shgl.fjfj.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class ShipmentsBiz {

	/**
	 * 插入待发货记录
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException	 */
	public void insertShipments(Connection conn, final int bindid, final String uid, final String jlbz)throws SQLException{
		insertHead(conn, bindid, uid, jlbz);//插入主表记录
		insertBody(conn, bindid, uid);//插入子表记录
	}
	
	/**
	 * 待发货主表插入
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param jlbz
	 * @throws SQLException
	 */
	public void insertHead(Connection conn, final int bindid, final String uid, final String jlbz)throws SQLException{
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ_P, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> head = new Hashtable<String, String>();
				head.put("DH", rs.getString("HPJCDH"));//单号
				head.put("XMLB", rs.getString("SSXM"));//项目类别
				head.put("DJLB", FJFJCnt.djlx);//单据类别
				head.put("WLZT", FJFJCnt.wlzt1);//物料状态
				
				String kfck = StrUtil.returnStr(rs.getString("JCKF"));//客服仓库
				String jcck = StrUtil.returnStr(rs.getString("SRKF"));//检测仓库
				
				if(jlbz.equals(FJFJCnt.jlbz0)){
					head.put("JLBZ", FJFJCnt.jlbz0);//记录标识
					fillShipmentData(conn, head, kfck, true);//客服仓库信息追加
					fillShipmentData(conn, head, jcck, false);//检测仓库信息追加
				}else{
					head.put("JLBZ", FJFJCnt.jlbz1);//记录标识
					fillShipmentData(conn, head, kfck, false);//客服仓库信息追加
					fillShipmentData(conn, head, jcck, true);//检测仓库信息追加
				}
				
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", head, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 待发货子表插入
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertBody(Connection conn, final int bindid, final String uid)throws SQLException{
		final String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_JCKF, bindid));//寄出库房
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//物料编号
				String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));//物料名称
				String xh = StrUtil.returnStr(rs.getString("PN"));//型号
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
				int sl = rs.getInt("SL");//数量
//				String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, hwdm, sx, FJFJCnt.zt4));//获取该物料库存批次号
				
				rec.put("WLBH", wlbh);
				rec.put("WLMC", wlmc);
				rec.put("XH", xh);
				rec.put("SX", sx);
				rec.put("PCH", pch);
				rec.put("SL", String.valueOf(sl));
				rec.put("QSSL", String.valueOf(sl));
				rec.put("HWDM", hwdm);
				rec.put("CKDM", ckdm);
				rec.put("JLBZ", FJFJCnt.jlbz0);//记录标识
				
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", rec, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 返货库和检测库信息封装
	 * @param conn
	 * @param head
	 * @param kfckbm
	 * @param direction
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> fillShipmentData(Connection conn, Hashtable<String, String> head, String kfckbm, Boolean flag) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(FJJHCnt.QUERY_KFCK);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, kfckbm);
			while(rs.next()){
				String kfckmc = StrUtil.returnStr(rs.getString("KFCKMC"));//客服仓库名称
				String lxr = StrUtil.returnStr(rs.getString("LXR"));//联系人
				String sjh = StrUtil.returnStr(rs.getString("SJH"));//手机号
				String dhqh = StrUtil.returnStr(rs.getString("DHQH"));//电话区号
				String dh = StrUtil.returnStr(rs.getString("DH"));//电话
				String email = StrUtil.returnStr(rs.getString("EMAIL"));//Email
				String yb = StrUtil.returnStr(rs.getString("YB"));//邮编
				String gj = StrUtil.returnStr(rs.getString("GJ"));//国家
				String s = StrUtil.returnStr(rs.getString("S"));//省
				String shi = StrUtil.returnStr(rs.getString("SHI"));//市
				String qx = StrUtil.returnStr(rs.getString("QX"));//区/县
				String dz = StrUtil.returnStr(rs.getString("DZ"));//地址
				if(flag){
					head.put("FHKFCKBM", kfckbm);//发货客服仓库编码
					head.put("FHF", kfckmc);//发货方
					head.put("FHR", lxr);//发货人
					head.put("FHRSJ", sjh);//发货人手机
					head.put("FHRDHQH", dhqh);//发货人电话区号
					head.put("FHRDH", dh);//发货人电话
					head.put("FHRYX", email);//发货人邮箱
					head.put("FHYB", yb);//发货邮编
					head.put("FHGJ", gj);//发货国家
					head.put("FHS", s);//发货省
					head.put("FHSHI", shi);//发货市
					head.put("FHQX", qx);//发货区（县）
					head.put("FHDZ", dz);//发货地址
				}else{
					head.put("SHKFCKBM", kfckbm);//收货客服仓库编码
					head.put("SHF", kfckmc);//收货方
					head.put("SHR", lxr);//收货人
					head.put("SHRSJ", sjh);//收货人手机
					head.put("SHRDHQH", dhqh);//收货人电话区号
					head.put("SHRDH", dh);//收货人电话
					head.put("SHRYX", email);//收货人邮箱
					head.put("SHYB", yb);//收货邮编
					head.put("SHGJ", gj);//收货国家
					head.put("SHS", s);//收货省
					head.put("SHSHI", shi);//收货市
					head.put("SHQX", qx);//收货区（县）
					head.put("SHDZ", dz);//收货地址
				}
			}
		} finally {
			DBSql.close(ps, rs);
		}
		return head;
	}
}
