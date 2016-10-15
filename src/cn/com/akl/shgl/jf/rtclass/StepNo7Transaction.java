package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.db.biz.DBConstant;
import cn.com.akl.shgl.dfh.biz.DfhBiz;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.kc.biz.RepositoryConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo7Transaction extends WorkFlowStepRTClassA {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    public StepNo7Transaction() {
        super();
    }

    public StepNo7Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("扣减库存.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskId = getParameter(PARAMETER_TASK_ID).toInt();
        String uid = getUserContext().getUID();

        boolean isBack = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
        if (isBack) {
            // 退回，删掉锁库.
            BOInstanceAPI.getInstance().removeProcessInstanceBOData("BO_AKL_SH_KCSK", bindid);
            return true;
        }

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, bindid);
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

        closeTask();
        return true;
    }

    /**
     * 关闭交付单超时流程.
     */
    public void closeTask() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();
        Hashtable<String, String> hashtable = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXJF_P", bindid);
        String jfdcslcbindid = hashtable.get("JFDCSLCBINDID");
        String jfdcslctaskid = hashtable.get("JFDCSLCTASKID");
        try {
            WorkflowInstanceAPI.getInstance().closeProcessInstance(uid, Integer.parseInt(jfdcslcbindid), Integer.parseInt(jfdcslctaskid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1、更新库存. <br/>
     * 2、更新送修单头状态和送修单身状态.<br/>
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        // 根据是否邮寄，插入邮寄信息.
        String sfyj = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_SFYJ, bindid);
        if (XSDDConstant.YES.equals(sfyj)) {
            insertWLData(conn, bindid);
        }

        repositoryBiz.removeLock(conn, bindid);
        updateDeliveryMaterial(conn, bindid);
        updateRepairFormState(conn, bindid);
        updatePart(conn, bindid);
        updateDYP(conn, bindid);

        /**
         * 检查送修单是否已交付完成，如果交付完成，就检查代用是否归还完毕.
         */

        /**更新交付记录状态.*/
        DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_JFJL_ZT, DeliveryConstant.JF_JLZT_YJF, bindid);
    }

    /**
     * 插入物流数据.
     *
     * @param conn
     * @param bindid
     */
    public void insertWLData(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();

        Hashtable<String, String> hashtable = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXJF_P", bindid);
        String xmlx = hashtable.get("XMLB");
        String dh = hashtable.get("JFDH");

        /** 向待发货物流表中插入数据 */
        Hashtable<String, String> dfhInfo = new Hashtable<String, String>();
        dfhInfo.put("DJLB", DfhConstant.DJLB_JF);
        dfhInfo.put("DH", dh);
        dfhInfo.put("XMLB", xmlx);
        dfhInfo.put("CLZT", DfhConstant.WLZT_DCL);
        dfhInfo.put("WLZT", DfhConstant.WLZT_DCL);
        dfhInfo.put("FHFLX", DfhConstant.SFHFLX_KFCK);
        dfhInfo.put("SHFLX", DfhConstant.SFHFLX_KH);
        DfhBiz.convertCustomerServiceAddressInfoToConsignor(conn, hashtable.get("BDKFCKBM"), dfhInfo);
        DfhBiz.convertCustomerAddressInfoToConsignee(conn, hashtable.get("KHBH"), dfhInfo);
        String sxr = hashtable.get("SXR");
        String sxrgddh = hashtable.get("SXRGDDH");
        String sxrsj = hashtable.get("SXRSJ");
        String sxrgddhqh = hashtable.get("SXRGDDHQH");

        if (sxr != null && !"".equals(sxr)) {
            dfhInfo.put("SHR", PrintUtil.parseNull(sxr));
            dfhInfo.put("SHRDHQH", "");
            dfhInfo.put("SHRDH", "");
            dfhInfo.put("SHRDHQH", "");
            if (sxrgddh != null && !"".equals(sxrgddh)) {
                dfhInfo.put("SHRDH", PrintUtil.parseNull(sxrgddh));
            }
            if (sxrgddhqh != null && !"".equals(sxrgddhqh)) {
                dfhInfo.put("SHRDHQH", PrintUtil.parseNull(sxrgddhqh));
            }
            if (sxrsj != null && !"".equals(sxrsj)) {
                dfhInfo.put("SHRSJ", PrintUtil.parseNull(sxrsj));
            }
        }

        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", dfhInfo, bindid, uid);

        /** 单身. */
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String xh = reset.getString("XH");
                String wlmc = reset.getString("WLMC");
                String ckhwdm = reset.getString("HWDM");
                String ckckdm = reset.getString("CKDM");
                int sjcksl = reset.getInt("SL");
                String pch = reset.getString("PCH");
                String cpsx = reset.getString("SX");

                Hashtable<String, String> dfhHashtable = new Hashtable<String, String>();
                dfhHashtable.put("WLBH", PrintUtil.parseNull(wlbh));
                dfhHashtable.put("XH", PrintUtil.parseNull(xh));
                dfhHashtable.put("WLMC", PrintUtil.parseNull(wlmc));
                dfhHashtable.put("SL", String.valueOf(sjcksl));
                dfhHashtable.put("SX", PrintUtil.parseNull(cpsx));
                dfhHashtable.put("QSSL", String.valueOf(sjcksl));
                dfhHashtable.put("PCH", PrintUtil.parseNull(pch));
                dfhHashtable.put("HWDM", PrintUtil.parseNull(ckhwdm));
                dfhHashtable.put("CKDM", PrintUtil.parseNull(ckckdm));
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", dfhHashtable, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 更新送修单状态.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    private void updateRepairFormState(Connection conn, int bindid) throws SQLException {
        String sxdh = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_SXDH, bindid);

        /** 若送修单单身全部交付完毕，则更新送修单状态为交付. */
        Integer queryCount = DAOUtil.getIntOrNull(conn,
                "SELECT COUNT(*) C FROM BO_AKL_SX_P p LEFT JOIN BO_AKL_SX_S s ON p.bindid=s.bindid WHERE SXDH=? AND s.ZT=?", sxdh,
                DeliveryConstant.SX_B_ZT_YJC);
        if (queryCount == null || queryCount == 0) {
            int updateCount = DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_SXDT_ZT, DeliveryConstant.SX_H_ZT_YJF, sxdh);
            if (updateCount != 1) {
                throw new RuntimeException("送修单：" + sxdh + " ，更新失败!");
            }
        }
    }

    /**
     * 处理代用品： <br/>
     * 1、若代用品归还，增加待用品库存. <br/>
     * 2、若代用品不还，则代用品钱不能为0.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    private void updateDYP(Connection conn, int bindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_DYPDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                int updateCount = DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_DYP_KC, reset.getInt("SL"), reset.getString("WLBH"),
                        reset.getString("HWDM"));
                if (updateCount != 1) {
                    throw new RuntimeException("代用品：“" + reset.getString("WLMC") + "”更新失败!");
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

    }

    /**
     * 处理交付物料记录。
     * 1、更新序列号信息.
     * 2、已交付的数据同步到送修记录中.
     * 3、更新库存状态.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    private void updateDeliveryMaterial(Connection conn, int bindid) throws SQLException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);

        int updateCount;

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String hwdm = reset.getString("HWDM");
                String sx = reset.getString("SX");
                String pch = reset.getString("PCH");
                int sl = reset.getInt("SL");
                int sxdhid = reset.getInt("SXCPHID");
                String gztm = reset.getString("GZTM");
                String sfjf = reset.getString("SFJF");
                String yjfhh = reset.getString("YJFHH");

                if (XSDDConstant.YES.equals(sfjf)) {
                    // 处理方式为退回或维修，需要减掉对应的序列号.
                    String clfs = reset.getString("CLFS");
                    if (clfs.equals(DeliveryConstant.CLFS_TH) || clfs.equals(DeliveryConstant.CLFS_BNWX) || clfs.equals(DeliveryConstant.CLFS_BWWX)) {
                        DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_GZTM_ZT, RepositoryConstant.GZTM_ZT_ZT, gztm);
                    }

                    Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE YJFHH=? AND SFJF=? AND ZT IN (?,?)", yjfhh, XSDDConstant.YES, DeliveryConstant.JF_JLZT_YTZ, DeliveryConstant.JF_JLZT_YJF);
                    if (count != null && count > 0) {
                        // 更新送修单单身交付状态.
                        updateCount = DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_SXDS_ZT, DeliveryConstant.SX_B_ZT_YJF, sxdhid);
                        if (updateCount != 1) {
                            throw new RuntimeException("送修单状态更新失败!");
                        }
                    }

                    // 更新库存状态.
                    updateCount = repositoryBiz.updateMaterialInfo(conn, xmlb, wlbh, pch, hwdm, sx, RepositoryConstant.WL_ZT_ZK, -sl);
                    if (updateCount != 1) {
                        throw new RuntimeException("库存数量更新失败!");
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 更新配件库存.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    private void updatePart(Connection conn, int bindid) throws SQLException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_PJDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String hwdm = reset.getString("HWDM");
                String sx = reset.getString("SX");
                String pch = reset.getString("PCH");
                int sl = reset.getInt("SL");
                int shsl = reset.getInt("SHSL");

                // 更新库存状态.
                int updateCount = repositoryBiz.updateMaterialInfo(conn, xmlb, wlbh, pch, hwdm, sx, RepositoryConstant.WL_ZT_ZK, -shsl);
                if (updateCount != 1) {
                    throw new RuntimeException("配件库存数量更新失败!");
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
