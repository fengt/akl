package cn.com.akl.shgl.sxtz.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.sxtz.biz.SXTZConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by huangming on 2015/5/7.
 */
public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    public StepNo1BeforeSave() {
    }

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setProvider("huangming");
        setDescription("自动根据单号带入数据.");
    }

    @Override
    public boolean execute() {
        int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();
        String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();

        Connection conn = null;
        if (tablename.equals(SXTZConstant.NEW_TABLE_MAIN)) {
            try {
                conn = DBSql.open();
                service(conn, bindid);
                return true;
            } catch (RuntimeException e) {
                e.printStackTrace();
                MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                MessageQueue.getInstance().putMessage(uid, "后台出现异常，请检查控制台", true);
                return false;
            } finally {
                DBSql.close(conn, null, null);
            }
        }
        return true;
    }

    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        Hashtable<String, String> main = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String sxdh = main.get(SXTZConstant.FROM_DH);
        String ysxdh = DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_FORM_SXDH, bindid);
        if (sxdh == null) {
            BOInstanceAPI.getInstance().removeBOData(SXTZConstant.NEW_TABLE_SUB, bindid);
            BOInstanceAPI.getInstance().removeBOData("BO_AKL_SH_YSX_S", bindid);
            return;
        }

        if (ysxdh == null || !ysxdh.equals(sxdh)) {
            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, SXTZConstant.NEW_TABLE_SUB, bindid);
            BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_YSX_S", bindid);
        } else {
            return;
        }

        Integer parentbindid = DAOUtil.getIntOrNull(conn, SXTZConstant.QUERY_OLD_FORM_BINDID, sxdh);
        if (parentbindid == null) {
            throw new RuntimeException("找不到送修单号：" + sxdh);
        }

        // 补充主表数据.
        saveMainForm(conn, bindid, parentbindid);
        // 补充子表数据.
        saveSubForm(conn, bindid, uid, parentbindid);

    }

    /**
     * 保存主表数据.
     *
     * @param conn
     * @param bindid
     * @param parentbindid
     * @throws SQLException
     */
    public void saveMainForm(Connection conn, int bindid, int parentbindid) throws SQLException {
        Hashtable<String, String> main = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String sxdh = main.get(SXTZConstant.FROM_DH);
        // 1. 获取原表数据.
        Hashtable<String, String> boData = null;
        boData = BOInstanceAPI.getInstance().getBOData(SXTZConstant.OLD_TABLE_MAIN, parentbindid);
        main.putAll(boData);
        // 2. 补充原表数据.
        Set<Map.Entry<String, String>> entries = boData.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            main.put("Y" + entry.getKey(), entry.getValue());
        }
        main.put("ISEND", String.valueOf(0));//重置ISEND为0
        main.put("LASTTIME", boData.get("UPDATEDATE"));
        main.put("PARENTBINDID", boData.get("BINDID"));
        main.put("PARENTID", boData.get("ID"));
    }

    /**
     * 保存子表数据.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param parentbindid
     * @throws SQLException
     * @throws AWSSDKException
     */
    public void saveSubForm(Connection conn, int bindid, String uid, int parentbindid) throws SQLException, AWSSDKException {
        // 1.获取原子表数据.
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(SXTZConstant.QUERY_OLD_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, parentbindid);
            while (reset.next()) {
                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                // 转化原子表信息.
                // 1.插入现在数据.
                hashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
                hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
                hashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
                hashtable.put("JG", PrintUtil.parseNull(reset.getString("JG")));
                hashtable.put("ZT", PrintUtil.parseNull(reset.getString("ZT")));
                hashtable.put("YKTMH", PrintUtil.parseNull(reset.getString("YKTMH")));
                hashtable.put("GZYY", PrintUtil.parseNull(reset.getString("GZYY")));
                hashtable.put("SFSJ", PrintUtil.parseNull(reset.getString("SFSJ")));
                hashtable.put("SJLX", PrintUtil.parseNull(reset.getString("SJLX")));
                hashtable.put("SJMS", PrintUtil.parseNull(reset.getString("SJMS")));
                hashtable.put("PFJG", PrintUtil.parseNull(reset.getString("PFJG")));
                hashtable.put("SFTP", PrintUtil.parseNull(reset.getString("SFTP")));
                hashtable.put("TPH", PrintUtil.parseNull(reset.getString("TPH")));
                hashtable.put("PFMS", PrintUtil.parseNull(reset.getString("PFMS")));
                hashtable.put("CLYJ", PrintUtil.parseNull(reset.getString("CLYJ")));
                hashtable.put("SN", PrintUtil.parseNull(reset.getString("SN")));
                hashtable.put("GMRQ", PrintUtil.parseNull(reset.getString("GMRQ")));
                hashtable.put("ZBJZRQ", PrintUtil.parseNull(reset.getString("ZBJZRQ")));
                hashtable.put("ZZJBRQ", PrintUtil.parseNull(reset.getString("ZZJBRQ")));
                hashtable.put("JBLX", PrintUtil.parseNull(reset.getString("JBLX")));
                hashtable.put("ZBYY", PrintUtil.parseNull(reset.getString("ZBYY")));
                hashtable.put("CLFS", PrintUtil.parseNull(reset.getString("CLFS")));
                hashtable.put("FJ", PrintUtil.parseNull(reset.getString("FJ")));
                hashtable.put("GZTM", PrintUtil.parseNull(reset.getString("GZTM")));
                hashtable.put("SL", PrintUtil.parseNull(reset.getString("SL")));
                hashtable.put("ZBLX", PrintUtil.parseNull(reset.getString("ZBLX")));
                hashtable.put("GZYYBZ", PrintUtil.parseNull(reset.getString("GZYYBZ")));
                hashtable.put("SXCPHH", PrintUtil.parseNull(reset.getString("SXCPHH")));
                hashtable.put("SYRLX", PrintUtil.parseNull(reset.getString("SYRLX")));
                hashtable.put("SFSCZB", PrintUtil.parseNull(reset.getString("SFSCZB")));
                hashtable.put("RBLH", PrintUtil.parseNull(reset.getString("RBLH")));
                hashtable.put("CCPN", PrintUtil.parseNull(reset.getString("CCPN")));
                hashtable.put("SFDC", PrintUtil.parseNull(reset.getString("SFDC")));
                hashtable.put("SX", PrintUtil.parseNull(reset.getString("SX")));
                hashtable.put("HWDM", PrintUtil.parseNull(reset.getString("HWDM")));
                hashtable.put("PCH", PrintUtil.parseNull(reset.getString("PCH")));
                hashtable.put("SCSXSN", PrintUtil.parseNull(reset.getString("SCSXSN")));
                hashtable.put("SJJG", PrintUtil.parseNull(reset.getString("SJJG")));
                hashtable.put("SJMS2", PrintUtil.parseNull(reset.getString("SJMS2")));
                hashtable.put("SFZCSJ", PrintUtil.parseNull(reset.getString("SFZCSJ")));
                hashtable.put("SFYJG", PrintUtil.parseNull(reset.getString("SFYJG")));

                hashtable.put("LASTTIME", PrintUtil.parseNull(reset.getString("UPDATEDATE")));
                hashtable.put("PARENTID", PrintUtil.parseNull(reset.getString("ID")));
                hashtable.put("PARENTBINDID", reset.getString("BINDID"));
                hashtable.put("ISOLD", XSDDConstant.NO);
                Hashtable copyHashtable = (Hashtable) hashtable.clone();
                BOInstanceAPI.getInstance().createBOData(conn, SXTZConstant.NEW_TABLE_SUB, hashtable, bindid, uid);
                // 2.插入原子表数据.
                copyHashtable.put("ISOLD", XSDDConstant.YES);
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_YSX_S", copyHashtable, bindid, uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }

}
