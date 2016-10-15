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
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo9Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	private SXBiz sxBiz = new SXBiz();
	private SXHandle sxHandle = new SXHandle();
	
	public StepNo9Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo9Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("ȷ�Ͽ�漰���к���ϸ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
	    int taskid = getParameter("PARAMETER_TASK_ID").toInt();
	    boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ȷ�ϴ���");
	    String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			String ckbm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKDM, bindid));//�ͷ��ֿ����
			String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//��Ŀ���
			String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_YWLX, bindid));//ҵ������
			String sfdyp = DAOUtil.getString(conn, SXCnt.QUERY_isDYP, bindid);//�Ƿ��д���Ʒ
			
			if(yes){
				/**
				 * 1��������͹�����ϸ
				 */
				sxHandle.setSXStatus(conn, bindid);//�������޵�״̬
				sxBiz.insertALL(conn, bindid, uid, ckbm, xmlb, ywlx);
				
				/**
				 * 2�����´���Ʒ���
				 */
				if(SXCnt.is.equals(sfdyp)){
					sxBiz.decreaseDYP(conn, bindid);//a��������Ʒ���
					sxBiz.increaseDYP(conn, bindid);//b���Ӵ���Ʒ���
					DAOUtil.executeUpdate(conn, SXCnt.UPDATE_DYP_SFYKKC, bindid);//c�����´���Ʒ'�Ƿ��ѿۿ��'
				}
				conn.commit();
				TimeoutBiz.closeTask(bindid, uid);//�ر����޵���ʱ����
			}
			
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
}
