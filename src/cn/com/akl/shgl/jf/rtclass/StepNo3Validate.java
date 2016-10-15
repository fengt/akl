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
        setDescription("��ڵ�У�飺У�����Ͽ����Ϣ. ���޵��Ƿ���Ч�����޵������Ƿ��ܽ���.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        int stepNo = 0;

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");
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
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ��һ�ڵ�У��.
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
         * ��֤������������.
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
                        MessageQueue.getInstance().putMessage(uid, "���Ƿ�������Ϊ'��'ʱ������Ҫ��д'�Ƿ�ͬ������'!");
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
