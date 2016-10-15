package cn.com.akl.shgl.yzsq.biz;

import cn.com.akl.util.DAOUtil;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Created by huangming on 2015/4/28.
 */
public class YZSQBiz {

    /**
     * ���ɲ�ѯ�����Ƿ��ڵ�ǰ�浵�ص�SQL.
     *
     * @param zz
     * @return
     */
    private String generatZZGLTableSQL(String zz) {
        String table = getTable(zz);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CASE DQCDD WHEN ZZCDD THEN 'y' ELSE 'n' END AS flag FROM ");
        sb.append(table);
        sb.append(" where id=?");

        return sb.toString();
    }

    /**
     * ��ȡ�������ʵ�ǰ�浵�ص�SQL.
     *
     * @param zz
     * @return
     */
    private String generatZZGLUpdateSQL2(String zz) {
        String table = getTable(zz);
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(table);
        sb.append(" SET DQCDD=?,ZZCDD=? where id=?");
        return sb.toString();
    }

    /**
     * ��ȡ�������ʵ�ǰ�浵�غ����մ浵�ص�SQL.
     *
     * @param zz
     * @return
     */
    private String generatZZGLUpdateSQL(String zz) {
        String table = getTable(zz);
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(table);
        sb.append(" SET DQCDD=? where id=?");
        return sb.toString();
    }

    /**
     * ��ȡ��ǰ���������ı��.
     *
     * @param zz
     * @return
     */
    private String getTable(String zz) {
        String[][] tableMap = YZSQConstant.TABLE_MAP;
        String table = null;

        for (String[] arr : tableMap) {
            String lx = arr[0];
            if (lx.equals(zz)) {
                table = arr[1];
            }
        }

        if (table == null) {
            throw new RuntimeException("�޷�ʶ������� " + zz);
        }
        return table;
    }

    /**
     * �Ƿ��ڴ浵��.
     *
     * @param conn
     * @param zz
     * @param zzid
     * @return
     * @throws SQLException
     */
    public String isInCDD(Connection conn, String zz, int zzid) throws SQLException {
        return DAOUtil.getStringOrNull(conn, generatZZGLTableSQL(zz), zzid);
    }

    /**
     * ���´浵���ڵ�.
     *
     * @param conn
     * @param zz
     * @param zzid
     * @param szd
     * @return
     */
    public int updateInPalce(Connection conn, String zz, int zzid, String szd) throws SQLException {
        return DAOUtil.executeUpdate(conn, generatZZGLUpdateSQL(zz), szd, zzid);
    }

    /**
     * ���´浵���ڵغ����մ浵��.
     *
     * @param conn
     * @param zz
     * @param zzid
     * @param szd
     * @return
     * @throws SQLException
     */
    public int updateALLPalce(Connection conn, String zz, int zzid, String szd) throws SQLException {
        return DAOUtil.executeUpdate(conn, generatZZGLUpdateSQL2(zz), szd, szd, zzid);
    }

}
