package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4TransactionAndTH extends WorkFlowStepRTClassA{
	//��ѯ�����е�������Ϣ
	private static final String queryWLXX = "SELECT WLBH,sum(SFSL) sfsl,DDH,CKDM,DW,HWDM,KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID=? group by DDH,CKDM,DW,HWDM,KHCGDH,WLBH";
	//�������۶��������е��ѳ�������
	private static final String updateYCKSL = "UPDATE BO_AKL_DGXS_S SET YCKSL=ISNULL(YCKSL,0)-? WHERE WLBH=? AND DDID=? AND ISNULL(YCKSL,0)>=? AND ISNULL(KHCGDH, '')=?";
	// ��ѯ���۶����Ƿ�δ����
	private static final String queryXSDDCKSL = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a join BO_AKL_DGXS_S b on a.BINDID=b.BINDID WHERE a.XSDDID=? GROUP BY a.XSDDID HAVING SUM(ISNULL(YCKSL, 0))>0";
	//����������е���������   
	private static final String updateXSSL = "UPDATE BO_AKL_DGCKSK SET XSSL=ISNULL(XSSL,0)+? WHERE WLBH=? AND XSDH=? AND PCH=? AND HWDM=? AND ISNULL(KHCGDH, '')=?";
	//��ѯ�ѳ�������
	private static final String queryCKSL = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a join BO_AKL_DGXS_S b on a.BINDID=b.BINDID WHERE a.XSDDID=? GROUP BY a.XSDDID HAVING SUM(ISNULL(YCKSL, 0))=SUM(ISNULL(XSSL, 0))";
	//��ѯ�����۶����ĸ���
	private static final String queryGS = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a join BO_AKL_DGXS_S b on a.BINDID=b.BINDID WHERE a.XSDDID=? GROUP BY a.XSDDID";
	// ����������к�״̬
	private static final String UPDATE_RKXLH = "UPDATE BO_AKL_CCB_RKD_XLH_S SET ZT='"+XSDDConstant.XLH_ZT_ZK+"' WHERE XLH=?";
	// ��ѯ�������к�
	private static final String QUERY_CKXLH = "SELECT XLH FROM BO_AKL_CCB_CKD_XLH_S WHERE BINDID=?";
	public StepNo4TransactionAndTH(UserContext arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		setVersion("1.0.0");
		setDescription("�Ľڵ������˲˵������¿��");
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "�˻�");
		if(th){
			Connection conn = null;
			Statement stat = null;
			ResultSet rs = null;
			String sql = null;
			int a = 0;
			int b = 0;
			try {
				conn = DAOUtil.openConnectionTransaction();
				stat = conn.createStatement();
				String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
				if(sfyy.equals("��")){
					final String xsdh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
					DAOUtil.executeQueryForParser(conn, "SELECT HWDM, sum(SFSL) sfsl, WLBH, PCH, SX, KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID=? group by WLBH, PCH, SX, KHCGDH, HWDM", new DAOUtil.ResultPaser() {
						public boolean parse(Connection conn, ResultSet reset) throws SQLException {
							if(0==DAOUtil.executeUpdate(conn, "update BO_AKL_DGKC_KCMX_S set KWSL=ISNULL(KWSL, 0)+? where HWDM=? AND WLBH=? AND PCH=? AND SX=?", 
									reset.getInt("SFSL"), reset.getString("HWDM"), reset.getString("WLBH"), reset.getString("PCH"), reset.getString("SX"))){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���κţ�"+reset.getString("PCH")+"���Ϻţ�"+reset.getString("WLBH")+"��λ���룺"+reset.getString("HWDM")+"��������ϸ���ʧ��", true);
								throw new RuntimeException("�ͻ��ɹ�����"+reset.getString("KHCGDH")+"���κţ�"+reset.getString("PCH")+"���Ϻţ�"+reset.getString("WLBH")+"��λ���룺"+reset.getString("HWDM")+"��������ϸ���ʧ��");
							}
							if(0==DAOUtil.executeUpdate(conn, "update BO_AKL_DGKC_KCHZ_P set CKSL=ISNULL(CKSL, 0)-?, PCSL=ISNULL(PCSL, 0)+? where WLBH=? AND PCH=? AND CKSL>=? AND ZT='042022'", 
									reset.getInt("SFSL"), reset.getInt("SFSL"), reset.getString("WLBH"), reset.getString("PCH"),  reset.getString("SFSL"))){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���κţ�"+reset.getString("PCH")+"���Ϻţ�"+reset.getString("WLBH")+"�����»��ܿ��ʧ��", true);
								throw new RuntimeException("�ͻ��ɹ�����"+reset.getString("KHCGDH")+"���κţ�"+reset.getString("PCH")+"���Ϻţ�"+reset.getString("WLBH")+"�����»��ܿ��ʧ��");
							}
							return true;
						}
					}, bindid);
					String me = DAOUtil.getStringOrNull(conn, queryCKSL, xsdh);
					String my = DAOUtil.getStringOrNull(conn, queryGS, xsdh);
					if((me!=null&&!"0".equals(me))){
						if(me.equals(my))
							b=1;
					}

					//2���������еĵ���������Ϣ
					DAOUtil.executeQueryForParser(conn, queryWLXX, new DAOUtil.ResultPaser() {
						@Override
						public boolean parse(Connection conn, ResultSet reset) throws SQLException {
							//2�����¶�Ӧ�����۶����е���������
							int count = DAOUtil.executeUpdate(conn, updateYCKSL, reset.getInt("SFSL"), reset.getString("WLBH"), xsdh, reset.getInt("SFSL"), reset.getString("KHCGDH")==null?"":reset.getString("KHCGDH"));
							if(count != 1){
								if(count == 0)
									throw new RuntimeException("�ͻ��ɹ�����:"+reset.getString("KHCGDH")+"���ϱ�ţ�"+reset.getString("WLBH")+"��λ���룺"+reset.getString("HWDM")+"���ܿ����۶����ѳ�����������ʧ��");
								if(count >1)
									throw new RuntimeException("���ܿ��һ�ڵ����۳��⣬��⵽��ǰ������Ϊ��" + xsdh +"�ͻ��ɹ�����:"+reset.getString("KHCGDH")+"���ж�����ͬ�����ϱ��Ϊ��"+reset.getString("WLBH")+"����λΪ"+reset.getString("DW")+"����Ϣ");
							}
							return true;
						}
					}, bindid);

					String message = DAOUtil.getStringOrNull(conn, queryXSDDCKSL, xsdh);
					if(message==null||"0".equals(message)){
						DAOUtil.executeUpdate(conn, "Update BO_AKL_DGXS_P Set ZT=? WHERE XSDDID=?", "δ����", xsdh);
						if(b==1)
							DAOUtil.executeUpdate(conn, "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?", "δ����", bindid);
						else
							DAOUtil.executeUpdate(conn, "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?", "���ֳ���", bindid);
					}	else {
						DAOUtil.executeUpdate(conn, "Update BO_AKL_DGXS_P Set ZT=? WHERE XSDDID=?", "���ֳ���", xsdh);
						if(b==1)
							DAOUtil.executeUpdate(conn, "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?", "δ����", bindid);
						else
							DAOUtil.executeUpdate(conn, "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?", "���ֳ���", bindid);				
					}
					//�ж��������������ݻ��Ǹ�������
					sql = "select count(*) sl from BO_AKL_DGCKSK where XSDH = '"+xsdh+"'";
					a = DAOUtil.getIntOrNull(conn, sql);
					//��������в������ݻ��������
					sql = "select WLBH, PCH, HWDM, SFSL, HWKYSL, KHCGDH from BO_BO_AKL_DGCK_S where bindid ="+bindid;
					rs = stat.executeQuery(sql);
					while(rs.next()){
						String wlbh = rs.getString(1)==null?"":rs.getString(1);
						String pch = rs.getString(2)==null?"":rs.getString(2);
						String hwdm = rs.getString(3)==null?"":rs.getString(3);
						String khcgdh = rs.getString(6)==null?"":rs.getString(6);
						if(a>0){
							//3�����¶�Ӧ����������е���������
							int count = DAOUtil.executeUpdate(conn, updateXSSL, rs.getInt(4), wlbh, xsdh, pch, hwdm, khcgdh);
							if(count != 1){
								if(count == 0)
									throw new RuntimeException("���ܿ������������������ʧ��");
								if(count >1)
									throw new RuntimeException("���ܿ�������У���⵽��ǰ������Ϊ��" + xsdh +"���ж�����ͬ�����ϱ��Ϊ��"+wlbh+"����Ϣ");
							}
						}
						else{
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("XSDH", xsdh);
							hashtable.put("WLBH", wlbh);
							hashtable.put("PCH", pch);
							hashtable.put("HWDM", hwdm);
							hashtable.put("KHCGDH", khcgdh);
							hashtable.put("XSSL", String.valueOf(rs.getInt(4)));
							hashtable.put("HWKYSL", String.valueOf(rs.getInt(5)));
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCKSK", hashtable, bindid, getUserContext().getUID());
						}
					}
					// ����������б�״̬
					ArrayList<String> collection = DAOUtil.getStringCollection(conn, QUERY_CKXLH, bindid);
					for(String XLH : collection){
						DAOUtil.executeUpdate(conn, UPDATE_RKXLH, XLH);
					}
					conn.commit();
				}
			} catch(RuntimeException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (SQLException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (AWSSDKException e) {
				// TODO Auto-generated catch block
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			}finally {
				DBSql.close(conn, stat, rs);
			}
		}
		return true;
	}

}
