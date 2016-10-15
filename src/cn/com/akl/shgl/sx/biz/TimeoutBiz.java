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
	 * �������޵���ʱ����
	 * @param bindid
	 * @param uid
	 * @param hashtable
	 * @throws Exception
	 */
	public static void startSXTimeout(int bindid, String uid, Hashtable<String, String> hashtable) throws Exception {
        // Ԥ������������.
        Connection conn = null;
        try {
            conn = DBSql.open();

            // ��ѯ�Ƿ�����ͬ���޵����������Ѿ���������,�������˾Ͳ�������.
            Integer sxdhCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_SXDCS_P WHERE SXDH=?", hashtable.get("SXDH"));
            if (sxdhCount != null && sxdhCount > 0) {
                return;
            }

            // ����������
            int ckBindid = 0;
            try {
                /** ƴ�ӱ���. */
                StringBuilder titleSb = new StringBuilder("���޵���ʱ-���޵��ţ�" + hashtable.get("SXDH"));

                /** ��������. */
                ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("752abc81a42c44815d92602ff07b05ab", uid, titleSb.toString());
                int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
                int[] processTaskInstance = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, uid, titleSb.toString());

                /** ��������. */
                BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SXDCS_P", (Hashtable<String, String>) hashtable.clone(), ckBindid, uid);

                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SX_P SET SXDCSBINDID=?,SXDCSTASKID=? WHERE BINDID=?", ckBindid, processTaskInstance[0], bindid);
            } catch (RuntimeException e) {
                if (ckBindid != 0) {
                    WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
                }
                throw e;
            } catch (Exception e) {
                // �������ʧ�ܣ��򴴽����������̡�
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
	 * �ر����޳�ʱ����
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
