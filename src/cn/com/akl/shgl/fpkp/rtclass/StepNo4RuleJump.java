package cn.com.akl.shgl.fpkp.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;

public class StepNo4RuleJump extends WorkFlowStepJumpRuleRTClassA {

    public StepNo4RuleJump(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("用于发给收货的客服.");
    }

    @Override
    public int getNextNodeNo() {
        return 0;
    }

    @Override
    public String getNextTaskUser() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet reset = null;
        QSCYBiz qscyBiz = new QSCYBiz();
        StringBuilder uidsSb = new StringBuilder(50);
        try {
            conn = DBSql.open();
            ps = conn.prepareStatement(FpkpConstant.QUERY_FPKP_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                // TODO：根据客服确定收货人.
                String kfckbm = reset.getString("KFCKBM");
                String kfckmc = reset.getString("KFCKMC");
                String uid = qscyBiz.getProcessUid(conn, kfckbm);
                if (uid == null || uid.equals("")) {
                    MessageQueue.getInstance().putMessage("客服仓库：" + kfckmc + "没有维护对应的办理人.", getUserContext().getUID());
                    uidsSb.append("admin");
                } else {
                    uidsSb.append(uid);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBSql.close(conn, ps, reset);
        }
        return uidsSb.toString();
    }

}
