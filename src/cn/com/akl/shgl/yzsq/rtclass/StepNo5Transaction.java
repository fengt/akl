package cn.com.akl.shgl.yzsq.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.dfh.biz.DfhBiz;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.yzsq.biz.YZSQBiz;
import cn.com.akl.shgl.yzsq.biz.YZSQConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;


/**
 * Created by huangming on 2015/4/28.
 */
public class StepNo5Transaction extends WorkFlowStepRTClassA {

    private YZSQBiz yzsqBiz = new YZSQBiz();

    public StepNo5Transaction() {
        super();
    }

    public StepNo5Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("插入物流信息，总部到客服.");
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
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();

        Hashtable<String, String> bo_data = BOInstanceAPI.getInstance().getBOData("BO_AKL_YZSQ_P", bindid);
        String kfzxbm = bo_data.get("KFZXBM");
        String shkfckbm = bo_data.get("SHKFCKBM");
        String shkfckmc = bo_data.get("SHKFCKMC");
        String shdz = bo_data.get("SHDZ");
        String shr = bo_data.get("SHR");
        String shrdh = bo_data.get("SHRDH");
        String shryx = bo_data.get("SHRYX");
        String shs = bo_data.get("SHS");
        String shshi = bo_data.get("SHSHI");
        String shqx = bo_data.get("SHQX");
        String shrsj = bo_data.get("SHRSJ");
        String yzsqdh = bo_data.get("YZSQDH");

        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        DfhBiz.convertCustomerServiceAddressInfoToConsignor(conn, DfhConstant.XZKFBM, hashtable);
        DfhBiz.convertCustomerServiceAddressInfoToConsignee(conn, kfzxbm, hashtable);

        /*
        hashtable.put("SHDZ", PrintUtil.parseNull(shdz));
        hashtable.put("SHR", PrintUtil.parseNull(shr));
        hashtable.put("SHRDH", PrintUtil.parseNull(shrdh));
        hashtable.put("SHRYX", PrintUtil.parseNull(shryx));
        hashtable.put("SHS", PrintUtil.parseNull(shs));
        hashtable.put("SHSHI", PrintUtil.parseNull(shshi));
        hashtable.put("SHQX", PrintUtil.parseNull(shqx));
        hashtable.put("SHRSJ", PrintUtil.parseNull(shrsj));
        */

        hashtable.put("DJLB", DfhConstant.DJLB_ZZYZ);
        hashtable.put("DH", yzsqdh);
        hashtable.put("XMLB", "");
        hashtable.put("JLBZ", YZSQConstant.DFH_JLBS_KTZ);
        hashtable.put("CLZT", DfhConstant.WLZT_DCL);
        hashtable.put("WLZT", DfhConstant.WLZT_DCL);

        BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", hashtable, bindid, uid);

        serviceInsertDfhs(conn, bindid, shkfckbm);
    }

    /**
     * 插入待发货单身信息。
     *
     * @param conn
     * @param bindid
     */
    public void serviceInsertDfhs(Connection conn, int bindid, String kfmc) throws SQLException, AWSSDKException {
        // 插入待发货记录.
        String uid = getUserContext().getUID();

        PreparedStatement stat = null;
        ResultSet reset = null;
        try {
            stat = conn.prepareStatement(YZSQConstant.QUERY_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, stat, bindid);
            while (reset.next()) {
                String zz = reset.getString("ZZ");
                String zzlx = reset.getString("ZZLX");
                String fzjgmc = reset.getString("FZJGMC");
                int zzid = reset.getInt("ZZID");
                int sl = 1;
                Hashtable<String, String> hashtable = new Hashtable<String, String>();
                hashtable.put("WLMC", zz + "-" + zzlx + "-" + fzjgmc);
                hashtable.put("SL", String.valueOf(sl));
                hashtable.put("QSSL", String.valueOf(sl));
                hashtable.put("JLBZ", YZSQConstant.DFH_JLBS_KTZ);
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", hashtable, bindid, uid);

                // 更新用章所在地.
                yzsqBiz.updateInPalce(conn, zz, zzid, kfmc);
            }
        } finally {
            DBSql.close(stat, reset);
        }
    }
}
