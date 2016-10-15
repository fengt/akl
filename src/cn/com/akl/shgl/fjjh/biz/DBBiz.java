package cn.com.akl.shgl.fjjh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DBBiz {

	/**
	 * 调拨单头数据插入
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertDBHead(Connection conn, int bindid, final int sub_bindid, final String uid, final String direction) throws SQLException{
		DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_FJJH_P, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				dbHead(conn, sub_bindid, rs, uid, direction);
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 调拨单头数据封装
	 * @param conn
	 * @param bindid
	 * @param rs
	 * @param uid
	 * @throws SQLException
	 */
	public void dbHead(Connection conn, int bindid, ResultSet rs, String uid, String direction) throws SQLException{
		Hashtable<String, String> head = new Hashtable<String, String>();
		String xmlb = rs.getString("XMLB");
		String fhckbm = rs.getString("FHCKBM");
		String jcckbm = rs.getString("JCCKBM");
		
		head.put("DBLX", FJJHCnt.dblx1);//调拨类型
		head.put("XMLX", xmlb);//项目类别
		
		if(direction.equals(FJJHCnt.direction0)){
			fillShipmentData(conn, head, fhckbm, true);//返货仓库信息追加
			fillShipmentData(conn, head, jcckbm, false);//检测仓库信息追加
		}else{
			fillShipmentData(conn, head, fhckbm, false);//返货仓库信息追加
			fillShipmentData(conn, head, jcckbm, true);//检测仓库信息追加
		}
		
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_P", head, bindid, uid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
					head.put("FHKFCKMC", kfckmc);//发货客服仓库名称
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
					head.put("SHKFCKMC", kfckmc);//收货客服仓库名称
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
