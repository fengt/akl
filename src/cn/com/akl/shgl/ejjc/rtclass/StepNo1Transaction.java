package cn.com.akl.shgl.ejjc.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {

    //查询二检子表信息
    private static final String QUERY_EJXX = "SELECT * FROM BO_AKL_FJ_S WHERE BINDID=?";

    //注释内容为之前任务分配人员的检测数量，现在已取消该功能。
    //二检产品数量
//	private static final String QUERY_COUNT = "SELECT COUNT(1) NUM FROM BO_AKL_FJ_S WHERE BINDID=?";
    //更新复检计划人员及数量
//	private static final String UPDATE_NUMBER = "UPDATE BO_AKL_FJRWFP_S SET YWCSL=ISNULL(YWCSL,0)+? WHERE BINDID=? AND YGGH=?";

    private Connection conn = null;
    private UserContext uc;

    public StepNo1Transaction() {
    }

    public StepNo1Transaction(UserContext arg0) {
        super(arg0);
        this.uc = arg0;
        setProvider("fengtao");
        setDescription("反向更新复检计划流程中信息。");
    }

    @Override
    public boolean execute() {
        int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_FJJH_P", bindid);
        String fjjhdh = head.get("FJJHDH").toString();//复检计划单号
//		String cjrgh = head.get("CJRGH").toString();//创建人工号
//		String userkey = DBSql.getString("SELECT USERID+'<'+USERNAME+'>' AS USERKEY FROM ORGUSER WHERE USERID='"+cjrgh+"'", "USERKEY");

        try {
            int fjjh_bindid = DBSql.getInt("SELECT BINDID FROM BO_AKL_FJJH_P WHERE FJDJBH='" + fjjhdh + "'", "BINDID");//复检计划BINDID
            conn = DAOUtil.openConnectionTransaction();

            Vector<Hashtable<String, String>> vector = getEJXX(conn, bindid);
            BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_FJ_S", vector, fjjh_bindid, uc.getUID());//二检信息插入
//			int num = DAOUtil.getInt(conn, QUERY_COUNT, bindid);

            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现错误，请联系管理员！");
            return false;
        }
    }

    /**
     * 获取二检的子表信息
     *
     * @param conn
     * @param bindid
     * @return
     * @throws SQLException
     */
    public Vector<Hashtable<String, String>> getEJXX(Connection conn, int bindid) throws SQLException {
        Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
        Hashtable<String, String> rec = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(QUERY_EJXX);
            rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (rs.next()) {
                rec = new Hashtable<String, String>();
                String hh = StrUtil.returnStr(rs.getString("HH"));//行号
                String kfckbh = StrUtil.returnStr(rs.getString("KFCKBH"));//客户仓库编号
                String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//送修单号
                String kfmc = StrUtil.returnStr(rs.getString("KFMC"));//客服名称
                String cpsx = StrUtil.returnStr(rs.getString("CPSX"));//产品属性
                String cplh = StrUtil.returnStr(rs.getString("CPLH"));//产品料号
                String sn = StrUtil.returnStr(rs.getString("SN"));//产品S/N
                String pn = StrUtil.returnStr(rs.getString("PN"));//产品S/N
                String cpmc = StrUtil.returnStr(rs.getString("CPMC"));//产品名称
                String gztmh = StrUtil.returnStr(rs.getString("GZTMH"));//故障条码号
                String rxbh = StrUtil.returnStr(rs.getString("RXBH"));//RX编号
                String tph = StrUtil.returnStr(rs.getString("TPH"));//特批号
                String sfhg = StrUtil.returnStr(rs.getString("SFHG"));//是否合格
                String gzyy = StrUtil.returnStr(rs.getString("GZYY"));//故障原因
                String xqms = StrUtil.returnStr(rs.getString("XQMS"));//详情描述
                String ejgzyy = StrUtil.returnStr(rs.getString("EJGZYY"));//二检故障原因
                String ejsfhg = StrUtil.returnStr(rs.getString("EJSFHG"));//二检是否合格
                String ejxqms = StrUtil.returnStr(rs.getString("EJXQMS"));//二检详情描述
                String ejjcr = StrUtil.returnStr(rs.getString("EJJCR"));//二检检测人
//				String sjgzyy = StrUtil.returnStr(rs.getString("SJGZYY"));//三检故障原因
//				String sjsfhg = StrUtil.returnStr(rs.getString("SJSFHG"));//三检是否合格
//				String sjxqms = StrUtil.returnStr(rs.getString("SJXQMS"));//三检详情描述
//				String sjjcr = StrUtil.returnStr(rs.getString("SJJCR"));//三检检测人

                rec.put("HH", hh);
                rec.put("SXDH", sxdh);
                rec.put("KFCKBH", kfckbh);
                rec.put("KFMC", kfmc);
                rec.put("CPSX", cpsx);
                rec.put("CPLH", cplh);
                rec.put("SN", sn);
                rec.put("PN", pn);
                rec.put("CPMC", cpmc);
                rec.put("GZTMH", gztmh);
                rec.put("RXBH", rxbh);
                rec.put("TPH", tph);
                rec.put("SFHG", sfhg);
                rec.put("GZYY", gzyy);
                rec.put("XQMS", xqms);
                rec.put("EJGZYY", ejgzyy);
                rec.put("EJSFHG", ejsfhg);
                rec.put("EJXQMS", ejxqms);
                rec.put("EJJCR", ejjcr);
//				rec.put("SJGZYY", sjgzyy);
//				rec.put("SJSFHG", sjsfhg);
//				rec.put("SJXQMS", sjxqms);
//				rec.put("SJJCR", sjjcr);

                vector.add(rec);
            }

        } finally {
            DBSql.close(ps, rs);
        }
        return vector;
    }

}
