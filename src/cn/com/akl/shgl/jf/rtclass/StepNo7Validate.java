package cn.com.akl.shgl.jf.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.jf.biz.DeliveryConstant;
import cn.com.akl.shgl.jf.biz.DeliveryValidater;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.sleepycat.je.rep.impl.TextProtocol;

public class StepNo7Validate extends WorkFlowStepRTClassA {

    private DeliveryValidater deliveryValidater = new DeliveryValidater();

    public StepNo7Validate() {
        super();
    }

    public StepNo7Validate(UserContext arg0) {
        super(arg0);
        setDescription("1.代用品验证. 2.SN验证.");
    }

    @Override
    public boolean execute() {
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DBSql.open();
            //validate(conn, bindid);
            return validate2(conn, bindid);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 验证代用品赔偿金.
     *
     * @param conn
     * @param bindid
     * @throws SQLException
     */
    public boolean validate(Connection conn, int bindid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet reset = null;

        try {
            ps = conn.prepareStatement(DeliveryConstant.QUERY_JF_DYPDS);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                if (deliveryValidater.isHaveNoYetSubstitute(conn, reset, bindid)) {
                    BigDecimal pcj = DAOUtil.getBigDecimalOrNull(conn, DeliveryConstant.QUERY_PCJ, bindid);
                    if (pcj == null || pcj.doubleValue() == 0) {
                        throw new RuntimeException("有代用品未归还，赔偿金不能为0！");
                    } else {
                        return true;
                    }
                }
            }
        } finally {
            DBSql.close(ps, reset);
        }

        return true;
    }

    /**
     * 校验sn号是否填写完成.
     *
     * @param conn
     * @param bindid
     * @return
     * @throws SQLException
     */
    public boolean validate2(Connection conn, int bindid) throws SQLException {
        String sxcphh = DAOUtil.getStringOrNull(conn, "SELECT SXCPHH FROM BO_AKL_WXJF_S WHERE BINDID=? GROUP BY SXCPHH,SFJF HAVING COUNT(*)>1", bindid);
        if (sxcphh != null) {
            MessageQueue.getInstance().putMessage(getUserContext().getUID(), "送修产品行号:" + sxcphh + " 必须一次交付!");
            return false;
        }
        return true;
    }


}
