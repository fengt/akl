package cn.com.akl.shgl.dwrk.rtclass;

import cn.com.akl.shgl.dwrk.biz.DWRKBiz;
import cn.com.akl.shgl.dwrk.biz.DWRKConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
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

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    public StepNo1BeforeSave() {
        super();
    }

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("�������κ�.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();

        if (tableName.equals("BO_AKL_SH_DWRK_S")) {
            String pch = hashtable.get("PCH");
            // �����κ�Ϊ�գ��Զ��������κ�.
            // 1�����ݵ����г��ֵ����κŴ���.
            // 2��������Ŀ����������κ�.
            if (pch == null || pch.equals("")) {
                Connection conn = null;
                try {
                    conn = DBSql.open();
                    pch = DAOUtil.getStringOrNull(conn, "SELECT PCH FROM BO_AKL_SH_DWRK_S WHERE BINDID=? AND PCH IS NOT NULL AND PCH<>''", bindid);
                    if(pch == null || pch.equals("")) {
                        String xmlb = DAOUtil.getStringOrNull(conn, DWRKConstant.QUERY_DWRK_XMLX, bindid);
                        if (xmlb == null || xmlb.equals("")) {
                            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��ȡ��Ŀ���ʧ�ܣ���ȷ�����Ƿ����ݴ�!");
                            return true;
                        }
                        pch = RepositoryBiz.getPCH(conn, xmlb);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��ȡ��Ŀ���ʧ�ܣ���ȷ�����Ƿ����ݴ�!");
                } finally {
                    DBSql.close(conn, null, null);
                }
            }
            hashtable.put("PCH", pch);
        }

        return true;
    }

}
