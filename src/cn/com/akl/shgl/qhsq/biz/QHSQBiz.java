package cn.com.akl.shgl.qhsq.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

public class QHSQBiz {

	/**
	 * 插入缺货记录
	 * @param conn
	 * @param rs
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public static void insertHander(Connection conn, ResultSet rs, int bindid, String uid, String kfzx, String kfmc) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));
		String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));
		String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));
		String yxj = StrUtil.returnStr(rs.getString("YXJ"));
		String xh = StrUtil.returnStr(rs.getString("PN"));
		String sx = StrUtil.returnStr(rs.getString("SX"));
		int sl = rs.getInt("SL");
		
		String sxdh = StrUtil.returnStr(rs.getString("SXDH"));
		String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));
		String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));
		rec.put("SXDH", sxdh);//送修单号
		rec.put("SXCPHH", sxcphh);//送修产品行号
		rec.put("JFCPHH", jfcphh);//交付产品行号
		
//		rec.put("CLJG", cljg);//处理结果
		rec.put("XMLB", xmlb);//项目类别
		rec.put("WLBH", wlbh);//物料编号
		rec.put("WLMC", wlmc);//物料名称
		rec.put("PN", xh);//申请产品型号
		rec.put("JFKFBM", kfzx);//交付库房编码
		rec.put("JFKFMC", kfmc);//交付库房名称
		rec.put("SQSJ", sqsj);//申请时间
		rec.put("SQLY", sqly);//申请理由
		rec.put("SL", String.valueOf(sl));//申请产品数量
		rec.put("SX", sx);//属性
		rec.put("SFJSTH", sfjsth);//是否接受替换
		rec.put("YXJ", yxj);//优先级
		rec.put("QHFS", QHSQCnt.bhlx1);//缺货方式
		rec.put("ZT", QHSQCnt.zt0);//缺货申请状态
		
		/*
		String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));
		String sqly = StrUtil.returnStr(rs.getString("SQLY"));
		String dqjlsh = StrUtil.returnStr(rs.getString("DQJLSH"));
		String bhyy = StrUtil.returnStr(rs.getString("BHYY"));
		String zbsh = StrUtil.returnStr(rs.getString("ZBSH"));
		String zbkc = StrUtil.returnStr(rs.getString("ZBKC"));
		String khlx = StrUtil.returnStr(rs.getString("KHLX"));
		String yhxm = StrUtil.returnStr(rs.getString("YHXM"));
		String yhdh = StrUtil.returnStr(rs.getString("YHDH"));
		String tjr = StrUtil.returnStr(rs.getString("TJR"));
		String bdkckyz = StrUtil.returnStr(rs.getString("BDKCKYZ"));
		
		rec.put("YHMC", yhxm);//用户姓名
		rec.put("DH", yhdh);//电话
		rec.put("SQSJ", sqsj);//申请时间
		rec.put("SQLY", sqly);//申请理由
		rec.put("KHLX", khlx);//客户类型
		rec.put("BDKCKYZ", bdkckyz);//本地库存可用值
		rec.put("TJR", tjr);//提交人
		rec.put("DQJLSH", dqjlsh);//大区经理审核
		rec.put("BHYY", bhyy);//驳回原因
		rec.put("ZBSH", zbsh);//总部审核
		rec.put("ZBKC", zbkc);//总部库存
		rec.put("YCPWLBH", );//原产品物料编号
		rec.put("YCPPN", );//原产品型号
		rec.put("YCPZWMC", );//原产品中文名称
		rec.put("JCSJ", );//检测时间
		rec.put("CLJG", );//处理结果
		rec.put("ZT", );//状态
		rec.put("BZ", );//备注
*/
		
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHJL", rec, bindid, uid);//插入缺货记录
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 缺货记录状态更新
	 * @param conn
	 * @param sql
	 * @param zt
	 * @param bindid
	 * @throws SQLException
	 */
	public static void setStatus(Connection conn, String sql, final String zt, int bindid) throws SQLException{
		DAOUtil.executeQueryForParser(conn, sql, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String xmlb = StrUtil.returnStr(rs.getString("XMLB"));//项目类别
				String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//送修单号
				String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//送修产品行号
				String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//交付产品行号
				String wlbh = StrUtil.returnStr(rs.getString("SQCPWLBH"));//申请产品物料编号
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String sqsj = StrUtil.returnStr(rs.getString("SQSJ"));//申请时间
				String sqly = StrUtil.returnStr(rs.getString("SQLY"));//申请理由
				String sfqhsq = StrUtil.returnStr(rs.getString("SFQHSQ"));//是否缺货申请
				String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//是否接受替换
				String yxj = StrUtil.returnStr(rs.getString("YXJ"));//优先级
				
				if(QHSQCnt.no.equals(sfqhsq)){
					DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHJL_ZT, sqsj, sqly, QHSQCnt.zt2, sfjsth, yxj, xmlb, sxdh, wlbh, sx, sxcphh, jfcphh);
				}else{
					int updateRow = DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHJL_ZT, sqsj, sqly, zt, sfjsth, yxj, xmlb, sxdh, wlbh, sx, sxcphh, jfcphh);
					if(updateRow != 1) throw new RuntimeException("缺货记录状态更新失败！");
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 单据状态更新
	 * @param conn
	 * @param bindid
	 * @param bhlx
	 * @param zt
	 * @throws SQLException
	 */
	public static void updateStatus(Connection conn, int bindid, String bhlx, String zt) throws SQLException{
		DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHSQ_P_ZT, zt, bindid);//单头状态更新
		if(QHSQCnt.bhlx1.equals(bhlx)){
			DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_TSSQ_S_ZT, zt, bindid);//子表状态更新（特殊）
		}else{
			DAOUtil.executeUpdate(conn, QHSQCnt.UPDATE_QHSQ_S_ZT, zt, bindid);//子表状态更新
		}
	}
	
}
