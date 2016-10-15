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
        setDescription("�����������ڵ�.");
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
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        serviceInsertDfhs(conn, bindid);
    }

    /**
     * ���������������Ϣ��
     *
     * @param conn
     * @param bindid
     */
    public void serviceInsertDfhs(Connection conn, int bindid) throws SQLException, AWSSDKException {
        // �����������¼.
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
                // �����������ڵ�.
                int updateCount = yzsqBiz.updateInPalce(conn, zz, zzid, DfhConstant.XZKFBM);
                if (updateCount != 1) {
                    throw new RuntimeException("���´浵��ʧ��!");
                }
            }
        } finally {
            DBSql.close(stat, reset);
        }
    }

}
