package cn.com.akl.shgl.yzsq.rtclass;

import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.yzsq.biz.YZSQConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by huangming on 2015/4/28.
 */
public class StepNo8Validate extends WorkFlowStepRTClassA {

    public StepNo8Validate() {
        super();
    }

    public StepNo8Validate(UserContext arg0) {
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
        String dh = DAOUtil.getStringOrNull(conn, YZSQConstant.QUERY_FORM_HEAD_YZSQDH, bindid);
        String uid = getUserContext().getUID();

        /** 获取待发货处理状态 */
        String clzt = DAOUtil.getStringOrNull(conn, "SELECT WLZT FROM BO_AKL_DFH_P WHERE DH=? AND (WLZT=? OR WLZT=?) AND JLBZ=?", dh, DfhConstant.WLZT_YCL, DfhConstant.WLZT_YQS, YZSQConstant.DFH_JLBS_ZTK);
        if (clzt != null && !clzt.equals("")) {
            return true;
        } else {
            MessageQueue.getInstance().putMessage(uid, "物流信息还未填写，不能继续办理，请通过物流流程填写本调拨单的物流信息!");
            return false;
        }

        /**  验证是否到达最终存档地. */

    }
}
