package cn.com.akl.cggl.cgdd.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.cggl.cgdd.biz.CalImportDatasBiz;
import cn.com.akl.cggl.cgdd.constant.CgddConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1ModelUpAfter extends ExcelDownFilterRTClassA{

	private UserContext uc;
	public StepNo1ModelUpAfter(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("订单导入后，计算价税合计!");
	}
	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas(CgddConstant.tableName1, bindid);
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(CgddConstant.tableName0, bindid);
		
		if(pTable.isEmpty()){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请在导入数据前先点击暂存，否则无法导入数据！");
			String sql = "delete from " + CgddConstant.tableName1 + " where bindid = " + bindid;
			DBSql.executeUpdate(sql);
		}else{
			CalImportDatasBiz calUtil = new CalImportDatasBiz();
			/**数据导入回填字段**/
			calUtil.ImportDataFillback(vector, pTable,bindid);
			/**进行价税合计**/
			calUtil.calDatasForAccount(vector,bindid);
		}
		return null;
	}
}
