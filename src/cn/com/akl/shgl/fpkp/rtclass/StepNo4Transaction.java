package cn.com.akl.shgl.fpkp.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.dfh.biz.DfhBiz;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

    public StepNo4Transaction() {
        super();
    }

    public StepNo4Transaction(UserContext arg0) {
        super(arg0);
        setDescription("");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

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
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 1.����Ҫ������������������������. 2.���µ���״̬. 3.���µ���Ʊ״̬.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException {

        PreparedStatement ps = null;
        ResultSet reset = null;

        try {
            ps = conn.prepareStatement(FpkpConstant.QUERY_FPKP_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String fpsqdh = reset.getString("FPSQDH");
                String xmlb = reset.getString("XMLB");
                String khbh = reset.getString("KHBM");

                // 1�� ��ȡ��Ʊ��״̬.
                String fpzt = DAOUtil.getStringOrNull(conn, FpkpConstant.QUERY_FPSQ_FPZT, fpsqdh);
                if (FpkpConstant.FPZT_YKP.equals(fpzt)) {

                    // 1.1�����������������¼.
                    Hashtable<String, String> main = new Hashtable<String, String>();
                    DfhBiz.convertCustomerServiceAddressInfoToConsignor(conn, DfhConstant.XZKFBM, main);
                    DfhBiz.convertCustomerAddressInfoToConsignee(conn, khbh, main);
                    DfhBiz.convertCustomerAddressInfoToConsignor(conn, khbh, main);

                    main.put("FHS", PrintUtil.parseNull(reset.getString("KHS")));
                    main.put("FHSHI", PrintUtil.parseNull(reset.getString("KHSHI")));
                    main.put("FHQX", PrintUtil.parseNull(reset.getString("KHQX")));
                    main.put("FHF", PrintUtil.parseNull(reset.getString("KHXM")));
                    main.put("FHDZ", PrintUtil.parseNull(reset.getString("KHDZ")));
                    main.put("FHLXR", PrintUtil.parseNull(reset.getString("KHXM")));
                    main.put("FHDH", PrintUtil.parseNull(reset.getString("KHDH")));

                    main.put("DJLB", DfhConstant.DJLB_FP);
                    main.put("DH", fpsqdh);
                    main.put("XMLB", xmlb);
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", main, bindid, getUserContext().getUID());

                    Hashtable<String, String> subtable = new Hashtable<String, String>();
                    subtable.put("WLMC", "��Ʊ");
                    subtable.put("XH", "��Ʊ");
                    subtable.put("SL", "1");
                    subtable.put("QSSL", "1");
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", subtable, bindid, getUserContext().getUID());

                    // 1.2�����¿�Ʊ����״̬.
                    int updateStateCount = DAOUtil.executeUpdate(conn, FpkpConstant.UPDATE_KPSQ_KPZT, FpkpConstant.FPZT_ZT, fpsqdh);
                    if (updateStateCount != 1) {
                        throw new RuntimeException("��Ʊ״̬����ʧ��!");
                    }
                }
            }

            // ����Ĭ��ֵΪǩ��.
            DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_FPKP_S SET QSZT=? WHERE BINDID=?", FpkpConstant.QSZT_QS, bindid);
        } catch (AWSSDKException e) {
            throw new RuntimeException(e);
        } finally {
            DBSql.close(ps, reset);
        }
    }
}
