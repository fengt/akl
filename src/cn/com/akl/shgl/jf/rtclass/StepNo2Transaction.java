package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryBiz;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

    public StepNo2Transaction() {
        super();
    }

    public StepNo2Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("1、验证是否要缺货，若缺货则进行缺货信息插入。");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        boolean isOk = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
        if (isOk) {
            Connection conn = null;
            try {
                conn = DAOUtil.openConnectionTransaction();
                // 更新交付单的客服仓库.
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_P SET BDKFCKBM=YDJFKFBM,SFYDJF=? WHERE BINDID=?", XSDDConstant.NO, bindid);
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

        return true;
    }

}
