package cn.com.akl.hhgl.hhrk.biz;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;


import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.IMAPI;

import cn.com.akl.hhgl.hhrk.constant.HHDJConstant;
import cn.com.akl.hhgl.hhrk.util.GetEmailContentUtil;
import cn.com.akl.util.StrUtil;

/**
 * ��ȡ�۸�����������Ϣ���������ʼ�֪ͨ
 * @author ActionSoft_2013
 *
 */
public class DealPriceForEmailNoticeBiz {

	private static Connection conn = null;
	/**
	 * ��ȡ�ɹ���ⵥ���к�˰�ɹ�����۸������в� ��ȵ�������Ϣ
	 * @param reVector
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List dealPriceEmailNotice(Vector reVector){
		conn = DBSql.open();
		PreparedStatement ps = null;
		ResultSet rs = null;
		List list = new ArrayList();
		Hashtable table = new Hashtable();
		for (int i = 0; i < reVector.size(); i++) {
			table = (Hashtable) reVector.get(i);
			String zcrq = table.get("zcrq").toString();
			String wlbh = table.get("wlbh").toString();
			double hscgj = Double.parseDouble(table.get("hscgj").toString());
			String sql = "select HSCGJ from " + HHDJConstant.tableName10 + " where ZXRQ = (select max(CONVERT(varchar(100), ZXRQ, 23)) from " + HHDJConstant.tableName10 + " where ZXRQ <= '"+ zcrq +"') and wlbh = '" + wlbh + "'";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs!=null){
					while(rs.next()){
						double hscgj2 = rs.getDouble("HSCGJ");//�۸������еĺ�˰�ɹ���
						if(hscgj!=hscgj2){
							list.add(wlbh);
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
		}
		return list;
	}
	
	/**
	 * ��ȡ�۸�����������Ϣ�󣬽����ʼ�֪ͨ
	 * @param zcrq
	 * @param wlbh
	 * @param hscgj
	 * @return
	 * @throws IOException
	 */
	public static void getEmailContent(Vector reVector) throws IOException{
		
		/**��ȡ�۸�����������ϢString zcrq,String wlbh,double hscgj**/
		List list = dealPriceEmailNotice(reVector);
		String emailContent = "";
		if(!list.isEmpty()){
			
			emailContent = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;�۸����ȶ�֪ͨ��" +
			"�������Ϻţ�<br/>"
			+ list.toString()
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;��ɹ����������е�������Ϣ�еġ���˰�ɹ��ۡ����ڲ��죬�ش�֪ͨ��";
			
			String realEmailContent = "";
			if(StrUtil.isNotNull(emailContent)){
				realEmailContent = GetEmailContentUtil.GetEmailModel(emailContent);
				int flag2 = IMAPI.getInstance().sendMail("admin", "admin" , "�۸����ȶ�֪ͨ", realEmailContent);
				System.out.println(flag2);
			}
		}
	}
}
