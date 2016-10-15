package cn.com.akl.shgl.jf.rtclass;

import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.jf.biz.DeliveryValidater;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StepNo2Validate extends WorkFlowStepRTClassA {

    private DeliveryValidater deliveryValidater = new DeliveryValidater();

    public StepNo2Validate() {
        super();
    }

    public StepNo2Validate(UserContext arg0) {
        super(arg0);
        setDescription("多节点校验：校验物料库存信息. 送修单是否还有效，送修单物料是否还能交付.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        int stepNo = 0;

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (isBack) {
            return true;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();
            return validate(conn, bindid, stepNo);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public boolean validate(Connection conn, int bindid, int stepNo) throws SQLException {
        return true;
    }

}
