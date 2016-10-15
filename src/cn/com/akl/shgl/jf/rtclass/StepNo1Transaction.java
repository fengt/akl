package cn.com.akl.shgl.jf.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.jf.biz.DeliveryBiz;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();
    private DeliveryBiz deliveryBiz = new DeliveryBiz();

    public StepNo1Transaction() {
        super();
    }

    public StepNo1Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("1����֤�Ƿ�Ҫȱ������ȱ�������ȱ����Ϣ���롣");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        int nextStepNo;
        try {
            nextStepNo = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, bindid, taskid);
        } catch (AWSSDKException e1) {
            e1.printStackTrace();
            return false;
        }

        Hashtable<String, String> hashtable = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXJF_P", bindid);

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();

            String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);

            if (nextStepNo == DeliveryConstant.STEP_YDJFSH || nextStepNo == DeliveryConstant.STEP_SJCL) {
                /** ���ò�����������ѡ��,�����´���д. */
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET THFACP='',THFACPMC='',SJMS='',CLYJ=null,SJLX='' WHERE BINDID=? AND SFSJ=?", bindid, XSDDConstant.NO);
                /** �Ѵ�������ÿ�. */
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET CLYJ=null WHERE BINDID=? AND SFSJ=?", bindid, XSDDConstant.YES);
                conn.commit();
                return true;
            } else {
                if (nextStepNo == DeliveryConstant.STEP_QHSQ) {
                    DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_JFJL_ZT, DeliveryConstant.JF_JLZT_QHDD, bindid);
                }
                if (nextStepNo == DeliveryConstant.STEP_TZKHQH) {
                    DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_JFJL_ZT, DeliveryConstant.JF_JLZT_YTZ, bindid);
                }

                repositoryBiz.removeLock(conn, bindid);

                PreparedStatement ps = null;
                ResultSet reset = null;
                try {
                    // ���µĲ�Ʒ��������.
                    ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
                    reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
                    while (reset.next()) {
                        /** ����ȱ����¼. */
                        deliveryBiz.insertShortageOfMaterials(conn, reset, bindid, uid, hashtable);
                        /** �������⴦����ȱ����. */
                        deliveryBiz.fetchAndLockMaterialFQHSQ(conn, reset, bindid, uid, xmlb);
                    }
                } finally {
                    DBSql.close(ps, reset);
                }
                insertPartOfLock(conn, bindid, uid, xmlb);
            }
            conn.commit();
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }

        try {
            // ����ȱ������.
            startQHSQ(bindid, uid, hashtable);
            // ������������ʱ.
            startJFDCS(bindid, uid, hashtable);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
        }

        return true;
    }

    /**
     * ������������ʱ����.
     *
     * @param bindid
     * @param uid
     * @param hashtable
     * @throws Exception
     */
    private void startJFDCS(int bindid, String uid, Hashtable<String, String> hashtable) throws Exception {
        // Ԥ������������.
        Connection conn = null;
        try {
            conn = DBSql.open();

            // ��ѯ�Ƿ�����ͬ���������������Ѿ���������,�������˾Ͳ�������.
            Integer jfdhCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SH_JFDCSCL_P WHERE JFDH=?", hashtable.get("JFDH"));
            if (jfdhCount != null && jfdhCount > 0) {
                return;
            }

            // ����������
            int ckBindid = 0;
            try {
                /** ƴ�ӱ���. */
                StringBuilder titleSb = new StringBuilder("��������ʱ-�������ţ�" + hashtable.get("JFDH"));

                /** ��������. */
                ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("bc164d7fb649530a7a856e1296dec2ef", uid, titleSb.toString());
                int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
                int[] processTaskInstance = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, uid, titleSb.toString());
                Hashtable<String, String> clone = (Hashtable<String, String>) hashtable.clone();
                clone.put("XM", PrintUtil.parseNull(clone.get("XMLB")));
                clone.put("DJLX", PrintUtil.parseNull(clone.get("YWLX")));
                clone.put("FWZX", PrintUtil.parseNull(clone.get("YDJFKF")));
                /** ��������. */
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_JFDCSCL_P", clone, ckBindid, uid);

                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_P SET JFDCSLCBINDID=?,JFDCSLCTASKID=? WHERE BINDID=?", ckBindid, processTaskInstance[0], bindid);
            } catch (RuntimeException e) {
                if (ckBindid != 0) {
                    WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                }
                throw e;
            } catch (Exception e) {
                // �������ʧ�ܣ��򴴽����������̡�
                try {
                    if (ckBindid != 0) {
                        WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                throw e;
            }
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ����ȱ����������.
     *
     * @param bindid
     * @param uid
     * @param hashtable
     * @throws Exception
     */
    private void startQHSQ(int bindid, String uid, Hashtable<String, String> hashtable) throws Exception {
        // Ԥ������������.
        Connection conn = null;
        try {
            conn = DBSql.open();
            String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);

            // ����ȱ�������������.
            Vector<Hashtable<String, String>> qhjlVector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_QHJL", bindid);
            if (qhjlVector != null && qhjlVector.size() > 0) {
                // ����������
                int ckBindid = 0;
                try {
                    /** ƴ�ӱ���. */
                    StringBuilder titleSb = new StringBuilder("ȱ������-�������ţ�" + hashtable.get("JFDH"));

                    /** ��������. */
                    ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("298b3069ce4e0e16049fbfbf7098a767", uid, titleSb.toString());
                    int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
                    WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, uid, titleSb.toString());

                    /** ��������. */
                    Hashtable<String, String> qhsqM = new Hashtable<String, String>();
                    String qhsqdh = RuleAPI.getInstance().executeRuleScript("QHSQ@replace(@date,-)@formatZero(3,@sequencefordateandkey(QHSQDH))", getUserContext());
                    qhsqM.put("XMLB", xmlb);
                    qhsqM.put("JHKFBM", PrintUtil.parseNull(hashtable.get("BDCKDM")));
                    qhsqM.put("JHKFMC", PrintUtil.parseNull(hashtable.get("YDJFKF")));
                    qhsqM.put("BHLX", QHSQCnt.bhlx0);
                    qhsqM.put("ZT", "δ�ύ");
                    qhsqM.put("QHSQDH", qhsqdh);
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHSQ_P", qhsqM, ckBindid, uid);

                    Vector<Hashtable<String, String>> qhsqVector = new Vector<Hashtable<String, String>>();
                    for (Hashtable<String, String> h : qhjlVector) {
                        Hashtable<String, String> qhsqS = new Hashtable<String, String>();
                        qhsqS.put("HH", RuleAPI.getInstance().executeRuleScript("@sequence:(#BO_AKL_QHSQ_S)"));
                        qhsqS.put("XMLB", PrintUtil.parseNull(h.get("XMLB")));
                        qhsqS.put("BDKCKYZ", String.valueOf(repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, h.get("WLBH"), hashtable.get("BDCKDM"), h.get("SX"))));
                        qhsqS.put("ZBKCKYZ", String.valueOf(repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, h.get("WLBH"), DfhConstant.ZBKFBM, h.get("SX"))));
                        qhsqS.put("SXDH", PrintUtil.parseNull(h.get("SXDH")));
                        qhsqS.put("SQCPWLBH", PrintUtil.parseNull(h.get("WLBH")));
                        qhsqS.put("SQCPZWMC", PrintUtil.parseNull(h.get("WLMC")));
                        qhsqS.put("SQCPPN", PrintUtil.parseNull(h.get("PN")));
                        qhsqS.put("SX", PrintUtil.parseNull(h.get("SX")));
                        qhsqS.put("SQCPSL", PrintUtil.parseNull(h.get("SL")));
                        qhsqS.put("SQSJ", PrintUtil.parseNull(h.get("SQSJ")));
                        qhsqS.put("SQLY", PrintUtil.parseNull(h.get("SQLY")));
                        qhsqS.put("CLJG", PrintUtil.parseNull(h.get("CLJG")));
                        qhsqS.put("SXCPHH", PrintUtil.parseNull(h.get("SXCPHH")));
                        qhsqS.put("JFCPHH", PrintUtil.parseNull(h.get("JFCPHH")));
                        qhsqVector.add(qhsqS);
                    }
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHSQ_S", qhsqVector, ckBindid, uid);
                } catch (RuntimeException e) {
                    if (ckBindid != 0) {
                        WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                    }
                    throw e;
                } catch (Exception e) {
                    // �������ʧ�ܣ��򴴽����������̡�
                    try {
                        if (ckBindid != 0) {
                            WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    throw e;
                }
            }
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 1���������������.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param xmlb
     * @throws SQLException
     */
    public void insertPartOfLock(Connection conn, int bindid, String uid, String xmlb) throws SQLException, AWSSDKException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_PJDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String sx = reset.getString("SX");
                String hwdm = reset.getString("HWDM");
                String ckdm = reset.getString("CKDM");
                String pch = reset.getString("PCH");
                int sl = reset.getInt("SHSL");
                // ���������¼.
                int hasNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);
                if (hasNum >= sl) {
                    repositoryBiz.insertLock(conn, bindid, uid, xmlb, wlbh, xh, pch, ckdm, hwdm, sx, sl);
                } else {
                    throw new RuntimeException("PN��" + xh + " �ڻ�λ��" + hwdm + "���������㣡");
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
