package cn.com.akl.shgl.fpsq.rtclass;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.fpsq.constant.FpsqConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by huangming on 2015/4/29.
 */
public class StepNo1Transaction extends WorkFlowStepRTClassA {

    public StepNo1Transaction() {
        super();
    }

    public StepNo1Transaction(UserContext arg0) {
        super(arg0);
        setDescription("发票申请更新交付产品的信息.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            //service(conn, bindid);
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

    public void service(Connection conn, int bindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(FpsqConstant.QUEYR_FPSQ_DH);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String fpsqdh = reset.getString("JFHH");
                // 更新交付产品记录为已开票.
                DAOUtil.executeUpdate(conn, FpsqConstant.UPDATE_JFCP_KPXX, XSDDConstant.YES, fpsqdh, XSDDConstant.NO);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}


