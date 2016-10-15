package cn.com.akl.cwgl.flfasq.rtclass;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class LastStepTransaction extends WorkFlowStepRTClassA {

    /**
     * ��ѯ��ʼ����
     */
    private static final String QUERY_KSSJ = "SELECT KSSJ FROM BO_AKL_WXB_XS_FL_HEAD WHERE BINDID=?";
    
    /**
     * ���·���״̬
     */
    private static final String UPDATE_ZT = "UPDATE BO_AKL_WXB_XS_FL_HEAD SET FAZT=? WHERE BINDID=?";
    
    /**
     * ����״̬��ִ����
     */
    private static final String FLZT_ZXZ="030003";
    
    public LastStepTransaction() {
        super();
    }

    public LastStepTransaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("���һ���ڵ����״̬");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

        Connection conn = null;
        try{
            conn = DAOUtil.openConnectionTransaction();
            Date kssj = DAOUtil.getDate(conn, QUERY_KSSJ, bindid);
            Date now = Calendar.getInstance().getTime();
            if(kssj.getTime() < now.getTime()){
                // ����״̬
                DAOUtil.executeUpdate(conn, UPDATE_ZT, FLZT_ZXZ, bindid);
            }
            conn.commit();
            return true;
        } catch(Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨���ִ�������ϵ����Ա");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
