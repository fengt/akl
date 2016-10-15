package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class TimeoutBiz {

	
	/**
	 * 启动送修单超时流程
	 * @param bindid
	 * @param uid
	 * @param hashtable
	 * @throws Exception
	 */
	public static void startSXTimeout(int bindid, String uid, Hashtable<String, String> hashtable) throws Exception {
        // 预备启动子流程.
        Connection conn = null;
        try {
            conn = DBSql.open();

            // 查询是否有相同送修单的子流程已经启动过了,启动过了就不启动了.
            Integer sxdhCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SXDCS_P WHERE SXDH=?", hashtable.get("SXDH"));
            if (sxdhCount != null && sxdhCount > 0) {
                return;
            }

            // 启动子流程
            int ckBindid = 0;
            try {
                /** 拼接标题. */
                StringBuilder titleSb = new StringBuilder("送修单超时-送修单号：" + hashtable.get("SXDH"));

                /** 启动流程. */
                ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("752abc81a42c44815d92602ff07b05ab", uid, titleSb.toString());
                int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
                int[] processTaskInstance = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, uid, titleSb.toString());

                /** 插入数据. */
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SXDCS_P", (Hashtable<String, String>) hashtable.clone(), ckBindid, uid);

                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SX_P SET SXDCSBINDID=?,SXDCSTASKID=? WHERE BINDID=?", ckBindid, processTaskInstance[0], bindid);
            } catch (RuntimeException e) {
                if (ckBindid != 0) {
                    WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                }
                throw e;
            } catch (Exception e) {
                // 如果启动失败，则创建出来的流程。
                try {
                    if (ckBindid != 0) {
                        WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                throw e;
            }
        } finally {
            DBSql.close(conn, null, null);
        }
    }
	
	/**
	 * 关闭送修超时流程
	 * @param bindid
	 * @param uid
	 */
	public static void closeTask(int bindid, String uid) {
        Hashtable<String, String> hashtable = BOInstanceAPI.getInstance().getBOData("BO_AKL_SX_P", bindid);
        String sxdcsBindid = hashtable.get("SXDCSBINDID");
        String sxdcsTaskid = hashtable.get("SXDCSTASKID");
        try {
            WorkflowInstanceAPI.getInstance().closeProcessInstance(uid, Integer.parseInt(sxdcsBindid), Integer.parseInt(sxdcsTaskid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
