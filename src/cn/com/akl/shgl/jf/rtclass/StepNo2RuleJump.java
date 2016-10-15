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
        setDescription("���������������������������û������������ȱ������ȱ��.");
    }

    @Override
    public int getNextNodeNo() {
        return 0;
    }

    @Override
    public String getNextTaskUser() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");
        if (isBack) {
            return null;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();
            /**
             * ��ؽ�����ˣ�
             * 	ͬ���
             * 		1�������һ�ڵ㡣
             * 		2������ؿֵⷿ ���� ���ؿⷿ��
             *		3���Ƿ���ؽ��� Ϊ ��
             *		4���Ҷ�Ӧ�Ŀⷿ������.
             * 	��ͬ�⣺
             * 		1��Ҳ�����һ�ڵ㡣
             * 		2�����Ա����ı䡣
             */
            String ydckdm = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YDCKDM, bindid);
            String processUid = QSCYBiz.getProcessUid(conn, ydckdm);
            return PrintUtil.parseNull(processUid);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return null;
        } catch (Exception e) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "����RuleJump�¼���������!");
            e.printStackTrace();
            return null;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
