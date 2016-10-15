package cn.com.akl.shgl.db.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.db.biz.DBConstant;
import cn.com.akl.shgl.db.biz.DBValidater;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

    private DBValidater validater = new DBValidater();

    public StepNo1Validate() {
        super();
    }

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setDescription("У�����Ͽ����Ϣ����Ϊ���ȷ��������鷵��ԭ��.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DBSql.open();
            return validate(conn, bindid, uid);
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

    public boolean validate(Connection conn, int bindid, String uid) throws SQLException {
        String xmlx = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_XMLX, bindid);
        String fhckbm = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_FHCKBM, bindid);
        return validater.validateMXAndKC(conn, bindid, uid, xmlx)
                && validater.validateXLH(conn, bindid, uid, xmlx, fhckbm)
                && validater.validateJDFJ(conn, bindid, uid);
    }

}
