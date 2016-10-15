package cn.com.akl.shgl.jf.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.actionsoft.awf.util.DBSql;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.util.DAOUtil;

public class ReplacementRuleBiz {

    /**
     * �滻����<br/>
     * 1�����������ҵ���Ӧ���滻�飬���ݻ������.<br/>
     * 2���оٳ��滻����������ϣ����ݻ������ˡ����ȼ�������ˡ����滻���ȼ�����.<br/>
     * 3�������ȼ�����.<br/>
     * 4����ѯ�����ϵļ�¼.<br/>
     * 5�����ؼ�¼.<br/>
     * 6����û���滻�����Ӧ��������Լ����Լ��ķ�ʽ.<br/>
     *
     * @param conn
     * @param wlbh
     * @return
     * @throws SQLException
     */
    public List<String> replaceMaterial(Connection conn, String xmlb, String wlbh, String sx) throws SQLException {

        ArrayList<String> arrayList = new ArrayList<String>(30);

        // ��ȡ���ϵ��滻����.
        ArrayList<String> thgzList = DAOUtil.getStringCollection(conn, DeliveryConstant.QUERY_THGZ, xmlb, XSDDConstant.YES, wlbh, sx);

        for (int i = 0; i < thgzList.size(); i++) {
            String thgz = thgzList.get(i);

            // �����滻������������ȼ�����.
            String yxjfz = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YXJFZ, xmlb, XSDDConstant.YES, wlbh, thgz, sx);

            PreparedStatement ps = null;
            ResultSet reset = null;
            try {
                // ����ͣ�����Ϲ���.
                ps = conn.prepareStatement(DeliveryConstant.QUERY_REPLACE_WLBH);
                reset = DAOUtil.executeFillArgsAndQuery(conn, ps, XSDDConstant.YES, thgz, yxjfz, sx);
                while (reset.next()) {
                    String kthwlbh = reset.getString("WLBH");
                    if (kthwlbh != null && !"".equals(kthwlbh)) {
                        if (!arrayList.contains(kthwlbh)) {
                            arrayList.add(kthwlbh);
                        }
                    }
                }
            } finally {
                DBSql.close(ps, reset);
            }
        }

        return arrayList;
    }

    /**
     * ��װ������Ϣ.
     *
     * @return
     */
    public Hashtable<String, String> packMaterialInfo(Connection conn, Hashtable<String, String> hashtable, String wlbh) {
        return hashtable;
    }

}
