package cn.com.akl.shgl.fpsq.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.fpsq.constant.FpsqConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * Created by huangming on 2015/4/29.
 */
public class StepNo1Validate extends WorkFlowStepRTClassA {

    public StepNo1Validate() {
        super();
    }

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setDescription("��֤��ֵ˰��Ʊ��Ϣ�Ƿ�ͨ���˲������.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
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
        // ��ֵ˰��Ʊ���.
        String uid = getUserContext().getUID();

        Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_FPSQ", bindid);
        String fplx = boData.get("FPLX");
        if (FpsqConstant.FPLX_ZZSFP.equals(fplx)) {
            // ��ֵ˰��Ʊ��Ҫ�ж���Ʊ���Ƿ����ͨ��.
            String sprbm = boData.get("SPRBM");
            Integer count = DAOUtil.getIntOrNull(conn, "SELECT ISEND FROM BO_AKL_SH_SPR WHERE SHPBH=? AND SFKY=?", sprbm, XSDDConstant.YES);
            Integer count2 = DAOUtil.getIntOrNull(conn, "SELECT ISEND FROM BO_AKL_SH_SPR WHERE SHPBH=?", sprbm);
            if (count2 == null) {
                MessageQueue.getInstance().putMessage(uid, "��Ʊ�˻�δά��!");
                return false;
            } else if ((count == null || count == 0) && count2 == 0) {
                MessageQueue.getInstance().putMessage(uid, "��Ʊ�˻�δ������!");
                return false;
            } else if ((count == null || count == 0) && count2 == 1) {
                MessageQueue.getInstance().putMessage(uid, "��Ʊ�����δͨ��!");
                return false;
            } else if (count == 1 && count2 == 1) {
                return true;
            } else {
                MessageQueue.getInstance().putMessage(uid, "����δ֪���!");
                return false;
            }
        }
        return true;
    }
}


