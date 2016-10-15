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
        setDescription("���������ı���س��������޵����ݡ�");
    }

    @Override
    public boolean execute() {
        final int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
        Hashtable<String, String> frmHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();//����ͷ����
        final String uid = uc.getUID();

        String fjdjbh = frmHead.get("FJJHDH");

        Connection conn = null;
        try {
            conn = DBSql.open();

            // ��ȡ����ƻ�����Ŀ��𣬿ͷ�

            if ("BO_AKL_FJJH_P".equals(tablename)) {
                //��ȡ��������
                String[] str = frmHead.get("GZTM").toString().split("__");
                String gztm = str[0];
                kfcks = DAOUtil.getStringOrNull(conn, "SELECT KFCKBM FROM BO_AKL_FJJH_P WHERE FJDJBH=?", fjdjbh);
                xmlb = DAOUtil.getStringOrNull(conn, "SELECT XMLB FROM BO_AKL_FJJH_P WHERE FJDJBH=?", fjdjbh);

                //�������
                if (!"".equals(gztm)) {
                    //�������޵��������
                    if (checkSXDH(gztm)) {
                        DAOUtil.executeQueryForParser(conn, QUERY_SXForSXDH, new ResultPaserAbs() {
                            public boolean parse(Connection conn, ResultSet rs) throws SQLException {
                                insertHander(conn, rs, bindid, uid);
                                return true;
                            }
                        }, gztm);
                    } else {//���ݹ��������������
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
            MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }


    public void insertHander(Connection conn, ResultSet rs, int bindid, String uid) throws SQLException {
        String kf = rs.getString("XMKF");
        String xmlb = rs.getString("XMLB");

        if (xmlb == null || !xmlb.equals(this.xmlb)) {
            MessageQueue.getInstance().putMessage(uid, "�˹������벻���ڴ���Ŀ!");
            return;
        }
        if (kf == null || kfcks == null || !kfcks.contains(kf)) {
            MessageQueue.getInstance().putMessage(uid, "�˹������벻���˴μƻ����Ŀͷ�!");
            return;
        }

        Hashtable<String, String> rec = new Hashtable<String, String>();
        rec.put("HH", rs.getString("SXCPHH"));//�к�
        rec.put("SXDH", rs.getString("SXDH"));//���޵���
        rec.put("KFCKBH", rs.getString("XMKF"));//�ͻ��ֿ���
        rec.put("KFMC", rs.getString("JFKFCKMC"));//�ͷ�����
        rec.put("CPSX", rs.getString("SX"));//��Ʒ����
        rec.put("CPLH", rs.getString("WLBH"));//��Ʒ�Ϻ�
        rec.put("SN", rs.getString("SN"));//��ƷS/N
        rec.put("PN", rs.getString("XH"));//��ƷPN
        rec.put("TPH", rs.getString("TPH"));//������
        rec.put("CPMC", rs.getString("WLMC"));//��Ʒ����
        rec.put("GZTMH", rs.getString("GZTM"));//���������
        rec.put("GZYY", rs.getString("GZYY"));//����ԭ��
        rec.put("EJJCR", getUserContext().getUserModel().getUserName());//����ԭ��
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
