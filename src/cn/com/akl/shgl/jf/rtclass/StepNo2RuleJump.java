package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2RuleJump extends WorkFlowStepJumpRuleRTClassA {

    public StepNo2RuleJump(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("若有升级处理则进入升级处理，若没有升级处理有缺货就走缺货.");
    }

    @Override
    public int getNextNodeNo() {
        return 0;
    }

    @Override
    public String getNextTaskUser() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (isBack) {
            return null;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();
            /**
             * 异地交付审核：
             * 	同意后：
             * 		1、进入第一节点。
             * 		2、把异地库房值 存入 本地库房。
             *		3、是否异地交付 为 否。
             *		4、找对应的库房办理人.
             * 	不同意：
             * 		1、也进入第一节点。
             * 		2、不对表单做改变。
             */
            String ydckdm = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YDCKDM, bindid);
            String processUid = QSCYBiz.getProcessUid(conn, ydckdm);
            return PrintUtil.parseNull(processUid);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return null;
        } catch (Exception e) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "流程RuleJump事件出现问题!");
            e.printStackTrace();
            return null;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
