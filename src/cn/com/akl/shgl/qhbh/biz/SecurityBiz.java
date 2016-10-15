package cn.com.akl.shgl.qhbh.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.jf.biz.ReplacementRuleBiz;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class SecurityBiz {

	/**
	 * 安全库存自动配货及锁库
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void securityAllocationAndLockMaterial(Connection conn, final int bindid, final String uid) throws SQLException{
		final String ydh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_QHBHDH, bindid));//补货单号
		final String bhck = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_FHCKBM, bindid));//补货仓库
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_XMLB, bindid));//项目类别
		
		final List<String> list = new ArrayList<String>();
		DAOUtil.executeQueryForParser(conn, QHBHCnt.QUERY_S,
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));//申请产品物料编号
				String xh = StrUtil.returnStr(rs.getString("YCPPN"));//申请产品PN
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String original_sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//是否接受替换
				String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//交付库房编码
				String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//送修单号
				String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//送修产品行号
				String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//交付产品行号
				int sl = rs.getInt("SL");//数量
				int id = rs.getInt("ID");//ID
				
				String sfbgph = StrUtil.returnStr(rs.getString("SFBGPH"));//是否变更配货
				String bgwlbh = StrUtil.returnStr(rs.getString("BGWLBH"));//变更物料编号
				String bgpn = StrUtil.returnStr(rs.getString("BGPN"));//变更PN
				String bgsx = StrUtil.returnStr(rs.getString("BGSX"));//变更属性
				
				String pn8L = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN8L, wlbh));//型号8L
				String pn9L = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN9L, wlbh));//型号9L
				String pn9L_wlbh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN9L_WLBH, pn9L));//型号9L作为8L的物料编号
				
				StringBuffer failMessage = new StringBuffer(QHBHCnt.failMessage0);//未成功配货原因
				
				/**
				 * 一、变更配货
				 * 是否变更配货为是时，根据变更料号进行配货，如果配货失败，则该配货结束
				 */
				boolean flag = false;
				if(QHBHCnt.is.equals(sfbgph)){
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, bgwlbh, bgpn, bgsx, bhck, sl, id);
					if(!flag) throw new RuntimeException("变更配货失败，请重新选择变更物料！");
				}
				
				/**
				 * 二、正常配货
				 * 1、优先配该物料的库存
				 * 2、如果库存不足，则根据是否接受替换规则进行配货
				 * 3、如果库存再次不足，闪迪项目则配货失败、罗技项目时（魔声类似）：
				 * 		a、RMA新品，则根据（9L型号+DOA新品）配货
				 * 		b、若a库存不足，则根据是否替换规则进行（9L型号+DOA新品）配货
				 * 4、如果库存不足，则配货失败
				 * 
				 * finalNum，根据补货类型确定的最终配货数量
				 * flag=false，配货失败
				 */
				
				int finalNum = getFinalNum(conn, bindid, xmlb, wlbh, sx, bhck, sl).intValue();//最终配货量
				if(!flag){
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, wlbh, pn8L, sx, bhck, finalNum, id);
				}
				
				if(!flag && sfjsth.equals(QHBHCnt.is)){//根据替换规则进行配货
					flag = setReplaceMaterialHander(conn, bindid, uid, ydh, rs, xmlb, wlbh, pn8L, xh, sx, bhck, finalNum, id, failMessage);
				}
				
				/**
				 * 罗技：RMA-->DOA
				 */
				if(!flag && QHBHCnt.xmlb0.equals(xmlb) && QHBHCnt.sx0.equals(sx) && !"".equals(pn9L)){
					sx = QHBHCnt.sx1;
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, pn9L_wlbh, pn9L, sx, bhck, finalNum, id);
					if(!flag && QHBHCnt.is.equals(sfjsth)){
						flag = setReplaceMaterialHander(conn, bindid, uid, ydh, rs, xmlb, pn9L_wlbh, pn9L, xh, sx, bhck, finalNum, id, failMessage);
					}
				}
				
				/**
				 * 魔声：FGER-->FG
				 */
				if(!flag && QHBHCnt.xmlb1.equals(xmlb) && QHBHCnt.sx2.equals(sx) && !"".equals(pn9L)){
					sx = QHBHCnt.sx3;
					flag = setMaterialHander(conn, bindid, uid, rs, ydh, xmlb, pn9L_wlbh, pn9L, sx, bhck, finalNum, id);
					if(!flag && QHBHCnt.is.equals(sfjsth)){
						flag = setReplaceMaterialHander(conn, bindid, uid, ydh, rs, xmlb, pn9L_wlbh, pn9L, xh, sx, bhck, finalNum, id, failMessage);
					}
				}
				
				
				/**
				 * 三、配货结束后操作
				 * 配货成功：更新缺货记录状态
				 * 配货失败：更新失败原因，过滤失败记录
				 */
				if(flag && finalNum != 0){
					int count = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_ZT, QHBHCnt.qhzt0, xmlb, wlbh, original_sx, jfkfbm, sfjsth, QHBHCnt.qhzt2, sxdh, sxcphh, jfcphh);//更新缺货记录的状态
					if(count != 1) throw new RuntimeException("缺货记录状态更新失败！");
				}else{
					list.add(xh);
					DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_SCWCGPHYY, failMessage.toString(), xmlb, wlbh, original_sx, jfkfbm, sfjsth, QHBHCnt.qhzt2);//更新未成功配货原因
					DAOUtil.executeUpdate(conn, QHBHCnt.DELETE_QHBH_S, id);//删除未成功配货记录
//					throw new RuntimeException("该产品【"+xh+"】无库存，暂无法配货！");
				}
				return true;
			}
			public void destory(Connection conn) throws SQLException{
				Integer count = DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_S_count, bindid);
				if(count == null || count == 0){
					throw new RuntimeException("该单没有可配货成功的物料，暂无法办理！");
				}
				if(list.size() > 0)
					MessageQueue.getInstance().putMessage(uid, "型号如下："+list.toString()+"未配货成功，已被系统自动过滤，特此提醒！");
			}
		}, bindid);
	}
	
	/**
	 * 根据补货类型判断补货数量
	 * @param conn
	 * @param bindid
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param bhck
	 * @param sl
	 * @return
	 * @throws SQLException
	 */
	public BigDecimal getFinalNum(Connection conn, int bindid, String xmlb, String wlbh, String sx, String bhck, int sl) throws SQLException{
		String bhlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_BHLX, bindid));//补货类型
		int kczl = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_Sum, xmlb, wlbh, sx, bhck));//补货库库存总量
		int lockAllNum = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_SK_All_SUM, xmlb, wlbh, bhck, sx));//锁库总量
		int kcxx = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_LimtInventory, xmlb, bhck, wlbh, sx));//补货库库存最小量
		int allShortage = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_All_Shortage, bindid, xmlb, wlbh, sx));//该物料总缺货量
		
		BigDecimal bd1 = new BigDecimal(sl);
		BigDecimal bd2 = new BigDecimal(allShortage);
		BigDecimal finalNum;
		if(QHBHCnt.bhlx0.equals(bhlx)){
			finalNum = bd1;
		}else{
			int kyz = kczl - lockAllNum - kcxx;//库存可用值 = 库存总量 - 锁库总量 - 库存下限
			BigDecimal bd3 = new BigDecimal(kyz);
			if(allShortage > kyz && kyz > 0){//库存不足时，按比例分配给各客服
				finalNum = bd1.divide(bd2, 2, BigDecimal.ROUND_HALF_EVEN).multiply(bd3);//实际配货数量 = 缺货数量 / 总缺货量 * 库存可用值
			}else{//库存足时，正常配货
				finalNum = bd1;
			}
		}
		return finalNum;
	}
	
	
	/**
	 * 根据替换规则配货及锁库
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param sqcpsl
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public boolean setReplaceMaterialHander(Connection conn, int bindid, String uid, String ydh, ResultSet rest,
			String xmlb, String wlbh, String pn, String xh, String sx, String ckdm, int sl, int id, StringBuffer failMessage)
			throws SQLException{
		boolean mark = false;
		ReplacementRuleBiz ruleBiz = new ReplacementRuleBiz();
		List<String> replaceWlbhList = ruleBiz.replaceMaterial(conn, xmlb, wlbh, sx);

		if (replaceWlbhList.size() != 0) {
			// 拼接待处理物料.
			StringBuilder replaceWlbhSb = new StringBuilder(50);
			replaceWlbhSb.append(replaceWlbhList.get(0));
			for (int i = 1; i < replaceWlbhList.size(); i++) {
				replaceWlbhSb.append(",");
				replaceWlbhSb.append(replaceWlbhList.get(i));
			}
		} else {
//			throw new RuntimeException("该型号【"+xh+"】没有可替换信息！");
			failMessage = failMessage.replace(0, failMessage.length(), QHBHCnt.failMessage1);
			return mark;
		}

		// 找到有库存的物料.
		for (String reWlbh : replaceWlbhList) {
			boolean flag = setMaterialHander(conn, bindid, uid, rest, ydh, xmlb, reWlbh, pn, sx, ckdm, sl, id);//如果flag=false，配货失败
			if(flag){
				mark = true;
			}else{
				failMessage = failMessage.replace(0, failMessage.length(), QHBHCnt.failMessage2);
			}
		}
		return mark;
	}
	
	
	/**
	 * 无替换规则配货及锁库
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param sqcpsl
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public boolean setMaterialHander(Connection conn, int bindid, String uid, ResultSet rest, String ydh, 
			String xmlb, String wlbh, String pn, String sx, String ckdm, int sl, int id) throws SQLException{
		String wlmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_WLMC, wlbh));//物料名称
		String xh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN8L, wlbh));//LPN8
		PreparedStatement ps = null;
		ResultSet rs = null;
		int total = sl;
		int count = 0;
		try {
			ps = conn.prepareStatement(QHBHCnt.QUERY_KCMX_PCHAndHWDM);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, sx, ckdm);
			while(total > 0 && rs.next()){
				String pch = rs.getString("PCH");
				String hwdm = rs.getString("HWDM");
				int kwsl = rs.getInt("KWSL");
				int lockNum = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_SK_SUM, xmlb, wlbh, pch, hwdm, sx));//锁库数量
				if(kwsl > lockNum){
					int useNum = kwsl - lockNum;
					if(total > useNum){
						allocation(conn, bindid, uid, ydh, rest, xmlb, wlbh, wlmc, xh, pch, ckdm, hwdm, sx, useNum, id, count);
						total -= useNum;
					}else{
						allocation(conn, bindid, uid, ydh, rest, xmlb, wlbh, wlmc, xh, pch, ckdm, hwdm, sx, total, id, count);
						total = 0;
					}
					
				}
				count ++;
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return total == 0;
	}
	
	/**
	 * 根据库存分配实际补货数量
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param rest
	 * @param xmlb
	 * @param wlbh
	 * @param wlmc
	 * @param pn
	 * @param pch
	 * @param hwdm
	 * @param sx
	 * @param sl
	 * @param id
	 * @param count
	 * @throws SQLException
	 */
	public void allocation(Connection conn, int bindid, String uid, String ydh, ResultSet rest,
			String xmlb, String wlbh, String wlmc, String pn, String pch, String ckdm, String hwdm, String sx, int sl, int id, int count) throws SQLException{
		
		/**
		 * 1、更新批次和货位。
		 * count=0是第一次配货：a.更新货位批次;b.锁库
		 * count!=0时：a.复制一条新记录;b.锁库
		 */
		if(count == 0){
			int updateCount = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_PCAndHWDM, wlbh, pn, wlmc, sl, sx, pch, hwdm, QHBHCnt.qhzt0, id);
			if(updateCount != 1) throw new RuntimeException("批次或货位更新失败！");
		}else{
			copyRecord(conn, bindid, uid, rest, pch, hwdm, sl, sx);
		}
		
		/**
		 * 2、插入或更新锁库
		 */
		int isLockExsit = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.isLockExsit, ydh, xmlb, wlbh, pch, hwdm, sx));
		if(isLockExsit >= 1){
			int n = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_LockNum, sl, ydh, xmlb, wlbh, pch, hwdm, sx);
			if(n != 1) throw new RuntimeException("锁库插入更新失败！");
		}else{
			try {
				LockBiz.insertSK(conn, bindid, uid, ydh, xmlb, wlbh, pn, pch, ckdm, hwdm, sx, sl);
			} catch (AWSSDKException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 复制拆分的补货记录
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param rs
	 * @param pch
	 * @param hwdm
	 * @param sl
	 * @throws SQLException
	 */
	public void copyRecord(Connection conn, int bindid, String uid, ResultSet rs, String pch, String hwdm, int sl, String phsx) throws SQLException{
		Hashtable<String, String> body = new Hashtable<String, String>();
		String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//送修单号
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));//申请时间
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));//申请理由
		String cljg = StrUtil.returnStr(rs.getString("CLJG"));//处理结果
		String sqcpsl = StrUtil.returnStr(rs.getString("SL"));//申请产品数量
		String sx = StrUtil.returnStr(rs.getString("SX"));//属性
		String zt = StrUtil.returnStr(rs.getString("ZT"));//状态
		String bz = StrUtil.returnStr(rs.getString("BZ"));//备注
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//项目类别
		String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));//物料编号
		String wlmc = StrUtil.returnStr(rs.getString("YCPZWMC"));//物料名称
		String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//交付库房编码
		String jfkfmc = StrUtil.returnStr(rs.getString("JFKFMC"));//交付库房名称
		String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//送修产品行号
		String pn = StrUtil.returnStr(rs.getString("YCPPN"));//PN
		String yxj = StrUtil.returnStr(rs.getString("YXJ"));//优先级
		String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//是否接受替换
		String qhfs = StrUtil.returnStr(rs.getString("PHFS"));//缺货方式
		
		String phcpwlbh = StrUtil.returnStr(rs.getString("SQCPWLBH"));//配货产品物料编号
		String phcppn = StrUtil.returnStr(rs.getString("SQCPPN"));//配货产品PN
		String phcpmc = StrUtil.returnStr(rs.getString("SQCPZWMC"));//配货产品中文名称
		
		String sxwlmc = StrUtil.returnStr(rs.getString("SXCPMC"));//送修产品名称
		String sxpn = StrUtil.returnStr(rs.getString("SXCPPN"));//送修PN
		String bdkcsl = StrUtil.returnStr(rs.getString("BDKCKYZ"));//本地库存可用值
		
		String sfbgph = StrUtil.returnStr(rs.getString("SFBGPH"));//是否变更配货
		String bgwlbh = StrUtil.returnStr(rs.getString("BGWLBH"));//变更物料编号
		String bgwlmc = StrUtil.returnStr(rs.getString("BGWLMC"));//变更物料名称
		String bgpn = StrUtil.returnStr(rs.getString("BGPN"));//变更PN
		String bgsx = StrUtil.returnStr(rs.getString("BGSX"));//变更属性
		
//		String phcpsl = StrUtil.returnStr(rs.getString("SQCPSL"));//配货产品数量
//		String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//交付产品行号
//		String hh = RuleAPI.getInstance().executeRuleScript("@sequence:(#BO_AKL_QHBH_S)");//行号
		
		
//		body.put("HH", hh);//行号
		body.put("SXDH", sxdh);//送修单号
		body.put("SQSJ", sqsj);//申请时间
		body.put("SQLY", sqly);//申请理由
		body.put("CLJG", cljg);//处理结果
		body.put("SL", sqcpsl);//申请产品数量
		body.put("SX", sx);//属性
		body.put("ZT", zt);//状态
		body.put("BZ", bz);//备注
		body.put("XMLB", xmlb);//项目类别
		body.put("YCPWLBH", wlbh);//申请产品物料编号
		body.put("YCPZWMC", wlmc);//申请产品中文名称
		body.put("JFKFBM", jfkfbm);//收货库房编码
		body.put("JFKFMC", jfkfmc);//收货库房名称
		body.put("SXCPHH", sxcphh);//送修产品行号
		body.put("YCPPN", pn);//申请产品PN
		body.put("YXJ", yxj);//优先级
		body.put("SFJSTH", sfjsth);//是否接受替换
		body.put("PHFS", qhfs);//配货方式
		
		body.put("SQCPWLBH", phcpwlbh);//配货产品物料编号
		body.put("SQCPPN", phcppn);//配货产品PN
		body.put("SQCPZWMC", phcpmc);//配货产品中文名称

		body.put("SXCPMC", sxwlmc);//送修产品名称
		body.put("SXCPPN", sxpn);//送修PN
		body.put("BDKCKYZ", bdkcsl);//本地库存可用值
		
		body.put("SQCPSL", String.valueOf(sl));//配货产品数量
		body.put("PHSX", phsx);//配货属性
		body.put("PCH", pch);//批次号
		body.put("HWDM", hwdm);//货位代码
		
		body.put("SFBGPH", sfbgph);//是否变更配货
		body.put("BGWLBH", bgwlbh);//变更物料编号
		body.put("BGWLMC", bgwlmc);//变更物料名称
		body.put("BGPN", bgpn);//变更PN
		body.put("BGSX", bgsx);//变更属性
		
		//插入数据
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHBH_S", body, bindid, uid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
	}
	
}
