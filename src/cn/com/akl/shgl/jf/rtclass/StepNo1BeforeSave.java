package cn.com.akl.shgl.jf.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.dfh.biz.AutoBuildRepository;
import cn.com.akl.shgl.jf.biz.DeliveryBiz;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    private DeliveryBiz deliveryBiz = new DeliveryBiz();

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("�������̣���һ�ڵ㣬����ѡ�����޲�Ʒ���Զ�����������Ʒ.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

        Connection conn = null;
        try {
            conn = DBSql.open();
            if ("BO_AKL_WXJF_P".equals(tableName)) {
                dealHead(conn, bindid);
            } else if ("BO_AKL_WXJF_SX_S".equals(tableName)) {
                // dealBody(conn, bindid);
            } else if ("BO_AKL_WXJF_S".equals(tableName)) {
                // �Ը��Ƽ�¼�����к�.
                Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
                String hh = hashtable.get("HH");
                if (hh == null || hh.trim().equals("")) {
                    String jfdh = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_JFDH, bindid);
                    String jfhh = DeliveryBiz.getJFRowNum(conn, bindid, jfdh);
                    hashtable.put("HH", jfhh);
                    hashtable.put("SCJFHH", "");
                    hashtable.put("YJFHH", jfhh);
                }
            }
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return true;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ������.<br/>
     * 1.��ѯ���޵�����û���Զ�����������¼�����ݣ������ɶ�Ӯ�Ľ�����¼.<br/>
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void dealBody(Connection conn, int bindid) throws SQLException, AWSSDKException {
        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String clfs = hashtable.get("CLFS");
        String sxcphh = hashtable.get("SXCPHH");
        String xh = hashtable.get("XH");
        String wlmc = hashtable.get("WLMC");
    }

    /**
     * ����ͷ. <br/>
     * 1.ɾ�����������ж����¼.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void dealHead(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String sxdh = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_SXDH, bindid);
        String ydckdm = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YDCKDM, bindid);

        Hashtable<String, String> main = getParameter(PARAMETER_FORM_DATA).toHashtable();

        String sxdh2 = main.get("SXDH");
        String ydjfkfbm = main.get("YDJFKFBM");
        // ���޵��Ÿı��ɾ������յ���.
        if (sxdh2 == null || !sxdh2.equals(sxdh) || ydjfkfbm == null || !ydjfkfbm.equals(ydckdm)) {
            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_WXJF_SX_S", bindid);
            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_WXJF_S", bindid);
            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_PJCP", bindid);
            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_WXJF_DYP_S", bindid);
            return;
        }

        /** �������޵���. */
        dealSXDS(conn, bindid);
        /** ���»�ȡ���������������Ϣ���д���. */
        dealMaintain(conn, bindid);

        /**
         * �������Ʒ��Ϣ.
         *  1����ȡ����Ʒ�黹��Ϣ.
         *  2���������Ʒ��Ϣ.
         */
        Integer sxCount = DAOUtil.getIntOrNull(conn, "SELECT SUM(SL) FROM BO_AKL_SX_P sxp LEFT JOIN BO_AKL_DYPXX dyp ON sxp.bindid=dyp.bindid WHERE SFSH=? AND SFYX=? AND sxp.SXDH=?", XSDDConstant.YES, XSDDConstant.YES, sxdh);
        Integer jfCount = DAOUtil.getIntOrNull(conn, "SELECT SUM(SL) FROM BO_AKL_WXJF_P jf LEFT JOIN BO_AKL_WXJF_DYP_S dyp ON jf.bindid=dyp.bindid WHERE jf.ISEND=1 AND jf.SXDH=?", sxdh);
        main.put("WGHDYPSL", String.valueOf(sxCount - jfCount));
        main.put("YGHDYPSL", String.valueOf(jfCount));

        Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_DYP_S WHERE BINDID=?", bindid);
        if (count == null || count == 0) {
            Integer sxbindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_SX_P WHERE SXDH=?", sxdh);
            Vector dypxxVector = BOInstanceAPI.getInstance().getBODatasBySQL("BO_AKL_DYPXX", "WHERE BINDID=" + sxbindid + " AND SFYX='" + XSDDConstant.YES + "' AND SFSH='" + XSDDConstant.YES + "' AND HH NOT IN (SELECT ISNULL(HH,0) FROM BO_AKL_WXJF_P jfp LEFT JOIN BO_AKL_WXJF_DYP_S dyps ON jfp.BINDID=dyps.BINDID WHERE SXDH='" + sxdh + "')");
            if (dypxxVector != null) {
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_WXJF_DYP_S", dypxxVector, bindid, getUserContext().getUID());
            }
        }
    }

    /**
     * �������޵���.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    private void dealSXDS(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();

        Vector<Hashtable<String, String>> sxBody = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXJF_SX_S", bindid);
        Vector<Hashtable<String, String>> jfBody = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXJF_S", bindid);

        if (sxBody != null && sxBody.size() != 0) {
            for (Hashtable<String, String> sx : sxBody) {
                String id = sx.get("ID");
                String sxcphid = sx.get("SXWLJLID");
                String hh = sx.get("HH");
                String clfs = sx.get("CLFS");
                String xh = sx.get("XH");
                String wlmc = sx.get("WLMC");

                // �����޼�¼����У��.
                if (hh == null || hh.equals("")) {
                    MessageQueue.getInstance().putMessage(uid, "���޲�Ʒ: " + wlmc + "�������к�Ϊ:" + hh + "���޼�¼ȱʧ�����кţ�");
                    continue;
                }
                if (clfs == null || clfs.equals("")) {
                    BOInstanceAPI.getInstance().removeBOData(conn, "BO_AKL_WXJF_SX_S", Integer.valueOf(id));
                    MessageQueue.getInstance().putMessage(uid, "���޲�Ʒ: " + wlmc + "�������к�Ϊ:" + hh + " ����ʽδ��д�����벻�ɹ���");
                    continue;
                } else {
                    if ("064220".equals(clfs)) {
                        BOInstanceAPI.getInstance().removeBOData(conn, "BO_AKL_WXJF_SX_S", Integer.valueOf(id));
                        MessageQueue.getInstance().putMessage(uid, "���޲�Ʒ: " + wlmc + "�������к�Ϊ:" + hh + " ���ڽ��з������죬���벻�ɹ���");
                        continue;
                    }
                }


                int matchIndex = -1;
                // ��ʼ�����ж�.
                if (jfBody != null && jfBody.size() != 0) {
                    for (int i = 0; i < jfBody.size(); i++) {
                        Hashtable<String, String> hashtable = jfBody.get(i);
                        String sxcphidR = hashtable.get("SXCPHID");
                        if (sxcphid != null && sxcphid.equals(sxcphidR)) {
                            matchIndex = i;
                            jfBody.remove(matchIndex);
                            if (i >= 0) {
                                i--;
                            }
                        }
                    }
                }

                if (matchIndex == -1) {
                    // ��û��ƥ���¼������в���.
                    deliveryBiz.insertDeliveryRecord(conn, bindid, getUserContext().getUID(), sx);
                }
            }
        }

        // ������ཻ����¼.
        if (jfBody != null && jfBody.size() != 0) {
            for (Hashtable<String, String> hashtable : jfBody) {
                String id = hashtable.get("ID");
                BOInstanceAPI.getInstance().removeBOData(conn, "BO_AKL_WXJF_S", Integer.parseInt(id));
            }
        }
    }

    /**
     * ��������͸��������Ϣ.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void dealMaintain(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String xmlb = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_XMLB, bindid);
        String ckdm = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_BDCKDM, bindid);

        // ���ԭ���������.
        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_PJCP SET SL=0 WHERE BINDID=?", bindid);

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_JFDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String clfs = reset.getString("CLFS");
                if (DeliveryConstant.CLFS_BNWX.equals(clfs) || DeliveryConstant.CLFS_BWWX.equals(clfs)) {
                    String wlbh = reset.getString("WLBH");
                    String wxbwStr = reset.getString("WXBW");
                    if (wxbwStr == null || wxbwStr.equals("")) {
                        continue;
                    }

                    String[] wxbws = wxbwStr.split(",");
                    for (String wxbw : wxbws) {
                        Vector<Hashtable<String, String>> pjVector = deliveryBiz.getPartMaterial(conn, bindid, getUserContext().getUID(), xmlb, wlbh,
                                ckdm, wxbw);
                        for (Hashtable<String, String> pjHashtable : pjVector) {
                            // ��ѯ�����Ϣ�Ƿ���ڣ��������������ӣ������������.
                            String pjWlbh = pjHashtable.get("WLBH");
                            String pjHwdm = pjHashtable.get("HWDM");
                            String pjPch = pjHashtable.get("PCH");
                            Integer count = DAOUtil.getIntOrNull(conn, DeliveryConstant.QUERY_PJXX_CFJL, bindid, pjWlbh, ckdm, pjHwdm, pjPch);
                            if (count == null || count == 0) {
                                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_PJCP", pjHashtable, bindid, getUserContext().getUID());
                            } else {
                                String sl = pjHashtable.get("SL");
                                int updateCount = DAOUtil.executeUpdate(conn, DeliveryConstant.UPDATE_PJXX_SL, sl, bindid, pjWlbh, ckdm, pjHwdm, pjPch);
                                if (updateCount != 1) {
                                    throw new RuntimeException("�����Ʒ��������ʧ��!");
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        // ������������.
        DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_PJCP WHERE SL=0 AND SHSL=0 AND BINDID=?", bindid);
    }

}
