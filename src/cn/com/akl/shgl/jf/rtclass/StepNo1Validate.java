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
        setDescription("多节点校验：校验物料库存信息. 送修单是否还有效，送修单物料是否还能交付.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
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
        String sxdh = getStringOrNull(conn, DeliveryConstant.QUERY_SXDH, bindid);
        String xmlb = getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String sfydjf = getStringOrNull(conn, DeliveryConstant.QUERY_SFYDJF, bindid);

        /**
         * 验证交付单头信息.
         */
        String sxdhV = getStringOrNull(conn, "SELECT SXDH FROM BO_AKL_SX_P WHERE SXDH=? AND ZT=?", sxdh, DeliveryConstant.SX_H_ZT_DJF);
        if (!sxdh.equals(sxdhV)) {
            throw new RuntimeException("送修单：" + sxdh + " 已经被交付完成！");
        }

        /**
         * 如果是异地交付，那么就验证送修记录不能填写.
         */
        if (XSDDConstant.YES.equals(sfydjf)) {
            Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE BINDID=?", bindid);
            if (count == null || count == 0) {
                return true;
            } else {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "异地交付时，请不要选择送修记录!");
                return false;
            }
        }


        PreparedStatement ps = null;
        ResultSet reset = null;

        /**
         * 验证交付送修单身.
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
         * 验证交付交付单身.
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

                // 检验未升级的一换多.
                if (XSDDConstant.NO.equals(clyj)) {
                    Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE SXCPHID=? AND BINDID=? AND CLFS=? AND SFSJ=? AND CLYJ=? AND ID<>?", sxcphid, bindid, DeliveryConstant.CLFS_HX, XSDDConstant.NO, XSDDConstant.NO, id);
                    if (count != null && count > 0) {
                        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET SFSJ=?,SJLX=? WHERE SXCPHID=? AND BINDID=? AND CLFS=?", XSDDConstant.YES, DeliveryConstant.SJLX_ZS, sxcphid, bindid, DeliveryConstant.CLFS_HX);
                        MessageQueue.getInstance().putMessage(uid, "存在一换多的物料已自动将一换多的 '是否升级' 改为 '是'! 送修行号:" + sxcphh);
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        /**
         * 验证配件数量.
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
         * 其他种类的一换多则视为非法数据.
         */
        ArrayList<String> qtcphidList = DAOUtil.getStringCollection(conn, "SELECT SXCPHID FROM BO_AKL_WXJF_S WHERE BINDID=? AND CLFS<>? GROUP BY SXCPHID HAVING COUNT(*)>1", bindid, DeliveryConstant.CLFS_HX);
        for (String sxcphid : qtcphidList) {
            String sxcphh = getStringOrNull(conn, "SELECT SXCPHH FROM BO_AKL_WXJF_S WHERE SXCPHID=?", sxcphid);
            MessageQueue.getInstance().putMessage(uid, "存在一换多的物料,但处理类型不是换新! 送修行号：" + sxcphh);
            return false;
        }

        /**
         * 验证送修单，业务类型.
         *  如果是赠送，那么就需要全体升级.
         *  如果有未同意的，需要将未同意的升级.
         */
        String ywlx = DAOUtil.getStringOrNull(conn, "SELECT YWLX FROM BO_AKL_SX_P WHERE SXDH=?", sxdh);
        if (DeliveryConstant.SJLX_ZS.equals(ywlx)) {
            // 判断是否已经升级过了.
            Integer sfsj = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE BINDID=? AND CLYJ=?", bindid, XSDDConstant.NO);
            // 如果还有没有升级完成的，那么就进行升级.
            // 未升级，则置换为赠送.
            if (sfsj != null && sfsj > 0) {
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET SFSJ=?,SJLX=? WHERE BINDID=? AND CLYJ=?", XSDDConstant.YES, DeliveryConstant.SJLX_ZS, bindid, XSDDConstant.NO);
            }
        }

        return true;
    }

}
