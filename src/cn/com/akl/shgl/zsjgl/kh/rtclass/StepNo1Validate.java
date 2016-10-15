package cn.com.akl.shgl.zsjgl.kh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

    public StepNo1Validate() {
        super();
    }

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setDescription("��֤�ͻ���Ϣά��ʱ���Ƿ�����ظ���¼.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DBSql.open();
            return validate(conn, bindid);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
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
    public boolean validate(Connection conn, int bindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_SH_KH WHERE BINDID=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String khmc = reset.getString("KHMC");
                String khlx = reset.getString("KHLX");
                String xmlx = reset.getString("XMLX");
                String sjh = reset.getString("SJH");
                String dh = reset.getString("DH");
                int id = reset.getInt("ID");

                Integer sjhCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SH_KH WHERE SJH=? AND SJH<>'' AND SJH IS NOT NULL AND ID<>? AND XMLX=? AND (ISEND=1 OR BINDID=?)", sjh, id, xmlx, bindid);
                Integer dhCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SH_KH WHERE DH=? AND DH<>'' AND DH IS NOT NULL AND ID<>? AND XMLX=? AND (ISEND=1 OR BINDID=?)", dh, id, xmlx, bindid);

                if (sjhCount != null && sjhCount > 0) {
                    MessageQueue.getInstance().putMessage(
                            getUserContext().getUID(),
                            "ϵͳ�Ѵ��ڴ��ֻ��ţ��ͻ����ƣ�" + khmc + "���ͻ����ͣ�" + DictionaryUtil.parseNoToChinese(khlx) + "����Ŀ���ͣ�"
                                    + DictionaryUtil.parseNoToChinese(xmlx) + "��");
                    return false;
                }

                if (dhCount != null && dhCount > 0) {
                    MessageQueue.getInstance().putMessage(
                            getUserContext().getUID(),
                            "ϵͳ�Ѵ��ڴ˵绰���ͻ����ƣ�" + khmc + "���ͻ����ͣ�" + DictionaryUtil.parseNoToChinese(khlx) + "����Ŀ���ͣ�"
                                    + DictionaryUtil.parseNoToChinese(xmlx) + "��");
                    return false;
                }
            }
            return true;
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
