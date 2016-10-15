package cn.com.akl.cwgl.yslc.rtclass;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);

		setVersion("1.0.0");
		setDescription("节点办理事件:出库单，差异处理完成后向财务系统中插入出库差异数量");
	}

	@Override
	public boolean execute() {
		//出库单，差异处理完成后向财务系统中插入出库差异数据；
		
		return true;
	}

}
