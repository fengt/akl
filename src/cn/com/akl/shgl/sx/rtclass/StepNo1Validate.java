package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("送修产品信息校验。");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		
		Connection conn = null;
		try {
			conn = DBSql.open();
			
			int sxCount = DAOUtil.getInt(conn, SXCnt.QUERY_isSXExist, bindid);
			String xmkf = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMKF, bindid));
			if("".equals(xmkf)){
				MessageQueue.getInstance().putMessage(uid, "【客服中心】的值为空，请联系管理员！");
				return false;
			}
			if(sxCount == 0){
				MessageQueue.getInstance().putMessage(uid, "送修子表信息不能为空！");
				return false;
			}
			
			String sfdyp = DAOUtil.getString(conn, SXCnt.QUERY_isDYP, bindid);
			int count = DAOUtil.getInt(conn, SXCnt.QUERY_DYP, bindid);
			if(sfdyp.equals(SXCnt.is) && count == 0){
				MessageQueue.getInstance().putMessage(uid, "代用品子表信息不能为空！");
				return false;
			}else if(sfdyp.equals(SXCnt.no) && count != 0){
				MessageQueue.getInstance().putMessage(uid, "代用品子表已录入信息，请删除后办理！");
				return false;
			}
			
			return true;
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

}
