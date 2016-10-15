package cn.com.akl.shgl.sx.rtclass;

import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

import java.sql.Connection;

/**
 * Created by huangming on 2015/7/22.
 */
public class StepNo6BeforeSave extends WorkFlowStepRTClassA {


    public StepNo6BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("防止流程结束后仍然有暂存!");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();
        Connection conn = null;
        try {
            conn = DBSql.open();
            Integer isEnd = DAOUtil.getIntOrNull(conn, "SELECT ISEND FROM BO_AKL_SX_P WHERE BINDID=?", bindid);
            if (isEnd != null && isEnd == 1) {
                MessageQueue.getInstance().putMessage(uid, "流程已经结束!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBSql.close(conn, null, null);
        }

        return true;
    }
}
