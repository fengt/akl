package cn.com.akl.shgl.fpkp.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

    public StepNo3Transaction() {
        super();
    }

    public StepNo3Transaction(UserContext arg0) {
        super(arg0);
        setDescription("更新发票状态和送修单开票状态.");
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

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, bindid);
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 1、发票状态更新. 2、送修单状态更新.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(FpkpConstant.QUERY_FPKP_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String fpsqdh = reset.getString("FPSQDH");

                // 1、更新开票单据状态.
                int updateStateCount = DAOUtil.executeUpdate(conn, FpkpConstant.UPDATE_KPSQ_KPZT, FpkpConstant.FPZT_YKP, fpsqdh);
                if (updateStateCount != 1) {
                    throw new RuntimeException("发票状态更新失败!");
                }

                Integer fpsqbindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_FPSQ WHERE FPSQDH=?", fpsqdh);
                if (fpsqbindid == null) {
                    throw new RuntimeException("无法查询到发票申请的单据!");
                } else {
                    dealFpsxSxd(conn, fpsqbindid);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 处理发票申请的送修单状态.
     *
     * @param conn
     * @param fpsqbindid
     * @throws SQLException
     */
    public void dealFpsxSxd(Connection conn, int fpsqbindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_FPSQ_S WHERE BINDID=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, fpsqbindid);
            while (reset.next()) {
                String sxdh = reset.getString("SXDH");
                int updateStateCount = DAOUtil.executeUpdate(conn, FpkpConstant.UPDATE_SXD_KPZT, XSDDConstant.YES, sxdh);
                if (updateStateCount != 1) {
                    throw new RuntimeException("送修单开票状态更新失败!");
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
