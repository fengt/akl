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
     * 替换规则：<br/>
     * 1、根据物料找到对应的替换组，根据换入过滤.<br/>
     * 2、列举出替换组的所有物料，根据换出过滤、优先级分组过滤、按替换优先级排序.<br/>
     * 3、按优先级排序.<br/>
     * 4、查询有物料的记录.<br/>
     * 5、返回记录.<br/>
     * 6、若没有替换规则对应，则采用自己换自己的方式.<br/>
     *
     * @param conn
     * @param wlbh
     * @return
     * @throws SQLException
     */
    public List<String> replaceMaterial(Connection conn, String xmlb, String wlbh, String sx) throws SQLException {

        ArrayList<String> arrayList = new ArrayList<String>(30);

        // 获取物料的替换规则.
        ArrayList<String> thgzList = DAOUtil.getStringCollection(conn, DeliveryConstant.QUERY_THGZ, xmlb, XSDDConstant.YES, wlbh, sx);

        for (int i = 0; i < thgzList.size(); i++) {
            String thgz = thgzList.get(i);

            // 若有替换规则，则进行优先级排序.
            String yxjfz = DAOUtil.getStringOrNull(conn, DeliveryConstant.QUERY_YXJFZ, xmlb, XSDDConstant.YES, wlbh, thgz, sx);

            PreparedStatement ps = null;
            ResultSet reset = null;
            try {
                // 加入停产物料过滤.
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
     * 包装物料信息.
     *
     * @return
     */
    public Hashtable<String, String> packMaterialInfo(Connection conn, Hashtable<String, String> hashtable, String wlbh) {
        return hashtable;
    }

}
