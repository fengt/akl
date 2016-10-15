package cn.com.akl.shgl.yzsq.biz;

import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by huangming on 2015/4/28.
 */
public class YZSQValidate {

    /**
     * 校验资质的位置，以及资质的占用情况.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateStepNo1(Connection conn, int bindid, String uid) throws SQLException {
        // 验证总部发总部，这就不能发.
        String kfzxbm = DAOUtil.getStringOrNull(conn, "SELECT KFZXBM FROM BO_AKL_YZSQ_P WHERE BINDID=?", bindid);
        if (DfhConstant.XZKFBM.equals(kfzxbm)) {
            throw new RuntimeException("请不要总部发往总部!");
        }

        YZSQBiz yzsqBiz = new YZSQBiz();
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_YZSQ_S WHERE BINDID=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                int zzid = reset.getInt("ZZID");
                String zzlx = reset.getString("ZZLX");
                String zz = reset.getString("ZZ");
                String fzjgmc = reset.getString("FZJGMC");
                if (zzid == 0) {
                    throw new RuntimeException("资质数据有误,请重新选择资质!");
                } else {
                    // 验证资质是否在最终存档地.
                    String result = yzsqBiz.isInCDD(conn, zz, zzid);
                    if (result == null || !result.equals("y")) {
                        MessageQueue.getInstance().putMessage(uid, "资质类型：" + zzlx + " -- " + zz + " -- " + fzjgmc + " 不在最终存档地，不能进行申请!");
                        return false;
                    }

                    // 验证资质是否被其他用章申请占用.
                    Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_YZSQ_S WHERE BINDID<>? AND WORKFLOWSTEPID<>'18945' AND ZZID=? AND ISEND=0", bindid, zzid);
                    if (count != null && count > 0) {
                        MessageQueue.getInstance().putMessage(uid, "资质类型：" + zzlx + " -- " + zz + " -- " + fzjgmc + " 已经被其他用章流程申请!");
                        return false;
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
        return true;
    }

    /**
     * 校验资质是否回到了最终位置.
     *
     * @param conn
     * @param bindid
     * @param uid
     * @return
     * @throws SQLException
     */
    public boolean validateStepNo7(Connection conn, int bindid, String uid) throws SQLException {
        YZSQBiz yzsqBiz = new YZSQBiz();
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM BO_AKL_YZSQ_S WHERE BINDID=?");
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                int zzid = reset.getInt("ZZID");
                String zzlx = reset.getString("ZZLX");
                String zz = reset.getString("ZZ");
                if (zzid == 0) {
                    throw new RuntimeException("资质数据有误,请重新选择资质!");
                } else {
                    // 验证资质是否在最终存档地.
                    String result = yzsqBiz.isInCDD(conn, zz, zzid);
                    if (result == null || !result.equals("y")) {
                        MessageQueue.getInstance().putMessage(uid, "");
                        return false;
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
        return true;
    }

}
