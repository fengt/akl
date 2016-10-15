package cn.com.akl.shgl.fjfj.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class FJFJBiz {

	/**
	 * ����棨�ͷ����ܲ���
	 * @param conn
	 * @param bindid
	 * @param xmlb
	 * @throws SQLException
	 */
	public static void decreaseKCXX(Connection conn, int bindid, final int stepNo)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//��Ŀ���
		
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//���ϱ��
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//��������
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
				String ejjl = StrUtil.returnStr(rs.getString("EJJL"));//�������
				int sl = rs.getInt("SL");//����
				
				String hwdm = "";
				if(stepNo == 3){//�ͷ�
					hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
				}else{//��ⲿ
					hwdm = StrUtil.returnStr(rs.getString("HWDM2"));//����λ����
					if(ejjl.equals(FJFJCnt.jcjg0)){//�й��ϣ���ò�Ʒ�����ظ��ͷ�
						return true;
					}
				}
				
				/**1�����¹�������״̬(��;)*/
				DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX_ZT, FJFJCnt.zt3, xmlb, wlbh, pch, gztm);
				
				/**2�����¿����Ϣ*/
				int n = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_DE, sl, xmlb, wlbh, sx, pch, hwdm);
				if(n != 1){
					throw new RuntimeException("���ۼ�ʧ�ܣ�����ϵ����Ա��");
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ���������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param rs
	 * @param xmlb
	 * @throws SQLException
	 */
	public static void insertXLH(Connection conn, int bindid, String uid, ResultSet rs, String xmlb)
			throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//���ϱ��
		String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));//��������
		String xh = StrUtil.returnStr(rs.getString("PN"));//�ͺ�
		String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//��������
		String sx = StrUtil.returnStr(rs.getString("SX"));//����
		String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
//		String kc_hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
		int sl = rs.getInt("SL");//����
		
//		String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, kc_hwdm, sx, FJFJCnt.zt4));//��ȡ�����Ͽ�����κ�
		
		rec.put("XMLB", xmlb);
		rec.put("WLBH", wlbh);
		rec.put("WLMC", wlmc);
		rec.put("XH", xh);
		rec.put("PCH", pch);
		rec.put("SX", sx);
		rec.put("ZT", FJFJCnt.zt3);
		rec.put("KWSL", String.valueOf(sl));
		rec.put("GZTM", gztm);
		
		/**
		 * ��ȡ�ֿ��λ��Ϣ�Ż����������Ͽ�λ��ϵ����
		 */
		String qdm = "";
		String ddm = "";
		String kwdm = "";
		String hwdm = "";
		String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_SRKF, bindid));//���ⷿ����
		String ckmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKMC, ckdm));//���ⷿ����
		
		Hashtable<String, String> hwdmRecord = getHWXX(conn, xmlb, wlbh, ckdm);
		if(hwdmRecord != null){
			qdm = hwdmRecord.get("qdm").toString();
			ddm = hwdmRecord.get("ddm").toString();
			kwdm = hwdmRecord.get("kwdm").toString();
			hwdm = hwdmRecord.get("hwdm").toString();
		}else{
			hwdm = ckdm;
		}
		rec.put("CKDM", ckdm);//�ֿ����
		rec.put("CKMC", ckmc);//�ֿ�����
		rec.put("QDM", qdm);//������
		rec.put("DDM", ddm);//������
		rec.put("KWDM", kwdm);//��λ����
		rec.put("HWDM", hwdm);//��λ����
		
		try {
			
			/**
			 * 1���ܲ���������
			 */
			int n = DAOUtil.getInt(conn, FJFJCnt.QUERY_isExistKCMX, wlbh, sx, ckdm, pch, FJFJCnt.zt3);
			if(n == 0){
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SHKC_S", rec, bindid, uid);
			}else{
				DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX, sl, wlbh, sx, ckdm, pch, FJFJCnt.zt3);
			}
			
			/**
			 * 2�����·����ӱ����λ����(HWDM2)������������ϸ(�ⷿ��Ϣ��״̬)
			 */
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_HWDM2, hwdm, bindid, wlbh, gztm);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX, ckdm, ckmc, qdm, ddm, kwdm, hwdm, xmlb, wlbh, pch, gztm);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * �������޵�����ʽ���������
	 * @param conn
	 * @param sxBindid
	 * @param clfs
	 * @param sx ԭ����
	 * @param sx2 ������
	 * @param xmlb
	 * @param wlbh
	 * @param pch
	 * @param hwdm �ͷ���λ����
	 * @param hwdm2 ����λ����
	 * @param gztm
	 * @throws SQLException
	 */
	public static void setAttribute(Connection conn, int sxBindid, String clfs, String sx, String sx2,
			String xmlb, String wlbh, String pch, String hwdm, String hwdm2, String gztm, int id, boolean flag) throws SQLException{
		
		/**1�����������ӱ�(����ʽ������)*/
		int n = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_SX_CLFS, clfs, sx2, sxBindid, wlbh, gztm, pch, hwdm, sx);
		if(n != 1) throw new RuntimeException("�õ���Ӧ�����޵��ݣ�����ʽ������ʧ�ܣ�");
		
		/**2���ͷ������¿������к�(����)*/
		int updateRow1 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_SX, sx2, xmlb, wlbh, pch, hwdm, sx);
//		String xhHWDM = hwdm;//�ͷ���<--����
//		if(flag) xhHWDM = hwdm2;//�ͷ���-->���⣨���к����ڼ��⣩
		int updateRow2 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX_SX, sx2, xmlb, wlbh, pch, gztm, sx);
		if(updateRow1 != 1 || updateRow2 != 1){
			throw new RuntimeException("�ͷ��������ת��ʧ�ܣ�");
		}
		
		/**3�����⣺���¿��(����)*/
		int updateRow3 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_SX, sx2, xmlb, wlbh, pch, hwdm2, sx);
		if(updateRow3 != 1) throw new RuntimeException("����������ת��ʧ�ܣ�");
		
		/**4�����������ӱ�����(����)*/
		int updateRow4 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_SX, sx2, id);
		if(updateRow4 != 1) throw new RuntimeException("�����ӱ�����ת��ʧ�ܣ�");
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
	public static Hashtable<String, String> getHWXX(Connection conn, String xmlb, String wlbh, String ckdm)throws SQLException{
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
