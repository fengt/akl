package cn.com.akl.shgl.db.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.db.biz.DBBiz;
import cn.com.akl.shgl.db.biz.DBConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

    public StepNo3Transaction() {
        super();
    }

    public StepNo3Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("填充装箱单");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        // 若路由走出库，则不需要装箱.
        boolean ckFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "出库");
        if (ckFlag) {
            return true;
        }

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
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 清空装箱单. 填充装箱单.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        Hashtable<String, String> main = BOInstanceAPI.getInstance().getBOData("BO_AKL_DB_P", bindid);

        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("FHR", convertNull(main.get("FHR")));
        hashtable.put("FHGS", convertNull(main.get("FHKFCKMC")));
        hashtable.put("FHRDH", convertNull(main.get("FHRDH")));
        hashtable.put("FHRDHQH", convertNull(main.get("FHRDHQH")));
        hashtable.put("FHRSJ", convertNull(main.get("FHRSJ")));
        hashtable.put("FHDZ", convertNull(main.get("FHDZ")));
        hashtable.put("BZ", convertNull(main.get("FHBZ")));
        hashtable.put("SHR", convertNull(main.get("SHR")));
        hashtable.put("SHGS", convertNull(main.get("SHKFCKMC")));
        hashtable.put("SHRDH", convertNull(main.get("SHRDH")));
        hashtable.put("SHRDHQH", convertNull(main.get("SHRDHQH")));
        hashtable.put("SHRSJ", convertNull(main.get("SHRSJ")));
        hashtable.put("SHRDZ", convertNull(main.get("SHDZ")));
        hashtable.put("WLDH", convertNull(main.get("DBDH")));

        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_ZXD_P", bindid);
        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_ZXD_S", bindid);

        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_ZXD_P", hashtable, bindid, getUserContext().getUID());

        DBBiz dbBiz = new DBBiz();
        dbBiz.fillSubTable(conn, bindid, getUserContext().getUID());
    }

    public String convertNull(String str) {
        return str == null ? "" : str;
    }

}
