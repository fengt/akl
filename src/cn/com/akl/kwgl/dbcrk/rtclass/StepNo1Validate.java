package cn.com.akl.kwgl.dbcrk.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.kwgl.constant.KwglConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public StepNo1Validate() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("校验调拨出库和入库不能相同。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DBCRK_S", bindid);
		String sql = "select * from BO_AKL_DBCRK_P where bindid="+bindid;
		String zcck = DBSql.getString(sql, "ZCCK");//转出仓库
		String zrck = DBSql.getString(sql, "ZRCK");//转入仓库
		String sfdgk = DBSql.getString("SELECT SFDGK FROM " +KwglConstant.table4+ " WHERE CKDM='"+zcck+"'", "SFDGK");//是否代管库
		if(zrck.equals(zcck)){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "调拨出库和调拨入库不能是同一仓库，请重新选择！", true);
			return false;
		}
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable records = (Hashtable)vector.get(i);
				String wlbh = records.get("WLBH").toString();//物料编号
				String pch = records.get("TZQPC").toString();//调整前批次
				String tzck = records.get("TZHCK").toString();//调整仓库
				String tzqkw = records.get("TZQKW").toString();//调整前货位
				int tzqsl = Integer.parseInt(records.get("TZQSL").toString());//调整前数量
				int tzhsl = Integer.parseInt(records.get("TZHSL").toString());//调整后数量

				//a.调整数量不为零校验
				if(tzhsl == 0){
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "【需调出数量】不能为零，请输入调整后数量！", true);
					return false;
				}

				//b.锁库批次的物料校验
				if(sfdgk.equals(KwglConstant.isProxies0)){//代管库
					String QUERY_DGSK = "SELECT SUM(ISNULL(XSSL,0))ZSDSL FROM BO_AKL_DGCKSK WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND HWDM='"+tzqkw+"'";
					int num = DBSql.getInt(QUERY_DGSK, "ZSDSL");//锁库总数量
					if(tzqsl - num < tzhsl){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "该物料批次【"+wlbh+","+pch+","+tzqkw+"】库存不足，暂不能调拨！");
						return false;
					}
				}else{//自营库
					String QUERY_SK = "SELECT SUM(ISNULL(SDSL,0))ZSDSL FROM BO_AKL_KC_SPPCSK WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND CKDM='"+zcck+"'";
					int num = DBSql.getInt(QUERY_SK, "ZSDSL");//锁库总数量
					if(tzqsl - num < tzhsl){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "该物料批次【"+wlbh+","+pch+"】库存不足，暂不能调拨！");
						return false;
					}
				}
			}
		}

		return true;
	}

}
