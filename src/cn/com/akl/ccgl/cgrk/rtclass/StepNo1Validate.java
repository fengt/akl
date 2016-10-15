package cn.com.akl.ccgl.cgrk.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.biz.CheckBiz;
import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA{

	private static final String QUERY_CNT = "SELECT COUNT(*) AS CNT FROM BO_AKL_CCB_RKD_BODY WHERE BINDID=? AND WLBH=? AND CGDDH=?";
	private static final String QUERY_WLBH = "SELECT WLBH FROM BO_AKL_CCB_RKD_BODY WHERE BINDID=? AND WLBH=? AND CGDDH=? GROUP BY WLBH,CGDDH HAVING(WLBH)>1";
	
	private UserContext uc;
	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("处理导入单身数据唯一且不为空以及入库数量是否与转仓数量相符!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector<Hashtable<String, String>> vector1 = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);//入库单单身
		Vector<Hashtable<String, String>> vector2 = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName2, bindid);//转仓明细表
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//采购入库单头
		String rkdb = pTable.get("RKDB").toString();
		
		
		/**根据入库单别判断是否需要转仓信息导入**/
		if(rkdb.equals(CgrkCnt.rkdb0)){
			/*判断转仓信息不为空*/
			if(vector2 != null && vector1 == null){
				/*a.判断料号是否存在且唯一;b.判断是否转仓信息校验*/
				return CheckBiz.XHCheck(uc, vector2, bindid);
			}else if(vector2 == null && vector1 == null){
				MessageQueue.getInstance().putMessage(uc.getUID(), "您的转仓信息不能为空，请输入数据！");
				return false;
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "您操作不当，入库单身信息存在数据，请删除！");
				return false;
			}
		}else{
			/*判断入库单身不为空*/
			if(vector1 == null || vector2 != null){
				MessageQueue.getInstance().putMessage(uc.getUID(), "您的入库单身为空或者入库转仓信息存在数据，请重新输入！");
				return false;
			}else{
				return wlbhCheck(uc,vector1,bindid,rkdb);
			}
			
			//业务拓展时会用
			/*else if(CgrkCnt.rkdb1.equals(rkdb)){//回采入库
				
			}else if(CgrkCnt.rkdb2.equals(rkdb)){//其他采购入库
				
			}else{//其他入库
				
			}*/
		}
	}
	
	/**
	 * 校验物料不能重复，且价格不能零
	 * @param uc
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean wlbhCheck(UserContext uc, Vector<Hashtable<String, String>> vector, int bindid, String rkdb){
		Connection conn = null;
		for (int i = 0; i < vector.size(); i++) {
			Hashtable<String, String> rec = vector.get(i);
			String cgddh = rec.get("CGDDH").toString();
			String wlbh = rec.get("WLBH").toString();
			double wsjg = Double.parseDouble(rec.get("WSJG").toString());//未税价格
			double hsjg = Double.parseDouble(rec.get("HSJG").toString());//含税价格
			int sssl = Integer.parseInt(rec.get("SSSL").toString());//实收数量
			try {
				conn = DBSql.open();
				//物料重复校验
				int n =  DAOUtil.getInt(conn, QUERY_CNT, bindid,wlbh,cgddh);
				if(n !=1){
					ArrayList<String> list = DAOUtil.getStringCollection(conn, QUERY_WLBH, bindid,wlbh,cgddh);
					if(list.size()>0 && list.size()<10){
						MessageQueue.getInstance().putMessage(uc.getUID(), "存在重复【料号】信息：" + list.toString());
						return false;
					}else{
						MessageQueue.getInstance().putMessage(uc.getUID(), "存在重复【料号】信息，请去重后重新办理！");
						return false;
					}
				}
				//实收数量不能为零
				if(sssl == 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+wlbh+"】的实收数量不能为零！");
					return false;
				}
				//当入库单别不为其他入库时，进行价格为零校验
				if(!rkdb.equals(CgrkCnt.rkdb3)&&(wsjg == 0.0 || hsjg == 0.0)){
					MessageQueue.getInstance().putMessage(uc.getUID(), "该物料【"+wlbh+"】的未税价格或含税价格不能为零！");
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally{
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	
}