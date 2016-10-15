package cn.com.akl.hhgl.hhrk.biz;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.biz.CreatePCHBiz;
import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;
import cn.com.akl.ccgl.cgrk.biz.CreatePCHBiz;

public class DealHzDatasBiz {
	
	/**
	 * ��װ�����ܱ�(����)��Ϣ
	 * @param pTable
	 * @return
	 */
	public static Vector<String> getPvector(Connection conn,Hashtable pTable,Vector sVector){
		
		Vector rePvector = new Vector();
		Hashtable rePtable = null;
		Hashtable reStable = new Hashtable();
		List<String> list = new ArrayList<String>();
		
		//String sql1 = "SELECT COUNT(*) c2 FROM (SELECT pch as c2 FROM BO_AKL_KC_KCHZ_P WHERE CREATEDATE>CONVERT(CHAR(10),GETDATE(),21) group by pch) a ";
		
		//int c2 = DBSql.getInt(sql1, "c2");//�ɹ������ʵ������
		//int c3 =c2;
		java.util.Date now2 = new java.util.Date(); 
		
		 
		String code=	new java.text.SimpleDateFormat("yyyy-MM-dd").format(now2).toString().replace("-", "");
		String code2=code;
		/**��ȡ����ӱ���Ϣ�������������ܱ���**/
		String s[][] ; 
		s = new String[sVector.size()][2] ;
		for (int i = 0; i < sVector.size(); i++) {
			rePtable = new Hashtable();
			reStable = (Hashtable)sVector.get(i);
			/**���޸��߼����жϸ����θ��ͺţ��Ƿ���ڿ����ܱ������ڣ���ֱ�Ӹ��£��������ڣ���ֱ�Ӳ��뵽�����ܱ���**/
//			int cnt = judgeKCHZDatas(reStable);
//			if(cnt<=0){
			    int bindid = Integer.parseInt(reStable.get("BINDID").toString());
				String pch = reStable.get("PCH").toString();
				String xh = reStable.get("XH").toString();
				String wlbh = reStable.get("WLBH").toString();
				String id = reStable.get("ID").toString();
				String sql = "select sum(sssl) sssl from " + HHDJConstant.tableName1 + " where bindid = " + bindid + " and wlbh = '" + wlbh + "' and id='"+id+"'";
				
				int sssl = DBSql.getInt(sql, "sssl");//�ɹ������ʵ������
				rePtable.put("RKDH", pTable.get("RKDH").toString());
				rePtable.put("ZT", HHDJConstant.kczt2);
				rePtable.put("XH", reStable.get("XH"));//�ͺ�
				rePtable.put("RKSL", sssl);//�������
				 code2=code;
				int isc=0;
				String pch1=CreatePCHBiz.createPCH(Date.valueOf(new java.text.SimpleDateFormat("yyyy-MM-dd").format(now2).toString()));
				String pch2="";				
				pch2=pch1;
				for(int j=0;j<s.length;j++)
				{
					if(s[j][0]!=null)
					{ if(s[j][0].indexOf(wlbh)==0){
						s[j][1]=String.valueOf(Integer.parseInt(s[j][1])+1).toString();	
						pch2=code+String.format("%03d", Integer.parseInt(pch1.substring(8,11))+Integer.parseInt(s[j][1])-1);
						
						isc=1;}
					}					
				}
				for(int j=0;j<s.length;j++)
				{
					if(isc!=1 &&s[j][0] ==null)
					{					
						s[j][0]=wlbh;
						s[j][1]="1";
						break;
					}
				}
				
				rePtable.put("PCH",pch2);//���κ�					
				rePtable.put("WLBH", reStable.get("WLBH"));//���ϱ��
				rePtable.put("WLMC", reStable.get("CPMC"));//��������
				rePtable.put("DJ", reStable.get("WSJG"));//����
				rePtable.put("CKSL", 0);//��������
				rePtable.put("PCSL", sssl);//��������		
				java.util.Date now = new java.util.Date(); 
				
				rePtable.put("RKRQ", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));//�������
			
				
//			}
			
			rePvector.add(rePtable);
		}
		
		return rePvector;
	}
	
	/**
	 * ��װ�����ϸ��(�ӱ�)��Ϣ
	 * @param sVector
	 * @return
	 */
	public static Vector<String> getSvector(Connection conn,Vector sVector,Vector PVector){
		
		Vector reSvector = new Vector();
		Hashtable reStable = null;
		Hashtable reptable = null;
		Hashtable reSStable = null;
		for (int i = 0; i < sVector.size(); i++) {
			reSStable = new Hashtable();
			reStable = (Hashtable)sVector.get(i);
			reSStable.put("WLBH", reStable.get("WLBH"));//���ϱ��
			reSStable.put("WLMC", reStable.get("CPMC"));//��������
			
			for (int j = 0; j < PVector.size(); j++) {
				reptable = new Hashtable();
				reptable = (Hashtable)PVector.get(j);
				String a=reStable.get("WLBH").toString();
				
				String b=reptable.get("WLBH").toString();
				
				String c=reStable.get("WSJG").toString();
				String d=reptable.get("DJ").toString();
				
				if(reStable.get("WLBH")==reptable.get("WLBH") && reStable.get("WSJG")==reptable.get("DJ"))
				{
					reSStable.put("PCH", reptable.get("PCH"));//���κ�
					
				}
			}
			
			
			reSStable.put("XH", reStable.get("XH"));//�ͺ�
			
			reSStable.put("CKDM", reStable.get("CKBM"));//�ֿ����
			String sq1 = "SELECT [CKMC] FROM BO_AKL_CK WHERE [CKDM]='"+reStable.get("CKBM")+"'";
			
			String CMC = DBSql.getString(sq1, "CKMC");//�ɹ������ʵ������
			
			reSStable.put("CKMC", CMC);//�ⷿ������
			reSStable.put("QDM", reStable.get("KFQBM"));//�ⷿ������
			reSStable.put("DDM", reStable.get("KFDBM"));//�ⷿ������
			reSStable.put("KWDM", reStable.get("KFKWDM"));//�ⷿ��λ����
			reSStable.put("HWDM", reStable.get("KWBH"));//��λ����
			reSStable.put("KWSL", reStable.get("SSSL"));//��λ����
			reSStable.put("SCRQ", reStable.get("SCRQ"));//��������
			PreparedStatement ps = null;
			ResultSet rs = null;
			conn = DBSql.open();
			String sql = "SELECT *  FROM [dbo].[BO_AKL_WLXX]  where [WLBH] = '"+reStable.get("WLBH")+"'"; 
			String sql2 = "";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs.next()) {

				reSStable.put("JLDW", rs.getString("DW"));//��Ʒ����
				reSStable.put("GG", rs.getString("GG"));//��Ʒ����
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
				reSStable.put("SX", "049088");//��Ʒ����
			
			//int rs=updateData(reStable.get("WLBH").toString(),reStable.get("WLBH").toString(), Integer.parseInt(reStable.get("SSSL").toString()));
			reSvector.add(reSStable);
		}
		//judgePvector(sVector);
		return reSvector;
	}
	
	/**
	 * ���Ľڵ㣺У��ת����������������Ƿ����
	 * @param pTable
	 * @return
	 */
	public static boolean judgePvector(Vector sVector){
		
		Hashtable rePtable = null;
		Hashtable reStable = new Hashtable();
		for (int i = 0; i < sVector.size(); i++) {
			rePtable = new Hashtable();
			reStable = (Hashtable)sVector.get(i);
			    int bindid = Integer.parseInt(reStable.get("BINDID").toString());
				String id = reStable.get("LYDH").toString();
				String xh = reStable.get("LYDH").toString();
				String wlbh = reStable.get("WLBH").toString();
				int SSSL = Integer.parseInt(reStable.get("SSSL").toString());
			int ss=	updateKCHZDatas(id,xh,wlbh,SSSL);
		}
		return true;
	}
	
	
	

	
	/**
	 * ���޸��߼������ݵ�ǰ��ⵥ����Ϣ�����½軹���������Ϣ
	 * @return
	 */
	public static int updateKCHZDatas(String id,String lh,String wlbh,int sssl){
//		
		String sql2 ="";
		String sql ="";	
	
		int cnt=-1;
		if(id.length()>1)
		{
		if(id.substring(0,1).equals("H"))
		{
			sql2 = "UPDATE a SET a.YHHSL=CASE WHEN a.YHHSL IS NULL THEN 0 ELSE a.YHHSL end+"+sssl+",a.DHSL=CASE WHEN a.DHSL IS NULL THEN 0 ELSE a.DHSL end-"+sssl+",a.hhzt=case when CASE WHEN a.DHSL IS NULL THEN 0 ELSE a.DHSL end-"+sssl+"=0 then 1 else 0 end FROM [AKL_BPM].[dbo].[BO_AKL_JHDD_BODY] a INNER JOIN dbo.BO_AKL_HHDD_BODY_TOTAL b ON b.JHDHH =a.HH INNER JOIN [dbo].[BO_AKL_HHDD_HEAD] c ON b.[BINDID]=c.[BINDID] AND a.[JHDH]=c.[JHDH] WHERE b.HH='"+lh+"'";
			sql= "UPDATE a SET a.YHHSL=CASE WHEN a.YHHSL IS NULL THEN 0 ELSE a.YHHSL end+"+sssl+",a.DHSL=CASE WHEN a.DHSL IS NULL THEN 0 ELSE a.DHSL end +"+sssl+" FROM [AKL_BPM].[dbo].[BO_AKL_JHDD_BODY] a   INNER join   dbo.BO_JHDD_BODY_TOTAL c ON a.WLBH=c.WLBH AND a.JHDH=c.JHDH INNER JOIN dbo.BO_AKL_HHDD_BODY_TOTAL b ON b.JHDHH = a.HH INNER JOIN [dbo].[BO_AKL_HHDD_HEAD] d ON b.[BINDID] = d.[BINDID] AND c.[JHDH] = d.[JHDH] WHERE b.HH='"+lh+"'";
			 cnt = DBSql.executeUpdate(sql2);
			 cnt = DBSql.executeUpdate(sql);
			
		}
		
		}
		
		
		
			
		return cnt;
	}
}
