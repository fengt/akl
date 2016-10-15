package cn.com.akl.ccgl.cgrk.biz;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;


public class ZCXXBiz {
	
	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";//��ѯ�ͻ�����
	private static final String QUERY_CGCK = "SELECT CKID FROM BO_AKL_CGDD_HEAD WHERE DDID=?";//��ѯ�ɹ��ֿ�
	//�ӿ�λ��ϵ���ȡ���»�λ��ϵ
	private static final String SFYX = "��";//�Ƿ���Ч
	private static final String QUERY_HWDM = "SELECT c.CKDM,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,( SELECT MAX(ID)AS ID FROM BO_AKL_WLKWGXB WHERE SFYX='"+SFYX+"' AND XH=? AND SSKHBM=? AND CKDM=? GROUP BY XH,SSKHBM )b WHERE c.ID=b.ID";
	private static final String QUERY_DH_HWDM = "SELECT c.CKDM,c.QDM,c.DDM,c.KWDM,c.KWBH FROM BO_AKL_WLKWGXB c,( SELECT MAX(ID)AS ID FROM BO_AKL_WLKWGXB WHERE SFYX='"+SFYX+"' AND XH=? AND SSKHBM=? GROUP BY XH,SSKHBM )b WHERE c.ID=b.ID";//������Ͽ�λ��ϵ
	
	/**
	 * ��ȡת����Ϣ
	 * @param pTable��������
	 * @param zcVector�ӱ�����
	 * @param rkrq�������
	 * @return
	 */
	public Vector<Hashtable<String, String>> getZcxx(Connection conn,
			Hashtable<String, String> pTable,Vector<Hashtable<String, String>> zcVector,Date zcrq) throws SQLException{
		Vector<Hashtable<String, String>> reZcVector = new Vector<Hashtable<String, String>>();//��װ���ת����ϸ��Ϣ
		Hashtable<String, String> reTable = null;//���ձ������ת����Ϣ
		Hashtable<String, String> subTable = new Hashtable<String, String>();//������ת����Ϣ
		
		/**���ݿͻ������жϴ��ܿ����κ�����*/
		int bindid = Integer.parseInt(pTable.get("BINDID").toString());
		String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//��ȡ�ͻ�����
		String xh = "";
		for(int i=0;i<zcVector.size();i++){
			reTable = new Hashtable<String, String>();
			subTable = zcVector.get(i);
			if(subTable!=null){
				//reTable.put("WLBH", subTable.get("WLBH").toString());//���ϱ��
				//reTable.put("DW", subTable.get("DW").toString());//��λ
				String cgdh = subTable.get("KHDDH").toString();
				xh = subTable.get("LH").toString();
				
				reTable.put("CGDDH", cgdh);//�ͻ�������(AKL)
				reTable.put("XH", xh);//�ͺ�
				reTable.put("YSSL", subTable.get("CHSL").toString());//��������(Ӧ������)
				reTable.put("SSSL", subTable.get("CHSL").toString());//��������(ʵ��������Ĭ�ϣ������޸�)
				reTable.put("LYDH", subTable.get("DDH").toString());//������
				reTable.put("DDHH", subTable.get("DDHH").toString());//�����к�
				reTable.put("RKDH", pTable.get("RKDH").toString());//��ⵥ��
				
				/**���κţ����Ͽ�λ��ϵ��Ϣ*/
				if("".equals(khdm) || khdm.equals(CgrkCnt.khdm0)){//��Ӫ
					reTable.put("PCH", CreatePCHBiz.createPCH(zcrq));//���κ�
					String cgck = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_CGCK, cgdh));//�ɹ���ͷ�ֿ����
					Hashtable<String, String> sysTable = getSysDatas(conn,subTable,cgck);
					if(sysTable!=null){
						reTable.put("CKBM", sysTable.get("ckdm").toString());//�ֿ����
						reTable.put("KFQBM", sysTable.get("qdm").toString());//�ⷿ������
						reTable.put("KFDBM", sysTable.get("ddm").toString());//�ⷿ������
						reTable.put("KFKWDM", sysTable.get("kwdm").toString());//�ⷿ��λ����
						reTable.put("KWBH", sysTable.get("kwbh").toString());//��λ���
					}else{//Ĭ�ϲɹ�ʱ�Ĳֿ�
						reTable.put("CKBM", cgck);//�ֿ����
						reTable.put("KWBH", cgck);//��λ���
					}
				}else{//����
					reTable.put("PCH", CreatePCHBiz.createPCH0(zcrq));//���κ�
					Hashtable<String, String> sysTable = getSysDatas(conn,subTable);
					if(sysTable!=null){
						reTable.put("CKBM", sysTable.get("ckdm").toString());//�ֿ����
						reTable.put("KFQBM", sysTable.get("qdm").toString());//�ⷿ������
						reTable.put("KFDBM", sysTable.get("ddm").toString());//�ⷿ������
						reTable.put("KFKWDM", sysTable.get("kwdm").toString());//�ⷿ��λ����
						reTable.put("KWBH", sysTable.get("kwbh").toString());//��λ���
					}
				}
			}
			
			/**�����ͺźͿͻ����룬�������������Ϣ**/
			Hashtable<String, String> wlxxTable = getWLXX(conn,pTable,subTable);
			if(wlxxTable!=null){
				String wlbh = wlxxTable.get("wlbh").toString();//���ϱ���
				reTable.put("WLBH", wlbh);
				reTable.put("CPMC", wlxxTable.get("wlmc").toString());//��������
				reTable.put("DW", wlxxTable.get("dw").toString());//��λ
				/**�����Ϻţ���ȡ�۸�������ִ������С�ڵ���ת�����ڵ����һ�θ��Ϻŵ�δ˰�۸�ͺ�˰�۸�**/
				Hashtable<String, String> jgglTabel = dealZCJG(conn, pTable, wlbh);
				if(jgglTabel!=null){
					double wsjg = Double.parseDouble(jgglTabel.get("wsjg").toString());//δ˰�۸�
					//double hsjg = Double.parseDouble(jgglTabel.get("hsjg").toString());//��˰�۸�
					double sl = Double.parseDouble(jgglTabel.get("sl").toString());//˰��
					double hsjg = wsjg*(sl+1.0);
					double wsje = Double.parseDouble(jgglTabel.get("wsjg").toString()) * Integer.parseInt(subTable.get("CHSL").toString());//δ˰���
					double hsje = Double.parseDouble(jgglTabel.get("hsjg").toString()) * Integer.parseInt(subTable.get("CHSL").toString());//��˰���
					if(khdm.equals(CgrkCnt.khdm0)&&(hsjg==0.0 || wsjg==0.0)){
						throw new RuntimeException("�����ϡ�"+xh+"��ת��ʱδ�ܶ�ȡ�����¼۸���˲�����ϵļ۸����Ϣ�Ƿ���ȷ��");
					}
					reTable.put("HSJG", String.valueOf(hsjg));
					reTable.put("WSJG", String.valueOf(wsjg));
					reTable.put("WSJE", String.valueOf(wsje));
					reTable.put("HSJE", String.valueOf(hsje));
					reTable.put("SL",String.valueOf(sl));
				}else{
					 if(khdm.equals(CgrkCnt.khdm0)){
						 throw new RuntimeException("�����ϡ�"+xh+"��ת��ʱδ�ܶ�ȡ�����¼۸���˲�����ϵļ۸����Ϣ�Ƿ���ȷ��");
					 }
				}
			}
			reZcVector.add(reTable);
		}
		return reZcVector;
	}
	
	/**
	 * �����ͺźͿͻ����룬��ȡ���������Ϣ
	 * @param table
	 * @return
	 */
	public Hashtable<String, String> getWLXX(Connection conn,Hashtable<String, String> pTable,Hashtable<String, String> table) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> wlxxTable = null;
		String xh = table.get("LH").toString();
		String khdm = table.get("KHDM").toString();
		String sql = "select * from " + CgrkCnt.tableName14 + " where gyskhbh = '" + khdm + "'";
		String khbh = DBSql.getString(sql, "KHBH");
		String sql2 = "select * from " + CgrkCnt.tableName8 + " where xh = '" + xh + "' and hzbm = '"+khbh+"'";
		try {
			ps = conn.prepareStatement(sql2);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					wlxxTable = new Hashtable<String, String>();
					// ��ȡ���������Ϣ
					String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
					String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
					String dw = StrUtil.returnStr(rs.getString("DW"));
					wlxxTable.put("wlbh", wlbh);
					wlxxTable.put("wlmc", wlmc);
					wlxxTable.put("dw", dw);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return wlxxTable;
	}
	
	/**
	 * 
	 * @param conn
	 * @param pTable ������Ϣ==>��ȡת������
	 * @param wlbh ���ϱ��==>��ȡ�۸�����и����Ϻ�����Ӧ��δ˰�۸񡢺�˰�۸�
	 * @return
	 */
	private Hashtable<String, String> dealZCJG(Connection conn,Hashtable<String, String> pTable,String wlbh) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> reTable = null;
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());//ת������
		String gysbh = pTable.get("GYSBH").toString();//��Ӧ�̱��
		/*String sql = "select * from " + CgrkCnt.tableName10 + " where (CONVERT(varchar(100), ZXRQ, 23)) = " +
				"(select max(CONVERT(varchar(100), ZXRQ, 23)) from " + CgrkCnt.tableName10 + " where (CONVERT(varchar(100), ZXRQ, 23)) <= '"+ zcrq +"' and wlbh = '" + wlbh + "' and gysbh = '" + gysbh + "') and wlbh = '" + wlbh + "' and gysbh = '" + gysbh + "'" +
						"AND ID = (SELECT MAX(ID) FROM " + CgrkCnt.tableName10 + " WHERE (CONVERT (VARCHAR(100), ZXRQ, 23)) <= '"+zcrq+"' AND wlbh = '"+wlbh+"' and gysbh = '" + gysbh + "')";
		*/
		String sql = "SELECT * FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) = ( SELECT MAX ( CONVERT (VARCHAR(100), ZXRQ, 23) ) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+ zcrq +"' AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' ) AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' AND ID = ( SELECT MAX (ID) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+zcrq+"' AND wlbh = '"+wlbh+"' AND gysbh = '" + gysbh + "' )";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					reTable = new Hashtable<String, String>();
					reTable.put("wsjg", String.valueOf(rs.getDouble("ZDCB")));//δ˰�۸�
					reTable.put("hsjg", String.valueOf(rs.getDouble("HSCGJ")));//��˰�۸�
					reTable.put("sl", String.valueOf(rs.getDouble("SL")));//˰��
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return reTable;
	}
	
	/**
	 * ��ȡ��λ�����Ϣ(����)
	 * @param table
	 * @return
	 */
	public Hashtable<String, String> getSysDatas(Connection conn,Hashtable<String, String> table,String cgck) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> sysTable = null;
		String xh = table.get("LH").toString();
		String khdm = table.get("KHDM").toString();
		String sql = "select KHBH from " + CgrkCnt.tableName14 + " where gyskhbh = '" + khdm + "'";
		String khbh = DBSql.getString(conn, sql, "KHBH");
		try {
			ps = conn.prepareStatement(QUERY_HWDM);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xh, khbh, cgck);
			if(rs!=null){
				while(rs.next()){
					sysTable = new Hashtable<String, String>();
					//��ȡ��λ�����Ϣ
					String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
					String qdm = StrUtil.returnStr(rs.getString("QDM"));
					String ddm = StrUtil.returnStr(rs.getString("DDM"));
					String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
					String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
					sysTable.put("ckdm", ckdm);
					sysTable.put("qdm", qdm);
					sysTable.put("ddm", ddm);
					sysTable.put("kwdm", kwdm);
					sysTable.put("kwbh", kwbh);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return sysTable;
	}
	
	/**
	 * ��ȡ��λ�����Ϣ(���)
	 * @param conn
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getSysDatas(Connection conn,Hashtable<String, String> table) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable<String, String> sysTable = null;
		String cgdh = table.get("KHDDH").toString();//�ɹ�����
		String xh = table.get("LH").toString();
		String khdm = table.get("KHDM").toString();
		String sql = "select * from " + CgrkCnt.tableName14 + " where gyskhbh = '" + khdm + "'";
		String khbh = DBSql.getString(sql, "KHBH");
		try {
			ps = conn.prepareStatement(QUERY_DH_HWDM);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xh, khbh);
			if(rs!=null){
				while(rs.next()){
					sysTable = new Hashtable<String, String>();
					//��ȡ��λ�����Ϣ
					String ckdm = StrUtil.returnStr(rs.getString("CKDM"));
					String qdm = StrUtil.returnStr(rs.getString("QDM"));
					String ddm = StrUtil.returnStr(rs.getString("DDM"));
					String kwdm = StrUtil.returnStr(rs.getString("KWDM"));
					String kwbh = StrUtil.returnStr(rs.getString("KWBH"));
					sysTable.put("ckdm", ckdm);
					sysTable.put("qdm", qdm);
					sysTable.put("ddm", ddm);
					sysTable.put("kwdm", kwdm);
					sysTable.put("kwbh", kwbh);
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return sysTable;
	}
}
