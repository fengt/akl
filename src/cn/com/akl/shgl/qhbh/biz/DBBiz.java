package cn.com.akl.shgl.qhbh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class DBBiz {

	/**
	 * ������ͷ��Ϣ��װ
	 * @param conn
	 * @param bindid
	 * @param dbdh
	 * @param xmlb
	 * @param bhck
	 * @param kfzx
	 * @param db_uid
	 * @param fhr
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getHead(Connection conn, int bindid, String dbdh, String xmlb, String bhck, String kfzx, String db_uid, String fhr) throws SQLException{
		Hashtable<String, String> head = new Hashtable<String, String>();
		head.put("DBDH", dbdh);//��������
		head.put("XMLX", xmlb);//��Ŀ���
		head.put("DBLX", QHBHCnt.dblx);//��������
		
		fillShipmentData(conn, head, bhck, true);//�����ֿ���Ϣ׷��
		fillShipmentData(conn, head, kfzx, false);//�ͷ��ֿ���Ϣ׷��
		
		return head;
	}
	
	/**
	 * ������ͷ�У��շ���Ϣ����װ
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
	
	
	/**
	 * ��ȡ���ͷ�������������(��ϸ)
	 * @param conn
	 * @param bindid
	 * @param kfzx
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> getDetailBody(Connection conn, int bindid, String kfzx) throws SQLException{
		Hashtable<String, String> rec = null;
		Vector<Hashtable<String, String>> body = new Vector<Hashtable<String, String>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			ps = conn.prepareStatement(QHBHCnt.QUERY_DB_DETAIL);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, kfzx);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				rec.put("WLBH", rs.getString("SQCPWLBH"));
				rec.put("WLMC", rs.getString("SQCPZWMC"));
				rec.put("XH", rs.getString("SQCPPN"));
				rec.put("CPSX", rs.getString("SX"));
				rec.put("PCH", rs.getString("PCH"));
				rec.put("RKCKDM", rs.getString("JFKFBM"));//���ֿ����
				rec.put("RKCKMC", rs.getString("JFKFMC"));//���ֿ�����
//				rec.put("CKCKDM", );//����ֿ����
//				rec.put("CKCKMC", );//����ֿ�����
				rec.put("CKHWDM", rs.getString("HWDM"));//�����λ����
				rec.put("CKSL", String.valueOf(rs.getInt("SQCPSL")));
				body.add(rec);
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return body;
	}
	
	
	/**
	 * ��ȡ���ͷ�������������(����)
	 * @param conn
	 * @param bindid
	 * @param kfzx
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> getGatherBody(Connection conn, int bindid, String kfzx, String bhck) throws SQLException{
		String bhmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_KFCKMC, bhck));//�����ֿ�����
		Hashtable<String, String> rec = null;
		Vector<Hashtable<String, String>> body = new Vector<Hashtable<String, String>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			ps = conn.prepareStatement(QHBHCnt.QUERY_DB_GATHER_NEW);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bhck, kfzx, bindid);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				rec.put("WLBH", rs.getString("SQCPWLBH"));//���ϱ��
				rec.put("WLMC", rs.getString("SQCPZWMC"));//��������
				rec.put("XH", rs.getString("SQCPPN"));//�ͺ�
				
				rec.put("GG", rs.getString("GG"));//���
				rec.put("SJLH", rs.getString("SJLH"));//�����Ϻ�
				rec.put("KCSL", rs.getString("KCKYZ"));//�������
				rec.put("CPLX", rs.getString("CPFL"));//��Ʒ����
//				rec.put("JG", rs.getString(""));//�۸�
				
				rec.put("CPSX", rs.getString("PHSX"));//�������
				rec.put("RKCKDM", rs.getString("JFKFBM"));//�˿�ֿ����
				rec.put("RKCKMC", rs.getString("JFKFMC"));//���ֿ�����
				rec.put("CKCKDM", bhck);//����ֿ����
				rec.put("CKCKMC", bhmc);//����ֿ�����
				rec.put("CKSL", String.valueOf(rs.getInt("CKSL")));
				body.add(rec);
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return body;
	}
	
	/**
	 * ��������
	 * @param conn
	 * @param bindid
	 * @param kfmc
	 * @param uid
	 * @param head
	 * @param body
	 * @throws AWSSDKException
	 */
	public int startWorkflow(Connection conn,String kfmc, String uid, Hashtable<String, String> head,
			Vector<Hashtable<String, String>> detailBody, Vector<Hashtable<String, String>> gatherBody) throws AWSSDKException{
		int sub_bindid = WorkflowInstanceAPI.getInstance().createProcessInstance(QHBHCnt.uuid, uid, kfmc + QHBHCnt.subTitle);
		WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, sub_bindid, 1, uid, kfmc + QHBHCnt.subTitle);
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_P", head, sub_bindid, uid);
//		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_S", detailBody, sub_bindid, uid);
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_HZ_S", gatherBody, sub_bindid, uid);
		return sub_bindid;
	}
	
	/**
	 * �˺Ÿ�ʽת��
	 * @param fzr
	 * @return
	 */
	@Deprecated
	public String accountParse(String fzr){
		String uid;
		String regex = "([a-z]{1,}|\\d{6})<.+>";
		Pattern p = Pattern.compile(regex);
		Matcher mt = p.matcher(fzr);
		if("".equals(fzr) || !mt.matches()){
			throw new RuntimeException("�ͷ����ĸ������˺Ÿ�ʽ���������飡");
		}else{
			String[] str = fzr.split("<");
			uid = str[0];
		}
		return uid;
	}
	
}
