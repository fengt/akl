package cn.com.akl.shgl.ejjc.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

    private final String QUERY_SXForGZTM = "SELECT b.*,a.XMKF,a.SXDH,a.XMLB,(SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=a.XMKF)JFKFCKMC FROM BO_AKL_SX_P a,BO_AKL_SX_S b WHERE a.BINDID=b.BINDID AND GZTM=?";

    private final String QUERY_SXForSXDH = "SELECT b.*,a.XMKF,a.SXDH,a.XMLB,(SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=a.XMKF)JFKFCKMC FROM BO_AKL_SX_P a,BO_AKL_SX_S b WHERE a.BINDID=b.BINDID AND a.SXDH=?";

    private UserContext uc;

    private String xmlb;
    private String kfcks;

    public StepNo1BeforeSave() {
    }

    public StepNo1BeforeSave(UserContext arg0) {
        super(arg0);
        this.uc = arg0;
        setProvider("fengtao");
        setDescription("故障条码文本框回车带出送修单数据。");
    }

    @Override
    public boolean execute() {
        final int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
        Hashtable<String, String> frmHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();//表单单头数据
        final String uid = uc.getUID();

        String fjdjbh = frmHead.get("FJJHDH");

        Connection conn = null;
        try {
            conn = DBSql.open();

            // 获取复检计划的项目类别，客服

            if ("BO_AKL_FJJH_P".equals(tablename)) {
                //获取故障条码
                String[] str = frmHead.get("GZTM").toString().split("__");
                String gztm = str[0];
                kfcks = DAOUtil.getStringOrNull(conn, "SELECT KFCKBM FROM BO_AKL_FJJH_P WHERE FJDJBH=?", fjdjbh);
                xmlb = DAOUtil.getStringOrNull(conn, "SELECT XMLB FROM BO_AKL_FJJH_P WHERE FJDJBH=?", fjdjbh);

                //填充数据
                if (!"".equals(gztm)) {
                    //根据送修单填充数据
                    if (checkSXDH(gztm)) {
                        DAOUtil.executeQueryForParser(conn, QUERY_SXForSXDH, new ResultPaserAbs() {
                            public boolean parse(Connection conn, ResultSet rs) throws SQLException {
                                insertHander(conn, rs, bindid, uid);
                                return true;
                            }
                        }, gztm);
                    } else {//根据故障条码填充数据
                        DAOUtil.executeQueryForParser(conn, QUERY_SXForGZTM, new ResultPaserAbs() {
                            public boolean parse(Connection conn, ResultSet rs) throws SQLException {
                                insertHander(conn, rs, bindid, uid);
                                return true;
                            }
                        }, gztm);
                    }
                }
            }

            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }


    public void insertHander(Connection conn, ResultSet rs, int bindid, String uid) throws SQLException {
        String kf = rs.getString("XMKF");
        String xmlb = rs.getString("XMLB");

        if (xmlb == null || !xmlb.equals(this.xmlb)) {
            MessageQueue.getInstance().putMessage(uid, "此故障条码不属于此项目!");
            return;
        }
        if (kf == null || kfcks == null || !kfcks.contains(kf)) {
            MessageQueue.getInstance().putMessage(uid, "此故障条码不属此次计划抽检的客服!");
            return;
        }

        Hashtable<String, String> rec = new Hashtable<String, String>();
        rec.put("HH", rs.getString("SXCPHH"));//行号
        rec.put("SXDH", rs.getString("SXDH"));//送修单号
        rec.put("KFCKBH", rs.getString("XMKF"));//客户仓库编号
        rec.put("KFMC", rs.getString("JFKFCKMC"));//客服名称
        rec.put("CPSX", rs.getString("SX"));//产品属性
        rec.put("CPLH", rs.getString("WLBH"));//产品料号
        rec.put("SN", rs.getString("SN"));//产品S/N
        rec.put("PN", rs.getString("XH"));//产品PN
        rec.put("TPH", rs.getString("TPH"));//特批号
        rec.put("CPMC", rs.getString("WLMC"));//产品名称
        rec.put("GZTMH", rs.getString("GZTM"));//故障条码号
        rec.put("GZYY", rs.getString("GZYY"));//故障原因
        rec.put("EJJCR", getUserContext().getUserModel().getUserName());//故障原因
        try {
            BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FJ_S", rec, bindid, uid);
        } catch (AWSSDKException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean checkSXDH(String str) {
        String regex = "SX\\d{12}";
        Pattern p = Pattern.compile(regex);
        Matcher mt = p.matcher(str);
        return mt.matches();
    }

}
