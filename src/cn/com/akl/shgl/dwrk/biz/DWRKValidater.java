package cn.com.akl.shgl.dwrk.biz;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;

public class DWRKValidater {

    /**
     * ��֤����״̬.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateWLZT(Connection conn, int bindid, String uid) throws SQLException {
        String dh = DAOUtil.getStringOrNull(conn, "SELECT DWRKDH FROM BO_AKL_SH_DWRK_P WHERE BINDID=?", bindid);

        /** ��ȡ����������״̬ */
        String clzt = DAOUtil.getStringOrNull(conn, "SELECT WLZT FROM BO_AKL_DFH_P WHERE DH=? AND (WLZT=? OR WLZT=?)", dh, DfhConstant.WLZT_YCL, DfhConstant.WLZT_YQS);
        if (clzt != null && !clzt.equals("")) {
            return true;
        } else {
            MessageQueue.getInstance().putMessage(uid, "������Ϣ��δ��д�����ܼ�������!");
            return false;
        }
    }

}
