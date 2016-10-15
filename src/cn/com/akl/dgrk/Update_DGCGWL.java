package cn.com.akl.dgrk;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class Update_DGCGWL extends ExcelDownFilterRTClassA {

	public Update_DGCGWL(UserContext uc) {
		super(uc);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("匹配导入型号的订单号、物料号、名称、规格、库存数量");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGCG_P", bindid);
		String ddbh = rkdtData.get("DDBH") == null ?"":rkdtData.get("DDBH").toString();//订单编号
		String khbh = rkdtData.get("KHBH") == null ?"":rkdtData.get("KHBH").toString();//客户编号
		//String gysbh = rkdtData.get("GYSBH") == null ?"":rkdtData.get("GYSBH").toString();//供应商编号
		if(ddbh.equals("") || khbh.equals("")){
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "单头信息不全，请检查");
		}
		
		Vector vc = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGCG_S", bindid);
		
		Connection conn = DBSql.open();
		try {
			Iterator t = vc.iterator();
			while(t.hasNext()){
				//读取采购单身数据
				Hashtable formData = (Hashtable) t.next();
				String xh = formData.get("XH") == null ?"":formData.get("XH").toString().trim();//型号
//				String gg = formData.get("GG") == null ?"":formData.get("GG").toString().trim();//规格
				String dw = formData.get("DW") == null ?"":formData.get("DW").toString().trim();//计量单位
				if("".equals(xh)){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "型号存在空值，请检查");
				}
				if("".equals(dw)){
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "单位存在空值，请检查");
				}
				if(!dw.matches("[0-9]+")){
					//获取单位编码
					String dwsql = "select XLBM from BO_AKL_DATA_DICT_S where DLBM='026' and XLMC='"+dw+"'";
					dw = DBSql.getString(dwsql, "XLBM");
				}
				//匹配采购单表物料号、名称、规格、库存数量
				String wlbhsql = "SELECT A.WLBH,WLMC,GG,B.KWSL FROM (SELECT WLBH,WLMC,GG FROM BO_AKL_WLXX WHERE XH='"+xh+"' AND HZBM = '"+khbh+"') A LEFT JOIN (SELECT WLBH,JLDW,SUM(KWSL) AS KWSL FROM BO_AKL_DGKC_KCMX_S GROUP BY WLBH,JLDW) B ON A.WLBH = B.WLBH";
				String wlbh = DBSql.getString(wlbhsql, "WLBH");//物料编号
				String wlmc = DBSql.getString(wlbhsql, "WLMC");//物料名称
				String gg = DBSql.getString(wlbhsql, "GG");//规格
				int kwsl = DBSql.getInt(wlbhsql, "KWSL");//库存数量
				String updatewl = "update BO_AKL_DGCG_S set DDBH='"+ddbh+"',WLBH='"+wlbh+"',WLMC='"+wlmc+"',GG='"+gg+"',DW='"+dw+"',KCSL='"+kwsl+"',CGZT='待采购' where bindid = '"+bindid+"' and XH='"+xh+"'";
				DBSql.executeUpdate(updatewl);
			}
		} catch (Exception e) {
			MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "匹配失败，请通知后台");
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, null, null);
		}
		return null;
	}
}
