package cn.com.akl.shgl.qhbh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class DBBiz {

	/**
	 * 调拨单头信息封装
	 * @param conn
	 * @param bindid
	 * @param dbdh
	 * @param xmlb
	 * @param bhck
	 * @param kfzx
	 * @param db_uid
	 * @param fhr
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getHead(Connection conn, int bindid, String dbdh, String xmlb, String bhck, String kfzx, String db_uid, String fhr) throws SQLException{
		Hashtable<String, String> head = new Hashtable<String, String>();
		head.put("DBDH", dbdh);//调拨单号
		head.put("XMLX", xmlb);//项目类别
		head.put("DBLX", QHBHCnt.dblx);//调拨类型
		
		fillShipmentData(conn, head, bhck, true);//补货仓库信息追加
		fillShipmentData(conn, head, kfzx, false);//客服仓库信息追加
		
		return head;
	}
	
	/**
	 * 调拨单头中（收发信息）封装
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
	
	
	/**
	 * 获取各客服调拨单身数据(明细)
	 * @param conn
	 * @param bindid
	 * @param kfzx
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> getDetailBody(Connection conn, int bindid, String kfzx) throws SQLException{
		Hashtable<String, String> rec = null;
		Vector<Hashtable<String, String>> body = new Vector<Hashtable<String, String>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			ps = conn.prepareStatement(QHBHCnt.QUERY_DB_DETAIL);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, kfzx);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				rec.put("WLBH", rs.getString("SQCPWLBH"));
				rec.put("WLMC", rs.getString("SQCPZWMC"));
				rec.put("XH", rs.getString("SQCPPN"));
				rec.put("CPSX", rs.getString("SX"));
				rec.put("PCH", rs.getString("PCH"));
				rec.put("RKCKDM", rs.getString("JFKFBM"));//入库仓库代码
				rec.put("RKCKMC", rs.getString("JFKFMC"));//入库仓库名称
//				rec.put("CKCKDM", );//出库仓库代码
//				rec.put("CKCKMC", );//出库仓库名称
				rec.put("CKHWDM", rs.getString("HWDM"));//出库货位代码
				rec.put("CKSL", String.valueOf(rs.getInt("SQCPSL")));
				body.add(rec);
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return body;
	}
	
	
	/**
	 * 获取各客服调拨单身数据(汇总)
	 * @param conn
	 * @param bindid
	 * @param kfzx
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> getGatherBody(Connection conn, int bindid, String kfzx, String bhck) throws SQLException{
		String bhmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_KFCKMC, bhck));//补货仓库名称
		Hashtable<String, String> rec = null;
		Vector<Hashtable<String, String>> body = new Vector<Hashtable<String, String>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			ps = conn.prepareStatement(QHBHCnt.QUERY_DB_GATHER_NEW);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bhck, kfzx, bindid);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				rec.put("WLBH", rs.getString("SQCPWLBH"));//物料编号
				rec.put("WLMC", rs.getString("SQCPZWMC"));//物料名称
				rec.put("XH", rs.getString("SQCPPN"));//型号
				
				rec.put("GG", rs.getString("GG"));//规格
				rec.put("SJLH", rs.getString("SJLH"));//缩减料号
				rec.put("KCSL", rs.getString("KCKYZ"));//库存数量
				rec.put("CPLX", rs.getString("CPFL"));//产品类型
//				rec.put("JG", rs.getString(""));//价格
				
				rec.put("CPSX", rs.getString("PHSX"));//配货属性
				rec.put("RKCKDM", rs.getString("JFKFBM"));//人库仓库编码
				rec.put("RKCKMC", rs.getString("JFKFMC"));//入库仓库名称
				rec.put("CKCKDM", bhck);//出库仓库编码
				rec.put("CKCKMC", bhmc);//出库仓库名称
				rec.put("CKSL", String.valueOf(rs.getInt("CKSL")));
				body.add(rec);
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return body;
	}
	
	/**
	 * 流程启动
	 * @param conn
	 * @param bindid
	 * @param kfmc
	 * @param uid
	 * @param head
	 * @param body
	 * @throws AWSSDKException
	 */
	public int startWorkflow(Connection conn,String kfmc, String uid, Hashtable<String, String> head,
			Vector<Hashtable<String, String>> detailBody, Vector<Hashtable<String, String>> gatherBody) throws AWSSDKException{
		int sub_bindid = WorkflowInstanceAPI.getInstance().createProcessInstance(QHBHCnt.uuid, uid, kfmc + QHBHCnt.subTitle);
		WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, sub_bindid, 1, uid, kfmc + QHBHCnt.subTitle);
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_P", head, sub_bindid, uid);
//		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_S", detailBody, sub_bindid, uid);
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_HZ_S", gatherBody, sub_bindid, uid);
		return sub_bindid;
	}
	
	/**
	 * 账号格式转换
	 * @param fzr
	 * @return
	 */
	@Deprecated
	public String accountParse(String fzr){
		String uid;
		String regex = "([a-z]{1,}|\\d{6})<.+>";
		Pattern p = Pattern.compile(regex);
		Matcher mt = p.matcher(fzr);
		if("".equals(fzr) || !mt.matches()){
			throw new RuntimeException("客服中心负责人账号格式不符，请检查！");
		}else{
			String[] str = fzr.split("<");
			uid = str[0];
		}
		return uid;
	}
	
}
