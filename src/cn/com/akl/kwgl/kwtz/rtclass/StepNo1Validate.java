package cn.com.akl.kwgl.kwtz.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.kwgl.constant.KwglConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;


public class StepNo1Validate extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo1Validate() {
	}

	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("1、校验单身数据是否为同一仓库及是否有锁库批次物料。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_KWTZ_S", bindid);
		String sql = "SELECT COUNT(DISTINCT TZHCK) AS isNO FROM " +KwglConstant.table1+ " WHERE BINDID="+bindid;
		String ckbm = DBSql.getString("SELECT CKBM FROM " +KwglConstant.table0+ " WHERE bindid ="+bindid, "ckbm");
		String sfdgk = DBSql.getString("SELECT SFDGK FROM " +KwglConstant.table4+ " WHERE CKDM='"+ckbm+"'", "SFDGK");
		int isNo = DBSql.getInt(sql, "isNo");
		if(isNo != 1){
			List list = ckdmCheck(uc,bindid);
			if(list.size()>0 && list.size()<10){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "库位调整不允许对多个仓库"+list.toString()+"操作，请重新录入！");
				return false;
			}else if(list.size()>=10){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "库位调整不允许对多个仓库操作，请重新录入！");
				return false;
			}
		}
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable records = (Hashtable)vector.get(i);
				String wlbh = records.get("WLBH").toString();//物料编号
				String pch = records.get("TZQPC").toString();//调整前批次
				String tzck = records.get("TZHCK").toString();//调整仓库
				String tzqkw = records.get("TZQKW").toString();//调整前货位
				int tzhsl = Integer.parseInt(records.get("TZHSL").toString());//调整后数量
				
				//a.调整数量不为零校验
				if(tzhsl == 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "调整后数量不能为零，请重新输入！", true);
					return false;
				}
				
				//b.锁库批次的物料校验
				if(sfdgk.equals(KwglConstant.isProxies0)){//代管库
					String QUERY_DGSK = "SELECT COUNT(1) NUM FROM BO_AKL_DGCKSK WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND HWDM='"+tzqkw+"'";
					int num = DBSql.getInt(QUERY_DGSK, "NUM");
					if(num > 0){
						MessageQueue.getInstance().putMessage(uc.getUID(), "该物料批次【"+wlbh+","+pch+","+tzqkw+"】已锁库，暂不能调整或删除后进行调整！");
						return false;
					}
				}else{//自营库
					String QUERY_SK = "SELECT COUNT(1) NUM FROM BO_AKL_KC_SPPCSK WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND CKDM='"+tzck+"'";
					int num = DBSql.getInt(QUERY_SK, "NUM");
					if(num > 0){
						MessageQueue.getInstance().putMessage(uc.getUID(), "该物料批次【"+wlbh+","+pch+"】已锁库，暂不能调整或删除后进行调整！");
						return false;
					}
				}
			}
		}
		return true;
	}
	
	
	public void lockCheck(){
		
	}
	/**
	 * 库位调整重复仓库校验
	 * @param uc
	 * @param bindid
	 * @return
	 */
	public static List ckdmCheck(UserContext uc, int bindid){
		Connection conn = DBSql.open();
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();
		String sql = "SELECT DISTINCT TZHCK AS CKDM FROM " +KwglConstant.table1+ " WHERE BINDID="+bindid;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
					if(StrUtil.isNotNull(ckdm)){
						list.add(ckdm);
					}
				}
			}
		} catch (SQLException e) {
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出错，请联系管理员！");
			e.printStackTrace();
		}
		return list;
	}

}
