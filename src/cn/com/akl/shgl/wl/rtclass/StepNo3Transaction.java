package cn.com.akl.shgl.wl.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.shgl.qscy.biz.QSCYConstants;
import cn.com.akl.shgl.wl.biz.WLConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

    private QSCYBiz qscyBiz = new QSCYBiz();

    public StepNo3Transaction() {
        super();
    }

    public StepNo3Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("启动差异流程");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DBSql.open();
            service(conn, bindid);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 处理代发货记录的更新.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException {
        String uid = getUserContext().getUID();

        Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WLYSD_P", bindid);
        String fhr = boData.get("FHR");
        String fhdz = boData.get("FHDZ");
        String fhf = boData.get("FHF");
        String fhs = boData.get("FHS");
        String fhshi = boData.get("FHSHI");
        String fhqx = boData.get("FHQX");
        String shr = boData.get("SHR");
        String shdz = boData.get("SHDZ");
        String shs = boData.get("SHS");
        String shf = boData.get("SHF");
        String shshi = boData.get("SHSHI");
        String shqx = boData.get("SHQX");
        String fhsj = boData.get("KDSJ");
        String qsr = boData.get("QSR");
        String qssj = boData.get("QSSJ");
        String sfzlcy = boData.get("SFZLCY");
        String wldh = boData.get("WLD");

        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("WLDH", PrintUtil.parseNull(boData.get("WLD")));
        hashtable.put("SHDW", PrintUtil.parseNull(boData.get("SHF")));
        hashtable.put("SHR", PrintUtil.parseNull(boData.get("SHR")));
        hashtable.put("SHR", PrintUtil.parseNull(boData.get("SHR")));
        hashtable.put("SHRLXFS", PrintUtil.parseNull(boData.get("SHRSJ")));
        hashtable.put("SHRSJ", PrintUtil.parseNull(boData.get("SHRSJ")));
        hashtable.put("SHRDH", PrintUtil.parseNull(boData.get("SHRDH")));
        hashtable.put("SHRDHQH", PrintUtil.parseNull(boData.get("SHRDHQH")));
        hashtable.put("FHRQ", PrintUtil.parseNull(boData.get("KDSJ")));
        hashtable.put("FHDW", PrintUtil.parseNull(boData.get("FHF")));
        hashtable.put("FHR", PrintUtil.parseNull(boData.get("FHR")));
        hashtable.put("FHRDH", PrintUtil.parseNull(boData.get("FHRDH")));
        hashtable.put("FHRDHQH", PrintUtil.parseNull(boData.get("FHRDHQH")));
        hashtable.put("FHRSJ", PrintUtil.parseNull(boData.get("FHRSJ")));
        hashtable.put("SFZ", PrintUtil.parseNull(boData.get("FZ")));
        hashtable.put("MDZ", PrintUtil.parseNull(boData.get("DZ")));
        hashtable.put("CYS", PrintUtil.parseNull(boData.get("CYF")));
        hashtable.put("YSFS", PrintUtil.parseNull(boData.get("YSFS")));

        if (XSDDConstant.YES.equals(sfzlcy)) {
            // 启动质量差异流程.
            Hashtable<String, String> zlcyMain = (Hashtable<String, String>) hashtable.clone();
            zlcyMain.put("CYLX", QSCYConstants.CYLX_ZLCY);
            qscyBiz.startQSCYSubProcessZLCY(conn, uid, "物流单 - " + boData.get("WLD") + " - 质量差异流程", zlcyMain);
        }


        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(WLConstant.QUERY_WLDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                // 如果有差异则启动子流程.
                String sfslcy = reset.getString("SFSLCY");
                if (XSDDConstant.YES.equals(sfslcy)) {
                    String dh = reset.getString("CKDH");
                    Hashtable<String, String> slcyMain = (Hashtable<String, String>) hashtable.clone();
                    slcyMain.put("CKDH", dh);
                    slcyMain.put("CYLX", QSCYConstants.CYLX_SLCY);

                    String title = qscyBiz.formatTitle(dh, fhf, fhr, qsr, shr, fhsj, qssj);
                    qscyBiz.startQSCYSubProcess(conn, uid, title, slcyMain);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
