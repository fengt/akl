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
        setDescription("产品升级全为否时，确认库存及序列号明细。");
    }

    @Override
    public boolean execute() {
        int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = uc.getUID();

        try {
            conn = DAOUtil.openConnectionTransaction();

            Integer isEnd = DAOUtil.getIntOrNull(conn, "SELECT ISEND FROM BO_AKL_SX_P WHERE BINDID=?", bindid);
            if (isEnd != null && isEnd == 1) {
                throw new RuntimeException("流程已结束!");
            }

            String ckbm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKDM, bindid));//客服仓库编码
            String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//项目类别
            String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_YWLX, bindid));//业务类型
            int n = DAOUtil.getInt(conn, SXCnt.QUERY_isSFSJ, bindid);//送修是否有升级记录
            String sfdyp = DAOUtil.getString(conn, SXCnt.QUERY_isDYP, bindid);//是否有代用品

            if (n > 0) {
                /**
                 * 暂无业务操作
                 */
            } else {//流程结束
                /**
                 * 1、更新或插入库存和故障明细
                 */
                sxHandle.setSXStatus(conn, bindid);//更新送修单状态
                sxBiz.insertALL(conn, bindid, uid, ckbm, xmlb, ywlx);

                /**
                 * 2、更新代用品库存
                 */
                if (SXCnt.is.equals(sfdyp)) {
                    sxBiz.decreaseDYP(conn, bindid);//a、减代用品库存
                    sxBiz.increaseDYP(conn, bindid);//b、加代用品库存
                    DAOUtil.executeUpdate(conn, SXCnt.UPDATE_DYP_SFYKKC, bindid);//c、更新代用品'是否已扣库存'
                }

                DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SX_P SET ISEND=1 WHERE BINDID=?", bindid);

                conn.commit();
                TimeoutBiz.closeTask(bindid, uid);//关闭送修单超时流程
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
            MessageQueue.getInstance().putMessage(uid, "后台出现异常，请检查控制台", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

}



