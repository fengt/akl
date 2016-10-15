package cn.com.akl.shgl.db.rtclass;

import cn.com.akl.shgl.db.biz.DBBiz;
import cn.com.akl.shgl.dwck.biz.DWCKBiz;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

public class StepNo3BeforeSave extends WorkFlowStepRTClassA {

    public StepNo3BeforeSave() {
        super();
    }

    public StepNo3BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("×°Ïäµ¥¼ÇÂ¼ÎóÉ¾³ý»Ö¸´.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
        String uid = getUserContext().getUID();
        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();

        if (tableName.equals("BO_AKL_ZXD_P")) {
            Connection conn = null;
            try {
                conn = DBSql.open();
                Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) HH FROM BO_AKL_ZXD_S WHERE BINDID=?", bindid);
                if (count == null || count == 0) {
                    DBBiz dbBiz = new DBBiz();
                    dbBiz.fillSubTable(conn, bindid, getUserContext().getUID());
                }
            } catch (AWSSDKException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBSql.close(conn, null, null);
            }
        }

        return true;
    }

}
