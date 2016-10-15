package cn.com.akl.shgl.zzgl.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by huangming on 2015/4/30.
 */
public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    private String[][] zzjg = {{"营业执照", "BO_AKL_ZZGL_YYZZ_S", "正本", "副本", "其他(后续补充)"},
            {"组织机构代码证", "BO_AKL_ZZGL_ZZJGDMZ_S", "正本", "副本", "组织机构IC申报卡", "组织机构U盾"},
            {"税务登记证---国税", "BO_AKL_ZZGL_SWDJZGS_S", "正本", "副本", "报税IC卡"},
            {"税务登记证---地税", "BO_AKL_ZZGL_SWDJZDS_S", "正本", "副本", "报税IC卡"},
            {"印章", "BO_AKL_ZZGL_YZXX_S", "公章", "财务章", "合同章", "人名章", "印章备案刻制证明"},
            {"开户行", "BO_AKL_ZZGL_KHH_S", "开户许可证", "信用机构代码证", "网银U盾", "银行回单柜卡"}
    };

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("资质管理自动带出分支机构.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

        Connection conn = null;
        try {
            conn = DBSql.open();
            if (!"BO_AKL_ZZGL_P".equals(tableName)) {
                dealBody(conn, bindid);
            } else {
                dealHead(conn, bindid);
            }
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 处理单头.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void dealHead(Connection conn, int bindid) throws SQLException, AWSSDKException {
        String uid = getUserContext().getUID();
        Hashtable<String, String> main = getParameter(PARAMETER_FORM_DATA).toHashtable();
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("SFBL", XSDDConstant.NO);
        hashtable.put("FZJGMC", PrintUtil.parseNull(main.get("FZJGMC")));
        for (int i = 0; i < zzjg.length; i++) {
            String tableName = zzjg[i][1];
            Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM " + tableName + " WHERE BINDID=?", bindid);
            if (count == null || count == 0) {
                for (int j = 2; j < zzjg[i].length; j++) {
                    hashtable.put("LX", zzjg[i][j]);
                    BOInstanceAPI.getInstance().createBOData(conn, tableName, (Hashtable<String, String>) hashtable.clone(), bindid, uid);
                }
            }
        }
    }

    /**
     * 处理单身.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void dealBody(Connection conn, int bindid) throws SQLException {
        String fzjgmc = DAOUtil.getStringOrNull(conn, "SELECT FZJGMC FROM BO_AKL_ZZGL_P WHERE BINDID=?", bindid);
        Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
        String fzjgmc1 = hashtable.get("FZJGMC");
        if (fzjgmc1 == null || fzjgmc1.equals("")) {
            hashtable.put("FZJGMC", fzjgmc);
        }
    }

}


