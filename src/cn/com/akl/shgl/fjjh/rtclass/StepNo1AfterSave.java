package cn.com.akl.shgl.fjjh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjjh.biz.SpotcheckBiz;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1AfterSave extends WorkFlowStepRTClassA {

	private SpotcheckBiz spotBiz = new SpotcheckBiz();
	private Connection conn = null;
	private UserContext uc;
	public StepNo1AfterSave() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1AfterSave(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("自动带入抽检调拨记录。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		String uid = uc.getUID();
		
		if("BO_AKL_FJJH_P".equals(tablename)){
			try{
				conn = DAOUtil.openConnectionTransaction();
				
				service(conn, bindid, uid);
				
				conn.commit();
			}catch (RuntimeException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "后台出现异常，请检查控制台", true);
				return false;
			} finally{
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	public void service(Connection conn, final int bindid, final String uid) throws SQLException{
		String xmlb = DAOUtil.getString(conn, FJJHCnt.QUERY_FJJH_P_XMLB, bindid);//项目类别
		String fjlx = DAOUtil.getString(conn, FJJHCnt.QUERY_FJJH_P_FJLX, bindid);//复检类型
		String fhck = DAOUtil.getStringOrNull(conn, FJJHCnt.QUERY_FJJH_P_FHCKBM, bindid);//返货库
		
		String kfckbm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJJHCnt.QUERY_FJJH_P_KFCKBM, bindid));//客服仓库编码集合
		String kffz = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJJHCnt.QUERY_FJJH_P_KFFZ, bindid));//客服分组
		
		int flag = 1;//默认不分组
		if(FJJHCnt.fjlx0.equals(fjlx) || FJJHCnt.fjlx1.equals(fjlx)){
			
		}else{
			flag = 0;
		}
		
		//被抽检客服调拨单的BIDNID汇总
		final ArrayList<Integer> list = DAOUtil.getInts(conn, FJJHCnt.QUERY_DB_ALL_BINDID, xmlb, fhck, flag, kfckbm, flag, kffz);
		
		/**
		 * 1、暂时性清空调拨单已更新的复检数量
		 */
		spotBiz.clearFJSL(conn, bindid);
		
		/**
		 * 2、平均分配抽检数量
		 */
		DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_JHWLXX, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				spotBiz.numberAvarageAndFillBackData(conn, bindid, uid, list, rs.getString("WLBH"), rs.getString("SX"), rs.getInt("CJSL"));
				return true;
			}
		}, bindid);
		
		/**
		 * 3、当正常复检时，校验客服分组中复检数量不满足设定值的客服中心，并给出提示
		 */
		if(FJJHCnt.fjlx4.equals(fjlx)){
			List<String> messages = spotBiz.checkTheNumber(conn, bindid, fhck, kffz);
			if(messages.size() > 0){
				MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), messages.toString(), true);
			}
		}
	}
	
}
