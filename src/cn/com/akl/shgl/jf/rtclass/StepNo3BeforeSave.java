package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.util.SQLUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

public class StepNo3BeforeSave extends WorkFlowStepRTClassA {

    public StepNo3BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("�滻�������룬���Ƶļ�¼�����к�.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

        Connection conn = null;
        try {
            conn = DBSql.open();
            if ("BO_AKL_WXJF_P".equals(tableName)) {
                dealHead(conn, bindid);
            } else if ("BO_AKL_WXJF_S".equals(tableName)) {
                dealBody(conn, bindid);
            }
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ������.<br/>
     * 1.��ѯ���޵�����û���Զ�����������¼�����ݣ������ɶ�Ӧ�Ľ�����¼.<br/>
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void dealBody(Connection conn, int bindid) throws SQLException, AWSSDKException {
    }

    /**
     * ����ͷ. <br/>
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void dealHead(Connection conn, int bindid) throws SQLException, AWSSDKException {
    }

}
