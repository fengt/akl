package cn.com.akl.shgl.qscy.biz;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

public class QSCYBiz {

    /**
     * 格式签收差异的标题.
     *
     * @param dh
     * @param fhf
     * @param fhr
     * @param qsr
     * @param shf
     * @param fhsj
     * @param qssj
     * @return
     */
    public static String formatTitle(String dh, String fhf, String fhr, String qsr, String shf, String fhsj, String qssj) {
        // 拼接标题.
        StringBuilder titleSb = new StringBuilder();
        titleSb.append("签收差异").append("--单号:").append(dh).append("--发货方:").append(fhf).append("--收货方:").append(shf).append("--发货时间")
                .append(fhsj);
        return titleSb.toString();
    }

    /**
     * 格式质量差异标题.
     *
     * @param dh
     * @param fhf
     * @param fhr
     * @param qsr
     * @param shf
     * @param fhsj
     * @param qssj
     * @return
     */
    public static String formatTitle2(String dh, String fhf, String fhr, String qsr, String shf, String fhsj, String qssj) {
        // 拼接标题.
        StringBuilder titleSb = new StringBuilder();
        titleSb.append("质量差异").append("--单号:").append(dh).append("--发货方:").append(fhf).append("--收货方:").append(shf).append("--发货时间")
                .append(fhsj);
        return titleSb.toString();
    }

    /**
     * 启动数量差异流程.
     *
     * @param conn
     * @param uid
     * @param title
     * @param dh
     * @return
     */
    public int startQSCYSubProcess(Connection conn, String uid, String title, Hashtable<String, String> dh) {
        return startQSCYProcess(conn, dh, uid, uid, title);
    }

    /**
     * 启动质量差异流程.
     *
     * @param conn
     * @param uid
     * @param title
     * @param dh
     * @return
     * @throws SQLException
     */
    public int startQSCYSubProcessZLCY(Connection conn, String uid, String title, Hashtable<String, String> dh) throws SQLException {
        int subBindid = 0;
        try {
            // 启动流程.
            subBindid = WorkflowInstanceAPI.getInstance().createProcessInstance(QSCYConstants.QSCY_WORKFLOW_UUID, uid, title);
            int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, subBindid, 0);
            WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, subBindid, n, uid, title);

            // 创建数据.
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            BOInstanceAPI.getInstance().createBOData(conn, QSCYConstants.QSCY_MAIN, dh, subBindid, uid);
            return subBindid;
        } catch (Exception e) {
            if (subBindid != 0) {
                try {
                    WorkflowInstanceAPI.getInstance().removeProcessInstance(subBindid);
                } catch (AWSSDKException e1) {
                    e1.printStackTrace();
                }
            }
            MessageQueue.getInstance().putMessage("子流程启动失败!", uid);
            return 0;
        }
    }


    /**
     * 填充单头数据.
     *
     * @param conn
     * @param bindid
     * @param hashtable
     * @param ckdh
     * @throws SQLException
     */
    public Hashtable<String, String> dealHead(Connection conn, int bindid, Hashtable<String, String> hashtable, String ckdh) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(QSCYConstants.QUERY_WLD);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, ckdh);
            while (reset.next()) {
                hashtable.put("CKDH", reset.getString("CKDH"));
                hashtable.put("WLDH", reset.getString("WLDH"));
                hashtable.put("SHDW", reset.getString("SHDW"));
                hashtable.put("SHR", reset.getString("SHR"));
                hashtable.put("SHRLXFS", reset.getString("SHRLXFS"));
                hashtable.put("SFZ", reset.getString("SFZ"));
                hashtable.put("MDZ", reset.getString("MDZ"));
                hashtable.put("CYS", reset.getString("CYS"));
                hashtable.put("YSFS", reset.getString("YSFS"));
            }
            return hashtable;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 抓取单身数据.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param ckdh   是否第一次抓取数据. 若非第一次，则取签收数量为数量.
     * @return
     * @throws SQLException
     * @throws AWSSDKException
     */
    public Vector<Hashtable<String, String>> dealBody(Connection conn, int bindid, String uid, String ckdh, String shr) throws SQLException,
            AWSSDKException {

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();

            ps = conn.prepareStatement(QSCYConstants.QUERY_WLDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, ckdh, shr);
            while (reset.next()) {
                int scdsl = 0;

                scdsl = reset.getInt("SL");
                int qssl = reset.getInt("QSSL");

                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                // hashtable.put("DFHID", reset.getString("ID"));
                hashtable.put("CYDDH", PrintUtil.parseNull(ckdh));
                hashtable.put("XMLB", PrintUtil.parseNull(reset.getString("XMLB")));
                hashtable.put("FHKFDM", PrintUtil.parseNull(reset.getString("FHKFDM")));
                hashtable.put("FHKFMC", PrintUtil.parseNull(reset.getString("FHKFMC")));
                hashtable.put("SHKFCKBM", PrintUtil.parseNull(reset.getString("SHKFCKBM")));
                hashtable.put("WLH", PrintUtil.parseNull(reset.getString("WLH")));
                hashtable.put("CPXH", PrintUtil.parseNull(reset.getString("CPXH")));
                hashtable.put("CPSX", PrintUtil.parseNull(reset.getString("SX")));
                hashtable.put("CPMC", PrintUtil.parseNull(reset.getString("CPMC")));
                hashtable.put("SL", PrintUtil.parseNull(reset.getString("SL")));
                hashtable.put("QSSL", PrintUtil.parseNull(reset.getString("QSSL")));
                hashtable.put("CYSL", String.valueOf(scdsl - qssl));

                vector.add(hashtable);
            }

            return vector;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 启动签收差异流程.
     *
     * @param conn
     * @param dbdh    出库单号.
     * @param uid     子流程办理者.
     * @param pUid    父流程办理者.
     * @param titleSb 流程标题.
     */
    public int startQSCYProcess(Connection conn, Hashtable<String, String> dbdh, String uid, String pUid, String titleSb) {
        String ckdh = dbdh.get("CKDH");

        int subBindid = 0;
        try {
            // 启动流程.
            subBindid = WorkflowInstanceAPI.getInstance().createProcessInstance(QSCYConstants.QSCY_WORKFLOW_UUID, uid, titleSb.toString());
            int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, subBindid, 0);
            WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, subBindid, n, pUid, titleSb.toString());

            // 创建数据.
            Vector<Hashtable<String, String>> vector = dealBody(conn, subBindid, pUid, ckdh, dbdh.get("SHR"));

            BOInstanceAPI.getInstance().createBOData(conn, QSCYConstants.QSCY_MAIN, dbdh, subBindid, uid);
            BOInstanceAPI.getInstance().createBOData(conn, QSCYConstants.QSCY_SUB, vector, subBindid, uid);

            return subBindid;
        } catch (Exception e) {
            if (subBindid != 0) {
                try {
                    WorkflowInstanceAPI.getInstance().removeProcessInstance(subBindid);
                } catch (AWSSDKException e1) {
                    e1.printStackTrace();
                }
            }
            MessageQueue.getInstance().putMessage("子流程启动失败!", uid);
            return 0;
        }
    }

    /**
     * 获取对应仓库的签收差异处理人.
     *
     * @param conn
     * @param kfckdm
     * @return
     */
    public static String getProcessUid(Connection conn, String kfckdm) {
        String pUid = null;

        try {
            // 获取收货仓库对应部门.
            Integer departmentId = DAOUtil.getIntOrNull(conn, "SELECT BMBH FROM BO_AKL_SH_BMCKGX WHERE KFCKBM=?", kfckdm);
            if (departmentId == null) {
                return pUid;
            }
            // 获取办理人.
            ArrayList<String> list = DAOUtil.getStringCollection(conn, "SELECT USERID FROM ORGUSER WHERE DEPARTMENTID=? AND DISENABLE=0 ORDER BY ISMANAGER DESC",
                    departmentId);
            StringBuilder sb = new StringBuilder();
            for (String s : list) {
                sb.append(s).append(' ');
            }
            pUid = sb.toString();
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        return pUid;
    }


    /**
     * 获取物流单数据.
     *
     * @param wldBindid
     * @return
     */
    public static Hashtable<String, String> getWldHashtable(int wldBindid) {
        Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WLYSD_P", wldBindid);
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
        return hashtable;
    }


}
