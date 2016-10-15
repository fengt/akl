package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class KCBiz {

	/**
	 * �����ܲ�������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param rs
	 * @param xmlb
	 * @throws SQLException
	 */
	public void insertKCHZ(Connection conn, int bindid, String uid, ResultSet rs, String xmlb, String pch) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		int rksl = rs.getInt("SL");
		rec.put("XMLB", xmlb);//��Ŀ���
		rec.put("WLBH", wlbh);//���ϱ��
		rec.put("WLMC", wlmc);//��������
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
	
	/**
	 * �����ϸ���£������Ա仯�ĸ��£�
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param body
	 * @throws SQLException
	 */
	public void insertKCMX(Connection conn, int bindid, String uid, Hashtable<String, String> body, String xmlb, String clfs) throws SQLException{
		String wlbh = body.get("WLBH");
		String sx = body.get("SX");
		String pch = body.get("PCH");
		String hwdm = body.get("HWDM");
		int sl = Integer.parseInt(body.get("KWSL"));
		
		/**
		 * 1��������һ�ڵ�������£��Ĵ���Ʒ��棻
		 * ע��������˼·���˲飨��һ�ڵ�������£��Ĵ���Ʒ��棬������ֵ = ������������ɾ���˿���¼����������ÿ��;�����˲����ϸ��ӣ�
		 */
		int updateCount = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_KWSL, -sl, xmlb, wlbh, hwdm, pch, SXCnt.sx2);
		if(updateCount != 1) throw new RuntimeException("����Ʒ������ʧ�ܣ�");
		int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistKCMX, xmlb, wlbh, sx, hwdm, pch, SXCnt.zt0);
		if(!SXCnt.clfs8.equals(clfs)){//��������ʽ����ʵ��������������棬������������
			if (n == 0) {
				//a��������û�У������¿��
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", body, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				//b�����������У��ӿ������
				int updateCount1 = DAOUtil.executeUpdate(conn, SXCnt.UPDATE_KCMX_KWSL, sl, xmlb, wlbh, hwdm, pch, sx);
				if(updateCount1 != 1) throw new RuntimeException("�����ϸ����ʧ�ܣ�");
			}
		}
	}
	
	/**
	 * ���������ϸ
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param body
	 */
	public void insertXLHMX(Connection conn, int bindid, String uid, Hashtable<String, String> body, String xmlb, String gztm) throws SQLException{
		String wlbh = body.get("WLBH");
		String sx = body.get("SX");
		String pch = body.get("PCH");
		String hwdm = body.get("HWDM");
		try {
			/**1���������¿����ϸ*/
			int n = DAOUtil.getInt(conn, SXCnt.QUERY_isExistGZMX, xmlb, wlbh, sx, hwdm, pch, SXCnt.zt0, gztm);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_XLH_S", body, bindid, uid);
			}else{
				throw new RuntimeException("�ù������롾"+gztm+"���ظ�¼�룬��˲飡");
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * ɾ����ʱ�����ϸ
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void removeKCMX(Connection conn, int bindid) throws SQLException{
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SHKC_S", bindid);
	}
	
	/**
	 * �����ϸ���ݷ�װ
	 * @param conn
	 * @param rs
	 * @param ckdm
	 * @param xmlb
	 * @param pch
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String,String> getKCMX(Connection conn, ResultSet rs, String ckdm, String xmlb, String ywlx, String pch, String sx)
			throws SQLException{
		
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		String syrlx = StrUtil.returnStr(rs.getString("SYRLX"));
		String gztm = StrUtil.returnStr(rs.getString("GZTM"));
		int id = rs.getInt("ID");
		int sl = rs.getInt("SL");
		double jg = rs.getDouble("JG");
		
		String ckmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKMC, ckdm));
		rec.put("KEYID", String.valueOf(id));
		rec.put("XMLB", xmlb);//��Ŀ���
		rec.put("WLBH", wlbh);//���ϱ��
		rec.put("WLMC", wlmc);//��������
		rec.put("SXRLX", syrlx);//ʹ��������
		rec.put("XH", xh);//�ͺ�
		rec.put("SX", sx);//����
		rec.put("PCH", pch);//���κ�
		rec.put("ZT", SXCnt.zt0);//״̬
		rec.put("KWSL", String.valueOf(sl));//��λ����
		rec.put("DJ", String.valueOf(jg));//����
		rec.put("GZTM", gztm);//��������
		
		/**
		 * ��ȡ�ֿ��λ��Ϣ�Ż����������Ͽ�λ��ϵ����
		 */
		Hashtable<String, String> hwdmRecord = getHWXX(conn, xmlb, wlbh, ckdm);
		String hwdm = "";
		rec.put("CKDM", ckdm);//�ͷ��ֿ����
		rec.put("CKMC", ckmc);//�ͷ��ֿ�����
		if(hwdmRecord != null){
			hwdm = hwdmRecord.get("hwdm").toString();
			rec.put("QDM", hwdmRecord.get("qdm").toString());//������
			rec.put("DDM", hwdmRecord.get("ddm").toString());//������
			rec.put("KWDM", hwdmRecord.get("kwdm").toString());//��λ����
			rec.put("HWDM", hwdm);//��λ����
		}else{
			hwdm = ckdm;
			rec.put("HWDM", ckdm);//��λ����
		}
		return rec;
	}
	
	/**
	 * ��ȡ�ͷ��ֿ�Ŀ�λ��ϵ��Ϣ
	 * @param conn
	 * @param xmlb
	 * @param wlbh
	 * @param ckdm
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getHWXX(Connection conn, String xmlb, String wlbh, String ckdm)throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> rec = null;
		try{
			ps = conn.prepareStatement(SXCnt.QUERY_HWXX);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, ckdm);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String qdm = StrUtil.returnStr(rs.getString("QDM"));
				String ddm = StrUtil.returnStr(rs.getString("DDM"));
				String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
				String kwbh = StrUtil.returnStr(rs.getString("HWDM"));
				rec.put("qdm", qdm);
				rec.put("ddm", ddm);
				rec.put("kwdm", kwdm);
				rec.put("hwdm", kwbh);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return rec;
	}
}
