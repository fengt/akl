package cn.com.akl.pdgl.kcpd.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo2Validate extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo2Validate() {
	}

	public StepNo2Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("盘点反馈导入数据时，校验导入盘点单号是否和单头一致。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable head = BOInstanceAPI.getInstance().getBOData(StepNo3Transaction.table0, bindid);
		Vector vector = BOInstanceAPI.getInstance().getBODatas(StepNo3Transaction.table2, bindid);
		
		String query_pddh = "select * from " +StepNo3Transaction.table0+ " where bindid = "+bindid;
		String mul_pddh = "select count(*) num from (select distinct PDDH from " +StepNo3Transaction.table2+ " where bindid = "+bindid+")a";
		String query_pddh_fk = "select distinct PDDH from " +StepNo3Transaction.table2+ " where bindid = "+bindid;
		
		int num = DBSql.getInt(mul_pddh, "num");//盘点单号数值(反馈)
		String pddh_fk = DBSql.getString(query_pddh_fk, "PDDH");//盘点单号(反馈)
		String pddh = head.get("PDDH").toString();//盘点单号(单头)
		if(num == 1){
			if(pddh.equals(pddh_fk)){
				return pdfkCheck(vector,bindid);
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "您导入的数据中【盘点单号】不是同一单号，请重新导入！");
				return false;
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "盘点反馈单身中【盘点单号】不唯一，请核查！");
			return false;
		}		
	}
	
	/**
	 * 校验是否盘点过的物料编号和批次号
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean pdfkCheck(Vector vector, int bindid){
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String wlbh = rec.get("WLBH").toString();
			String pc = rec.get("PC").toString();
			String hwdm = rec.get("HWDM").toString();
			
			String isRepeat = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table2+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND HWDM='"+hwdm+"' AND BINDID="+bindid;
			String isExist = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table1+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND BINDID="+bindid; 
			int isR = DBSql.getInt(isRepeat, "NUM");//数据是否重复
			int isE = DBSql.getInt(isExist, "NUM");//物料是否盘点过
			
			if(isR == 1){
				if(isE <= 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "盘点反馈单身中【物料编号："+wlbh+"】和【批次号："+pc+"】的物料没有在该单盘点过，请核查！");
					return false;
				}
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "盘点反馈单身中【物料编号："+wlbh+"和批次号："+pc+"和货位代码："+hwdm+"】的物料信息重复，请核查！");
				return false;
			}
		}
		return true;
	}

}
