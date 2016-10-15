package cn.com.akl.ccgl.cgrk.biz;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.ccgl.cgrk.util.GetEmailContentUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.local.level0.IMAPI;

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
			String sql = "select HSCGJ from " + CgrkCnt.tableName10 + " where CONVERT(varchar(100),ZXRQ,23) = (select max(CONVERT(varchar(100), ZXRQ, 23)) from " + CgrkCnt.tableName10 + " where CONVERT(varchar(100),ZXRQ,23)<= '"+ zcrq +"') and wlbh = '" + wlbh + "'";
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
