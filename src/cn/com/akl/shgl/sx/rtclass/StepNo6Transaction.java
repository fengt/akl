package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;

import cn.com.akl.shgl.sx.biz.SXBiz;
import cn.com.akl.shgl.sx.biz.SXHandle;
import cn.com.akl.shgl.sx.biz.TimeoutBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo6Transaction extends WorkFlowStepRTClassA {


    private Connection conn = null;
    private UserContext uc;
    private SXBiz sxBiz = new SXBiz();
    private SXHandle sxHandle = new SXHandle();

    public StepNo6Transaction() {
        // TODO Auto-generated constructor stub
    }

    public StepNo6Transaction(UserContext arg0) {
        super(arg0);
        this.uc = arg0;
        setProvider("fengtao");
        setDescription("��Ʒ����ȫΪ��ʱ��ȷ�Ͽ�漰���к���ϸ��");
    }

    @Override
    public boolean execute() {
        int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = uc.getUID();

        try {
            conn = DAOUtil.openConnectionTransaction();

            Integer isEnd = DAOUtil.getIntOrNull(conn, "SELECT ISEND FROM BO_AKL_SX_P WHERE BINDID=?", bindid);
            if (isEnd != null && isEnd == 1) {
                throw new RuntimeException("�����ѽ���!");
            }

            String ckbm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKDM, bindid));//�ͷ��ֿ����
            String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//��Ŀ���
            String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_YWLX, bindid));//ҵ������
            int n = DAOUtil.getInt(conn, SXCnt.QUERY_isSFSJ, bindid);//�����Ƿ���������¼
            String sfdyp = DAOUtil.getString(conn, SXCnt.QUERY_isDYP, bindid);//�Ƿ��д���Ʒ

            if (n > 0) {
                /**
                 * ����ҵ�����
                 */
            } else {//���̽���
                /**
                 * 1�����»������͹�����ϸ
                 */
                sxHandle.setSXStatus(conn, bindid);//�������޵�״̬
                sxBiz.insertALL(conn, bindid, uid, ckbm, xmlb, ywlx);

                /**
                 * 2�����´���Ʒ���
                 */
                if (SXCnt.is.equals(sfdyp)) {
                    sxBiz.decreaseDYP(conn, bindid);//a��������Ʒ���
                    sxBiz.increaseDYP(conn, bindid);//b���Ӵ���Ʒ���
                    DAOUtil.executeUpdate(conn, SXCnt.UPDATE_DYP_SFYKKC, bindid);//c�����´���Ʒ'�Ƿ��ѿۿ��'
                }

                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SX_P SET ISEND=1 WHERE BINDID=?", bindid);

                conn.commit();
                TimeoutBiz.closeTask(bindid, uid);//�ر����޵���ʱ����
            }

            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨�����쳣���������̨", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}



