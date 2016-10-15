package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	// ��ѯ��������������Ϣ
	private static final String queryAllWlxx="SELECT b.WLBH,b.WLMC,b.GG,b.XH,b.JLDW,b.XSSL,b.KCSL,b.YCKSL,b.KHCGDH  FROM BO_AKL_DGXS_P a, BO_AKL_DGXS_S b WHERE a.BINDID=b.BINDID AND XSDDID=?";
	// ��ѯ�ֿ�Ŀ���������Ϣ 	BO_AKL_DGKC_KCHZ_P BO_AKL_DGKC_KCMX_S
	private static final String queryKyWlxx="SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.HWDM, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL, ISNULL(ISNULL(s.kwsl, 0)-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = s.WLBH and HWDM = s.HWDM AND PCH = s.PCH AND (XSDH<>? or ISNULL(KHCGDH, '')<>?)), ISNULL(s.kwsl, 0)) kysl FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? ORDER BY s.PCH, s.KWSL";
	// ��������
	private static final String queryKyWlxxQT="SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.HWDM, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL, s.KWSL-(select ISNULL(SUM(ISNULL(XSSL,0)), 0) from BO_AKL_DGXS_S where DDID=? AND WLBH = a.WLBH AND ISNULL(KHCGDH, '')<>?) kysl FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? ORDER BY s.PCH, s.KWSL";
	// ��ѯ���۵���
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	// ��ѯ������Ϣ�����������
//	private static final String queryTJZL = "SELECT ZL, TJ FROM BO_AKL_WLXX WHERE WLBH = ?";
	// ��ѯ���ⵥ�Ƿ��ѳ�
	private static final String queryXSDDCKSL = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a join BO_AKL_DGXS_S b on a.BINDID=b.BINDID WHERE a.XSDDID=? GROUP BY a.XSDDID HAVING SUM(ISNULL(YCKSL, 0))<>SUM(ISNULL(XSSL, 0))";
	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("������ǰ�¼�����ֿ��");
		
		/**
		 * �������⣺��������̲�ֹ���δ�������һ���������̽��˿�����ߣ��ʹ����˳�ͻ��
		 */
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String,String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		if("BO_BO_AKL_DGCK_P".equals(tablename)){
			Connection conn = null;
			try{
				conn = DAOUtil.openConnectionTransaction();
				String xsddh = hashtable.get("XSDH");
				String cklx = hashtable.get("CKLX");
				
				String YC = DAOUtil.getStringOrNull(conn, queryXSDDCKSL, xsddh);
				if(YC==null||"0".equals(YC)){
					MessageQueue.getInstance().putMessage(getUserContext().getUID(),"�����۶����ѳ��⣡", true);
					return false;
				}
				
				// ��¼������۶���Ϊ��ֵʱ�������ӱ��ֵ����ϼ�¼
				if(xsddh == null || xsddh.trim().length()==0) {
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid); 
					conn.commit();
					return true;
				}
				
				// �жϴ����۶����Ƿ��Ѳ��
				String xsddh2 = null;
				xsddh2 = DAOUtil.getStringOrNull(conn, queryXSDH, bindid);
				
				// �Ѳ���򷵻أ���������һ�β��
				if(!xsddh.equals(xsddh2)){
					// δ����������
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid); 
					queryAllWlxx(conn, bindid, uid, xsddh, cklx); 
				}
				
				conn.commit();
				return true;
			} catch(RuntimeException e){
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return true;
			}catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		
		return true;
	}
	
	
	/**
	 * ��ѯ������������ϣ����ҴӲֿ�ץȡ����
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException 
	 */
	public void queryAllWlxx(Connection conn, int bindid, String uid, String xsddh, String cklx) throws SQLException, AWSSDKException{
		PreparedStatement ps = conn.prepareStatement(queryAllWlxx);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddh);
		int rowNum = 1;
		try{
			while(reset.next()){
				// ��ѯ��λ������ʱ������
				String wlbh = PrintUtil.parseNull(reset.getString("WLBH"));
				String wlmc = PrintUtil.parseNull(reset.getString("WLMC"));
				String wlgg = PrintUtil.parseNull(reset.getString("GG"));
				String xh = PrintUtil.parseNull(reset.getString("XH"));
				String jldw = PrintUtil.parseNull(reset.getString("JLDW"));
				String khcgdh = PrintUtil.parseNull(reset.getString("KHCGDH"));
//				String kcsl = reset.getString("KCSL");
				
				int xssl = reset.getInt("XSSL");
				int ycksl = reset.getInt("YCKSL");
//				int TJ = 0;
//				int ZL = 0;
				if(xssl == ycksl)
					continue;
				else
					xssl -= ycksl;
				
				int sl = xssl;
//				PreparedStatement JLTJP = null;
//				ResultSet JLTJR =null;
//				try{
//					JLTJP = conn.prepareStatement(queryTJZL);
//					JLTJR = DAOUtil.executeFillArgsAndQuery(conn, JLTJP, wlbh);
//					TJ = JLTJR.getInt("TJ");
//					ZL = JLTJR.getInt("ZL");
//				}finally {
//					DBSql.close(JLTJP, JLTJR);
//				}
				
				
				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset =null;
				try{
					if(!cklx.equals("054143")&&!cklx.equals("054144")){
						kywlxxPs = conn.prepareStatement(queryKyWlxx);
						kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, xsddh, khcgdh, wlbh);
					}
					else{
						kywlxxPs = conn.prepareStatement(queryKyWlxxQT);
						kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, xsddh, khcgdh,wlbh);
					}
					// �ֻ�������� falseΪ�ѽ���
					boolean overFlag = true;
					while(overFlag&&kywlxxReset.next()){
						int kysl = kywlxxReset.getInt("kysl");
						int haveSl = kysl;
						int kwsl = kywlxxReset.getInt("KWSL");
						String hwdm = PrintUtil.parseNull(kywlxxReset.getString("HWDM"));
						String qdm = PrintUtil.parseNull(kywlxxReset.getString("QDM"));
						String ddm = PrintUtil.parseNull(kywlxxReset.getString("DDM"));
						String kwdm = PrintUtil.parseNull(kywlxxReset.getString("KWDM"));
						String pch = PrintUtil.parseNull(kywlxxReset.getString("PCH"));
						String ckdm = PrintUtil.parseNull(kywlxxReset.getString("CKDM"));
						String ckmc = PrintUtil.parseNull(kywlxxReset.getString("CKMC"));
						String sx = PrintUtil.parseNull(kywlxxReset.getString("SX"));
						int TJ = kywlxxReset.getInt("TJ");
						int ZL = kywlxxReset.getInt("ZL");
						if(haveSl > 0){
							sl -= haveSl;
							
							// Ԥ��ת����ⵥ����
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("KHCGDH", khcgdh);
							hashtable.put("CKDM", ckdm);
							hashtable.put("CKMC", ckmc);
							hashtable.put("QDM", qdm);
							hashtable.put("DDM", ddm);
							hashtable.put("KWDM", kwdm);
							hashtable.put("HWDM", hwdm);
							hashtable.put("DDH", xsddh);
							hashtable.put("PCH", pch);
							hashtable.put("WLBH", wlbh);
							hashtable.put("XH", xh);
							hashtable.put("GG", wlgg);
							hashtable.put("WLMC", wlmc);
							hashtable.put("DW", jldw);
							hashtable.put("HWKYSL", String.valueOf(kysl));
							hashtable.put("KCSL", String.valueOf(kwsl));
							hashtable.put("SX", sx);
							hashtable.put("TJ", String.valueOf(TJ));
							hashtable.put("ZL", String.valueOf(ZL));
							if(sl<=0){
								hashtable.put("SFSL", String.valueOf(haveSl+sl));
								hashtable.put("YFSL", String.valueOf(haveSl+sl));
								// ��������
								BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", hashtable, bindid, uid);
								// �ֻ�����
								overFlag = false;
							} else {
								hashtable.put("SFSL", String.valueOf(haveSl));
								hashtable.put("YFSL", String.valueOf(haveSl));
								// ��������
								BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", hashtable, bindid, uid);
							}
						}
					}
					if(overFlag==true&&sl>0){
						throw new RuntimeException("���۶�����"+xsddh+"�����ϱ��Ϊ"+wlbh+"�ͺ�Ϊ"+xh+"������λΪ"+jldw+"�����Ͽ����������㡣");
					}
				} finally {
					DBSql.close(kywlxxPs, kywlxxReset);
				}
			}
		}finally {
			DBSql.close(ps, reset);
		}
	}
}
