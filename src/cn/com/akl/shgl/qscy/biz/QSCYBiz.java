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
     * ��ʽǩ�ղ���ı���.
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
        // ƴ�ӱ���.
        StringBuilder titleSb = new StringBuilder();
        titleSb.append("ǩ�ղ���").append("--����:").append(dh).append("--������:").append(fhf).append("--�ջ���:").append(shf).append("--����ʱ��")
                .append(fhsj);
        return titleSb.toString();
    }

    /**
     * ��ʽ�����������.
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
        // ƴ�ӱ���.
        StringBuilder titleSb = new StringBuilder();
        titleSb.append("��������").append("--����:").append(dh).append("--������:").append(fhf).append("--�ջ���:").append(shf).append("--����ʱ��")
                .append(fhsj);
        return titleSb.toString();
    }

    /**
     * ����������������.
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
     * ����������������.
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
            // ��������.
            subBindid = WorkflowInstanceAPI.getInstance().createProcessInstance(QSCYConstants.QSCY_WORKFLOW_UUID, uid, title);
            int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, subBindid, 0);
            WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, subBindid, n, uid, title);

            // ��������.
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
            MessageQueue.getInstance().putMessage("����������ʧ��!", uid);
            return 0;
        }
    }


    /**
     * ��䵥ͷ����.
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
     * ץȡ��������.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param ckdh   �Ƿ��һ��ץȡ����. ���ǵ�һ�Σ���ȡǩ������Ϊ����.
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
     * ����ǩ�ղ�������.
     *
     * @param conn
     * @param dbdh    ���ⵥ��.
     * @param uid     �����̰�����.
     * @param pUid    �����̰�����.
     * @param titleSb ���̱���.
     */
    public int startQSCYProcess(Connection conn, Hashtable<String, String> dbdh, String uid, String pUid, String titleSb) {
        String ckdh = dbdh.get("CKDH");

        int subBindid = 0;
        try {
            // ��������.
            subBindid = WorkflowInstanceAPI.getInstance().createProcessInstance(QSCYConstants.QSCY_WORKFLOW_UUID, uid, titleSb.toString());
            int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, subBindid, 0);
            WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, subBindid, n, pUid, titleSb.toString());

            // ��������.
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
            MessageQueue.getInstance().putMessage("����������ʧ��!", uid);
            return 0;
        }
    }

    /**
     * ��ȡ��Ӧ�ֿ��ǩ�ղ��촦����.
     *
     * @param conn
     * @param kfckdm
     * @return
     */
    public static String getProcessUid(Connection conn, String kfckdm) {
        String pUid = null;

        try {
            // ��ȡ�ջ��ֿ��Ӧ����.
            Integer departmentId = DAOUtil.getIntOrNull(conn, "SELECT BMBH FROM BO_AKL_SH_BMCKGX WHERE KFCKBM=?", kfckdm);
            if (departmentId == null) {
                return pUid;
            }
            // ��ȡ������.
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
     * ��ȡ����������.
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
