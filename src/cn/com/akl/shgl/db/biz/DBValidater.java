package cn.com.akl.shgl.db.biz;

import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBValidater {

    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    /**
     * ��֤��ϸ�Ϳ��.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateMXAndKC(Connection conn, int bindid, String uid, String xmlx) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String wlmc = reset.getString("WLMC");
                String ckhwdm = reset.getString("CKHWDM");
                String pch = reset.getString("PCH");
                int cksl = reset.getInt("CKSL");
                String cpsx = reset.getString("CPSX");

                /** ��֤������� */
                int haveSl = repositoryBiz.queryMaterialCanUse(conn, xmlx, wlbh, pch, ckhwdm, cpsx);
                if (cksl > haveSl) {
                    MessageQueue.getInstance().putMessage(uid, "���ϣ�" + wlmc + " ����������㣡");
                    return false;
                }
            }

            return true;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * ��֤���к��Ƿ����.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateXLH(Connection conn, int bindid, String uid, String xmlx, String fhckbm) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(DBConstant.QUERY_DB_FORM_XLH);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String gztm = reset.getString("XLH");
                String xh = reset.getString("XH");

                /** ��֤���кţ�ͨ��λ��. */
                if (gztm != null && !"".equals(gztm.trim())) {
                    int haveXlh = repositoryBiz.queryXLHCount(conn, xmlx, xh, fhckbm, gztm);
                    if (haveXlh == 0) {
                        MessageQueue.getInstance().putMessage(uid, "���кţ�" + gztm + " ��ϵͳ��û�У��ֿ⣺" + fhckbm + "��");
                        return false;
                    }
                }
            }
            return true;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * ��֤����״̬.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateWLZT(Connection conn, int bindid, String uid) throws SQLException {
        String dh = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_DBDH, bindid);

        /** ��ȡ����������״̬ */
        String clzt = DAOUtil.getStringOrNull(conn, "SELECT WLZT FROM BO_AKL_DFH_P WHERE DH=? AND WLZT<>?", dh, DfhConstant.WLZT_DCL);
        if (clzt != null && !clzt.equals("")) {
            return true;
        } else {
            MessageQueue.getInstance().putMessage(uid, "������Ϣ��δ��д�����ܼ���������ͨ������������д����������������Ϣ!");
            return false;
        }
    }

    /**
     * ��֤���ȷ���.
     *
     * @param conn
     * @param bindid
     * @param uid
     */
    public boolean validateJDFJ(Connection conn, int bindid, String uid) throws SQLException {
        String dblx = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_DBLX, bindid);
        if (DBConstant.DBLX_JDFJ.equals(dblx)) {
            // ��֤�������ķ���ԭ���Ƿ�����д��
            String xh = DAOUtil.getStringOrNull(conn, "select XH from BO_AKL_DB_S WHERE BINDID=? AND (BFHYY='' OR BFHYY IS NULL) AND SJFHSL<>CKSL", bindid);
            if (xh != null) {
                MessageQueue.getInstance().putMessage(uid, "PN��" + xh + " ����δ��д����ԭ��ļ�¼!");
                return false;
            }
        }

        return true;
    }


    /**
     * ��֤���������Ƿ���ͬ.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateZXDAndCKD(Connection conn, int bindid, String uid) throws SQLException {
        String sfgjpchzx = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_SFGJPCHZX, bindid);

        List<String> zxd = getList(conn, bindid, uid, DBConstant.QUERY_ZXD_GROUP_WLHPCH);
        List<String> ckd = getList(conn, bindid, uid, DBConstant.QUERY_DB_FORM_BODY_GROUP_WLH);
        if (sfgjpchzx != null && sfgjpchzx.equals(XSDDConstant.YES)) {
            ckd = getList(conn, bindid, uid, DBConstant.QUERY_DB_FORM_BODY_GROUP_WLHPCH);
        }

        boolean isPass = true;
        for (String ckdflag : ckd) {
            boolean isEquals = false;
            for (String zxdflag : zxd) {
                if (zxdflag != null && zxdflag.equals(ckdflag)) {
                    isEquals = true;
                }
            }
            if (isEquals == false) {
                isPass = false;
                StringBuilder messSb = new StringBuilder();
                messSb.append("����װ������!");
                try {
                    String[] split = ckdflag.split(",");
                    messSb.append("���ϣ�");
                    messSb.append(split[2]);
                    messSb.append("����������Ӧ��Ϊ��");
                    messSb.append(split[3]);
                    messSb.append("��");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MessageQueue.getInstance().putMessage(uid, messSb.toString());
            }
        }

        String message = DAOUtil.getStringOrNull(conn, "SELECT '��Ʒ���ƣ�'+CPMC+' �����κţ�'+ PCH+'��װ����ţ�' +ZXXH+' �������ظ���¼�����飡' FROM BO_AKL_ZXD_S WHERE BINDID=? GROUP BY WLBH, CPMC, PCH, ZXXH HAVING COUNT(*)>1", bindid);
        if (message != null) {
            MessageQueue.getInstance().putMessage(uid, message);
            isPass = false;
        }

        String message2 = DAOUtil.getStringOrNull(conn, "SELECT CPMC FROM BO_AKL_ZXD_S WHERE BINDID=? AND ZXSL=?", bindid, 0);
        if (message2 != null) {
            MessageQueue.getInstance().putMessage(uid, "���ϣ�" + message2 + " ����һ��װ������Ϊ0�ļ�¼������!");
            isPass = false;
        }

        return isPass;
    }

    /**
     * ��ȡ���ⵥ������Ϣ.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    private List<String> getList(Connection conn, int bindid, String uid, String sql) throws SQLException {
        List<String> ckd = new ArrayList<String>(20);

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(sql);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String wlbh = reset.getString("WLBH");
                String pch = reset.getString("PCH");
                String sl = reset.getString("SL");
                String cpmc = reset.getString("CPMC");

                if (sl == null || "0".equals(sl)) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                sb.append(wlbh).append(",");
                sb.append(pch).append(",");
                sb.append(cpmc).append(",");
                sb.append(sl);
                ckd.add(sb.toString());
            }
        } finally {
            DBSql.close(ps, reset);
        }
        return ckd;
    }

}
