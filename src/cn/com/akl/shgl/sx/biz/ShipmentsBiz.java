package cn.com.akl.shgl.sx.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.dfh.biz.DfhConstant;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
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
	public static void insertShipments(Connection conn, final int bindid, final String uid)throws SQLException{
		final String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_CKDM, bindid));//�ͷ��ֿ����
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXCnt.QUERY_XMLB, bindid));//��Ŀ���
		
		/**���������¼*/
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SX_P, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> head = new Hashtable<String, String>();
				String lxr = StrUtil.returnStr(rs.getString("KHMC"));
				String sjh = StrUtil.returnStr(rs.getString("SJH"));
				String dhqh = StrUtil.returnStr(rs.getString("DHQH"));
				String dh = StrUtil.returnStr(rs.getString("DH"));
				String email = StrUtil.returnStr(rs.getString("EMAIL"));
				String yb = StrUtil.returnStr(rs.getString("YB"));
				String gj = StrUtil.returnStr(rs.getString("GJ"));
				String s = StrUtil.returnStr(rs.getString("S"));
				String shi = StrUtil.returnStr(rs.getString("SHI"));
				String qx = StrUtil.returnStr(rs.getString("QX"));
				String dz = StrUtil.returnStr(rs.getString("XXDZ"));
				
				
				head.put("DH", rs.getString("SXDH"));//����
				head.put("XMLB", xmlb);//��Ŀ���
				head.put("DJLB", SXCnt.djlx);//�������
				head.put("WLZT", SXCnt.wlzt1);//����״̬
				head.put("FHFLX", DfhConstant.SFHFLX_KH);//����������
				head.put("SHFLX", DfhConstant.SFHFLX_KFCK);//�ջ�������
				
				//�ջ��ֿ���Ϣ
				fillShipmentData(conn, head, ckdm);
				
				//��������Ϣ
//				head.put("FHKFCKBM", );//�����ͷ��ֿ����
//				head.put("FHKFCKMC", );//�����ͷ��ֿ�����
				head.put("FHR", lxr);//������
				head.put("FHF", lxr);//�ͻ�����
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
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_P", head, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);

		
		/**�����ӱ��¼*/
		DAOUtil.executeQueryForParser(conn, SXCnt.QUERY_SXMX, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				Hashtable<String, String> rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//���ϱ��
				String wlmc = StrUtil.returnStr(rs.getString("WLMC"));//��������
				String xh = StrUtil.returnStr(rs.getString("XH"));//�ͺ�
//				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
				int sl = rs.getInt("SL");//����
//				String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, hwdm, sx, FJFJCnt.zt4));//��ȡ�����Ͽ�����κ�
				
				rec.put("WLBH", wlbh);
				rec.put("WLMC", wlmc);
				rec.put("XH", xh);
				rec.put("SX", SXCnt.sx2);
				rec.put("PCH", pch);
				rec.put("SL", String.valueOf(sl));
				rec.put("QSSL", String.valueOf(sl));
				rec.put("HWDM", hwdm);
				rec.put("CKDM", ckdm);
				
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
	
	public static Hashtable<String, String> fillShipmentData(Connection conn, Hashtable<String, String> head, String ckdm) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(FJJHCnt.QUERY_KFCK);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, ckdm);
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
				
				head.put("SHKFCKBM", ckdm);//�ջ��ͷ��ֿ����
				head.put("SHKFCKMC", kfckmc);//�ջ��ͷ��ֿ�����
				head.put("SHF", kfckmc);//�ջ��ͷ��ֿ�����
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
		} finally {
			DBSql.close(ps, rs);
		}
		return head;
	}
}
