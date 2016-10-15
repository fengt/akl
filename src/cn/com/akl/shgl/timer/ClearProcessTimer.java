package cn.com.akl.shgl.timer;

import cn.com.akl.util.DAOUtil;
import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by huangming on 2015/7/8.
 * <p/>
 * 清除无用的送修交付记录.
 */
public class ClearProcessTimer implements IJob {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Connection conn = null;
        PreparedStatement state = null;
        ResultSet reset = null;

        try {
            conn = DBSql.open();
            // 获取送修和交付录入节点的任务.
            state = conn.prepareStatement("SELECT BIND_ID FROM WF_TASK WHERE ((WFID=32799 AND WFSID=32801) OR (WFID=33659 AND WFSID=33661)) AND STATUS=1");
            reset = state.executeQuery();
            int i=0;
            while (reset.next()) {
                int bindid = reset.getInt("BIND_ID");
                Integer logTaskCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM WF_TASK_LOG WHERE BIND_ID=?", bindid);
                if (logTaskCount == null || logTaskCount == 0) {
                    WorkflowInstanceAPI.getInstance().removeProcessInstance(bindid);
                    i++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AWSSDKException e) {
            e.printStackTrace();
        } finally {
            DBSql.close(conn, state, reset);
        }
    }
}
