package cn.com.akl.pdgl.kcpd.rtclass;

import java.util.Hashtable;
import java.util.Vector;

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
		setDescription("检验单身数据是否重复！");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable head = BOInstanceAPI.getInstance().getBOData(StepNo3Transaction.table0, bindid);
		Vector vector = BOInstanceAPI.getInstance().getBODatas(StepNo3Transaction.table1, bindid);
		
		String pdfs = head.get("PDFS").toString();//盘点方式
		
		if(vector != null){
			if(StepNo3Transaction.pdfs_mx.equals(pdfs)){//明细
				return pd_mxCheck(vector,bindid);
			}else if(StepNo3Transaction.pdfs_hz.equals(pdfs)){//汇总
				return pd_hzCheck(vector,bindid);
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "盘点方式不正确，请核查！");
				return false;
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "盘点单身数据为空！");
			return false;
		}
	}
	
	/**
	 * 明细重复校验
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean pd_mxCheck(Vector vector, int bindid){
		String str = "SELECT COUNT(*) num FROM " +StepNo3Transaction.table1+ " WHERE KWBM='' AND BINDID="+bindid;
		int n = DBSql.getInt(str, "num");//是否有空货位
		if(n == 0){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String pc = rec.get("PC").toString();
				String hwdm = rec.get("KWBM").toString();
				
				String isRepeat = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table1+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND KWBM='"+hwdm+"' AND BINDID="+bindid;
				int isR = DBSql.getInt(isRepeat, "NUM");//数据是否重复
				
				if(isR != 1){
					MessageQueue.getInstance().putMessage(uc.getUID(), "盘点单身中【物料编号："+wlbh+"和批次号："+pc+"和货位代码："+hwdm+"】的物料信息重复，请核查！");
					return false;
				}
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "单身数据因“盘点方式”操作不当导致数据不对，请核查！");
			return false;
		}
		return true;
	}
	
	/**
	 * 汇总重复校验
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean pd_hzCheck(Vector vector, int bindid){
		String str = "SELECT COUNT(*) num FROM " +StepNo3Transaction.table1+ " WHERE KWBM<>'' AND BINDID="+bindid;
		int n = DBSql.getInt(str, "num");//是否有非空货位
		if(n == 0){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String pc = rec.get("PC").toString();
				
				String isRepeat = "SELECT COUNT(*) NUM FROM " +StepNo3Transaction.table1+ " WHERE WLBH='"+wlbh+"' AND PC='"+pc+"' AND BINDID="+bindid;
				int isR = DBSql.getInt(isRepeat, "NUM");//数据是否重复
				
				if(isR != 1){
					MessageQueue.getInstance().putMessage(uc.getUID(), "盘点单身中【物料编号："+wlbh+"和批次号："+pc+"】的物料信息重复，请核查！");
					return false;
				}
			}
		}else{
			MessageQueue.getInstance().putMessage(uc.getUID(), "单身数据因“盘点方式”操作不当导致数据不对，请核查！");
			return false;
		}
		return true;
	}

}
