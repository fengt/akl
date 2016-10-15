package cn.com.akl.shgl.yzsq.rtclass;

import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.yzsq.biz.YZSQConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by huangming on 2015/4/28.
 */
public class StepNo7Validate extends WorkFlowStepRTClassA {

    public StepNo7Validate() {
        super();
    }

    public StepNo7Validate(UserContext arg0) {
        super(arg0);
        setDescription("验证物流信息是否填写. 验证是否到达最终存档地.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DBSql.open();
            return true;//validate(conn, bindid);
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
}
