package cn.com.akl.shgl.fybx.fwfybx;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.fpsq.constant.FpsqConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by huangming on 2015/4/29.
 */
public class StepNo1Validate extends WorkFlowStepRTClassA {

    public StepNo1Validate() {
        super();
    }

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setDescription("验证报销类型是否填写.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
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
        String uid = getUserContext().getUID();
        Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_FWZXBX_FPHZ_S WHERE BINDID=? AND (BXLX='' OR BXLX is NULL)", bindid);
        MessageQueue.getInstance().putMessage(uid, "请填写报销类型!");
        return count == null || count == 0;
    }
}


