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
        setDescription("生成批次号.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();

        if (tableName.equals("BO_AKL_SH_DWRK_S")) {
            String pch = hashtable.get("PCH");
            // 若批次号为空，自动带出批次号.
            // 1、根据单据中出现的批次号带出.
            // 2、根据项目类别生成批次号.
            if (pch == null || pch.equals("")) {
                Connection conn = null;
                try {
                    conn = DBSql.open();
                    pch = DAOUtil.getStringOrNull(conn, "SELECT PCH FROM BO_AKL_SH_DWRK_S WHERE BINDID=? AND PCH IS NOT NULL AND PCH<>''", bindid);
                    if(pch == null || pch.equals("")) {
                        String xmlb = DAOUtil.getStringOrNull(conn, DWRKConstant.QUERY_DWRK_XMLX, bindid);
                        if (xmlb == null || xmlb.equals("")) {
                            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "获取项目类别失败，请确定表单是否已暂存!");
                            return true;
                        }
                        pch = RepositoryBiz.getPCH(conn, xmlb);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    MessageQueue.getInstance().putMessage(getUserContext().getUID(), "获取项目类别失败，请确定表单是否已暂存!");
                } finally {
                    DBSql.close(conn, null, null);
                }
            }
            hashtable.put("PCH", pch);
        }

        return true;
    }

}
