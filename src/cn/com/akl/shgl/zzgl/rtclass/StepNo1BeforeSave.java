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

    private String[][] zzjg = {{"Ӫҵִ��", "BO_AKL_ZZGL_YYZZ_S", "����", "����", "����(��������)"},
            {"��֯��������֤", "BO_AKL_ZZGL_ZZJGDMZ_S", "����", "����", "��֯����IC�걨��", "��֯����U��"},
            {"˰��Ǽ�֤---��˰", "BO_AKL_ZZGL_SWDJZGS_S", "����", "����", "��˰IC��"},
            {"˰��Ǽ�֤---��˰", "BO_AKL_ZZGL_SWDJZDS_S", "����", "����", "��˰IC��"},
            {"ӡ��", "BO_AKL_ZZGL_YZXX_S", "����", "������", "��ͬ��", "������", "ӡ�±�������֤��"},
            {"������", "BO_AKL_ZZGL_KHH_S", "�������֤", "���û�������֤", "����U��", "���лص���"}
    };

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("���ʹ����Զ�������֧����.");
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
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
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
     * ������.
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


