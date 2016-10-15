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
        setDescription("���·�Ʊ״̬�����޵���Ʊ״̬.");
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
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 1����Ʊ״̬����. 2�����޵�״̬����.
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

                // 1�����¿�Ʊ����״̬.
                int updateStateCount = DAOUtil.executeUpdate(conn, FpkpConstant.UPDATE_KPSQ_KPZT, FpkpConstant.FPZT_YKP, fpsqdh);
                if (updateStateCount != 1) {
                    throw new RuntimeException("��Ʊ״̬����ʧ��!");
                }

                Integer fpsqbindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_FPSQ WHERE FPSQDH=?", fpsqdh);
                if (fpsqbindid == null) {
                    throw new RuntimeException("�޷���ѯ����Ʊ����ĵ���!");
                } else {
                    dealFpsxSxd(conn, fpsqbindid);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * ����Ʊ��������޵�״̬.
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
                    throw new RuntimeException("���޵���Ʊ״̬����ʧ��!");
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
