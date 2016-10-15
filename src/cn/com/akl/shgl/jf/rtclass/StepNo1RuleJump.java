package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;

public class StepNo1RuleJump extends WorkFlowStepJumpRuleRTClassA {

    public StepNo1RuleJump(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("若是异地交付，则进入第二节点审批，若不是则进行升级和缺货判断.");
    }

    @Override
    public int getNextNodeNo() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try {
            conn = DBSql.open();
            String sfydjf = DAOUtil.getStringOrNull(conn, "SELECT SFYDJF FROM BO_AKL_WXJF_P WHERE BINDID=?", bindid);
            String ywlx = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YWLX, bindid);

            if (DeliveryConstant.YWLX_ZS.equals(ywlx)) {
                return DeliveryConstant.STEP_ZPSH;
            }

            if (sfydjf.equals(XSDDConstant.YES)) {
                return DeliveryConstant.STEP_YDJFSH;
            } else {
                String sfsj = DAOUtil.getStringOrNull(conn, "SELECT SFSJ FROM BO_AKL_WXJF_S WHERE BINDID=? AND SFSJ=?", bindid, XSDDConstant.YES);
                if (sfsj == null || sfsj.equals("")) {
                    // 无升级记录.

                    String sfqhsq = DAOUtil.getStringOrNull(conn, "SELECT SFQHSQ FROM BO_AKL_WXJF_S WHERE BINDID=? AND SFQHSQ=?", bindid,
                            XSDDConstant.YES);
                    if (sfqhsq == null || sfqhsq.equals("")) {
                        // 无缺货申请记录.
                        return DeliveryConstant.STEP_TZKHQH;
                    } else {
                        // 有缺货申请记录.
                        return DeliveryConstant.STEP_QHSQ;
                    }
                } else {
                    // 有升级记录.
                    return DeliveryConstant.STEP_SJCL;
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return this.getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();
        } catch (Exception e) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "流程RuleJump事件出现问题!");
            e.printStackTrace();
            return this.getParameter(PARAMETER_WORKFLOW_STEP_NO).toInt();
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    @Override
    public String getNextTaskUser() {
        return null;
    }

}
