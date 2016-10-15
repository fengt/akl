package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;

import cn.com.akl.shgl.sx.biz.SXBiz;
import cn.com.akl.shgl.sx.biz.SXHandle;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo7Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	private SXBiz sxBiz = new SXBiz();
	private SXHandle sxHandle = new SXHandle();
	public StepNo7Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo7Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("更新特批号。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			sxHandle.setTPH(conn, bindid);
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
}
