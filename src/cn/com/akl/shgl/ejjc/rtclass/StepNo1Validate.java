package cn.com.akl.shgl.ejjc.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private static final String QUERY_FJ_S = "SELECT COUNT(1)N FROM BO_AKL_FJ_S WHERE BINDID=?";
	
	private static final String QUERY_FJ_S_GATHER = "SELECT KFCKBH,CPLH,CPSX,PN,KFMC,COUNT(1)SL FROM BO_AKL_FJ_S WHERE BINDID=? GROUP BY KFCKBH,CPLH,CPSX,PN,KFMC";
	
	private static final String QUERY_FJ_S_isGZTMHRepeat = "SELECT GZTMH FROM BO_AKL_FJ_S WHERE BINDID=? GROUP BY GZTMH HAVING COUNT(1)>1";
	
	private static final String QUERY_FJJH_isExsit = "SELECT COUNT(1)N FROM BO_AKL_FJJH_S WHERE BINDID=? AND KFZX=? AND WLBH=? AND SX=?";
	
	private static final String QUERY_FJJH_NUM = "SELECT FJJHSL FROM BO_AKL_FJJH_S WHERE BINDID=? AND KFZX=? AND WLBH=? AND SX=?";
	
	
	private static final String STEP_NAME = "二检检测任务完成";
	
	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("产品信息校验。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_FJJH_P", bindid);
		String fjjhdh = head.get("FJJHDH").toString();//复检计划单号
		int fjjh_bindid = DBSql.getInt("SELECT BINDID FROM BO_AKL_FJJH_P WHERE FJDJBH='"+fjjhdh+"'", "BINDID");//复检计划BINDID
		Connection conn = null;
		try {
			conn = DBSql.open();
			String stepName = WorkflowInstanceAPI.getInstance().getCurrentStepName(fjjh_bindid).toString();//获取复检计划当前的节点名称
			if(stepName.equals(STEP_NAME)){//二检检测任务完成
				int isEmpty = DAOUtil.getInt(conn, QUERY_FJ_S, bindid);
				ArrayList<String> gztmList = DAOUtil.getStringCollection(conn, QUERY_FJ_S_isGZTMHRepeat, bindid);
				if(isEmpty == 0){
					throw new RuntimeException("复检信息不能为空！");
				}
				if(gztmList.size() > 0){
					throw new RuntimeException("复检信息中存在重复的条码号"+gztmList.toString()+"，请核查！");
				}
				validate(conn, bindid, fjjh_bindid);
			}else{
				throw new RuntimeException("复检计划流程中暂无该任务，请核查！");
			}
			
			return true;
		} catch (RuntimeException e) {
 			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	public void validate(Connection conn, int bindid, final int fjjh_bindid) throws SQLException{
		DAOUtil.executeQueryForParser(conn, QUERY_FJ_S_GATHER, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String kfbm = StrUtil.returnStr(rs.getString("KFCKBH"));
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));
				String sx = StrUtil.returnStr(rs.getString("CPSX"));
				String xh = StrUtil.returnStr(rs.getString("PN"));
				String kfmc = StrUtil.returnStr(rs.getString("KFMC"));
				int sl = rs.getInt("SL");//二检数量
				Integer fjsl1 = DAOUtil.getIntOrNull(conn, QUERY_FJJH_NUM, fjjh_bindid, kfbm, wlbh, sx);//复检数量
				int fjsl = fjsl1 == null ? 0 : fjsl1.intValue();
				int n = DAOUtil.getInt(conn, QUERY_FJJH_isExsit, fjjh_bindid, kfbm, wlbh, sx);
				if(n == 0){
					throw new RuntimeException("复检信息中该客服【"+kfmc+"】，型号【"+xh+"】不在复检计划中，请检查！");
				}else if(sl > fjsl){
					throw new RuntimeException("复检信息中该客服【"+kfmc+"】，型号【"+xh+"】已检测"+sl+"个，大于复检计划数量"+fjsl+"，请检查！");
				}
				
				return true;
			}
		}, bindid);
	}
	
}
