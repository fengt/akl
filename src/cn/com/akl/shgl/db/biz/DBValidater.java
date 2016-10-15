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
     * 验证明细和库存.
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

                /** 验证库存数量 */
                int haveSl = repositoryBiz.queryMaterialCanUse(conn, xmlx, wlbh, pch, ckhwdm, cpsx);
                if (cksl > haveSl) {
                    MessageQueue.getInstance().putMessage(uid, "物料：" + wlmc + " 库存数量不足！");
                    return false;
                }
            }

            return true;
        } finally {
            DBSql.close(ps, reset);
        }
    }

    /**
     * 验证序列号是否存在.
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

                /** 验证序列号，通过位置. */
                if (gztm != null && !"".equals(gztm.trim())) {
                    int haveXlh = repositoryBiz.queryXLHCount(conn, xmlx, xh, fhckbm, gztm);
                    if (haveXlh == 0) {
                        MessageQueue.getInstance().putMessage(uid, "序列号：" + gztm + " 在系统中没有，仓库：" + fhckbm + "！");
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
     * 验证物流状态.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateWLZT(Connection conn, int bindid, String uid) throws SQLException {
        String dh = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_DBDH, bindid);

        /** 获取待发货处理状态 */
        String clzt = DAOUtil.getStringOrNull(conn, "SELECT WLZT FROM BO_AKL_DFH_P WHERE DH=? AND WLZT<>?", dh, DfhConstant.WLZT_DCL);
        if (clzt != null && !clzt.equals("")) {
            return true;
        } else {
            MessageQueue.getInstance().putMessage(uid, "物流信息还未填写，不能继续办理，请通过物流流程填写本调拨单的物流信息!");
            return false;
        }
    }

    /**
     * 验证季度返京.
     *
     * @param conn
     * @param bindid
     * @param uid
     */
    public boolean validateJDFJ(Connection conn, int bindid, String uid) throws SQLException {
        String dblx = DAOUtil.getStringOrNull(conn, DBConstant.QUERY_DB_FORM_DBLX, bindid);
        if (DBConstant.DBLX_JDFJ.equals(dblx)) {
            // 验证不返货的返货原因是否有填写。
            String xh = DAOUtil.getStringOrNull(conn, "select XH from BO_AKL_DB_S WHERE BINDID=? AND (BFHYY='' OR BFHYY IS NULL) AND SJFHSL<>CKSL", bindid);
            if (xh != null) {
                MessageQueue.getInstance().putMessage(uid, "PN：" + xh + " ，有未填写返货原因的记录!");
                return false;
            }
        }

        return true;
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
