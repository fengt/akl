package cn.com.akl.shgl.sxtz.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.sx.biz.KCBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class SXTZBiz {

	KCBiz kcBiz = new KCBiz();
	/**
     * 库存汇总：减
     * 库存明细：减
     * 序列号：删除
     * 新记录按送修流程操作
     * 
     * 交付单处理：若已交付，则交付单加回库存；若未交付但已引入该送修单数据，则提示删交付单再调整操作；
     */
	
	public void repositoryHandle(Connection conn, int bindid, String uid, 
			String parentBindid, final String xmlb) throws SQLException{
		String ckbm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_XMKF, bindid));//客服仓库编码
		String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_YWLX, bindid));//业务类型
		
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_YSX_S, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				String gztm = StrUtil.returnStr(rs.getString("GZTM"));
				String clfs = StrUtil.returnStr(rs.getString("CLFS"));
				int sl = rs.getInt("SL");
				if(SXCnt.clfs8.equals(clfs)) return true;
				int updateCount1 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCHZ, -sl, -sl, xmlb, wlbh, pch);
				int updateCount2 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCMX_KWSL, -sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("送修调整库存更新失败！");
				DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_XLH, gztm);//删除序列号
				return true;
			}
		}, bindid);
		
		repositoryInsert(conn, bindid, uid, parentBindid, xmlb, ywlx, ckbm);
	}
	
	/**
	 * 库存汇总和明细操作
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param parentBindid
	 * @param xmlb
	 * @param ywlx
	 * @param ckdm
	 * @throws SQLException
	 */
	public void repositoryInsert(Connection conn, final int bindid, final String uid, 
			final String parentBindid, final String xmlb, final String ywlx, final String ckdm) throws SQLException{
		final String sxdh = DAOUtil.getString(conn, SXTZConstant.QUERY_FORM_SXDH, bindid);
		
		//1、插入库存汇总
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_SXTZ_S_HZ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				kcBiz.insertKCHZ(conn, Integer.parseInt(parentBindid), uid, rs, xmlb, pch);
				return true;
			}
		}, bindid);
		
		//2、插入库存明细
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_NEW_FORM_BODY, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String hh = getSXRowNum(conn, bindid, sxdh);//生成送修产品行号
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
				insertKCMX(conn, Integer.parseInt(parentBindid), uid, body, xmlb, clfs);
				
				/**
				 * 2、插入序列号明细
				 * 条件：处理方式为换新或复检升级&&故障条码不能为空
				 */
				if(!"".equals(gztm)&&(clfs.equals(SXCnt.clfs0)||clfs.equals(SXCnt.clfs1))){
					kcBiz.insertXLHMX(conn, Integer.parseInt(parentBindid), uid, xlhBody, xmlb, gztm);
				}
				
				/**
				 * 3、反向更新送修子表（行号、货位代码和属性）
				 */
				if(SXCnt.clfs4.equals(clfs) || SXCnt.clfs5.equals(clfs)){//保内、保外维修：反填收入属性
					tempSX = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_SRSX, xmlb, clfs, ywlx));
				}
				
				int count = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_SXTZ_HHAndHWDMAndSX, hh, tempHW, tempSX, id);
				if(count != 1) throw new RuntimeException("送修子表的属性和货位代码更新失败！");
				return true;
			}
		}, bindid);
	}
	
	/**
	 * 插入库存明细
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param body
	 * @param xmlb
	 * @throws SQLException
	 */
	public void insertKCMX(Connection conn, int bindid, String uid, Hashtable<String, String> body, String xmlb, String clfs) throws SQLException{
		String wlbh = body.get("WLBH");
		String sx = body.get("SX");
		String pch = body.get("PCH");
		String hwdm = body.get("HWDM");
		int sl = Integer.parseInt(body.get("KWSL"));
		try {
			/**1、插入或更新库存明细*/
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCMX, xmlb, wlbh, sx, hwdm, pch, SXCnt.zt0);
			if(!SXCnt.clfs8.equals(clfs)){
				if(n == 0){
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", body, bindid, uid);
				}else{
					int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
					if(updateCount != 1) throw new RuntimeException("送修单据调整库存明细更新失败！");
				}
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 产生送修调整的产品行号
	 * @param conn
	 * @param bindid
	 * @param sxdh
	 * @return
	 * @throws SQLException
	 */
	public String getSXRowNum(Connection conn, int bindid, String sxdh) throws SQLException {
        Integer rowNum = DAOUtil.getIntOrNull(conn, "SELECT ISNULL(MAX(CONVERT(INT, SUBSTRING(SXCPHH,16,19))),0)+1 FROM BO_AKL_SH_SXTZ_S WHERE BINDID=?", bindid);
        StringBuilder sxrow = new StringBuilder(20);
        if (rowNum == null) {
            return sxrow.append(sxdh).append("-").append(1).toString();
        } else {
            return sxrow.append(sxdh).append("-").append(rowNum).toString();
        }
    }
	
	
	/**==========================================================================================================================*/
	/**
	 * 交付操作：加库存|配件、删邮寄、删除交付单流程
	 * @param conn
	 * @param bindid
	 * @param jfdh
	 * @param xmlb
	 * @throws SQLException
	 */
	public void jfdHandle(Connection conn, int bindid, String jfdh, final String xmlb) throws SQLException{
		/**
		 * 1、加库存
		 */
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_JF_S, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				int sl = rs.getInt("SL");
				int updateCount1 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCHZ, sl, sl, xmlb, wlbh, pch);
				int updateCount2 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("送修调整库存更新失败！");
				return true;
			}
		}, jfdh);
		
		/**
		 * 2、加配件
		 */
		DAOUtil.executeQueryForParser(conn, SXTZConstant.QUERY_JF_S_PJ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				int sl = rs.getInt("SL");
				int updateCount1 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCHZ, sl, xmlb, wlbh, pch);
				int updateCount2 = DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("送修调整库存更新失败！");
				return true;
			}
		}, jfdh);
		
		/**
		 * 3、删邮寄信息
		 */
		String sfyj = DAOUtil.getString(conn, SXTZConstant.QUERY_JF_SFYJ, jfdh);
		if(XSDDConstant.YES.equals(sfyj)){
			DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_JF_DFH_P, jfdh);
			DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_JF_DFH_S, jfdh);
		}
		
		/**
		 * 4、删除交付单流程
		 */
		
		try {
			int jfBindid = DAOUtil.getInt(conn, SXTZConstant.QUERY_JF_BINDID, jfdh);
			WorkflowInstanceAPI.getInstance().removeProcessInstance(jfBindid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(" 交付流程删除失败！");
		}
	}
	
	
}
