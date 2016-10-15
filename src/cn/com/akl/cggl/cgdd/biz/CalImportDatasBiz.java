package cn.com.akl.cggl.cgdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.util.DBSql;

public class CalImportDatasBiz {

	/**
	 * ���м�˰�ϼ�
	 * @param vector
	 * @param bindid
	 */
	public void calDatasForAccount(Vector vector,int bindid){
		List list = new ArrayList();
		BigDecimal total = new BigDecimal(0);
		if(vector!=null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable table = (Hashtable) vector.get(i);
				int cgsl = Integer.parseInt(table.get("CGSL").toString());
				BigDecimal cgdj = parseNull(new BigDecimal(table.get("CGDJ").toString()));//δ˰�۸�
				BigDecimal sl = parseNull(new BigDecimal(table.get("SL").toString()));//˰��
				BigDecimal hsdj = cgdj.multiply(sl.add(new BigDecimal(1)));//��˰�۸�
				total = total.add(hsdj.multiply(new BigDecimal(cgsl)));
			}
		}
		/**�����˰�ϼ�**/
		calDatasForAccount(total, bindid);
	}
	
	/**
	 * �����˰�ϼ�
	 * @param total
	 * @param bindid
	 */
	public void calDatasForAccount(BigDecimal total,int bindid){
		String sql = " update " + CgddConstant.tableName0 + " set JSHJ = " + total + " where bindid = " + bindid;
		int cnt = DBSql.executeUpdate(sql);
	}
	
	/**
	 * �������ݺ��Զ�������Ӧ���ֶ�
	 * @param vector
	 * @param bindid
	 */
	public void ImportDataFillback(Vector vector, Hashtable pTable, int bindid){
		double zdcb = 0.0000d;
		double sl = 0.0000d;
		double zero = 0.000d;
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String xh = rec.get("XH").toString();//�ͺ�
				double frmCgdj = Double.parseDouble(rec.get("CGDJ").toString());//�ɹ�����
				double frmSl = Double.parseDouble(rec.get("SL").toString());//˰��
				int cgsl = Integer.parseInt(rec.get("CGSL").toString());
				 
				
				String str = "select * from " + CgddConstant.tableName3 + " where xh = '"+xh+"' and hzbm = '"+CgddConstant.hzbm0+"'";
				String wlbh = DBSql.getString(str, "wlbh");
				String wlmc = DBSql.getString(str, "wlmc");
				String gg = DBSql.getString(str, "gg");
				int minqdl = DBSql.getInt(str, "minqdl");
				String ddid = pTable.get("DDID").toString();
				
				/**�Ӽ۸���л�ȡ���²ɹ����ۺ�˰��**/
				Date cgrq = Date.valueOf(pTable.get("CGRQ").toString());
				String gysbh = pTable.get("GYSID").toString();//��Ӧ�̱��
				String cgdb = pTable.get("DBID").toString();//�ɹ�����
				/*String str2 = "select * from " + CgddConstant.tableName4 + " where wlbh='"+wlbh+"' and " +
						"(CONVERT(varchar(100), ZXRQ, 23)) = (select max(CONVERT(varchar(100), ZXRQ, 23)) from " + CgddConstant.tableName4 + " where (CONVERT(varchar(100), ZXRQ, 23)) <= '"+cgrq+"' and wlbh='"+wlbh+"')" +
						"AND ID = (SELECT MAX(ID) FROM " + CgddConstant.tableName4 + " WHERE (CONVERT (VARCHAR(100), ZXRQ, 23)) <= '"+cgrq+"' AND wlbh = '"+wlbh+"')";
				*/
				String str2 = "SELECT * FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) = ( SELECT MAX ( CONVERT (VARCHAR(100), ZXRQ, 23) ) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+ cgrq +"' AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' ) AND wlbh = '" + wlbh + "' AND gysbh = '" + gysbh + "' AND ID = ( SELECT MAX (ID) FROM BO_AKL_JGGL WHERE ( CONVERT (VARCHAR(100), ZXRQ, 23) ) <= '"+cgrq+"' AND wlbh = '"+wlbh+"' AND gysbh = '" + gysbh + "' )";
				zdcb = DBSql.getDouble(str2, "ZDCB");
				sl = DBSql.getDouble(str2, "SL");
				
				if(cgdb.equals(CgddConstant.dbid1) || cgdb.equals(CgddConstant.dbid2)){//�زɡ������ɹ���excel����۸�Ϊ׼
					if(frmCgdj != zero && zdcb != frmCgdj){
						zdcb = frmCgdj;
					}
					if(frmSl != zero && sl != frmSl){
						sl = frmSl;
					}
				}
				
				//�������ϱ��ȡ�������
				String str4 = "SELECT SUM(PCSL) AS KCSL FROM " + CgddConstant.tableName6 + " WHERE WLBH='"+wlbh+"'";
				int kcsl = DBSql.getInt(str4, "KCSL");
				 
				String str3 = "update " + CgddConstant.tableName1 + " set ddid = '"+ddid+"', wlbh = '"+wlbh+"', wlmc = '"+wlmc+"', gg = '"+gg+"', sl = "+sl+", qdl = "+minqdl+", cgdj = "+zdcb+",cgsl = "+cgsl+",kcsl = "+kcsl+" where xh = '"+xh+"' and bindid = "+bindid;
				int cnt = DBSql.executeUpdate(str3);
			}
		}
	}
	
	
	private BigDecimal parseNull(BigDecimal bd){
		if(bd == null){
			return new BigDecimal(0);
		}else{
			return bd;
		}
	}
}
