package cn.com.akl.ccgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

    /**
     * 查询出库单单身.
     */
    private final String QUERY_CKD_BODY = "SELECT KWBH, SJSL, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
    /**
     * 库存操作类.
     */
    private KCBiz kcbiz = new KCBiz();
    /**
     * 填充操作类.
     */
    private FillBiz fillbiz = new FillBiz();

    public StepNo3Transaction() {
        super();
    }

    public StepNo3Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("流程流转后事件: 更新库存");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        boolean flag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "退回");
        if (flag == true) {
            return true;
        }

        boolean zt = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "自提");

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();

            kcbiz.deleteLockBase(conn, bindid, getUserContext().getUID());

            // 2、更新库存明细
            // 查询子表记录，扣减库存
            DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new DAOUtil.ResultPaser() {
                public boolean parse(Connection conn, ResultSet reset) throws SQLException {
                    kcbiz.outOfWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SJSL"));
                    kcbiz.outOfWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SJSL"));
                    return true;
                }
            }, bindid);

            // 更新销售订单状态
            String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
            DAOUtil.executeUpdate(conn, "Update BO_AKL_WXB_XSDD_HEAD Set DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_YFH, xsddh);

            // 自提判断.
            if (zt) {
                DAOUtil.executeUpdate(conn, "Update BO_AKL_WXB_XSDD_HEAD Set DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_QRQS, xsddh);
                DAOUtil.executeUpdate(conn, "Update BO_AKL_CKD_HEAD Set CKZT=? WHERE BINDID=?", XSCKConstant.CKD_CKZT_QRQS, bindid);

                // 插入签收记录
                fillbiz.fillQSDHead(conn, bindid, uid);
                fillbiz.fillQSDBody(conn, bindid, uid);
                // 插入应收记录
                fillbiz.insertYS(conn, bindid, getUserContext().getUID());

                // 更新签收单的签收日期.
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_QSD_P SET QSRQ=GETDATE() WHERE BINDID=?", bindid);
            } else {
                String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
                if ("是".equals(sfyy) || XSDDConstant.YES.equals(sfyy)) {
                    // 填充预约单
                    fillbiz.fillYYD(conn, bindid, getUserContext().getUID());
                } else {
                    // 非预约 填充运单
                    fillbiz.fillYD(conn, bindid, getUserContext().getUID());
                }
            }

            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
