package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
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
	public static void insertShipments(Connection conn, final int bindid, final String uid)throws SQLException{
		final String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKDM, bindid));//客服仓库编码
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//项目类别
		
		/**插入主表记录*/
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SX_P, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> head = new Hashtable<String, String>();
				String lxr = StrUtil.returnStr(rs.getString("KHMC"));
				String sjh = StrUtil.returnStr(rs.getString("SJH"));
				String dhqh = StrUtil.returnStr(rs.getString("DHQH"));
				String dh = StrUtil.returnStr(rs.getString("DH"));
				String email = StrUtil.returnStr(rs.getString("EMAIL"));
				String yb = StrUtil.returnStr(rs.getString("YB"));
				String gj = StrUtil.returnStr(rs.getString("GJ"));
				String s = StrUtil.returnStr(rs.getString("S"));
				String shi = StrUtil.returnStr(rs.getString("SHI"));
				String qx = StrUtil.returnStr(rs.getString("QX"));
				String dz = StrUtil.returnStr(rs.getString("XXDZ"));
				
				
				head.put("DH", rs.getString("SXDH"));//单号
				head.put("XMLB", xmlb);//项目类别
				head.put("DJLB", SXCnt.djlx);//单据类别
				head.put("WLZT", SXCnt.wlzt1);//物料状态
				head.put("FHFLX", DfhConstant.SFHFLX_KH);//发货方类型
				head.put("SHFLX", DfhConstant.SFHFLX_KFCK);//收货方类型
				
				//收货仓库信息
				fillShipmentData(conn, head, ckdm);
				
				//发货方信息
//				head.put("FHKFCKBM", );//发货客服仓库编码
//				head.put("FHKFCKMC", );//发货客服仓库名称
				head.put("FHR", lxr);//发货人
				head.put("FHF", lxr);//客户名称
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
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", head, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);

		
		/**插入子表记录*/
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
				String wlmc = StrUtil.returnStr(rs.getString("WLMC"));//物料名称
				String xh = StrUtil.returnStr(rs.getString("XH"));//型号
//				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
				int sl = rs.getInt("SL");//数量
//				String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, hwdm, sx, FJFJCnt.zt4));//获取该物料库存批次号
				
				rec.put("WLBH", wlbh);
				rec.put("WLMC", wlmc);
				rec.put("XH", xh);
				rec.put("SX", SXCnt.sx2);
				rec.put("PCH", pch);
				rec.put("SL", String.valueOf(sl));
				rec.put("QSSL", String.valueOf(sl));
				rec.put("HWDM", hwdm);
				rec.put("CKDM", ckdm);
				
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
	
	public static Hashtable<String, String> fillShipmentData(Connection conn, Hashtable<String, String> head, String ckdm) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(FJJHCnt.QUERY_KFCK);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, ckdm);
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
				
				head.put("SHKFCKBM", ckdm);//收货客服仓库编码
				head.put("SHKFCKMC", kfckmc);//收货客服仓库名称
				head.put("SHF", kfckmc);//收货客服仓库名称
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
		} finally {
			DBSql.close(ps, rs);
		}
		return head;
	}
}
