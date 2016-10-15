package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;

public class StepNo5RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo5RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("根据检测结果判断路由节点。");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			int wgz = DAOUtil.getInt(conn, FJFJCnt.QUERY_WGZ, bindid);
			int gcjc = DAOUtil.getInt(conn, FJFJCnt.QUERY_GCJC, bindid);
			
			if(gcjc > 0){//工厂检测，跳转第6节点
				return 0;
			}else if(wgz > 0){//存在无故障产品，跳转第7节点
				return 7;
			}else{//流程结束
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
