package cn.com.akl.shgl.fjjh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DBBiz {

	/**
	 * ������ͷ���ݲ���
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void insertDBHead(Connection conn, int bindid, final int sub_bindid, final String uid, final String direction) throws SQLException{
		DAOUtil.executeQueryForParser(conn, FJJHCnt.QUERY_FJJH_P, new ResultPaser(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				dbHead(conn, sub_bindid, rs, uid, direction);
				return true;
			}
		}, bindid);
	}
	
	/**
	 * ������ͷ���ݷ�װ
	 * @param conn
	 * @param bindid
	 * @param rs
	 * @param uid
	 * @throws SQLException
	 */
	public void dbHead(Connection conn, int bindid, ResultSet rs, String uid, String direction) throws SQLException{
		Hashtable<String, String> head = new Hashtable<String, String>();
		String xmlb = rs.getString("XMLB");
		String fhckbm = rs.getString("FHCKBM");
		String jcckbm = rs.getString("JCCKBM");
		
		head.put("DBLX", FJJHCnt.dblx1);//��������
		head.put("XMLX", xmlb);//��Ŀ���
		
		if(direction.equals(FJJHCnt.direction0)){
			fillShipmentData(conn, head, fhckbm, true);//�����ֿ���Ϣ׷��
			fillShipmentData(conn, head, jcckbm, false);//���ֿ���Ϣ׷��
		}else{
			fillShipmentData(conn, head, fhckbm, false);//�����ֿ���Ϣ׷��
			fillShipmentData(conn, head, jcckbm, true);//���ֿ���Ϣ׷��
		}
		
		try {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_P", head, bindid, uid);
		} catch (AWSSDKException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
					head.put("FHKFCKMC", kfckmc);//�����ͷ��ֿ�����
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
					head.put("SHKFCKMC", kfckmc);//�ջ��ͷ��ֿ�����
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
