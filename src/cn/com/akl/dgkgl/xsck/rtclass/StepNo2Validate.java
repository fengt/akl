package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Validate extends WorkFlowStepRTClassA{
	// ��ѯ���к�������������������ƥ������Ϻ�
	private static final String queryXLHNoEnoughWLBH = "SELECT a.XH FROM (SELECT XH, SUM(ISNULL(SFSL, 0)) as YCK FROM BO_BO_AKL_DGCK_S WHERE BINDID=? GROUP BY XH) a LEFT JOIN (SELECT XH, COUNT(*) as SCK FROM BO_AKL_CCB_CKD_XLH_S WHERE BINDID=? GROUP BY XH) b ON a.XH=b.XH INNER JOIN BO_AKL_WLXX c ON a.XH=c.XH WHERE c.SFXLH=? AND ISNULL(YCK,0)>ISNULL(SCK, 0)";
	// ��ѯ���к�������������������ƥ������Ϻ�
	private static final String queryXLHMOREEnoughWLBH = "SELECT a.XH FROM (SELECT XH, SUM(ISNULL(SFSL, 0)) as YCK FROM BO_BO_AKL_DGCK_S WHERE BINDID=? GROUP BY XH) a LEFT JOIN (SELECT XH, COUNT(*) as SCK FROM BO_AKL_CCB_CKD_XLH_S WHERE BINDID=? GROUP BY XH) b ON a.XH=b.XH INNER JOIN BO_AKL_WLXX c ON a.XH=c.XH WHERE c.SFXLH=? AND ISNULL(YCK,0)<ISNULL(SCK, 0)";
	// ��ѯ���к�������������������ƥ������Ϻ�
	private static final String queryXLHBIGEnoughWLBH = "SELECT a.XH FROM (SELECT XH, COUNT(*) as SCK FROM BO_AKL_CCB_CKD_XLH_S WHERE BINDID=? GROUP BY XH) a LEFT JOIN (SELECT XH, SUM(ISNULL(SFSL, 0)) as YCK FROM BO_BO_AKL_DGCK_S WHERE BINDID=? GROUP BY XH) b ON a.XH=b.XH INNER JOIN BO_AKL_WLXX c ON a.XH=c.XH WHERE c.SFXLH=? AND ISNULL(YCK,0)<>ISNULL(SCK, 0)";
	// ��ѯ���к�����
	private static final String queryXLHSL= "SELECT COUNT(*) FROM BO_AKL_CCB_CKD_XLH_S a WHERE BINDID=?";
	// ��ѯ��ȷ���к�����
	private static final String queryXLHZQSL="SELECT COUNT(*) FROM BO_AKL_CCB_CKD_XLH_S a, BO_AKL_CCB_RKD_XLH_S b WHERE a.XH=b.XH AND a.XLH=b.XLH AND b.ZT=? AND a.BINDID=?";
	// ��ѯ�������к���Ϣ
	private static final String queryCWXLHXX = "SELECT '�ͺ�:'+a.XH+',���к�:'+a.XLH FROM BO_AKL_CCB_CKD_XLH_S a LEFT JOIN BO_AKL_CCB_RKD_XLH_S b ON a.XLH=b.XLH AND a.XH=b.XH AND b.ZT=? WHERE a.bindid=? AND (b.XLH is null OR b.XH is NULL)";
	// ��ѯ�������к��Ƿ����ظ���Ϣ
	private static final String queryCWXLHCF = "SELECT COUNT(*) from BO_AKL_CCB_CKD_XLH_S WHERE XLH=? AND bindid=?";
	// ��ѯ���۵���
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	// ��ѯ�Ƿ����к�
	private static final String querySFXLH = "SELECT SFXLH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";

	public StepNo2Validate() {
		super();
	}

	public StepNo2Validate(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�����ڵ�У���¼�");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;
		// ��֤���к������Ƿ���ȷ
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�˻�");
		try {
			if(!th){
				conn = DAOUtil.openConnectionTransaction();
				String xsdh = DAOUtil.getString(conn, queryXSDH, bindid);
				String sfxlh = DAOUtil.getString(conn, querySFXLH, bindid);
				stat = conn.createStatement();
				//��ѯ���۵���
				String queryYFSL = "SELECT XSSL, WLBH, YCKSL, KHCGDH FROM BO_AKL_DGXS_S where DDID='"+xsdh+"'";
				rs = stat.executeQuery(queryYFSL);
				while(rs.next()){
					//��ѯ����ĺ������Ӧ������֮��
					String WLBH = rs.getString(2);
					String KHCGDH = rs.getString(4);
					String queryGHSL = "SELECT sum(YFSL) YFSL FROM BO_BO_AKL_DGCK_S where bindid="+bindid+"and WLBH='"+WLBH+"' AND KHCGDH='"+KHCGDH+"'";
					int YFSL = DBSql.getInt(queryGHSL, "YFSL");
					int SYXSSL = rs.getInt(1)-rs.getInt(3);
					if(YFSL!=SYXSSL){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͻ��ɹ�����Ϊ��"+KHCGDH+"���ϱ��Ϊ��"+WLBH+"�ĸĺ�ʵ�������ܼƣ�"+YFSL+"�������Ӧ�������ܼƣ�"+SYXSSL+"����");
						return false;
					}
				}
				if(sfxlh.equals("1")){
					String xh = DAOUtil.getStringOrNull(conn, queryXLHNoEnoughWLBH, bindid, bindid, XSDDConstant.SFXLH_YES);
					if(xh != null){
						// �������кŲ�������
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͺ�Ϊ"+xh+"�����кŲ���!");
						return false;
					}
					xh = DAOUtil.getStringOrNull(conn, queryXLHMOREEnoughWLBH, bindid, bindid, XSDDConstant.SFXLH_YES);
					if(xh != null){
						// �������кŹ�������
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͺ�Ϊ"+xh+"�����кŹ���!");
						return false;
					}
					xh = DAOUtil.getStringOrNull(conn, queryXLHBIGEnoughWLBH, bindid, bindid, XSDDConstant.SFXLH_YES);
					if(xh != null){
						// �������кŶ�������
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�ͺ�Ϊ"+xh+"�����кŶ���!");
						return false;
					}
					int xlhCount = DAOUtil.getInt(conn, queryXLHSL, bindid);
					int xlhZQCount = DAOUtil.getInt(conn, queryXLHZQSL, XSDDConstant.XLH_ZT_ZK, bindid);
					if(xlhCount != xlhZQCount){
						String cwxlhMessage = DAOUtil.getStringOrNull(conn, queryCWXLHXX, XSDDConstant.XLH_ZT_ZK, bindid);
						throw new RuntimeException(cwxlhMessage+"������ѳ�");
					}
					String queryXLH = "select XLH from BO_AKL_CCB_CKD_XLH_S where bindid = "+bindid;
					rs = stat.executeQuery(queryXLH);
					while(rs.next()){
						int XLH = DAOUtil.getInt(conn, queryCWXLHCF, rs.getString(1), bindid);
						if(XLH >1){
							MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���к�Ϊ"+rs.getString(1)+"�ڳ������кű��г���"+XLH+"��!�жϴ����Ϻ��ѳ���");
							return false;
						}
					}
					
				}
				conn.commit();
			}
			return true;
		} catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch(Exception e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
