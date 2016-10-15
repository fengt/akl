package cn.com.akl.shgl.fpkp.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.shgl.fpsq.constant.FpsqConstant;
import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo5Transaction extends WorkFlowStepRTClassA {

    public StepNo5Transaction() {
        super();
    }

    public StepNo5Transaction(UserContext arg0) {
        super(arg0);
        setDescription("根据不同的人收取不同的单据.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

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
     * 对当前办理者应处理的发票进行处理.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException {
        String uid = getUserContext().getUID();
        QSCYBiz qscyBiz = new QSCYBiz();

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(FpkpConstant.QUERY_FPKP_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String fpsqdh = reset.getString("FPSQDH");
                String fpfhfs = reset.getString("FPFHFS");
                String kfckbm = reset.getString("KFCKBM");
                String qszt = reset.getString("QSZT");
                String pUid = qscyBiz.getProcessUid(conn, kfckbm);

                if (pUid == null || pUid.equals("")) {
                    throw new RuntimeException("客服仓库:" + kfckbm + " 没有维护对应的部门!");
                }

                // 1、过滤当前办理者的单据.
                if (pUid.indexOf(uid) != -1 || uid.equals("admin")) {
                    // 1.1、根据填写的签收状态来反向更新发票的状态.
                    if (FpkpConstant.QSZT_DS.equals(qszt)) {
                        DAOUtil.executeUpdate(conn, FpkpConstant.UPDATE_KPSQ_KPZT, FpkpConstant.FPZT_DS, fpsqdh);
                    } else if (FpkpConstant.QSZT_QS.equals(qszt)) {
                        DAOUtil.executeUpdate(conn, FpkpConstant.UPDATE_KPSQ_KPZT, FpkpConstant.FPZT_YQS, fpsqdh);
                    } else {
                        throw new RuntimeException("发票签收状态：" + qszt + "， 不能识别！");
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }
}
