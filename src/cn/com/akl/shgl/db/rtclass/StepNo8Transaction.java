package cn.com.akl.shgl.db.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.db.biz.DBBiz;
import cn.com.akl.shgl.db.biz.DBConstant;
import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.shgl.qscy.biz.QSCYConstants;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.sybase.jdbc3.tds.HASessionContext;

public class StepNo8Transaction extends WorkFlowStepRTClassA {

    private DBBiz dbBiz = new DBBiz();
    private QSCYBiz qscyBiz = new QSCYBiz();

    public StepNo8Transaction() {
        super();
    }

    public StepNo8Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("签收数量录入，并往签收差异子流程中插入数据.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Integer wldBindid = null;

        Hashtable<String, String> main = BOInstanceAPI.getInstance().getBOData("BO_AKL_DB_P", bindid);

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, main, bindid);
            wldBindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_WLYSD_S WHERE CKDH=?", main.get("DBDH"));
            if (wldBindid == null || wldBindid == 0) {
                throw new RuntimeException("此调拨单还未填写物流信息!");
            }
            conn.commit();

            startSubProcess(conn, bindid, main, wldBindid);
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
     * 将签收数量入库，将差异的数量推送至签收差异流程中.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void service(Connection conn, Hashtable<String, String> main, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        String xmlb = main.get("XMLX");
        String shckbm = main.get("SHKFCKBM");

        dbBiz.deductInventory(conn, bindid, uid, xmlb);
        dbBiz.enterXLH(conn, bindid, uid, shckbm);

        // 清除物流单身数据，更新签收数量.
        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DFH_S", bindid);
        dbBiz.insertWLDataBody(conn, bindid, uid);
    }

    /**
     * 启动差异子流程.
     *
     * @param conn
     * @param bindid
     */
    public void startSubProcess(Connection conn, int bindid, Hashtable<String, String> boData, int ykwldh) {
        String uid = getUserContext().getUID();

        // 验证是否要启动差异.
        Integer cyCount = null;
        try {
            cyCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_DB_S WHERE BINDID=? AND SJCKSL<>SJRKSL", bindid);
        } catch (SQLException e1) {
            e1.printStackTrace();
            MessageQueue.getInstance().putMessage("后台出现错误!", uid);
        }

        if (cyCount != null && cyCount != 0) {
            String dbdh = boData.get("DBDH");
            String fhsj = boData.get("FHSJ");
            String fhf = boData.get("FHF");
            String fhkfckdm = boData.get("FHKFCKBM");
            String shf = boData.get("SHF");
            String pUid = null;

            pUid = qscyBiz.getProcessUid(conn, fhkfckdm);
            if (pUid == null) {
                System.err.println("发货方：" + fhf + " 未找到对应的签收差异处理人!");
                MessageQueue.getInstance().putMessage("发货方：" + fhf + " 未找到对应的签收差异处理人!", uid);
                pUid = "admin";
            }

            pUid = uid;

            // 拼接标题.
            StringBuilder titleSb = new StringBuilder();
            titleSb.append("签收差异").append("--调拨流程--单号:").append(dbdh).append("--发货方:").append(fhf).append("--收货方:").append(shf).append("--发货时间")
                    .append(fhsj);
            Hashtable<String, String> wldHashtable = QSCYBiz.getWldHashtable(ykwldh);
            wldHashtable.put("CKDH", dbdh);
            wldHashtable.put("CYLX", QSCYConstants.CYLX_SLCY);
            qscyBiz.startQSCYProcess(conn, wldHashtable, uid, pUid, titleSb.toString());
        }
    }

}
