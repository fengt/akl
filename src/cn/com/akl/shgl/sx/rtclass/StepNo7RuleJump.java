package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;

public class StepNo7RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo7RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���ݲ�Ʒ���������ת�ڵ㡣");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isSFZCSJ, bindid);
			if(n > 0){//�ٴ�����������ʱ����ת��8�ڵ�
				return 0;
			}else{
				return 9;
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
