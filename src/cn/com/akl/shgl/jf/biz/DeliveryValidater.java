package cn.com.akl.shgl.jf.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.dict.util.DictionaryUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;

public class DeliveryValidater {

    /**
     * ��ѯ�Ƿ������������Ѿ�ʹ���˴˽�����¼.
     */
    private static final String QUERY_DYJFJL = "SELECT COUNT(*) FROM BO_AKL_WXJF_S jfs LEFT JOIN SYSFLOWSTEP step ON jfs.WORKFLOWSTEPID=step.ID WHERE jfs.SXCPHID=? AND jfs.ID<>? AND step.STEPNO>1";

    RepositoryBiz repositoryBiz = new RepositoryBiz();

    /**
     * ��֤�ֶ�ֵ�� <br/>
     * 1����֤�Ƿ������ֶκʹ���ʽ.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @throws SQLException
     */
    public void validateJfInfo(Connection conn, ResultSet reset, int bindid) throws SQLException {
        String clfs = reset.getString("CLFS");
        String sfsj = reset.getString("SFSJ");
        if (clfs == null || clfs.equals("")) {
            throw new RuntimeException("���齻����Ʒ��Ϣ���Ƿ��д���ʽ��");
        } else {
            if (sfsj == null || sfsj.equals("")) {
                throw new RuntimeException("���齻����Ʒ��Ϣ�С��Ƿ��������ֶ��Ƿ���д��");
            } else {
                if (sfsj.equals(XSDDConstant.YES)) {
                    if (!clfs.equals(DeliveryConstant.CLFS_HX)) {
                        throw new RuntimeException("������Ʒ��Ϣ�С�����ʽ��Ϊ��" + DictionaryUtil.parseNoToChinese(clfs) + "���ļ�¼���ܽ����滻������������");
                    }
                }
            }
        }

        String sxid = reset.getString("SXCPHH");

        /** ��ѯ���޼�¼״̬. */
        int count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SX_S WHERE SXCPHH=? AND ZT=? ", sxid, DeliveryConstant.SX_B_ZT_YJC);
        if (count == 0) {
            throw new RuntimeException("���޲�Ʒ�кţ�" + reset.getString("HH") + "���Ѿ������������������ˣ�");
        }
    }

    /**
     * ��֤���޵������Ƿ��ܽ���. <br/>
     * 1����֤���޼�¼״̬. <br/>
     * 2����֤�Ƿ��������ݰ�����.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @throws SQLException
     */
    public void validateSxInfo(Connection conn, ResultSet reset, int bindid, String uid) throws SQLException {

        /** ��ѯ�����������Ƿ��д����޼�¼. */
        /*
        count = DAOUtil.getIntOrNull(conn, QUERY_DYJFJL, id, sxid);
        if (count > 0) {
            throw new RuntimeException("���޲�Ʒ�кţ�" + reset.getString("HH") + "���Ѿ������������������ˣ�");
        }
        */

        /** �Ƿ��״��ʱ���ѯ. */
        int count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXJF_S WHERE JFSN=?", reset.getString("SN"));
        if (count > 0) {
            MessageQueue.getInstance().putMessage(uid, "���޲�Ʒ�кţ�" + reset.getString("HH") + "�����״��ʱ���");
        }

    }

    /**
     * ȱ����������֤.<br/>
     * 1�������֪ͨ����ô��Ҫ������Ƿ��㹻. <br/>
     * 2����֤�Ƿ�Ҫȱ�����������ȱ�������ȱ�㹻��Ҫ��ֹ.<br/>
     *
     * @param conn
     * @param reset
     * @param bindid
     * @param xmlb
     * @throws SQLException
     */
    public void validateRepository(Connection conn, int bindid, String uid, ResultSet reset, String xmlb) throws SQLException {
        String wlbh = reset.getString("WLBH");
        String wlmc = reset.getString("WLMC");
        String hwdm = reset.getString("HWDM");
        String pch = reset.getString("PCH");
        String sx = reset.getString("SX");
        int sl = reset.getInt("SL");
        String sfqhsq = reset.getString("SFQHSQ");

        int remaingNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);

        if (XSDDConstant.YES.equals(sfqhsq)) {
            if (sl <= remaingNum) {
                MessageQueue.getInstance().putMessage(uid, "�������ƣ�" + wlmc + "���п�棬�����Լ�������ȱ������!");
            }
        } else {
            if (sl > remaingNum) {
                throw new RuntimeException("�������ƣ�" + wlmc + "����治��!");
            }
        }
    }

    /**
     * У�����Ƿ����.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @param xmlb
     * @throws SQLException
     */
    public void validateRepository2(Connection conn, ResultSet reset, int bindid, String xmlb) throws SQLException {
        String wlbh = reset.getString("WLBH");
        String wlmc = reset.getString("WLMC");
        String hwdm = reset.getString("HWDM");
        String ckdm = reset.getString("CKDM");
        String pch = reset.getString("PCH");
        String sx = reset.getString("SX");
        int sl = reset.getInt("SL");
        String sfjf = reset.getString("SFJF");
        if (XSDDConstant.YES.equals(sfjf)) {
            int remaingNum = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, ckdm, sx);
            if (sl > 0) {
                if (sl > remaingNum) {
                    throw new RuntimeException("�������ƣ�" + wlmc + "����治��!");
                }
            }
        }
    }

    /**
     * ��֤�������.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @param xmlb
     * @throws SQLException
     */
    public void validatePart(Connection conn, ResultSet reset, int bindid, String xmlb) throws SQLException {
        String wlbh = reset.getString("WLBH");
        String wlmc = reset.getString("MC");
        String hwdm = reset.getString("HWDM");
        String ckdm = reset.getString("CKDM");
        String sx = reset.getString("SX");
        String pch = reset.getString("PCH");
        int sl = reset.getInt("SHSL");
        int remaingNum = repositoryBiz.queryMaterialCanUse(conn, xmlb, wlbh, pch, hwdm, sx);
        if (sl > 0) {
            if (sl > remaingNum) {
                throw new RuntimeException("�������ƣ�" + wlmc + "����治��!");
            }
        }
    }

    /**
     * �Ƿ���Ϊ�黹�Ĵ���Ʒ.
     *
     * @param conn
     * @param reset
     * @param bindid
     * @throws SQLException
     */
    public boolean isHaveNoYetSubstitute(Connection conn, ResultSet reset, int bindid) throws SQLException {
        String sfsh = reset.getString("SFSH");
        if (sfsh == null || sfsh.equals("")) {
            throw new RuntimeException("����Ʒ���� ���Ƿ��ջء� ����Ϊ�գ�");
        } else {
            return sfsh.equals(XSDDConstant.YES);
        }
    }
}
