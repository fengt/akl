package cn.com.akl.shgl.jf.rtclass;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
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

public class StepNo3Validate extends WorkFlowStepRTClassA {

    private DeliveryValidater deliveryValidater = new DeliveryValidater();

    public StepNo3Validate() {
        super();
    }

    public StepNo3Validate(UserContext arg0) {
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
            stepNo = WorkFlowUtil.getProcessInstanceStepNo(conn, bindid);
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
     * 第一节点校验.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public boolean validate(Connection conn, int bindid, int stepNo) throws SQLException {
        String uid = getUserContext().getUID();
        String sxdh = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_SXDH, bindid);
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
                String kthwlbh1 = reset.getString("KTHWLBH");
                String thfacp = reset.getString("THFACP");
                int id = reset.getInt("ID");
                String clyj = reset.getString("CLYJ");
                String sfsj = reset.getString("SFSJ");
                if (XSDDConstant.YES.equals(sfsj)) {
                    if (clyj == null || clyj.equals("")) {
                        MessageQueue.getInstance().putMessage(uid, "‘是否升级’为'是'时，必须要填写'是否同意升级'!");
                        return false;
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        return true;
    }

}
