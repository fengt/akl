package cn.com.akl.dgkgl.qssl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo1Validate extends WorkFlowStepRTClassA{
	//��ѯǩ��¼����ǩ�յ�����������������
	private static final String query_SLBF = "SELECT COUNT (*) SL, b.CKDH FROM ( SELECT COUNT (*) SL, CKDH FROM BO_AKL_QSD_P p, BO_AKL_QSD_S s WHERE p.BINDID = s.BINDID GROUP BY CKDH ) a, ( SELECT COUNT (*) SL, CKDH FROM BO_AKL_DGCK_QSSL_S WHERE bindid = ? GROUP BY CKDH ) b WHERE a.CKDH = b.CKDH AND a.SL <> b.SL GROUP BY b.CKDH";
	public StepNo1Validate(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("��֤¼��ǩ�յ�����������ⵥǩ�յ��������Ƿ����");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		conn = DBSql.open();
		
		try {
			ps = conn.prepareStatement(query_SLBF);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				int SL = rs.getInt(1);
				String CKDH = rs.getString(2);
				if(SL==1){
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���ⵥ�ţ�"+CKDH+" ��ǩ��¼�뵥���¼������ǩ�յ������¼��������������");
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "ϵͳ��������,����ϵ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, ps, rs);
		}
		return true;
	}

}
