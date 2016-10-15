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
        setDescription("1、验证是否要缺货，若缺货则进行缺货信息插入。");
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
                /** 重置不升级的升级选项,方便下次填写. */
                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_S SET THFACP='',THFACPMC='',SJMS='',CLYJ=null,SJLX='' WHERE BINDID=? AND SFSJ=?", bindid, XSDDConstant.NO);
                /** 把处理意见置空. */
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
                    // 换新的产品插入锁库.
                    ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
                    reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
                    while (reset.next()) {
                        /** 插入缺货记录. */
                        deliveryBiz.insertShortageOfMaterials(conn, reset, bindid, uid, hashtable);
                        /** 交付锁库处理（非缺货）. */
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
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }

        try {
            // 启动缺货申请.
            startQHSQ(bindid, uid, hashtable);
            // 启动交付单超时.
            startJFDCS(bindid, uid, hashtable);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
        }

        return true;
    }

    /**
     * 启动交付单超时流程.
     *
     * @param bindid
     * @param uid
     * @param hashtable
     * @throws Exception
     */
    private void startJFDCS(int bindid, String uid, Hashtable<String, String> hashtable) throws Exception {
        // 预备启动子流程.
        Connection conn = null;
        try {
            conn = DBSql.open();

            // 查询是否有相同交付单的子流程已经启动过了,启动过了就不启动了.
            Integer jfdhCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SH_JFDCSCL_P WHERE JFDH=?", hashtable.get("JFDH"));
            if (jfdhCount != null && jfdhCount > 0) {
                return;
            }

            // 启动子流程
            int ckBindid = 0;
            try {
                /** 拼接标题. */
                StringBuilder titleSb = new StringBuilder("交付单超时-交付单号：" + hashtable.get("JFDH"));

                /** 启动流程. */
                ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("bc164d7fb649530a7a856e1296dec2ef", uid, titleSb.toString());
                int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
                int[] processTaskInstance = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, uid, titleSb.toString());
                Hashtable<String, String> clone = (Hashtable<String, String>) hashtable.clone();
                clone.put("XM", PrintUtil.parseNull(clone.get("XMLB")));
                clone.put("DJLX", PrintUtil.parseNull(clone.get("YWLX")));
                clone.put("FWZX", PrintUtil.parseNull(clone.get("YDJFKF")));
                /** 插入数据. */
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_JFDCSCL_P", clone, ckBindid, uid);

                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WXJF_P SET JFDCSLCBINDID=?,JFDCSLCTASKID=? WHERE BINDID=?", ckBindid, processTaskInstance[0], bindid);
            } catch (RuntimeException e) {
                if (ckBindid != 0) {
                    WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                }
                throw e;
            } catch (Exception e) {
                // 如果启动失败，则创建出来的流程。
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
     * 启动缺货申请流程.
     *
     * @param bindid
     * @param uid
     * @param hashtable
     * @throws Exception
     */
    private void startQHSQ(int bindid, String uid, Hashtable<String, String> hashtable) throws Exception {
        // 预备启动子流程.
        Connection conn = null;
        try {
            conn = DBSql.open();
            String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);

            // 启动缺货申请的子流程.
            Vector<Hashtable<String, String>> qhjlVector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_QHJL", bindid);
            if (qhjlVector != null && qhjlVector.size() > 0) {
                // 启动子流程
                int ckBindid = 0;
                try {
                    /** 拼接标题. */
                    StringBuilder titleSb = new StringBuilder("缺货申请-交付单号：" + hashtable.get("JFDH"));

                    /** 启动流程. */
                    ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("298b3069ce4e0e16049fbfbf7098a767", uid, titleSb.toString());
                    int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
                    WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, uid, titleSb.toString());

                    /** 插入数据. */
                    Hashtable<String, String> qhsqM = new Hashtable<String, String>();
                    String qhsqdh = RuleAPI.getInstance().executeRuleScript("QHSQ@replace(@date,-)@formatZero(3,@sequencefordateandkey(QHSQDH))", getUserContext());
                    qhsqM.put("XMLB", xmlb);
                    qhsqM.put("JHKFBM", PrintUtil.parseNull(hashtable.get("BDCKDM")));
                    qhsqM.put("JHKFMC", PrintUtil.parseNull(hashtable.get("YDJFKF")));
                    qhsqM.put("BHLX", QHSQCnt.bhlx0);
                    qhsqM.put("ZT", "未提交");
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
                    // 如果启动失败，则创建出来的流程。
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
     * 1、将配件插入锁库.
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
                // 插入锁库记录.
                int hasNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);
                if (hasNum >= sl) {
                    repositoryBiz.insertLock(conn, bindid, uid, xmlb, wlbh, xh, pch, ckdm, hwdm, sx, sl);
                } else {
                    throw new RuntimeException("PN：" + xh + " 在货位：" + hwdm + "上数量不足！");
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
