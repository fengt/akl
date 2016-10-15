package cn.com.akl.shgl.qhsq.rtclass;

import java.sql.Connection;
import java.sql.SQLException;

import cn.com.akl.shgl.qhsq.cnt.QHSQCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;

public class StepNo1RuleJump extends WorkFlowStepJumpRuleRTClassA {

	public StepNo1RuleJump(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�����Ƿ�����Ʒ�����ȼ���ת�ڵ㡣");
	}

	@Override
	public int getNextNodeNo() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		try {
			conn = DBSql.open();
			String bhlx = DAOUtil.getStringOrNull(conn, QHSQCnt.QUERY_QHSQ_P_BHLX, bindid);//��������
			Integer highCount = DAOUtil.getIntOrNull(conn, QHSQCnt.QUERY_HIGH, bindid);//���ȼ��߼�¼
			Integer isCount = DAOUtil.getIntOrNull(conn, QHSQCnt.QUERY_SFZCP, bindid);//�Ƿ�����Ʒ
			if(QHSQCnt.bhlx0.equals(bhlx)){//������������
				if(highCount != null && highCount > 0)
					return 0;
				else
					return 9999;
			}else if(QHSQCnt.bhlx1.equals(bhlx)){//�������벹��
				if(isCount != null && isCount > 0)
					return 0;
				else
					return 9999;
			}else{//��ȫ��油��
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
