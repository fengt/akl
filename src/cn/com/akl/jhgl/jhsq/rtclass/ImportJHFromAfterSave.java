package cn.com.akl.jhgl.jhsq.rtclass;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.ExcelUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class ImportJHFromAfterSave extends ExcelDownFilterRTClassA {

	public ImportJHFromAfterSave(UserContext uc){
		super(uc);
		this.setVersion("2.0.0");
		this.setDescription("借货上传，只需填写型号&数量");
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		try{		
			//获得单头信息
			Hashtable<String, String> headData = BOInstanceAPI.getInstance().getBOData("BO_AKL_JHDD_HEAD", processInstanceId);
			String ckdm = headData.get("WFJHCKBH");
			HSSFSheet sheet  = arg0.getSheetAt(0);
			//获得借货汇总表数据
			int j = 1;
			for(int i = 6; i<=sheet.getLastRowNum();i++){
				HSSFRow row = sheet.getRow(i);
			    HSSFCell cell_3 = row.getCell(3);
			    HSSFCell cell_5 = row.getCell(5);
			    //HSSFCell cell_13 = row.getCell(12);
			    //验证
			    if("".equals(cell_3+"")){
			    	MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请填写产品型号!", true);
			    	return 	ExcelUtil.getClearWorkBook(arg0);
			    }
			    if("".equals(cell_5+"")){
			    	MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请填写借货数量!", true);
			    	return 	ExcelUtil.getClearWorkBook(arg0);
			    }	
			    String xh = cell_3.toString();
			    BigDecimal jhsl;
			    try{
			    	jhsl = new BigDecimal(cell_5.toString());
			    }catch(Exception ex){
			    	ex.printStackTrace();
			    	MessageQueue.getInstance().putMessage(getUserContext().getUID(), "借货数量只能整数的数字!", true);
			    	return 	ExcelUtil.getClearWorkBook(arg0);
			    }
			    //后台数据获取
				Hashtable<String, Object> ht = this.getData(conn, rs, ps,ckdm,xh,jhsl);
				//excel 重新赋值
				row = this.writeDataInRow(row, ht, j);
				//回写excel后验证数据是否有效
				if("".equals(row.getCell(1))){
					MessageQueue.getInstance().putMessage(getUserContext().getUID(),"系统后台出错，请先维护型号："+xh+"的产品记录!", true);
					return 	ExcelUtil.getClearWorkBook(arg0);
				}
				if("".equals(row.getCell(8))){
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "系统后台出错，请先维护型号："+xh+"的产品总代成本价!", true);
					return 	ExcelUtil.getClearWorkBook(arg0);
				}
				j++;
			}
		}catch(Exception e){
			e.printStackTrace();
			return 	ExcelUtil.getClearWorkBook(arg0);
		}finally{
			DBSql.close(conn, ps, rs);
		}
		return arg0;
		
	}
	/**
	 * @param conn
	 * @param rs
	 * @param ps
	 * @param ckdm
	 * @param xh
	 * @param jhsl
	 * @return
	 * @throws SQLException
	 * @author hzy
	 * @desc 获取后台数据
	 */
	private Hashtable<String, Object> getData(Connection conn,ResultSet rs,PreparedStatement ps,String ckdm,
			String xh,BigDecimal jhsl) throws SQLException {
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		String sql = " select t.id,t.WLBH,t.WLMC,t.XH,case when t.ZDCB is null then 0 else t.ZDCB end as ZDCB," +
				" t.SL,case when t.PCSL is null then 0 else t.PCSL end as PCSL," +
				" case when t.SDSL is null then 0 else t.SDSL end as SDSL," +
				" (case when t.PCSL is null then 0 else t.PCSL end-case when t.SDSL is null then 0 else t.SDSL end) as KYSL" +
				" from (select *,(select top 1 b.ZDCB from BO_AKL_JGGL b " +
				" where a.WLBH = b.WLBH and (case when b.ZXRQ is NULL then '2000-01-01' else b.ZXRQ end) < GETDATE() order by b.CREATEDATE DESC) as ZDCB," +
				" (SELECT SUM(c.KWSL) from BO_AKL_KC_KCMX_S c join BO_AKL_KC_KCHZ_P e on(c.WLBH = e.WLBH AND c.PCH = e.PCH) " +
				" where c.WLBH = a.WLBH and c.CKDM = ? and c.SX = '049088' and e.ZT = '042022' GROUP BY c.WLBH) as PCSL," +
				" (select sum(d.SDSL) from BO_AKL_KC_SPPCSK d where d.CKDM = ? and d.WLBH = a.WLBH  GROUP BY d.WLBH) as SDSL " +
				" from BO_AKL_WLXX a where a.HZBM = '01065') t where t.XH = ? ";
		 conn = DBSql.open();
		 ps = conn.prepareStatement(sql);
		 ps.setString(1, ckdm);
		 ps.setString(2, ckdm);
		 ps.setString(3, xh);
		 rs = ps.executeQuery();
		 while(rs.next()){
			 String wlbh = rs.getString("WLBH");
			 String wlmc = rs.getString("WLMC");
			 BigDecimal wscgj = new BigDecimal(rs.getString("ZDCB"));//
			 BigDecimal sl = new BigDecimal(rs.getString("SL"));//
			 BigDecimal pcsl = new BigDecimal(rs.getString("PCSL"));//
			 BigDecimal sdsl = new BigDecimal(rs.getString("SDSL"));//
			 BigDecimal kysl = new BigDecimal(rs.getString("KYSL"));//
			 ht.put("WLBH", wlbh);
			 ht.put("WLMC", wlmc);
			 ht.put("JHZXCB", wscgj);
			 ht.put("SL", sl);
			 ht.put("KCSL", pcsl);
			 ht.put("SKSL", sdsl);
			 ht.put("KYSL", kysl);
			 ht.put("JHZXWSCB", wscgj.multiply(jhsl));
			 ht.put("JHZXHSZCB", wscgj.multiply(jhsl).multiply(sl.add(new BigDecimal(1))));
		 }
		return ht;
	}
	
	/**
	 * @param row
	 * @param ht
	 * @return
	 * @throws GeneralSecurityException
	 * @author hzy
	 * @desc 向excel行写入数据
	 */
	private HSSFRow writeDataInRow(HSSFRow row,Hashtable<String, Object> ht,int j)throws GeneralSecurityException{
		HSSFCell cell_0  = row.createCell(0);
		HSSFCell cell_1  = row.createCell(1);
		HSSFCell cell_2  = row.createCell(2);
		HSSFCell cell_4  = row.createCell(4);
		HSSFCell cell_6  = row.createCell(6);
		HSSFCell cell_7  = row.createCell(7);
		HSSFCell cell_8  = row.createCell(8);
		HSSFCell cell_9  = row.createCell(9);
		HSSFCell cell_10  = row.createCell(10);
		HSSFCell cell_11  = row.createCell(11);
		cell_0.setCellValue(j);
		cell_1.setCellValue(ht.get("WLBH")+"");
		cell_2.setCellValue(ht.get("WLMC")+"");
		cell_4.setCellValue(ht.get("KCSL")+"");
		cell_6.setCellValue(ht.get("SKSL")+"");
		cell_7.setCellValue(ht.get("KYSL")+"");
		cell_8.setCellValue(ht.get("JHZXCB")+"");
		cell_9.setCellValue(ht.get("JHZXWSCB")+"");
		cell_10.setCellValue(ht.get("SL")+"");
		cell_11.setCellValue(ht.get("JHZXWSCB")+"");
		return row;
	}
}
