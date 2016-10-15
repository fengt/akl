package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BiaoDanZZHSJCF extends WorkFlowStepRTClassA {

	// ��ѯ�������
	private static final String QUERY_HZBH = "SELECT HZBH FROM BO_AKL_DGXS_P WHERE XSDDID=?";
	// ��ѯ������д����۶���������
	private static final String queryAllWlxx="SELECT a.HWKYSL, a.WLBH, a.XSDH, a.XSSL, a.PCH, a.HWDM, b.GG, b.XH, b.DW, b.WLMC, a.KHCGDH  FROM BO_AKL_DGCKSK a left join BO_AKL_WLXX b on a.WLBH = b.WLBH WHERE a.XSDH=? AND b.HZBM=? AND b.WLZT in (0, 1, 4) order by a.KHCGDH, b.XH";
	// ��ѯ�ֿ�Ŀ���������Ϣ 	BO_AKL_DGKC_KCHZ_P BO_AKL_DGKC_KCMX_S
	private static final String queryKyWlxx="SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? and s.PCH=? and s.HWDM=? and a.HZBM=? AND s.SX in ('049088', '049090') AND a.WLZT in (0, 1, 4)";
	// ��ѯ���۵���
	private static final String queryXSDH = "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";
	// ��ѯ������Ϣ�����������
	//	private static final String queryTJZL = "SELECT ZL, TJ FROM BO_AKL_WLXX WHERE WLBH = ?";
	public StepNo1BiaoDanZZHSJCF() {
		super();
	}

	public StepNo1BiaoDanZZHSJCF(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("��װ�غ󣺲��뵥������");

		/**
		 * �������⣺��������̲�ֹ���δ�������һ���������̽��˿�����ߣ��ʹ����˳�ͻ��
		 */
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable ckdtData = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
		Hashtable ckData = BOInstanceAPI.getInstance().getBOData("BO_BO_AKL_DGCK_P", bindid);//���ݿ��ȡ��ͷ��Ϣ
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_BO_AKL_DGCK_S", bindid);
		//		Hashtable<String,String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		String CKDH = ckData.get("CKDH") == null?"":ckData.get("CKDH").toString();
		String XSDH = ckData.get("XSDH") == null?"":ckData.get("XSDH").toString();
		if(CKDH.equals("")&&v==null&&!XSDH.equals("")){
			Connection conn = null;
			try{
				conn = DAOUtil.openConnectionTransaction();
				final String xsddh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
				
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
				// δ����������
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid); 
				queryAllWlxx(conn, bindid, uid, xsddh); 
				CKDH = ckdtData.get("CKDH")==null?"":ckdtData.get("CKDH").toString();
				String ZDR = ckdtData.get("ZDR")==null?"":ckdtData.get("ZDR").toString();
				String DJRQ = ckdtData.get("DJRQ")==null?"":ckdtData.get("DJRQ").toString();
				CKDH = CKDH.substring(CKDH.lastIndexOf(">")+1);
				ZDR = ZDR.substring(ZDR.lastIndexOf(">")+1);
				DJRQ = DJRQ.substring(DJRQ.lastIndexOf(">")+1);
				String sql = "update BO_BO_AKL_DGCK_P Set CKDH=?, ZDR=?, DJRQ=? where bindid=?";
				DAOUtil.executeUpdate(conn, sql, CKDH, ZDR, DJRQ, bindid);

				conn.commit();

			} catch(RuntimeException e){
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			}catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��ֳ������⣬����ϵ����Ա��", true);
				return true;
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
	public void queryAllWlxx(Connection conn, int bindid, String uid, String xsddh) throws SQLException, AWSSDKException{
		String hzbh = DAOUtil.getStringOrNull(conn, QUERY_HZBH, xsddh);
		PreparedStatement ps = conn.prepareStatement(queryAllWlxx);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddh, hzbh);
		int rowNum = 1;
		try{
			while(reset.next()){
				// ��ѯ��λ������ʱ������
				int hwkysl = reset.getInt("HWKYSL");
				String wlbh = PrintUtil.parseNull(reset.getString("WLBH"));
				String khcgdh = PrintUtil.parseNull(reset.getString("KHCGDH"));
				String wlmc = PrintUtil.parseNull(reset.getString("WLMC"));
				String wlgg = PrintUtil.parseNull(reset.getString("GG"));
				String xh = PrintUtil.parseNull(reset.getString("XH"));
				String jldw = PrintUtil.parseNull(reset.getString("DW"));
				int xssl = reset.getInt("XSSL");
				String pch = PrintUtil.parseNull(reset.getString("PCH"));
				String hwdm = PrintUtil.parseNull(reset.getString("HWDM"));
				//��ѯ���۵���ժҪ
				String zysql = "SELECT zy from BO_AKL_DGXS_S where DDID='"+xsddh+"' and WLBH='"+wlbh+"'";
				String zy = PrintUtil.parseNull(DBSql.getString(conn,zysql, "zy").toString());
				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset =null;
				try{
					kywlxxPs = conn.prepareStatement(queryKyWlxx);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh, pch, hwdm, hzbh);
					while(kywlxxReset.next()){
						int kwsl = kywlxxReset.getInt("KWSL");
						int haveSl = hwkysl;
						String qdm = PrintUtil.parseNull(kywlxxReset.getString("QDM"));
						String ddm = PrintUtil.parseNull(kywlxxReset.getString("DDM"));
						String kwdm = PrintUtil.parseNull(kywlxxReset.getString("KWDM"));
						String ckdm = PrintUtil.parseNull(kywlxxReset.getString("CKDM"));
						String ckmc = PrintUtil.parseNull(kywlxxReset.getString("CKMC"));
						String sx = PrintUtil.parseNull(kywlxxReset.getString("SX"));
						int TJ = kywlxxReset.getInt("TJ");
						int ZL = kywlxxReset.getInt("ZL");
						if(haveSl >= xssl){
							// Ԥ��ת����ⵥ����
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("CKDM", ckdm);
							hashtable.put("KHCGDH", khcgdh);
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
							hashtable.put("KCSL", String.valueOf(kwsl));
							hashtable.put("SX", sx);
							hashtable.put("TJ", String.valueOf(TJ));
							hashtable.put("ZL", String.valueOf(ZL));
							hashtable.put("BZ", zy);

							hashtable.put("SFSL", String.valueOf(xssl));
							hashtable.put("YFSL", String.valueOf(xssl));
							hashtable.put("HWKYSL", String.valueOf(hwkysl));

							BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", hashtable, bindid, uid);
						}
						else
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
