package cn.com.akl.xsgl.jggl.biz;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.util.DBSql;

public class ExcelImpDatasComputeImp {
	
	/**
	 * 计算回填销售供货价、价格毛利率、含税采购价
	 * @param vector
	 * @param bindid
	 */
	public void PriceCompute(Vector vector, int bindid){
		//List list = new ArrayList();
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable table = (Hashtable)vector.get(i);
				double zdcb = Double.parseDouble(table.get("ZDCB").toString());
				double xs = Double.parseDouble(table.get("XS").toString());
				double sl = Double.parseDouble(table.get("SL").toString());
				int id = Integer.parseInt((table.get("ID").toString()));
				String sql="select * from BO_AKL_WLXX where wlbh="+StrUtil.returnStr(table.get("WLBH").toString());
				String wlmc = DBSql.getString(sql, "WLMC");
				String xh=DBSql.getString(sql, "XH");
				String sql2="SELECT * FROM BO_AKL_GYS_P where gysbh="+StrUtil.returnStr(table.get("GYSBH").toString());
				String gysmc=DBSql.getString(sql2, "GYSMC");
				double xsghj = zdcb*(xs + 1.0000)*(sl + 1.0000);
				double hscgj = zdcb*(sl + 1.0000);
				double jgmlh = (xsghj - hscgj)/xsghj;
				
				/**格式化计算出的值的保留小数位**/
				/*DecimalFormat df = new DecimalFormat("#.0000");
				System.out.println(df.format(xsghj));*/
				DBSql.executeUpdate("update BO_AKL_JGGL set XSGHJ ="+xsghj+",JGMLH ="+jgmlh+",HSCGJ="+hscgj+",WLMC='"+wlmc+"',XH='"+xh+"',GYSMC='"+gysmc+"' where id = "+id);
			}
		}
	}
	
	/**
	 * 回填物料名称和型号
	 * @param vector
	 * @param bindid
	 */
	public void DataFillBack(Vector vector, int bindid){
		if(vector != null){
			for(int i=0; i<vector.size(); i++){
				Hashtable rec = (Hashtable)vector.get(i);
				int id = Integer.parseInt(rec.get("ID").toString());
				String wlbh = rec.get("wlbh").toString();
				String sql = "SELECT * FROM BO_AKL_WLXX WHERE WLBH='"+wlbh+"'";
				String wlmc = DBSql.getString(sql, "WLMC");
				String xh = DBSql.getString(sql, "XH");
				String sql2 = "UPDATE BO_AKL_KH_JGGL_S SET WLMC='"+wlmc+"',XH='"+xh+"' WHERE ID ="+id;
				DBSql.executeUpdate(sql2);
			}
		}
	}
}
