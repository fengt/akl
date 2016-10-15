package cn.com.akl.pdgl.kcpd.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo2Transaction() {
	}

	public StepNo2Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("办理后事件，盘点反馈信息回填到单身子表中");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		
		int tastid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean audit_fk = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, StepNo3Transaction.auditName1);//获取审核菜单
		boolean audit_zf = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, tastid, StepNo3Transaction.auditName2);//获取审核菜单
		
		Vector vector = BOInstanceAPI.getInstance().getBODatas(StepNo3Transaction.table2, bindid);
		String query_pdfs = "select * from " + StepNo3Transaction.table0 +" where bindid="+bindid;
		String pdfs = DBSql.getString(query_pdfs, "PDFS");//盘点方式
		if(audit_fk){//审核为反馈
			if(StepNo3Transaction.pdfs_mx.equals(pdfs)){//明细方式
				pdFillBack(vector,bindid,pdfs);
			}else if(StepNo3Transaction.pdfs_hz.equals(pdfs)){//汇总方式
				Vector fk_vector = StepNo3Transaction.fkPackage(uc, bindid);
				pdFillBack(fk_vector,bindid,pdfs);
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "盘点方式不正确，请核查！");
				return false;
			}
			
			/**删除盘点反馈中没有差异的记录**/
			String del_sql = "DELETE FROM " +StepNo3Transaction.table2+ " WHERE KWSL=PKSJSL AND BINDID="+bindid;
			DBSql.executeUpdate(del_sql);
		}else if(audit_zf){//审核为作废
			/**删除盘点单身及反馈数据，更新为作废状态*/
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(StepNo3Transaction.table1, bindid);//删除盘点单身数据
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(StepNo3Transaction.table2, bindid);//删除盘点反馈数据
			StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt3);//更新为已作废
		}else{//审核为不反馈
			/**删除盘点反馈所有的记录，更新盘点状态**/
			String del_sql = "DELETE FROM " +StepNo3Transaction.table2+ " WHERE BINDID="+bindid;
			DBSql.executeUpdate(del_sql);
			StepNo1Transaction.update_Pdzt(bindid, StepNo3Transaction.pdzt0);
		}
		
		return true;
	}
	
	/**
	 * 盘点反馈反写到盘点单身
	 * @param vector
	 * @param bindid
	 * @param pdfs
	 */
	public void pdFillBack(Vector vector, int bindid, String pdfs){
		for (int i = 0; i < vector.size(); i++) {
			Hashtable table = (Hashtable)vector.get(i); 
			String wlbh = (String)table.get("WLBH");
			String pc = (String)table.get("PC");
			int kwsl = Integer.parseInt(table.get("KWSL").toString());
			int pksjsl = Integer.parseInt(table.get("PKSJSL").toString());
			String cyyy = table.get("CYYY").toString();
			int cysl = pksjsl - kwsl;
			
			if(StepNo3Transaction.pdfs_mx.equals(pdfs)){
				String hwdm = (String)table.get("HWDM");
				String sql = "update " + StepNo3Transaction.table1 + " set PKSJSL="+pksjsl+", CYSL="+cysl+", CYYY='"+cyyy+"' where WLBH='"+wlbh+"' and PC='"+pc+"' and KWBM='"+hwdm+"' and bindid="+bindid ;
				DBSql.executeUpdate(sql);
			}else if(StepNo3Transaction.pdfs_hz.equals(pdfs)){
				String sql2 = "update " + StepNo3Transaction.table1 + " set PKSJSL="+pksjsl+", CYSL="+cysl+", CYYY='"+cyyy+"' where WLBH='"+wlbh+"' and PC='"+pc+"' and bindid="+bindid;
				DBSql.executeUpdate(sql2);
			}
		}
	}

}
