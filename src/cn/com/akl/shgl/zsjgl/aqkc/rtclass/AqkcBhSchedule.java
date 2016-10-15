package cn.com.akl.shgl.zsjgl.aqkc.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 安全库存自动插入缺货记录定时器.
 */
public class AqkcBhSchedule implements IJob {

	private static final String no = "025001";
	private static final String zt = "076278";//已申请
	private static final String qhfs = "073265";//缺货方式：安全库存补货
	
	private static final String DELETE_QHJL = "DELETE FROM BO_AKL_QHJL WHERE QHFS=? AND ZT=?";//删除安全库存缺货记录
	
    private RepositoryBiz repositoryBiz = new RepositoryBiz();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 1、获取安全库存值.
        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet reset = null;

        try {
            conn = DAOUtil.openConnectionTransaction();
            
            /**
             * 先删除未配货的安全库存缺货记录，然后再插入新安全库存缺货记录
             */
            DAOUtil.executeUpdate(conn, DELETE_QHJL, qhfs, zt);
            
            stat = conn.prepareStatement("SELECT * FROM BO_AKL_AQKCWH_S WHERE ISEND=1");
            reset = DAOUtil.executeFillArgsAndQuery(conn, stat);
            Date date = Calendar.getInstance().getTime();
            DateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sf.format(date);
            while (reset.next()) {
                // 2、比较库存值.
                String xmlb = reset.getString("XMLB");
                String wlbh = reset.getString("WLBH");
                String mc = reset.getString("MC");
                String ckdm = reset.getString("CKBM");
                String ckmc = reset.getString("CKMC");
                String xh = reset.getString("XH");
                String sx = reset.getString("SXID");
                int phbl = reset.getInt("PHBL");
                int kcsx = reset.getInt("KCSX");
                int kcxx = reset.getInt("KCXX");
                String aqkcdj = reset.getString("AQKCDJ");

                int kcsl = repositoryBiz.queryMaterialCanUseInCK(conn, xmlb, wlbh, ckdm, sx);
                if (kcxx > kcsl) {
                    int sl = kcxx - kcsl;
                    // 3、对缺货的记录插入到缺货申请流程中.
                    Hashtable<String, String> hashtable = new Hashtable<String, String>();
                    hashtable.put("SXDH", PrintUtil.parseNull(""));
                    hashtable.put("XMLB", PrintUtil.parseNull(xmlb));
                    hashtable.put("KHLX", PrintUtil.parseNull(""));
                    hashtable.put("DH", PrintUtil.parseNull(""));
                    hashtable.put("SL", String.valueOf(sl));
                    hashtable.put("SX", PrintUtil.parseNull(sx));
                    hashtable.put("JFCPHH", PrintUtil.parseNull(""));
                    hashtable.put("WLBH", PrintUtil.parseNull(wlbh));
                    hashtable.put("WLMC", PrintUtil.parseNull(mc));
                    hashtable.put("PN", PrintUtil.parseNull(xh));
                    hashtable.put("XH", PrintUtil.parseNull(xh));
                    hashtable.put("JFKFBM", PrintUtil.parseNull(ckdm));
                    hashtable.put("JFKFMC", PrintUtil.parseNull(ckmc));
                    hashtable.put("SXCPHH", PrintUtil.parseNull(""));
                    
                    hashtable.put("YXJ", "0");
                    hashtable.put("SQSJ", now);
                    hashtable.put("SFJSTH", no);
                    hashtable.put("ZT", zt);
                    hashtable.put("QHFS", QHSQCnt.bhlx3);
                    BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QHJL", hashtable, "admin");
                }
            }
            conn.commit();
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage("admin", e.getMessage());
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage("admin", "后台出现错误，请联系系统管理员!");
        } finally {
            DBSql.close(conn, stat, reset);
        }

    }
}
