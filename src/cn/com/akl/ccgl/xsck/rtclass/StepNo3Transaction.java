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
     * ��ѯ���ⵥ����.
     */
    private final String QUERY_CKD_BODY = "SELECT KWBH, SJSL, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
    /**
     * ��������.
     */
    private KCBiz kcbiz = new KCBiz();
    /**
     * ��������.
     */
    private FillBiz fillbiz = new FillBiz();

    public StepNo3Transaction() {
        super();
    }

    public StepNo3Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("������ת���¼�: ���¿��");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();
        boolean flag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "�˻�");
        if (flag == true) {
            return true;
        }

        boolean zt = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "����");

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();

            kcbiz.deleteLockBase(conn, bindid, getUserContext().getUID());

            // 2�����¿����ϸ
            // ��ѯ�ӱ��¼���ۼ����
            DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new DAOUtil.ResultPaser() {
                public boolean parse(Connection conn, ResultSet reset) throws SQLException {
                    kcbiz.outOfWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SJSL"));
                    kcbiz.outOfWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SJSL"));
                    return true;
                }
            }, bindid);

            // �������۶���״̬
            String xsddh = DAOUtil.getString(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
            DAOUtil.executeUpdate(conn, "Update BO_AKL_WXB_XSDD_HEAD Set DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_YFH, xsddh);

            // �����ж�.
            if (zt) {
                DAOUtil.executeUpdate(conn, "Update BO_AKL_WXB_XSDD_HEAD Set DDZT=? WHERE DDID=?", XSDDConstant.XSDD_DDZT_QRQS, xsddh);
                DAOUtil.executeUpdate(conn, "Update BO_AKL_CKD_HEAD Set CKZT=? WHERE BINDID=?", XSCKConstant.CKD_CKZT_QRQS, bindid);

                // ����ǩ�ռ�¼
                fillbiz.fillQSDHead(conn, bindid, uid);
                fillbiz.fillQSDBody(conn, bindid, uid);
                // ����Ӧ�ռ�¼
                fillbiz.insertYS(conn, bindid, getUserContext().getUID());

                // ����ǩ�յ���ǩ������.
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_QSD_P SET QSRQ=GETDATE() WHERE BINDID=?", bindid);
            } else {
                String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
                if ("��".equals(sfyy) || XSDDConstant.YES.equals(sfyy)) {
                    // ���ԤԼ��
                    fillbiz.fillYYD(conn, bindid, getUserContext().getUID());
                } else {
                    // ��ԤԼ ����˵�
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
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
