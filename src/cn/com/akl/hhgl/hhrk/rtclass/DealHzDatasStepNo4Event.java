package cn.com.akl.hhgl.hhrk.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.hhgl.hhrk.biz.DealHzDatasBiz;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
/**
 * 第四节点，有效性校验事件
 * @author ActionSoft_2013
 *
 */
public class DealHzDatasStepNo4Event extends WorkFlowStepRTClassA{

	private UserContext uc;
	public DealHzDatasStepNo4Event(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("qjc");
		setDescription("V1.0");
		setDescription("处理入库数量是否与转仓数量相符!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable pTable = BOInstanceAPI.getInstance().getBOData(HHDJConstant.tableName0, bindid);//采购入库单头
		Vector sVector = BOInstanceAPI.getInstance().getBODatas(HHDJConstant.tableName1, bindid);//采购入库单身
		
		/**入库数量是否与转仓数量相符**/
		//boolean flag = DealHzDatasBiz.judgePvector(pTable, sVector);
		return true;
	}

}
