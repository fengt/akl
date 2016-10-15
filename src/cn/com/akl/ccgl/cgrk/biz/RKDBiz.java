package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;

public class RKDBiz {

	/**
	 * ��װ�����ܱ�����
	 * @param pTable
	 * @param vector
	 * @return
	 */
	public static Vector<Hashtable<String, String>> getPvectorV2(
			Hashtable<String, String> pTable, Vector<Hashtable<String, String>> vector,String rkdb){
		
		Vector<Hashtable<String, String>> rceVector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rceHash = null;
		String zt = CgrkCnt.kczt;
		if(rkdb.equals(CgrkCnt.rkdb3)){
			zt = CgrkCnt.kczt3;
		}
		for (int i = 0; i < vector.size(); i++) {
			rceHash = new Hashtable<String, String>();
			Hashtable<String, String> rec = vector.get(i);
			rceHash.put("WLBH", rec.get("wlbh"));
			rceHash.put("WLMC", rec.get("cpmc"));
			rceHash.put("XH", rec.get("xh"));
			rceHash.put("PCH", rec.get("pch"));
			rceHash.put("DW", rec.get("dw"));
			rceHash.put("RKSL", rec.get("sssl"));
			rceHash.put("PCSL", rec.get("sssl"));
			rceHash.put("DJ", rec.get("wsjg"));
			rceHash.put("RKDH", pTable.get("RKDH"));
			rceHash.put("RKRQ", pTable.get("RKRQ"));
			rceHash.put("ZT",zt);
			
			rceVector.add(rceHash);
		}
		return rceVector;
	}
	
	/**
	 * ��װ�����ϸ������
	 * @param pTable
	 * @param vector
	 * @return
	 */
	public static Vector<Hashtable<String, String>> getSvectorV2(Vector<Hashtable<String, String>> vector){
		Vector<Hashtable<String, String>> rceVector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rceHash = null;
		for (int i = 0; i < vector.size(); i++) {
			rceHash = new Hashtable<String, String>();
			Hashtable<String, String> rec = vector.get(i);
			String wlbh = rec.get("wlbh").toString();
			String str0 = "SELECT * FROM " + CgrkCnt.tableName8 + " WHERE WLBH='"+wlbh+"'";
			String gg = DBSql.getString(str0, "GG");
			rceHash.put("WLBH", wlbh);//���ϱ��
			rceHash.put("WLMC", rec.get("cpmc"));//��������
			rceHash.put("PCH", rec.get("pch"));//���κ�
			rceHash.put("XH", rec.get("xh"));//�ͺ�
			rceHash.put("GG", gg);//���
			
			rceHash.put("FZSX", rec.get("fzsx"));//��������
			rceHash.put("SCRQ", rec.get("scrq"));//��������
			rceHash.put("JLDW", rec.get("dw"));//������λ
			rceHash.put("KWSL", rec.get("sssl"));//��λ����
			
			String ckbm = rec.get("ckbm").toString();
			rceHash.put("CKDM",ckbm);//�ֿ����
			String ckmc = DBSql.getString("select CKMC from BO_AKL_CK where CKDM='"+ckbm+"'", "ckmc");
			rceHash.put("CKMC",ckmc);//�ֿ�����
			
			rceHash.put("QDM", rec.get("kfqbm"));//������
			rceHash.put("DDM", rec.get("kfdbm"));//������
			rceHash.put("KWDM", rec.get("kfkwdm"));//��λ����
			rceHash.put("HWDM", rec.get("kwbh"));//��λ����
			rceHash.put("SX", CgrkCnt.sx0);
			rceVector.add(rceHash);
		}
		return rceVector;
	}
	
	/**
	 * ��ⵥ�����ݻ���(����Ի�λ�����ڻ��ܲ���ʱ)
	 * @param conn
	 * @param pTable
	 * @return
	 */
	public static Vector<Hashtable<String, String>> RKDPackageV1(Connection conn, Hashtable<String, String> pTable) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		int bindid = Integer.parseInt(pTable.get("BINDID").toString());
		String sql = "SELECT wlbh,cpmc,xh,pch,ISNULL(wsjg, 0) as dj,sum(sssl) as sssl,dw FROM " + CgrkCnt.tableName1 + " WHERE bindid="+bindid+" GROUP BY wlbh,cpmc,xh,pch,wsjg,dw";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					rec = new Hashtable<String, String>();
					rec.put("wlbh", rs.getString("wlbh"));
					rec.put("cpmc",StrUtil.returnStr(rs.getString("cpmc")));
					rec.put("xh", rs.getString("xh"));
					rec.put("pch", rs.getString("pch"));
					rec.put("wsjg", String.valueOf((rs.getDouble("dj"))));
					rec.put("sssl", String.valueOf(rs.getInt("sssl")));
					rec.put("dw",StrUtil.returnStr(rs.getString("dw")));
					vector.add(rec);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return vector;
	}
	
	/**
	 * ��ⵥ�����ݻ���(��Ի�λ�����ܻ����ͬһ��λ���ظ�����,����Դ���Ų�ͬ)
	 * @param conn
	 * @param pTable
	 * @return
	 */
	public static Vector<Hashtable<String, String>> RKDPackageV2(Connection conn,Hashtable<String, String> pTable) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		int bindid = Integer.parseInt(pTable.get("BINDID").toString());
		String sql = "SELECT WLBH,CPMC,PCH,XH,FZSX,SCRQ,DW,CKBM,KFQBM,KFDBM,KFKWDM,KWBH,sum(SSSL)as SSSL FROM " + CgrkCnt.tableName1 + " WHERE bindid="+bindid+" GROUP BY WLBH,CPMC,PCH,XH,FZSX,SCRQ,DW,CKBM,KFQBM,KFDBM,KFKWDM,KWBH";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs != null){
				while(rs.next()){
					rec = new Hashtable<String, String>();
					rec.put("wlbh", rs.getString("wlbh"));//���ϱ��
					rec.put("cpmc",StrUtil.returnStr(rs.getString("cpmc")));//��������
					rec.put("pch", rs.getString("pch"));//���κ�
					rec.put("xh", rs.getString("xh"));//�ͺ�
					rec.put("fzsx", StrUtil.returnStr(rs.getString("fzsx")));//��������
					rec.put("scrq", StrUtil.returnStr(rs.getString("SCRQ")));//��������
					rec.put("dw",StrUtil.returnStr(rs.getString("dw")));//��λ
					rec.put("ckbm", StrUtil.returnStr(rs.getString("ckbm")));//�ֿ����
					rec.put("kfqbm", StrUtil.returnStr(rs.getString("kfqbm")));//������
					rec.put("kfdbm", StrUtil.returnStr(rs.getString("kfdbm")));//������
					rec.put("kfkwdm", StrUtil.returnStr(rs.getString("kfkwdm")));//��λ����
					rec.put("kwbh", StrUtil.returnStr(rs.getString("kwbh")));//��λ���
					rec.put("sssl", String.valueOf(rs.getInt("sssl")));//��λ����
					vector.add(rec);
				}
			}
		}finally{
			DBSql.close(null, ps, rs);
		}
		return vector;
	}
	
	
	public static Vector<Hashtable<String, String>> getRKDVector(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> rkdVector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> hs = null;
		String query_rkd = "SELECT * FROM BO_AKL_CCB_RKD_BODY WHERE BINDID="+bindid; 
		try{
			ps = conn.prepareStatement(query_rkd);
			rs = ps.executeQuery();
			while(rs.next()){
				hs = new Hashtable<String, String>();
				hs.put("CGDDH",StrUtil.returnStr(rs.getString("CGDDH")));//�ɹ�������
				hs.put("LYDH",StrUtil.returnStr(rs.getString("LYDH")));//��Դ����
				hs.put("WLBH",StrUtil.returnStr(rs.getString("WLBH")));//���ϱ��
				hs.put("CPMC",StrUtil.returnStr(rs.getString("CPMC")));//��������
				hs.put("XH",StrUtil.returnStr(rs.getString("XH")));//�ͺ�
				hs.put("PCH",StrUtil.returnStr(rs.getString("PCH")));//���κ�
				hs.put("SCRQ",StrUtil.returnStr(rs.getString("SCRQ")));//��������
				hs.put("YSSL",String.valueOf(rs.getInt("YSSL")));//Ӧ������
				hs.put("SSSL",String.valueOf(rs.getInt("SSSL")));//ʵ������
				hs.put("WSJG",String.valueOf(rs.getDouble("WSJG")));//δ˰�۸�
				hs.put("HSJG",String.valueOf(rs.getDouble("HSJG")));//��˰�۸�
				hs.put("WSJE",String.valueOf(rs.getDouble("WSJE")));//δ˰���
				hs.put("HSJE",String.valueOf(rs.getDouble("HSJE")));//��˰���
				hs.put("FZSX",StrUtil.returnStr(rs.getString("FZSX")));//��������
				hs.put("SFLP",StrUtil.returnStr(rs.getString("SFLP")));//�Ƿ���Ʒ
				hs.put("DW",StrUtil.returnStr(rs.getString("DW")));//��λ
				hs.put("CKBM",StrUtil.returnStr(rs.getString("CKBM")));//�ֿ����
				hs.put("KFQBM",StrUtil.returnStr(rs.getString("KFQBM")));//������
				hs.put("KFDBM",StrUtil.returnStr(rs.getString("KFDBM")));//������
				hs.put("KFKWDM",StrUtil.returnStr(rs.getString("KFKWDM")));//��λ����
				hs.put("KWBH",StrUtil.returnStr(rs.getString("KWBH")));//��λ���
				hs.put("RKDH",StrUtil.returnStr(rs.getString("RKDH")));//��ⵥ��
				rkdVector.add(hs);
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return rkdVector;
	}
	
}
