package cn.com.akl.shgl.fjfj.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class ShipmentsBiz {

	/**
	 * �����������¼
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException	 */
	public void insertShipments(Connection conn, final int bindid, final String uid, final String jlbz)throws SQLException{
		insertHead(conn, bindid, uid, jlbz);//���������¼
		insertBody(conn, bindid, uid);//�����ӱ��¼
	}
	
	/**
	 * �������������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param jlbz
	 * @throws SQLException
	 */
	public void insertHead(Connection conn, final int bindid, final String uid, final String jlbz)throws SQLException{
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ_P, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> head = new Hashtable<String, String>();
				head.put("DH", rs.getString("HPJCDH"));//����
				head.put("XMLB", rs.getString("SSXM"));//��Ŀ���
				head.put("DJLB", FJFJCnt.djlx);//�������
				head.put("WLZT", FJFJCnt.wlzt1);//����״̬
				
				String kfck = StrUtil.returnStr(rs.getString("JCKF"));//�ͷ��ֿ�
				String jcck = StrUtil.returnStr(rs.getString("SRKF"));//���ֿ�
				
				if(jlbz.equals(FJFJCnt.jlbz0)){
					head.put("JLBZ", FJFJCnt.jlbz0);//��¼��ʶ
					fillShipmentData(conn, head, kfck, true);//�ͷ��ֿ���Ϣ׷��
					fillShipmentData(conn, head, jcck, false);//���ֿ���Ϣ׷��
				}else{
					head.put("JLBZ", FJFJCnt.jlbz1);//��¼��ʶ
					fillShipmentData(conn, head, kfck, false);//�ͷ��ֿ���Ϣ׷��
					fillShipmentData(conn, head, jcck, true);//���ֿ���Ϣ׷��
				}
				
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", head, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * �������ӱ����
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertBody(Connection conn, final int bindid, final String uid)throws SQLException{
		final String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_JCKF, bindid));//�ĳ��ⷿ
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//���ϱ��
				String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));//��������
				String xh = StrUtil.returnStr(rs.getString("PN"));//�ͺ�
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
				int sl = rs.getInt("SL");//����
//				String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, hwdm, sx, FJFJCnt.zt4));//��ȡ�����Ͽ�����κ�
				
				rec.put("WLBH", wlbh);
				rec.put("WLMC", wlmc);
				rec.put("XH", xh);
				rec.put("SX", sx);
				rec.put("PCH", pch);
				rec.put("SL", String.valueOf(sl));
				rec.put("QSSL", String.valueOf(sl));
				rec.put("HWDM", hwdm);
				rec.put("CKDM", ckdm);
				rec.put("JLBZ", FJFJCnt.jlbz0);//��¼��ʶ
				
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", rec, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ������ͼ�����Ϣ��װ
	 * @param conn
	 * @param head
	 * @param kfckbm
	 * @param direction
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> fillShipmentData(Connection conn, Hashtable<String, String> head, String kfckbm, Boolean flag) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(FJJHCnt.QUERY_KFCK);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, kfckbm);
			while(rs.next()){
				String kfckmc = StrUtil.returnStr(rs.getString("KFCKMC"));//�ͷ��ֿ�����
				String lxr = StrUtil.returnStr(rs.getString("LXR"));//��ϵ��
				String sjh = StrUtil.returnStr(rs.getString("SJH"));//�ֻ���
				String dhqh = StrUtil.returnStr(rs.getString("DHQH"));//�绰����
				String dh = StrUtil.returnStr(rs.getString("DH"));//�绰
				String email = StrUtil.returnStr(rs.getString("EMAIL"));//Email
				String yb = StrUtil.returnStr(rs.getString("YB"));//�ʱ�
				String gj = StrUtil.returnStr(rs.getString("GJ"));//����
				String s = StrUtil.returnStr(rs.getString("S"));//ʡ
				String shi = StrUtil.returnStr(rs.getString("SHI"));//��
				String qx = StrUtil.returnStr(rs.getString("QX"));//��/��
				String dz = StrUtil.returnStr(rs.getString("DZ"));//��ַ
				if(flag){
					head.put("FHKFCKBM", kfckbm);//�����ͷ��ֿ����
					head.put("FHF", kfckmc);//������
					head.put("FHR", lxr);//������
					head.put("FHRSJ", sjh);//�������ֻ�
					head.put("FHRDHQH", dhqh);//�����˵绰����
					head.put("FHRDH", dh);//�����˵绰
					head.put("FHRYX", email);//����������
					head.put("FHYB", yb);//�����ʱ�
					head.put("FHGJ", gj);//��������
					head.put("FHS", s);//����ʡ
					head.put("FHSHI", shi);//������
					head.put("FHQX", qx);//���������أ�
					head.put("FHDZ", dz);//������ַ
				}else{
					head.put("SHKFCKBM", kfckbm);//�ջ��ͷ��ֿ����
					head.put("SHF", kfckmc);//�ջ���
					head.put("SHR", lxr);//�ջ���
					head.put("SHRSJ", sjh);//�ջ����ֻ�
					head.put("SHRDHQH", dhqh);//�ջ��˵绰����
					head.put("SHRDH", dh);//�ջ��˵绰
					head.put("SHRYX", email);//�ջ�������
					head.put("SHYB", yb);//�ջ��ʱ�
					head.put("SHGJ", gj);//�ջ�����
					head.put("SHS", s);//�ջ�ʡ
					head.put("SHSHI", shi);//�ջ���
					head.put("SHQX", qx);//�ջ������أ�
					head.put("SHDZ", dz);//�ջ���ַ
				}
			}
		} finally {
			DBSql.close(ps, rs);
		}
		return head;
	}
}
