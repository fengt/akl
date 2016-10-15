package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

public class SXBiz {

	private KCBiz kcBiz = new KCBiz();
	private SXHandle sxHandle = new SXHandle();
	
	/**
	 * 更新库存明细和插入序列号明细（库存汇总第一节点已插入）
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ckbm
	 * @param xmlb
	 * @throws SQLException
	 */
	public void insertALL(Connection conn, final int bindid, final String uid, 
			final String ckdm, final String xmlb, final String ywlx)throws SQLException{
		final String sxdh = DAOUtil.getString(conn, SXCnt.QUERY_SXDH, bindid);
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String hh = sxHandle.getSXRowNum(conn, bindid, sxdh);//生成送修产品行号
				String clfs = StrUtil.returnStr(rs.getString("CLFS"));
				String gztm = StrUtil.returnStr(rs.getString("GZTM"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String xh = StrUtil.returnStr(rs.getString("XH"));
				
				String sx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_SRSX, xmlb, clfs, ywlx));
				if("".equals(clfs)){
					throw new RuntimeException("送修子表【处理方式】获取失败！");
				}else if(clfs.equals(SXCnt.clfs2)||clfs.equals(SXCnt.clfs3)){
					throw new RuntimeException("该型号["+xh+"]处理方式选择有误，请检查！");
				}else if(SXCnt.clfs4.equals(clfs) || SXCnt.clfs5.equals(clfs)){//保内、保外属性转换为换出属性入库
					sx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_HCSX, xmlb, clfs, ywlx));
				}
				//无法根据处理方式获取属性，则为坏品
				if("".equals(sx)){
					sx = SXCnt.sx1;
				}
				
				Hashtable<String, String> body = kcBiz.getKCMX(conn, rs, ckdm, xmlb, ywlx, pch, sx);
				@SuppressWarnings("unchecked")
				Hashtable<String, String> xlhBody = (Hashtable<String, String>)body.clone();
				
				@SuppressWarnings("unchecked")//主要是避免货位代码入库时与第一节点不同，所以重新获取
				Hashtable<String, String> tempBody = (Hashtable<String, String>)body.clone();
				int id = Integer.parseInt(tempBody.get("KEYID"));//唯一ID
				String tempSX = tempBody.get("SX");//临时属性
				String tempHW = tempBody.get("HWDM");//临时货位
				
				/**
				 * 1、更新库存明细
				 */
				kcBiz.insertKCMX(conn, bindid, uid, body, xmlb, clfs);
				
				/**
				 * 2、插入序列号明细
				 * 条件：处理方式为换新或复检升级&&故障条码不能为空
				 */
				if(!"".equals(gztm)&&(clfs.equals(SXCnt.clfs0)||clfs.equals(SXCnt.clfs1))){
					kcBiz.insertXLHMX(conn, bindid, uid, xlhBody, xmlb, gztm);
				}
				
				/**
				 * 3、反向更新送修子表（行号、货位代码和属性）
				 */
				if(SXCnt.clfs4.equals(clfs) || SXCnt.clfs5.equals(clfs)){//保内、保外维修：反填收入属性
					tempSX = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_SRSX, xmlb, clfs, ywlx));
				}
				sxHandle.setHHAndHWDMAndSX(conn, hh, tempHW, tempSX, id);
				return true;
			}
		}, bindid);
		
	}
	
	/**
	 * 减代用品库存
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void decreaseDYP(Connection conn, int bindid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//项目类别
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_DYP_DE, 
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				int count1 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCHZ_DYP_DE, rs.getInt("SL"), rs.getInt("SL"), xmlb, rs.getString("WLBH"), pch);
				int count2 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_DYP_DE, rs.getInt("SL"), xmlb, rs.getString("WLBH"), pch, rs.getString("HWDM"));
				if(count2 != 1){
					throw new RuntimeException(" 代用品库存扣减失败！");
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 加代用品库存
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void increaseDYP(Connection conn, int bindid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//项目类别
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_DYP_IN, 
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCHZ_DYP_IN, rs.getInt("SL"), rs.getInt("SL"), xmlb, rs.getString("WLBH"), rs.getString("PCH"));
				DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_DYP_IN, rs.getInt("SL"), rs.getString("WLBH"), rs.getString("HWDM"), rs.getString("PCH"));
				return true;
			}
		}, bindid);
	}
	
	
}
