package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo8Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	public StepNo8Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo8Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("更新客服中心库存信息。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**1、更新库存状态*/
			String wlzt = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_WLZT, bindid, FJFJCnt.jlbz1, FJFJCnt.djzt1));//物流状态
			if(!wlzt.equals(FJFJCnt.wlzt)){
				throw new RuntimeException("该单还未完成返货，暂无法办理。");
			}else{
				setKCMXStatue(conn, bindid);
			}
			
			/**2、更新单据状态*/
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_P_ZT, FJFJCnt.djzt3, bindid);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_ZT, FJFJCnt.djzt3, bindid);
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * 客服库存及序列号状态更新（在途-->在库）
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setKCMXStatue(Connection conn, final int bindid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//项目类别
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//物料编号
				String sx = StrUtil.returnStr(rs.getString("SX"));//属性
				String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//货位代码
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//故障条码
				
				int updateCount1 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_ZT, xmlb, wlbh, sx, pch, hwdm);
				int updateCount2 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX_ZT, FJFJCnt.zt4, xmlb, wlbh, pch, gztm);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("库存序列号更新失败！");
				return true;
			}
		}, bindid);
	}
}



