package cn.com.akl.shgl.yzsq.rtclass;

import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.yzsq.biz.YZSQBiz;
import cn.com.akl.shgl.yzsq.biz.YZSQConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by huangming on 2015/4/28.
 */
public class StepNo8Transaction extends WorkFlowStepRTClassA {

    private YZSQBiz yzsqBiz = new YZSQBiz();

    public StepNo8Transaction() {
        super();
    }

    public StepNo8Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("更新资质所在地.");
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
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        serviceInsertDfhs(conn, bindid);
    }

    /**
     * 插入待发货单身信息。
     *
     * @param conn
     * @param bindid
     */
    public void serviceInsertDfhs(Connection conn, int bindid) throws SQLException, AWSSDKException {
        // 插入待发货记录.
        String uid = getUserContext().getUID();

        PreparedStatement stat = null;
        ResultSet reset = null;
        try {
            stat = conn.prepareStatement(YZSQConstant.QUERY_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, stat, bindid);
            while (reset.next()) {
                String zz = reset.getString("ZZ");
                String zzlx = reset.getString("ZZLX");
                int zzid = reset.getInt("ZZID");
                // 更新用章所在地.
                int updateCount = yzsqBiz.updateInPalce(conn, zz, zzid, DfhConstant.XZKFBM);
                if (updateCount != 1) {
                    throw new RuntimeException("更新存档地失败!");
                }
            }
        } finally {
            DBSql.close(stat, reset);
        }
    }

}
