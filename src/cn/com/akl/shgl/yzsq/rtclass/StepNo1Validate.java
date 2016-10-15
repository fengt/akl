package cn.com.akl.shgl.yzsq.rtclass;

import cn.com.akl.shgl.yzsq.biz.YZSQConstant;
import cn.com.akl.shgl.yzsq.biz.YZSQValidate;
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
public class StepNo1Validate extends WorkFlowStepRTClassA {

    public StepNo1Validate() {
        super();
    }

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setDescription("校验资质的当前所在地是否在存档地，是否没有被其他用章申请占用.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        YZSQValidate validate = new YZSQValidate();
        Connection conn = null;
        try {
            conn = DBSql.open();
            return validate.validateStepNo1(conn, bindid, uid);
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
