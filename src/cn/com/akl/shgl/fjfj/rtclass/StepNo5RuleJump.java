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
		setDescription("���ݼ�����ж�·�ɽڵ㡣");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			int wgz = DAOUtil.getInt(conn, FJFJCnt.QUERY_WGZ, bindid);
			int gcjc = DAOUtil.getInt(conn, FJFJCnt.QUERY_GCJC, bindid);
			
			if(gcjc > 0){//������⣬��ת��6�ڵ�
				return 0;
			}else if(wgz > 0){//�����޹��ϲ�Ʒ����ת��7�ڵ�
				return 7;
			}else{//���̽���
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
