package cn.com.akl.shgl.fjjh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import org.apache.commons.lang.StringUtils;

public class SpotcheckBiz {

    /**
     * ������ȡ���ϵĳ������ƽ����������и����ϵĸ����ͷ�������ȡ��Щ�ͷ���������Ϣ���븴��ƻ��ӱ���
     *
     * @param conn
     * @param bindid
     * @param uid
     * @param allBindid
     * @param wlbh
     * @param sx
     * @param cjsl
     * @throws SQLException
     */
    public void numberAvarageAndFillBackData(Connection conn, final int bindid, final String uid,
                                             ArrayList<Integer> allBindid, String wlbh, String sx, int cjsl) throws SQLException {
        // ƴ��SQL������ƴ��IN��ѯ��������ѯ�����б�ѡ�еĵ�����.
        String allbindid = StringUtils.join(allBindid.toArray(), ",");
        StringBuilder sqlBuilder = new StringBuilder("SELECT BINDID, ISNULL(SJCKSL,0) FJSL FROM BO_AKL_DB_S WHERE WLBH=? AND CPSX=? AND BINDID IN (");
        if (allBindid.size() > 0) {
            sqlBuilder.append(allbindid);
        } else {
            sqlBuilder.append(0);
        }
        sqlBuilder.append(") ORDER BY ISNULL(SJCKSL,0)");

        // ��ȡ�������еĿɳ������.
        List<int[]> kfjslList = new ArrayList<int[]>(50);
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(sqlBuilder.toString());
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, wlbh, sx);
            while (reset.next()) {
                int[] arr = new int[3];
                arr[0] = reset.getInt("BINDID");
                arr[1] = reset.getInt("FJSL");
                arr[2] = 0;
                kfjslList.add(arr);
            }
        } finally {
            DBSql.close(ps, reset);
        }

        if (kfjslList.size() == 0) {
            throw new RuntimeException("�����ϲ��ڱ��ƻ���Χ�ĵ�������!");
        }

        // ��ȡ���ƽ��ֵ.
        int average = cjsl / kfjslList.size();
        int more = cjsl - average * kfjslList.size();
        int count = 0;

        // ƽ����������.
        for (int[] arr : kfjslList) {
            int dbBindid = arr[0];
            int canUse = arr[1];
            int a;
            a = average - canUse;
            if (a > 0) {
                more = more + a;
                arr[2] = average - a;
            } else if (a < 0) {
                int pf = more / (kfjslList.size() - count);
                if (-a < pf) {
                    more = more - Math.abs(a);
                    arr[2] = average + Math.abs(a);
                } else {
                    more = more - pf;
                    arr[2] = average + pf;
                }
            } else {
                arr[2] = average;
            }

            int updateCount = DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_DB_S SET FJSL=? WHERE BINDID=? AND WLBH=? AND CPSX=?", arr[2], dbBindid, wlbh, sx);
            if (updateCount != 1) throw new RuntimeException("���ʱ��������������ʧ�ܣ�");

            count++;
        }

        for (int[] arr : kfjslList) {
            DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_DB_P_S, new ResultPaserAbs() {
                public boolean parse(Connection conn, ResultSet rs) throws SQLException {
                    insertFJJHData(conn, bindid, rs, uid);
                    return true;
                }
            }, arr[0], wlbh, sx);
        }
    }

    /**
     * ��дComparator��������map�����ɴ�С��
     *
     * @param map
     * @return
     */
    public List<Map.Entry<Integer, Integer>> sortMap(Map<Integer, Integer> map) {
        List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>();
        list.addAll(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> obj1, Map.Entry<Integer, Integer> obj2) {
                if (obj1.getValue().intValue() < obj2.getValue().intValue())
                    return 1;
                if (obj1.getValue().intValue() == obj2.getValue().intValue())
                    return 0;
                else
                    return -1;
            }
        });
        return list;
    }

    /**
     * ����ƻ��ӱ����ݴ���
     *
     * @param conn
     * @param bindid
     * @param rs
     * @param uid
     * @throws SQLException
     */
    public void insertFJJHData(Connection conn, int bindid, ResultSet rs, String uid) throws SQLException {
        Hashtable<String, String> body = new Hashtable<String, String>();
        String kfzx = StrUtil.returnStr(rs.getString("FHKFCKBM"));
        String kfbm = StrUtil.returnStr(rs.getString("FHKFCKMC"));
        String dbdh = StrUtil.returnStr(rs.getString("DBDH"));
        String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
        String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
        String xh = StrUtil.returnStr(rs.getString("XH"));
        String sx = StrUtil.returnStr(rs.getString("CPSX"));
        String fjsl = StrUtil.returnStr(rs.getString("SJCKSL"));
        int fjjhsl = rs.getInt("FJSL");
        body.put("KFZX", kfzx);
        body.put("KFBM", kfbm);
        body.put("DBDH", dbdh);
        body.put("WLBH", wlbh);
        body.put("WLMC", wlmc);
        body.put("XH", xh);
        body.put("SX", sx);
        body.put("FJSL", fjsl);
        body.put("FJJHSL", String.valueOf(fjjhsl));

        try {
            if (fjjhsl > 0)
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FJJH_S", body, bindid, uid);
        } catch (AWSSDKException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * ��ո��������������޸ĳ���������ٴε������ʱ��
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void clearFJSL(Connection conn, int bindid) throws SQLException {
        DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_FJJH_S, new ResultPaserAbs() {
            public boolean parse(Connection conn, ResultSet rs) throws SQLException {
                String dbdh = rs.getString("DBDH");
                String wlbh = rs.getString("WLBH");
                String sx = rs.getString("SX");
                int updateCount = DAOUtil.executeUpdate(conn, FJJHCnt.UPDATE_FJSLToNull, wlbh, sx, dbdh);
                if (updateCount != 1) throw new RuntimeException("����������ʱ�����ʧ�ܣ�");
                return true;
            }
        }, bindid);

        //����ӱ�����
        DAOUtil.executeUpdate(conn, FJJHCnt.DELETE_FJJH_S, bindid);
    }

    /**
     * У��ͷ������и��������������趨ֵ�Ŀͷ�����
     *
     * @param conn
     * @param bindid
     * @param fhck
     * @param kffz
     * @return
     * @throws SQLException
     */
    public List<String> checkTheNumber(Connection conn, int bindid, String fhck, String kffz) throws SQLException {
        List<String> list = new ArrayList<String>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(FJJHCnt.QUERY_UNFINISHED);
            rs = DAOUtil.executeFillArgsAndQuery(conn, ps, kffz, fhck, kffz);
            while (rs.next()) {
                String kfzx = StrUtil.returnStr(rs.getString("KFCKMC"));
                int sdz = rs.getInt("SDZ");//�趨ֵ
                int fjzl = rs.getInt("FJZL");//��������
                String message = "��" + kfzx + "���ĸ�������" + fjzl + "С���趨ֵ" + sdz;
                list.add(message);
            }
        } finally {
            DBSql.close(ps, rs);
        }
        return list;
    }
}
