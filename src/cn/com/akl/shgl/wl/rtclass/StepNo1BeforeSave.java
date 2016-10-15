package cn.com.akl.shgl.wl.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.wl.biz.WLConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;

/**
 * Created by huangming on 2015/5/19.
 */
public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

        Connection conn = null;
        try {
            conn = DBSql.open();
            if ("BO_AKL_WLYSD_P".equals(tableName)) {
                dealHead(conn, bindid);
            } else if ("BO_AKL_WLYSD_XM_S".equals(tableName)) {
                //dealBody(conn, bindid);
            }
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * ������.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void dealBody(Connection conn, int bindid) throws SQLException {
        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String ckdh = hashtable.get("CKDH");
        String smjz = hashtable.get("SMJZ");
        String xmlb = hashtable.get("XMLB");
        if (smjz != null && !smjz.equals("0")) {
            BigDecimal xs = DAOUtil.getBigDecimalOrNull(conn, "SELECT SUM (JG) SMJZ FROM ( SELECT ISNULL(( SELECT TOP 1 JG FROM BO_AKL_SH_JGGL WHERE WLBH = dfhs.WLBH ORDER BY UPDATEDATE ), 0 ) * dfhs.QSSL JG FROM BO_AKL_DFH_P dfhp LEFT JOIN BO_AKL_DFH_S dfhs ON dfhp.bindid = dfhs.bindid AND ISNULL(dfhp.JLBZ, 0) = ISNULL(dfhs.JLBZ, 0) WHERE XMLB = ? AND DH = ? ) a", xmlb, ckdh);
            if (xs == null) {
                hashtable.put("SMJZ", "0");
            } else {
                hashtable.put("SMJZ", xs.toString());
            }
        }
    }

    /**
     * ����ͷ.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void dealHead(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        int count = 0;
        BigDecimal hjBigDecimal = new BigDecimal(0);

        Hashtable<String, String> main = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String hj = main.get("HJ");
        if (hj == null) {
            throw new RuntimeException("����д�˷���Ϣ!");
        } else {
            // �˷��޸���Ի��ܽ���ɾ��������������в���.
            try {
                hjBigDecimal = BigDecimal.valueOf(Double.parseDouble(hj));
            } catch (Exception e) {
            }
            BigDecimal yf = DAOUtil.getBigDecimalOrNull(conn, "SELECT HJ FROM BO_AKL_WLYSD_P WHERE BINDID=?", bindid);
            if (hjBigDecimal == null || yf == null || hjBigDecimal.doubleValue() != yf.doubleValue()) {
                BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_WLYSD_XM_S", bindid);
            }
        }

        String yf = RuleAPI.getInstance().executeRuleScript("@year-@month");

        // ���������Ϣ.
        // �˶������ͽ���Ƿ�һ�£����һ�£��򲻽��в����������һ����ɾ�������»��ܡ�
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(WLConstant.QUERY_WLMX_TOHZ);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String xmlb = reset.getString("XMLB");
                String tj = reset.getString("TJ");
                String zl = reset.getString("ZL");
                String xs = reset.getString("XS");
                String cpsl = reset.getString("CPSL");
                String je = reset.getString("JE");

                int updateCount = DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WLYSD_XM_S SET CPPS=?,TJ=?,ZL=?,SMJZ=?,XS=? WHERE BINDID=? AND XMLB=?", cpsl, tj, zl, je, xs, bindid, xmlb);
                if (updateCount == 0) {
                    // �����¼�¼.
                    Hashtable<String, String> hashtable = new Hashtable<String, String>();
                    hashtable.put("XMLB", xmlb);
                    hashtable.put("YF", PrintUtil.parseNull(yf));
                    hashtable.put("TJ", PrintUtil.parseNull(tj));
                    hashtable.put("ZL", PrintUtil.parseNull(zl));
                    hashtable.put("SMJZ", PrintUtil.parseNull(je));
                    hashtable.put("CPPS", PrintUtil.parseNull(cpsl));
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_WLYSD_XM_S", hashtable, bindid, uid);
                }
                count++;
            }
        } finally {
            DBSql.close(ps, reset);
        }

        if (count > 0) {
            completeYFBL(conn, bindid, count, hjBigDecimal);
        } else {
            // ������ܵ���.
            DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_WLYSD_XM_S WHERE BINDID=?", bindid);
        }
    }

    /**
     * �˷ѱ�������.
     *
     * @param conn
     */
    public void completeYFBL(Connection conn, int bindid, int count, BigDecimal yf) throws SQLException {
        double bl = 1.0 / count;
        BigDecimal yfze = yf.multiply(new BigDecimal(bl));

        // �Զ��������ⱨ��.
        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WLYSD_XM_S SET DWJE=? WHERE BINDID=? AND (DWJE=0 OR DWJE IS NULL)", yfze, bindid);
        // ���ñ����ļ���.
        DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WLYSD_XM_S SET FYBL=?,YFZE=? WHERE BINDID=? AND (FYBL=0 OR FYBL IS NULL)", bl * 100, yfze, bindid);
    }
}

