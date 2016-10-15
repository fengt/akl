package cn.com.akl.shgl.fjjh.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.DAOUtil.ResultPaser;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class FJJHBiz {

	//��ѯ����ƻ��ӱ�(�ͷ����Ĺ���)
	private static final String QUERY_FJJH = "SELECT * FROM BO_AKL_FJJH_S WHERE BINDID=? AND KFBM=?";
	
	//��ѯȷ�ϵĸ���ƻ��ӱ�
	private static final String QUERY_FJ = "SELECT WLBH,WLMC,XH,SX,SUM(FJJHSL)FJJHSL FROM BO_AKL_FJJH_S WHERE BINDID=? GROUP BY WLBH,WLMC,XH,SX";
	
	//��ѯ���ܵĸ��������ӱ�
	private static final String QUERY_FJLC = "SELECT CPLH,CPMC,CPSX,SN,COUNT(1)SL FROM BO_AKL_FJ_S WHERE BINDID=? GROUP BY CPLH,CPMC,CPSX,SN";
	
	/**
	 * ɸѡ��ѡ��Ŀͷ����ĵ�������Ϣ
	 * @param conn
	 * @param kfckbm
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public Vector<Hashtable<String, String>> queryByKfzx(Connection conn, String kfckbm, int bindid) throws SQLException{
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			String[] ckbm = kfckbm.split("\\|");//�ͷ��ֿ����
			for (int i = 0; i < ckbm.length; i++) {
				String kfzx = ckbm[i];
				ps = conn.prepareStatement(QUERY_FJJH);
				rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, kfzx);
				while(rs.next()){
					rec = new Hashtable<String, String>();
					String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//���ϱ��
					String dbdh = StrUtil.returnStr(rs.getString("DBDH"));//��������
					String sx = StrUtil.returnStr(rs.getString("SX"));//����
					int fjsl = rs.getInt("FJSL");//��������
					
					rec.put("WLBH", wlbh);
					rec.put("DBDH", dbdh);
					rec.put("SX", sx);
					rec.put("FJSL", String.valueOf(fjsl));
					vector.add(rec);
				}
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
	/**
	 * ���������ӱ����
	 * ע���÷�����ʱͣ�ã����Ժ����-->������ĵ������ݴӸ���������ץȡʱ�������ø÷���
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> queryByCplh(Connection conn, int bindid) throws SQLException{
		String jcck = DAOUtil.getStringOrNull(conn, FJJHCnt.QUERY_FJJH_P_JCCKBM, bindid);//����
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(QUERY_FJLC);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String cplh = StrUtil.returnStr(rs.getString("CPLH"));//��Ʒ�Ϻ�
				String cpmc = StrUtil.returnStr(rs.getString("CPMC"));//��Ʒ����
				String cpsx = StrUtil.returnStr(rs.getString("CPSX"));//��Ʒ����
				String pn = StrUtil.returnStr(rs.getString("PN"));//�ͺ�
				int sl = rs.getInt("SL");//����
				
				rec.put("WLBH", cplh);//���ϱ��
				rec.put("WLMC", cpmc);//��������
				rec.put("XH", pn);//�ͺ�
				rec.put("CPSX", cpsx);//����
				rec.put("SJFHSL", String.valueOf(sl));//ʵ�ʷ�������
				rec.put("CKSL", String.valueOf(sl));//�������
				rec.put("CKCKDM", jcck);//����ֿ����
				vector.add(rec);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
	/**
	 * ����ƻ��ӱ����
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> queryByWlbh(Connection conn, int bindid, String ckdm) throws SQLException{
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(QUERY_FJ);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				rec = new Hashtable<String, String>();
				String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
				String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
				String pn = StrUtil.returnStr(rs.getString("XH"));
				String sx = StrUtil.returnStr(rs.getString("SX"));
				int fjjhsl = rs.getInt("FJJHSL");
				
				rec.put("WLBH", wlbh);//���ϱ��
				rec.put("WLMC", wlmc);//��������
				rec.put("XH", pn);//PN
				rec.put("CPSX", sx);//����
				rec.put("SJFHSL", String.valueOf(fjjhsl));//ʵ�ʷ�������
				rec.put("CKSL", String.valueOf(fjjhsl));//�������
				rec.put("CKCKDM", ckdm);//����ֿ����
				vector.add(rec);
			}
		} finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
}




