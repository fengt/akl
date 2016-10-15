package cn.com.akl.shgl.qscy.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.shgl.qscy.biz.QSCYConstants;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    private QSCYBiz qscyBiz = new QSCYBiz();

    public StepNo1BeforeSave() {
        super();
    }

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("签收差异，第一节点表单保存事件.");
    }

    @Override
    public boolean execute() {
        // 查询父流程与子流程
        String uid = getUserContext().getUID();
        final int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

        if (!QSCYConstants.QSCY_MAIN.equals(tableName))
            return true;

        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();

        Connection conn = null;
        try {
            conn = DBSql.open();

            String ckdh = hashtable.get("CKDH");

            if (ckdh == null || ckdh.trim().length() == 0) {
                BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, QSCYConstants.QSCY_SUB, bindid);
                return true;
            }

            String ckdh2 = DAOUtil.getStringOrNull(conn, QSCYConstants.QUEYR__CKDH, bindid);
            if (ckdh.equals(ckdh2)) {
                return true;
            }

            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, QSCYConstants.QSCY_SUB, bindid);

            qscyBiz.dealHead(conn, bindid, hashtable, ckdh);
            Vector<Hashtable<String, String>> dealBody = qscyBiz.dealBody(conn, bindid, uid, ckdh, hashtable.get("SHR"));
            BOInstanceAPI.getInstance().createBOData(conn, QSCYConstants.QSCY_SUB, dealBody, bindid, uid);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}
