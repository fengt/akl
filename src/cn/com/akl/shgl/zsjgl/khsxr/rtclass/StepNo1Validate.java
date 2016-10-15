package cn.com.akl.shgl.zsjgl.khsxr.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

    public StepNo1Validate() {
        super();
    }

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setDescription("验证客户信息维护时，是否存在重复记录.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

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
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_SXR WHERE BINDID=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String sxr = reset.getString("SXR");
                String dh = reset.getString("DH");
                String sjh = reset.getString("SJH");
                String khbh = reset.getString("KHBH");
                int id = reset.getInt("ID");

                Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SXR WHERE SXR=? AND DH=? AND SJH=? AND KHBH=? AND ID<>? AND (ISEND=1 OR BINDID=?)",
                        PrintUtil.parseNull(sxr), PrintUtil.parseNull(dh), PrintUtil.parseNull(sjh), PrintUtil.parseNull(khbh),
                        id, bindid);
                if (count != null && count > 0) {
                    MessageQueue.getInstance().putMessage(getUserContext().getUID(),
                            "系统已存在此记录，送修人：" + sxr + "，电话：" + dh + "，手机号：" + sjh + "。");
                    return false;
                }
            }
            return true;
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
