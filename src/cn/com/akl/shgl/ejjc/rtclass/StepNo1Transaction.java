package cn.com.akl.shgl.ejjc.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

    //��ѯ�����ӱ���Ϣ
    private static final String QUERY_EJXX = "SELECT * FROM BO_AKL_FJ_S WHERE BINDID=?";

    //ע������Ϊ֮ǰ���������Ա�ļ��������������ȡ���ù��ܡ�
    //�����Ʒ����
//	private static final String QUERY_COUNT = "SELECT COUNT(1) NUM FROM BO_AKL_FJ_S WHERE BINDID=?";
    //���¸���ƻ���Ա������
//	private static final String UPDATE_NUMBER = "UPDATE BO_AKL_FJRWFP_S SET YWCSL=ISNULL(YWCSL,0)+? WHERE BINDID=? AND YGGH=?";

    private Connection conn = null;
    private UserContext uc;

    public StepNo1Transaction() {
    }

    public StepNo1Transaction(UserContext arg0) {
        super(arg0);
        this.uc = arg0;
        setProvider("fengtao");
        setDescription("������¸���ƻ���������Ϣ��");
    }

    @Override
    public boolean execute() {
        int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_FJJH_P", bindid);
        String fjjhdh = head.get("FJJHDH").toString();//����ƻ�����
//		String cjrgh = head.get("CJRGH").toString();//�����˹���
//		String userkey = DBSql.getString("SELECT USERID+'<'+USERNAME+'>' AS USERKEY FROM ORGUSER WHERE USERID='"+cjrgh+"'", "USERKEY");

        try {
            int fjjh_bindid = DBSql.getInt("SELECT BINDID FROM BO_AKL_FJJH_P WHERE FJDJBH='" + fjjhdh + "'", "BINDID");//����ƻ�BINDID
            conn = DAOUtil.openConnectionTransaction();

            Vector<Hashtable<String, String>> vector = getEJXX(conn, bindid);
            BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FJ_S", vector, fjjh_bindid, uc.getUID());//������Ϣ����
//			int num = DAOUtil.getInt(conn, QUERY_COUNT, bindid);

            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uc.getUID(), "��̨���ִ�������ϵ����Ա��");
            return false;
        }
    }

    /**
     * ��ȡ������ӱ���Ϣ
     *
     * @param conn
     * @param bindid
     * @return
     * @throws SQLException
     */
    public Vector<Hashtable<String, String>> getEJXX(Connection conn, int bindid) throws SQLException {
        Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
        Hashtable<String, String> rec = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(QUERY_EJXX);
            rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (rs.next()) {
                rec = new Hashtable<String, String>();
                String hh = StrUtil.returnStr(rs.getString("HH"));//�к�
                String kfckbh = StrUtil.returnStr(rs.getString("KFCKBH"));//�ͻ��ֿ���
                String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//���޵���
                String kfmc = StrUtil.returnStr(rs.getString("KFMC"));//�ͷ�����
                String cpsx = StrUtil.returnStr(rs.getString("CPSX"));//��Ʒ����
                String cplh = StrUtil.returnStr(rs.getString("CPLH"));//��Ʒ�Ϻ�
                String sn = StrUtil.returnStr(rs.getString("SN"));//��ƷS/N
                String pn = StrUtil.returnStr(rs.getString("PN"));//��ƷS/N
                String cpmc = StrUtil.returnStr(rs.getString("CPMC"));//��Ʒ����
                String gztmh = StrUtil.returnStr(rs.getString("GZTMH"));//���������
                String rxbh = StrUtil.returnStr(rs.getString("RXBH"));//RX���
                String tph = StrUtil.returnStr(rs.getString("TPH"));//������
                String sfhg = StrUtil.returnStr(rs.getString("SFHG"));//�Ƿ�ϸ�
                String gzyy = StrUtil.returnStr(rs.getString("GZYY"));//����ԭ��
                String xqms = StrUtil.returnStr(rs.getString("XQMS"));//��������
                String ejgzyy = StrUtil.returnStr(rs.getString("EJGZYY"));//�������ԭ��
                String ejsfhg = StrUtil.returnStr(rs.getString("EJSFHG"));//�����Ƿ�ϸ�
                String ejxqms = StrUtil.returnStr(rs.getString("EJXQMS"));//������������
                String ejjcr = StrUtil.returnStr(rs.getString("EJJCR"));//��������
//				String sjgzyy = StrUtil.returnStr(rs.getString("SJGZYY"));//�������ԭ��
//				String sjsfhg = StrUtil.returnStr(rs.getString("SJSFHG"));//�����Ƿ�ϸ�
//				String sjxqms = StrUtil.returnStr(rs.getString("SJXQMS"));//������������
//				String sjjcr = StrUtil.returnStr(rs.getString("SJJCR"));//��������

                rec.put("HH", hh);
                rec.put("SXDH", sxdh);
                rec.put("KFCKBH", kfckbh);
                rec.put("KFMC", kfmc);
                rec.put("CPSX", cpsx);
                rec.put("CPLH", cplh);
                rec.put("SN", sn);
                rec.put("PN", pn);
                rec.put("CPMC", cpmc);
                rec.put("GZTMH", gztmh);
                rec.put("RXBH", rxbh);
                rec.put("TPH", tph);
                rec.put("SFHG", sfhg);
                rec.put("GZYY", gzyy);
                rec.put("XQMS", xqms);
                rec.put("EJGZYY", ejgzyy);
                rec.put("EJSFHG", ejsfhg);
                rec.put("EJXQMS", ejxqms);
                rec.put("EJJCR", ejjcr);
//				rec.put("SJGZYY", sjgzyy);
//				rec.put("SJSFHG", sjsfhg);
//				rec.put("SJXQMS", sjxqms);
//				rec.put("SJJCR", sjjcr);

                vector.add(rec);
            }

        } finally {
            DBSql.close(ps, rs);
        }
        return vector;
    }

}
