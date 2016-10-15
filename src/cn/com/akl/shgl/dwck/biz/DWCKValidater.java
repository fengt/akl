package cn.com.akl.shgl.dwck.biz;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.db.biz.DBConstant;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DWCKValidater {

    /**
     * 验证物流状态.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateWLZT(Connection conn, int bindid, String uid) throws SQLException {
        String dh = DAOUtil.getStringOrNull(conn, "SELECT DWCKDH FROM BO_AKL_SH_DWCK_P WHERE BINDID=?", bindid);

        /** 获取待发货处理状态 */
        String clzt = DAOUtil.getStringOrNull(conn, "SELECT WLZT FROM BO_AKL_DFH_P WHERE DH=? AND (WLZT=? OR WLZT=?)", dh, DfhConstant.WLZT_YCL, DfhConstant.WLZT_YQS);
        if (clzt != null && !clzt.equals("")) {
            return true;
        } else {
            MessageQueue.getInstance().putMessage(uid, "物流信息还未填写，不能继续办理!");
            return false;
        }
    }

    /**
     * 验证汇总数据是否相同.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateZXDAndCKD(Connection conn, int bindid, String uid) throws SQLException {
        String sfgjpchzx = DAOUtil.getStringOrNull(conn, DWCKConstnat.QUERY_DWCK_SFGJPCHZX, bindid);

        List<String> zxd = getList(conn, bindid, uid, DWCKConstnat.QUERY_ZXD_GROUP_WLHPCH);
        List<String> ckd = getList(conn, bindid, uid, DWCKConstnat.QUERY_DWCK_GROUP_WLH);
        if (sfgjpchzx != null && sfgjpchzx.equals(XSDDConstant.YES)) {
            ckd = getList(conn, bindid, uid, DWCKConstnat.QUERY_DWCK_GROUP_WLHPCH);
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
                messSb.append("请检查装箱数量!");
                try {
                    String[] split = ckdflag.split(",");
                    messSb.append("物料：");
                    messSb.append(split[2]);
                    messSb.append("，数量总数应该为：");
                    messSb.append(split[3]);
                    messSb.append("。");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MessageQueue.getInstance().putMessage(uid, messSb.toString());
            }
        }

        String message = DAOUtil.getStringOrNull(conn, "SELECT '产品名称：'+CPMC+' ，批次号：'+ PCH+'，装箱箱号：' +ZXXH+' 出现了重复记录，请检查！' FROM BO_AKL_ZXD_S WHERE BINDID=? GROUP BY WLBH, CPMC, PCH, ZXXH HAVING COUNT(*)>1", bindid);
        if (message != null) {
            MessageQueue.getInstance().putMessage(uid, message);
            isPass = false;
        }

        String message2 = DAOUtil.getStringOrNull(conn, "SELECT CPMC FROM BO_AKL_ZXD_S WHERE BINDID=? AND ZXSL=?", bindid, 0);
        if (message2 != null) {
            MessageQueue.getInstance().putMessage(uid, "物料：" + message2 + " 存在一条装箱数量为0的记录，请检查!");
            isPass = false;
        }

        return isPass;
    }

    /**
     * 获取出库单汇总信息.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    private List<String> getList(Connection conn, int bindid, String uid, String sql) throws SQLException {
        List<String> ckd = new ArrayList<String>(20);
        StringBuilder sb = new StringBuilder();

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

                sb.append(wlbh).append(",");
                sb.append(pch).append(",");
                sb.append(cpmc).append(",");
                sb.append(sl);
                ckd.add(sb.toString());
                sb.delete(0, sb.length());
            }
        } finally {
            DBSql.close(ps, reset);
        }
        return ckd;
    }

}
