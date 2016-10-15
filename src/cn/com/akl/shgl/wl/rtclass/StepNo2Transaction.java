package cn.com.akl.shgl.wl.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import cn.com.akl.shgl.wl.biz.WLConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

    private QSCYBiz qscyBiz = new QSCYBiz();

    public StepNo2Transaction() {
        super();
    }

    public StepNo2Transaction(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("更新单据状态");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            service(conn, bindid);
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 处理代发货记录的更新.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public void service(Connection conn, int bindid) throws SQLException {
        String uid = getUserContext().getUID();

        Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_WLYSD_P", bindid);
        String fhr = boData.get("FHR");
        String fhdz = boData.get("FHDZ");
        String fhf = boData.get("FHF");
        String fhs = boData.get("FHS");
        String fhshi = boData.get("FHSHI");
        String fhqx = boData.get("FHQX");
        String shdz = boData.get("SHDZ");
        String shs = boData.get("SHS");
        String shshi = boData.get("SHSHI");
        String shqx = boData.get("SHQX");
        String fhsj = boData.get("KDSJ");
        String qsr = boData.get("QSR");
        String qssj = boData.get("QSSJ");

        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(WLConstant.QUERY_WLDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                String dh = reset.getString("CKDH");
                String jlbz = reset.getString("JLBZ");
                int updateCount = DAOUtil.executeUpdate(conn, WLConstant.UPDATE_DFH_ZT, DfhConstant.WLZT_YQS, dh, jlbz);
                if (updateCount == 0) {
                    throw new RuntimeException("业务单号" + dh + "已经填写过物流信息, 物流单状态更新失败!");
                }

                // 查询物流单身是否存在数量和签收数量不符的,如果有则置为有数量差异的状态.
                Integer cyCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_DFH_P dfhp LEFT JOIN BO_AKL_DFH_S dfhs ON dfhp.BINDID=dfhs.BINDID AND ISNULL(dfhp.JLBZ,0)=ISNULL(dfhs.JLBZ,0) WHERE DH=? AND QSSL<>SL", dh);
                if (cyCount != null && cyCount > 0) {
                    DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_WLYSD_XM_S SET SFSLCY=? WHERE BINDID=? AND CKDH=?", XSDDConstant.YES, bindid, dh);
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }
    }
}
