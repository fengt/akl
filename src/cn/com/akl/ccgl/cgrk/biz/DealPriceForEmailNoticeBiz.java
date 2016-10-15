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
 * 获取价格差异的物料信息，并发送邮件通知
 * @author ActionSoft_2013
 *
 */
public class DealPriceForEmailNoticeBiz {

	private static Connection conn = null;
	/**
	 * 获取采购入库单身中含税采购价与价格管理表中不 相等的物料信息
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
						double hscgj2 = rs.getDouble("HSCGJ");//价格管理表中的含税采购价
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
	 * 获取价格差异的物料信息后，进行邮件通知
	 * @param zcrq
	 * @param wlbh
	 * @param hscgj
	 * @return
	 * @throws IOException
	 */
	public static void getEmailContent(Vector reVector) throws IOException{
		
		/**获取价格差异的物料信息String zcrq,String wlbh,double hscgj**/
		List list = dealPriceEmailNotice(reVector);
		String emailContent = "";
		if(!list.isEmpty()){
			
			emailContent = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;价格管理比对通知，" +
			"以下物料号：<br/>"
			+ list.toString()
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;与采购订单单身中的物料信息中的【含税采购价】存在差异，特此通知！";
			
			String realEmailContent = "";
			if(StrUtil.isNotNull(emailContent)){
				realEmailContent = GetEmailContentUtil.GetEmailModel(emailContent);
				int flag2 = IMAPI.getInstance().sendMail("admin", "admin" , "价格管理比对通知", realEmailContent);
				System.out.println(flag2);
			}
		}
	}
}
