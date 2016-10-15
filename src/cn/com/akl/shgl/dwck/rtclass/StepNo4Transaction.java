package cn.com.akl.shgl.dwck.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.dwck.biz.DWCKBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

    private DWCKBiz dwckBiz = new DWCKBiz();

    public StepNo4Transaction() {
        super();
    }

    public StepNo4Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("扣减库存，插入待发货物流信息.");
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
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        // 删除锁库，扣减库存.
        Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_SH_DWCK_P", bindid);
        dwckBiz.removeLock(conn, bindid);
        dwckBiz.deductInventory(conn, bindid, getUserContext().getUID());

        // 更新签收数量默认值.
        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SH_DWCK_S SET QSSL=SJCKSL WHERE BINDID=?", bindid);

        // 如果是物流出库则插入物流信息.
        int taskId = getParameter(PARAMETER_TASK_ID).toInt();
        boolean isInsert = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "物流出库");
        if (isInsert) {
            // 出库插入物流信息.
            int taskid = getParameter(PARAMETER_TASK_ID).toInt();
            boolean isChuku = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "物流出库");
            if (isChuku) {
                dwckBiz.insertWLDate(conn, bindid, getUserContext().getUID(), boData);
            }
        }
    }
}
