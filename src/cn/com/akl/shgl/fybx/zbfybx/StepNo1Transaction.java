package cn.com.akl.shgl.fybx.zbfybx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fybx.cnt.FYBXCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

    public StepNo1Transaction(UserContext arg0) {
        super(arg0);
        setDescription("总部费用报销状态更新。");
    }

    @Override
    public boolean execute() {
        String uid = getUserContext().getUID();
        int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, bindid);
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现异常，请检查控制台", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    public void service(Connection conn, int bindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(FYBXCnt.QUERY_ZBFY_S);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String xtdh = reset.getString("XTDH");
                String xmlb = reset.getString("XMLB");
                Integer ydBindid = DAOUtil.getIntOrNull(conn, "SELECT bindid FROM BO_AKL_WLYSD_P WHERE YKDH=?", xtdh);
                int updateCount = DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WLYSD_XM_S SET ZBBXZT=? WHERE BINDID=? AND XMLB=? AND ISNULL(ZBBXZT,'0')=?", FYBXCnt.KFFYBXZT_YBX, ydBindid, xmlb, FYBXCnt.KFFYBXZT_WBX);
                if (updateCount != 1) {
                    throw new RuntimeException("单号:" + xtdh + " 状态更新不成功，请检查是否已经报销过了!");
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
