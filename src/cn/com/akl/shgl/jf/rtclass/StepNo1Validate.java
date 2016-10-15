package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.jf.biz.DeliveryValidater;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import static cn.com.akl.util.DAOUtil.getStringOrNull;

public class StepNo1Validate extends WorkFlowStepRTClassA {

    private DeliveryValidater deliveryValidater = new DeliveryValidater();

    public StepNo1Validate() {
        super();
    }

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setDescription("��ڵ�У�飺У�����Ͽ����Ϣ. ���޵��Ƿ���Ч�����޵������Ƿ��ܽ���.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");
        if (isBack) {
            return true;
        }

        int nextStepNo;
        try {
            nextStepNo = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, bindid, taskid);
        } catch (AWSSDKException e1) {
            e1.printStackTrace();
            return false;
        }

        Connection conn = null;
        try {
            conn = DBSql.open();
            return validate(conn, bindid, nextStepNo);
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
        String sxdh = getStringOrNull(conn, DeliveryConstant.QUERY_SXDH, bindid);
        String xmlb = getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String sfydjf = getStringOrNull(conn, DeliveryConstant.QUERY_SFYDJF, bindid);

        /**
         * ��֤������ͷ��Ϣ.
         */
        String sxdhV = getStringOrNull(conn, "SELECT SXDH FROM BO_AKL_SX_P WHERE SXDH=? AND ZT=?", sxdh, DeliveryConstant.SX_H_ZT_DJF);
        if (!sxdh.equals(sxdhV)) {
            throw new RuntimeException("���޵���" + sxdh + " �Ѿ���������ɣ�");
        }

        /**
         * �������ؽ�������ô����֤���޼�¼������д.
         */
        if (XSDDConstant.YES.equals(sfydjf)) {
            Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE BINDID=?", bindid);
            if (count == null || count == 0) {
                return true;
            } else {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��ؽ���ʱ���벻Ҫѡ�����޼�¼!");
                return false;
            }
        }


        PreparedStatement ps = null;
        ResultSet reset = null;

        /**
         * ��֤�������޵���.
         */
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_SXDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                deliveryValidater.validateSxInfo(conn, reset, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }

        /**
         * ��֤������������.
         */
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String sfsj = reset.getString("SFSJ");
                String id = reset.getString("ID");
                String sxcphid = reset.getString("SXCPHID");
                String sxcphh = reset.getString("SXCPHH");
                String clyj = reset.getString("CLYJ");
                deliveryValidater.validateJfInfo(conn, reset, bindid);
                if (!XSDDConstant.YES.equals(sfsj)) {
                    //deliveryValidater.validateRepository(conn, bindid, uid, reset, xmlb);
                }

                // ����δ������һ����.
                if (XSDDConstant.NO.equals(clyj)) {
                    Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE SXCPHID=? AND BINDID=? AND CLFS=? AND SFSJ=? AND CLYJ=? AND ID<>?", sxcphid, bindid, DeliveryConstant.CLFS_HX, XSDDConstant.NO, XSDDConstant.NO, id);
                    if (count != null && count > 0) {
                        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET SFSJ=?,SJLX=? WHERE SXCPHID=? AND BINDID=? AND CLFS=?", XSDDConstant.YES, DeliveryConstant.SJLX_ZS, sxcphid, bindid, DeliveryConstant.CLFS_HX);
                        MessageQueue.getInstance().putMessage(uid, "����һ������������Զ���һ����� '�Ƿ�����' ��Ϊ '��'! �����к�:" + sxcphh);
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        /**
         * ��֤�������.
         */
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_PJDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                if (stepNo == DeliveryConstant.STEP_LRXX) {
                    deliveryValidater.validatePart(conn, reset, bindid, xmlb);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        /**
         * ���������һ��������Ϊ�Ƿ�����.
         */
        ArrayList<String> qtcphidList = DAOUtil.getStringCollection(conn, "SELECT SXCPHID FROM BO_AKL_WXJF_S WHERE BINDID=? AND CLFS<>? GROUP BY SXCPHID HAVING COUNT(*)>1", bindid, DeliveryConstant.CLFS_HX);
        for (String sxcphid : qtcphidList) {
            String sxcphh = getStringOrNull(conn, "SELECT SXCPHH FROM BO_AKL_WXJF_S WHERE SXCPHID=?", sxcphid);
            MessageQueue.getInstance().putMessage(uid, "����һ���������,���������Ͳ��ǻ���! �����кţ�" + sxcphh);
            return false;
        }

        /**
         * ��֤���޵���ҵ������.
         *  ��������ͣ���ô����Ҫȫ������.
         *  �����δͬ��ģ���Ҫ��δͬ�������.
         */
        String ywlx = DAOUtil.getStringOrNull(conn, "SELECT YWLX FROM BO_AKL_SX_P WHERE SXDH=?", sxdh);
        if (DeliveryConstant.SJLX_ZS.equals(ywlx)) {
            // �ж��Ƿ��Ѿ���������.
            Integer sfsj = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE BINDID=? AND CLYJ=?", bindid, XSDDConstant.NO);
            // �������û��������ɵģ���ô�ͽ�������.
            // δ���������û�Ϊ����.
            if (sfsj != null && sfsj > 0) {
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET SFSJ=?,SJLX=? WHERE BINDID=? AND CLYJ=?", XSDDConstant.YES, DeliveryConstant.SJLX_ZS, bindid, XSDDConstant.NO);
            }
        }

        return true;
    }

}
