package cn.com.akl.shgl.dwck.rtclass;

import cn.com.akl.shgl.dwck.biz.DWCKBiz;
import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.shgl.qscy.biz.QSCYConstants;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

public class StepNo5Transaction extends WorkFlowStepRTClassA {

    private DWCKBiz dwckBiz = new DWCKBiz();
    private QSCYBiz qscyBiz = new QSCYBiz();

    public StepNo5Transaction() {
        super();
    }

    public StepNo5Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("ǩ������¼�룬����ǩ�ղ����������в�������.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Integer wldBindid = null;
        Hashtable<String, String> main = BOInstanceAPI.getInstance().getBOData("BO_AKL_SH_DWCK_P", bindid);

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, bindid);
            wldBindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_WLYSD_S WHERE CKDH=?", main.get("DWCKDH"));
            if (wldBindid == null || wldBindid == 0) {
                throw new RuntimeException("�˵�������δ��д������Ϣ!");
            }
            conn.commit();

            startSubProcess(conn, bindid, main, wldBindid);
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ��ǩ��������⣬�����������������ǩ�ղ���������.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();

        // ���´�������¼��ǩ������.
        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DFH_S", bindid);
        dwckBiz.insertWLDateBody(conn, bindid, uid);
    }

    /**
     * ��������������.
     *
     * @param conn
     * @param bindid
     */
    public void startSubProcess(Connection conn, int bindid, Hashtable<String, String> boData, int ykwldh) {
        String dbdh = boData.get("DWCKDH");
        String fhsj = boData.get("FHSJ");
        String fhf = boData.get("FHF");
        String shkfckdm = boData.get("FHKFCKBM");
        String shf = boData.get("SHF");

        String uid = getUserContext().getUID();

        // ��֤�Ƿ�Ҫ��������.
        Integer cyCount = null;
        try {
            cyCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SH_DWCK_S WHERE BINDID=? AND SJCKSL<>QSSL", bindid);
        } catch (SQLException e1) {
            e1.printStackTrace();
            MessageQueue.getInstance().putMessage("��̨���ִ���!", uid);
        }

        if (cyCount != null && cyCount > 0) {
            String pUid = null;
            pUid = qscyBiz.getProcessUid(conn, shkfckdm);
            pUid = uid;

            // ƴ�ӱ���.
            StringBuilder titleSb = new StringBuilder();
            titleSb.append("ǩ�ղ���").append("--���������������--����:").append(dbdh).append("--������:").append(fhf).append("--�ջ���:").append(shf).append("--����ʱ��")
                    .append(fhsj);

            // ƴ�ӱ���.
            Hashtable<String, String> wldHashtable = QSCYBiz.getWldHashtable(ykwldh);
            wldHashtable.put("CKDH", dbdh);
            wldHashtable.put("CYLX", QSCYConstants.CYLX_SLCY);
            qscyBiz.startQSCYProcess(conn, wldHashtable, uid, pUid, titleSb.toString());
        }
    }
}
