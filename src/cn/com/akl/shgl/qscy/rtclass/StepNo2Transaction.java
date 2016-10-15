package cn.com.akl.shgl.qscy.rtclass;

import java.sql.Connection;

import cn.com.akl.shgl.qscy.biz.QSCYConstants;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

    public StepNo2Transaction(UserContext arg0) {
        super(arg0);
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        Integer parentBindid = getParameter(PARAMETER_PARENT_WORKFLOW_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            DAOUtil.connectRollBack(conn);
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            DAOUtil.connectRollBack(conn);
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台!", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }
}
