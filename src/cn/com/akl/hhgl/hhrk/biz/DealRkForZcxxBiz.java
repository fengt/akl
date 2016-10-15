package cn.com.akl.hhgl.hhrk.biz;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.util.DBSql;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;


public class DealRkForZcxxBiz {
	
	private Connection conn = null;
	
	/**
	 * ��ȡת����Ϣ
	 * @param pTable��������
	 * @param zcVector�ӱ�����
	 * @param rkrq�������
	 * @return
	 */
	public Vector getZcxx(Hashtable pTable,Vector zcVector,Date zcrq){
		conn = DBSql.open();
		Vector reZcVector = new Vector();//��װ���ת����ϸ��Ϣ
		Hashtable reTable = null;//���ձ������ת����Ϣ
		Hashtable subTable = new Hashtable();//������ת����Ϣ
		for(int i=0;i<zcVector.size();i++){
			reTable = new Hashtable();
			subTable = (Hashtable) zcVector.get(i);
			if(subTable!=null){
				/**�������κţ�����������ⵥ��**/
				reTable.put("PCH", CreatePCHBiz.createPCH(zcrq));//���κ�
				
				reTable.put("XH", subTable.get("LH").toString());//�Ϻ�
				reTable.put("SSSL", subTable.get("CHSL").toString());//��������
				reTable.put("DW", subTable.get("DW").toString());//��λ
				reTable.put("CGDDH", subTable.get("KHDDH").toString());//�ͻ�������(AKL)
				reTable.put("LYDH", subTable.get("DDH").toString());//������
			}
			
			
			/**�����ͺţ�����ֿ������Ϣ**/
			Hashtable sysTable = getSysDatas(conn,subTable);
			if(sysTable!=null){
				reTable.put("CKBM", sysTable.get("ckdm").toString());//�ֿ����
				reTable.put("KFQBM", sysTable.get("qdm").toString());//�ⷿ������
				reTable.put("KFDBM", sysTable.get("ddm").toString());//�ⷿ������
				reTable.put("KFKWDM", sysTable.get("kwdm").toString());//�ⷿ��λ����
				reTable.put("KWBH", sysTable.get("kwbh").toString());//��λ���
			}
			
			/**�����ͺţ��������������Ϣ**/
			Hashtable wlxxTable = getWLXX(conn,pTable,subTable);
			if(wlxxTable!=null){
				reTable.put("WLBH", wlxxTable.get("wlbh").toString());//���ϱ���
				reTable.put("CPMC", wlxxTable.get("wlmc").toString());//��������
				/**�����Ϻţ���ȡ�۸�������ִ������С�ڵ���ת�����ڵ����һ�θ��Ϻŵ�δ˰�۸�ͺ�˰�۸�**/
				Hashtable jgglTabel = dealZCJG(conn, pTable, wlxxTable.get("wlbh").toString());
				if(jgglTabel!=null){
					reTable.put("HSJG", jgglTabel.get("hsjg").toString());//δ˰�۸�
					reTable.put("WSJG", jgglTabel.get("wsjg").toString());//��˰�۸�
					reTable.put("WSJE", Double.parseDouble(jgglTabel.get("wsjg").toString()) * Integer.parseInt(subTable.get("SSSL").toString()));//��˰���
					reTable.put("HSJE", Double.parseDouble(jgglTabel.get("hsjg").toString()) * Integer.parseInt(subTable.get("SSSL").toString()));//��˰���
				}
			}
			
			reZcVector.add(reTable);
		}
		//���ر����ݿ�����
		DBSql.close(conn, null, null);
		return reZcVector;
	}
	
	/**
	 * ��ȡ��λ�����Ϣ
	 * @param table
	 * @return
	 */
	public Hashtable getSysDatas(Connection conn,Hashtable table){
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable sysTable = null;
		String xh = table.get("LH").toString();
		String sql = "select * from " + HHDJConstant.tableName9 + " where xh = '" + xh + "'";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					sysTable = new Hashtable();
					// ��ȡ��λ�����Ϣ
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
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(null, ps, rs);
		}
		return sysTable;
	}
	
	/**
	 * �����ͺţ���ȡ���������Ϣ
	 * @param table
	 * @return
	 */
	public Hashtable getWLXX(Connection conn,Hashtable pTable,Hashtable table){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable wlxxTable = null;
		String xh = table.get("LH").toString();
		String sql = "select * from " + HHDJConstant.tableName8 + " where xh = '" + xh + "'";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					wlxxTable = new Hashtable();
					// ��ȡ���������Ϣ
					String wlmc = StrUtil.returnStr(rs.getString("WLMC"));
					String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
					wlxxTable.put("wlbh", wlbh);
					wlxxTable.put("wlmc", wlmc);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(null, ps, rs);
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
	private Hashtable dealZCJG(Connection conn,Hashtable pTable,String wlbh){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Hashtable reTable = null;
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());
		//select * from BO_AKL_JGGL where ZXRQ = (select max(CONVERT(varchar(100), ZXRQ, 23)) from BO_AKL_JGGL where ZXRQ <= '2014-07-21') 
		String sql = "select * from " + HHDJConstant.tableName10 + " where ZXRQ = (select max(CONVERT(varchar(100), ZXRQ, 23)) from " + HHDJConstant.tableName10 + " where ZXRQ <= '"+ zcrq +"') and wlbh = '" + wlbh + "'";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					reTable = new Hashtable();
					double wsjg = rs.getDouble("WSCGJ");//δ˰�۸�
					double hsjg = rs.getDouble("HSCGJ");//��˰�۸�
					reTable.put("wsjg", wsjg);
					reTable.put("hsjg", hsjg);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(null, ps, rs);
		}
		return reTable;
	}
}
