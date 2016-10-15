package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.jf.biz.DeliveryValidater;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo5Validate extends WorkFlowStepRTClassA {

    private DeliveryValidater deliveryValidater = new DeliveryValidater();

    public StepNo5Validate(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("验证库存是否有.");
    }

    @Override
    public boolean execute() {
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (isBack) {
            return true;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();
            return validate(conn, bindid);
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
    public boolean validate(Connection conn, int bindid) throws SQLException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);

        PreparedStatement ps = null;
        ResultSet reset = null;

        /**
         * 验证交付交付单身.
         */
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String sfqhsq = reset.getString("SFQHSQ");
                String sfjf = reset.getString("SFJF");
                if (XSDDConstant.YES.equals(sfqhsq) && XSDDConstant.YES.equals(sfjf)) {
                    deliveryValidater.validateRepository2(conn, reset, bindid, xmlb);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        return true;
    }

}
