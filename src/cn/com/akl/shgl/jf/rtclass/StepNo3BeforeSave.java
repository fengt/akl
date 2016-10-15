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
        setDescription("替换规则申请，复制的记录生成行号.");
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
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 处理单身.<br/>
     * 1.查询送修单身中没有自动带出交付记录的数据，并生成对应的交付记录.<br/>
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void dealBody(Connection conn, int bindid) throws SQLException, AWSSDKException {
    }

    /**
     * 处理单头. <br/>
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void dealHead(Connection conn, int bindid) throws SQLException, AWSSDKException {
    }

}
