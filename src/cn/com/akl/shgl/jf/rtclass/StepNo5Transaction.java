package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo5Transaction extends WorkFlowStepRTClassA {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    public StepNo5Transaction() {
        super();
    }

    public StepNo5Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("缺货申请库存验证，缺货信息作废处理.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (isBack) {
            BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_QHJL", bindid);
            return true;
        }

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, bindid);
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            DAOUtil.connectRollBack(conn);
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            DAOUtil.connectRollBack(conn);
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
        fetchAndLockMaterialQHSQ(conn, bindid, getUserContext().getUID());
        cancelQhjl(conn, bindid);

        /**更新交付记录状态.*/
        DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_JFJL_ZT, DeliveryConstant.JF_JLZT_YTZ, bindid);
    }

    /**
     * 将未起作用的缺货申请的记录作废.
     *
     * @param conn
     * @param bindid
     */
    public void cancelQhjl(Connection conn, int bindid) throws SQLException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String sxdh = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_SXDH, bindid);

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String sx = reset.getString("SX");
                String pch = reset.getString("PCH");
                String ckdm = reset.getString("CKDM");
                String hwdm = reset.getString("HWDM");
                String hh = reset.getString("HH");
                String sfqhsq = reset.getString("SFQHSQ");
                String yjfhh = reset.getString("YJFHH");
                int sl = reset.getInt("SL");
                String sfjf = reset.getString("SFJF");

                if (XSDDConstant.YES.equals(sfqhsq)) {
                    // 将未生效的缺货记录作废.
                    DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_QHJL SET ZT=? WHERE JFCPHH=? AND SXDH=? AND (ZT IS NULL OR ZT='')", QHSQCnt.zt2, hh, sxdh);

                    // 不交付的时候，缺货子流程无效.
                    if (XSDDConstant.NO.equals(sfjf)) {
                        // 删除缺货申请中的本交付行号记录.
                        DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_QHSQ_S WHERE JFCPHH=?", hh);
                    }
                }
            }
        } finally

        {
            DBSql.close(ps, reset);
        }

    }

    /**
     * 对缺货申请成功的物料进行锁库.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void fetchAndLockMaterialQHSQ(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String sx = reset.getString("SX");
                String pch = reset.getString("PCH");
                String ckdm = reset.getString("CKDM");
                String hwdm = reset.getString("HWDM");
                int sl = reset.getInt("SL");
                int id = reset.getInt("ID");
                String sfqhsq = reset.getString("SFQHSQ");
                String clfs = reset.getString("CLFS");
                String sfjf = reset.getString("SFJF");
                if (XSDDConstant.YES.equals(sfqhsq) && DeliveryConstant.CLFS_HX.equals(clfs) && XSDDConstant.YES.equals(sfjf)) {
                    boolean isOk = repositoryBiz.autoFetch1(conn, bindid, uid, xmlb, wlbh, xh, ckdm, sx, sl, "UPDATE BO_AKL_WXJF_S SET PCH=?, HWDM=? WHERE ID=?", id);
                    if (!isOk) {
                        throw new RuntimeException("PN:" + xh + ", 数量不足!");
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }
}
