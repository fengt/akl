package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryBiz;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

    public StepNo3Transaction() {
        super();
    }

    public StepNo3Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("升级物料校验");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();

            PreparedStatement ps = null;
            ResultSet reset = null;
            try {
                ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
                reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
                while (reset.next()) {
                    String kthwlbh1 = reset.getString("KTHWLBH");
                    String thfacp = reset.getString("THFACP");
                    int id = reset.getInt("ID");
                    String clyj = reset.getString("CLYJ");
                    String sfsj = reset.getString("SFSJ");
                    if (XSDDConstant.YES.equals(sfsj) && XSDDConstant.YES.equals(clyj)) {
                        // 将升级的物料插入可升级的物料中.
                        if (thfacp != null && !thfacp.equals("")) {
                            DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET KTHWLBH=? WHERE ID=?", kthwlbh1 + "," + thfacp, id);
                        }
                        // 重置升级信息.
                        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET SFSJ=?,SHJG=? WHERE ID=?", XSDDConstant.NO, DeliveryConstant.SHJG_TY, id);
                    }

                    if (XSDDConstant.YES.equals(sfsj) && XSDDConstant.NO.equals(clyj)) {
                        // 重置升级信息.
                        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET SHJG=? WHERE ID=?", DeliveryConstant.SHJG_BH, id);
                    }
                }
            } finally {
                DBSql.close(ps, reset);
            }

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

}
