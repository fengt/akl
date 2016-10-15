/**
 * 
 */
package cn.com.akl.u8.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * @author hzy
 *
 */
public class InterfaceUtil {
	/**
	 * �ӻ�������ά�����л�ȡU8��������IP��ַ 
	 */
	public static String getU8IPAddress(){
		Connection DBcon=DBSql.open();
	    Statement stmt = null;
	    ResultSet rs = null;
	    String U8IPAddr=null;
	    String IpSql="select U8SYSIP from BO_AKL_U8_INFORMATIO where U8SYSCODE = '002'";
	    try {
			stmt = DBcon.createStatement();
			rs = stmt.executeQuery(IpSql);
			while (rs.next()){
				U8IPAddr=rs.getString("U8SYSIP");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				DBcon.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    return U8IPAddr;
	}
	/**
	 * @return
	 * @author hzy
	 * @desc ��ȡ�ϴ�u8�ļ�&�����ļ���·��
	 */
	public static Map<String, String> getFilePosition(){
		Map<String, String> fp = new HashMap<String, String>();
		Connection DBcon=DBSql.open();
	    Statement stmt = null;
	    ResultSet rs = null;
	    String ys=null;
	    String md = null;
	    String IpSql="select YS,MD from BO_AKL_FILE_POSITION where dm = '002'";
	    try {
			stmt = DBcon.createStatement();
			rs = stmt.executeQuery(IpSql);
			while (rs.next()){
				ys=rs.getString("YS");
				md=rs.getString("MD");
			}
			fp.put("YS", ys);
			fp.put("MD", md);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				DBcon.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fp;
	}
	
	/**
	 * @param ht
	 * @return
	 * @author hzy
	 * @desc ���ϴ��ļ�&�����ļ�·�����浽���ݿ�
	 */
	public static void addDate(Hashtable<String,String> ht){
		Connection DBcon=DBSql.open();
	    try {
	    	DBcon.setAutoCommit(false);// �ֹ��ύ����
			BOInstanceAPI.getInstance().createBOData(DBcon, "BO_AKL_INTERFACE",ht,301482, "admin");
			DBcon.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				DBcon.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}finally{
			try {
				DBcon.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param bhflbh
	 * @param bpmflbh
	 * @param bpmflbhmc
	 * @return
	 * @author hzy
	 * @desc ͨ��bpm�еı�Ż��߱�����ƻ�ȡu8�еı��
	 */
	public static String getU8Number(String bhflbh,String bpmflbh,String bpmflbhmc){
		Connection DBcon=DBSql.open();
		PreparedStatement ps = null;
	    ResultSet rs = null;
	    String U8flbh=null;
	    String sql="select U8FLBH from BO_AKL_U8_Number_R where BHFLBH = ? AND (BPMFLBH = ? OR BPMFLMC = ? )";
	    try {
			ps = DBcon.prepareStatement(sql);
			ps.setString(1, bhflbh);
			ps.setString(2,bpmflbh);
			ps.setString(3, bpmflbhmc);
			rs = ps.executeQuery();
			while (rs.next()){
				U8flbh=rs.getString("U8FLBH");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				DBcon.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    return U8flbh;
	}
}
