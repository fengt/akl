package cn.com.akl.shgl.zsjgl.kcwh.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Transaction extends WorkFlowStepRTClassA {
	
	private static final String QUERY_KCHZ = "SELECT * FROM BO_AKL_SHKC_P_TEMP WHERE BINDID=?";
	private static final String QUERY_KCMX = "SELECT * FROM BO_AKL_SHKC_S_TEMP WHERE BINDID=?";
	private static final String UPDATE_KCMX_KWSL = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE XMLB=? AND WLBH=? AND HWDM=? AND PCH=? AND SX=? AND ZT=?";//���¿�棨������
	
	private UserContext uc;
	private Connection conn;
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("�������¿����ϸ�ͻ��ܡ�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			insertInventory(conn, bindid, uid);
			
			conn.commit();
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
        return true;
	}
	
	/**
	 * ����������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertInventory(Connection conn, final int bindid, final String uid) throws SQLException{
		
		//1�����������
		DAOUtil.executeQueryForParser(conn, QUERY_KCHZ, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				insertKCHZ(conn, bindid, uid, rs);
				return true;
			}
		}, bindid);
		
		//2����������ϸ
		DAOUtil.executeQueryForParser(conn, QUERY_KCMX, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> rec = new Hashtable<String, String>();
				String xmlb = StrUtil.returnStr(rs.getString("XMLB"));
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
				String gg = StrUtil.returnStr(rs.getString("GG"));
				String xh = StrUtil.returnStr(rs.getString("XH"));
				String pch = StrUtil.returnStr(rs.getString("PCH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
				String ckmc = StrUtil.returnStr(rs.getString("CKMC"));
				String qdm = StrUtil.returnStr(rs.getString("QDM"));
				String ddm = StrUtil.returnStr(rs.getString("DDM"));
				String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));
				int kwsl = rs.getInt("KWSL");
				String zjm = StrUtil.returnStr(rs.getString("ZJM"));
				double dj = rs.getDouble("DJ");
				String bzq = StrUtil.returnStr(rs.getString("BZQ"));
				String fzsx = StrUtil.returnStr(rs.getString("FZSX"));
				String scrq = StrUtil.returnStr(rs.getString("SCRQ"));
				String jldw = StrUtil.returnStr(rs.getString("JLDW"));
				String zt = StrUtil.returnStr(rs.getString("ZT"));
				
				rec.put("XMLB", xmlb);//��Ŀ���
				rec.put("WLBH", wlbh);//���ϱ��
				rec.put("WLMC", wlmc);//��������
				rec.put("GG", gg);
				rec.put("ZJM", zjm);
				rec.put("BZQ", bzq);
				rec.put("FZSX", fzsx);
				rec.put("SCRQ", scrq);
				rec.put("JLDW", jldw);
				rec.put("XH", xh);//�ͺ�
				rec.put("SX", sx);//����
				rec.put("PCH", pch);//���κ�
				rec.put("ZT", zt);//״̬
				rec.put("KWSL", String.valueOf(kwsl));//��λ����
				rec.put("DJ", String.valueOf(dj));//����
				
				rec.put("CKDM", ckdm);//�ͷ��ֿ����
				rec.put("CKMC", ckmc);//�ͷ��ֿ�����
				rec.put("QDM", qdm);//������
				rec.put("DDM", ddm);//������
				rec.put("KWDM", kwdm);//��λ����
				rec.put("HWDM", hwdm);//��λ����
				
				try {
					/**1���������¿����ϸ*/
					int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCMX, xmlb, wlbh, sx, hwdm, pch, zt);
					if(n == 0){
						BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", rec, bindid, uid);
					}else{
						int updateCount = DAOUtil.executeUpdate(conn, UPDATE_KCMX_KWSL, kwsl, xmlb, wlbh, hwdm, pch, sx, zt);
						if(updateCount != 1) throw new RuntimeException("�����ϸ����ʧ�ܣ�");
					}
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
		
	}
	
	public void insertKCHZ(Connection conn, int bindid, String uid, ResultSet rs) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String xmlb = StrUtil.returnStr(rs.getString("XMLB"));
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String gg = StrUtil.returnStr(rs.getString("GG"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		String pch = StrUtil.returnStr(rs.getString("PCH"));
		int rksl = rs.getInt("RKSL");
		rec.put("XMLB", xmlb);//��Ŀ���
		rec.put("WLBH", wlbh);//���ϱ��
		rec.put("WLMC", wlmc);//��������
		rec.put("GG", gg);//���
		rec.put("XH", xh);//�ͺ�
		rec.put("PCH", pch);//���κ�
		rec.put("RKSL", String.valueOf(rksl));//�������
		rec.put("PCSL", String.valueOf(rksl));//��������
		
		try {
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCHZ, xmlb, wlbh, pch);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_P", rec, bindid, uid);//���������
			}else{
				int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCHZ, rksl, rksl, xmlb, wlbh, xh, pch);//���¿�����
				if(updateCount != 1) throw new RuntimeException("�����ܸ���ʧ�ܣ�"); 
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
