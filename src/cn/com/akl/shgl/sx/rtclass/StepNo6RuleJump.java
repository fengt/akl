package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo6RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo6RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("根据产品是否升级跳转节点。");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isSFSJ, bindid);
			if(n > 0){//存在升级记录，跳转第5节点
				return 0;
			}else{
				return 9999;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	@Override
	public String getNextTaskUser() {
		return null;
	}

}
