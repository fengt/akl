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
        setDescription("�ۼ���棬���������������Ϣ.");
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
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
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
        // ɾ�����⣬�ۼ����.
        Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_SH_DWCK_P", bindid);
        dwckBiz.removeLock(conn, bindid);
        dwckBiz.deductInventory(conn, bindid, getUserContext().getUID());

        // ����ǩ������Ĭ��ֵ.
        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SH_DWCK_S SET QSSL=SJCKSL WHERE BINDID=?", bindid);

        // ������������������������Ϣ.
        int taskId = getParameter(PARAMETER_TASK_ID).toInt();
        boolean isInsert = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "��������");
        if (isInsert) {
            // �������������Ϣ.
            int taskid = getParameter(PARAMETER_TASK_ID).toInt();
            boolean isChuku = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "��������");
            if (isChuku) {
                dwckBiz.insertWLDate(conn, bindid, getUserContext().getUID(), boData);
            }
        }
    }
}
