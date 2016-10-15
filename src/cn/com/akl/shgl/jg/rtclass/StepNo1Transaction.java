package cn.com.akl.shgl.jg.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.jg.biz.JGBiz;
import cn.com.akl.shgl.jg.biz.JGConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

    private JGBiz jgBiz = new JGBiz();

    public StepNo1Transaction() {
        super();
    }

    public StepNo1Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("插入锁库.");
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
     * @throws AWSSDKException
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        String xmlb = DAOUtil.getStringOrNull(conn, JGConstant.QUERY_XMLB, bindid);

        jgBiz.removeLock(conn, bindid);
        jgBiz.insertPjLock(conn, bindid, uid, xmlb);

        //插入加工完成汇总
        jgBiz.insertHz(conn, bindid, uid, xmlb);

        String jglx = DAOUtil.getStringOrNull(conn, JGConstant.QUERY_JGLX, bindid);

        /**
         * 正常加工，扣减消耗库存.
         * 非正常加工，插入消耗锁库.
         */
        if (JGConstant.JGLX_ZCJG.equals(jglx)) {
            jgBiz.dealXh(conn, bindid, JGConstant.subtract, jglx);
        } else {
            jgBiz.insertXhLock(conn, bindid, uid, xmlb);
        }
    }

}
