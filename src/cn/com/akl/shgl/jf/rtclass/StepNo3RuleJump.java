package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3RuleJump extends WorkFlowStepJumpRuleRTClassA {

    public StepNo3RuleJump(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("若有缺货就走缺货.");
    }

    @Override
    public int getNextNodeNo() {
        return 1;
    }

    @Override
    public String getNextTaskUser() {
        return null;
    }

}
