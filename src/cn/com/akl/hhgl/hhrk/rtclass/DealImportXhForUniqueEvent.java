package cn.com.akl.hhgl.hhrk.rtclass;

import java.util.Hashtable;
import java.util.List;
import cn.com.akl.hhgl.hhrk.biz.DealImportXhForUniqueBiz;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DealImportXhForUniqueEvent extends WorkFlowStepRTClassA{

	private UserContext uc;
	public DealImportXhForUniqueEvent(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("qjc");
		setDescription("V1.0");
		setDescription("处理导入单身数据唯一且不为空以及入库数量是否与转仓数量相符!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(HHDJConstant.tableName0, bindid);//采购入库单头
		
		/**处理导入单身数据唯一且不为空**/
		List list = DealImportXhForUniqueBiz.DealUtil(bindid);
		
		if(list.size()>0 && list.size()<15){
			MessageQueue.getInstance().putMessage(uc.getUID(), "导入转仓信息中不允许存在重复【料号】信息：" + list.toString());
			System.out.println(list.toString());
			return false;
		}else if(list.size()>=15){
			MessageQueue.getInstance().putMessage(uc.getUID(), "导入转仓信息中不允许存在重复【料号】信息，请去重后重新办理！");
			return false;
		}
		
		return true;
	}
}