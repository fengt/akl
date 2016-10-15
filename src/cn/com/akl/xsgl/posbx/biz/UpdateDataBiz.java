package cn.com.akl.xsgl.posbx.biz;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class UpdateDataBiz {
	
	/**
	 * POS更新资金池操作
	 * @param vector
	 * @param bindid
	 */
	public void DataHander(UserContext uc,Vector vector,int bindid){
		String sql ="select * from BO_AKL_POSBX_P where bindid="+bindid;
		String tpm = DBSql.getString(sql, "TPMH");
		int mark = 0;
		if(vector != null){
			for(int i=0; i<vector.size(); i++){
				Hashtable JErecord = (Hashtable)vector.get(i);
				double posprice = Double.parseDouble(JErecord.get("POSPRICE").toString());
				double ysje = Double.parseDouble(JErecord.get("YPPOSZE").toString());
				double ssje = Double.parseDouble(JErecord.get("SPPOSZE").toString());
				//String tpm = JErecord.get("TPMH").toString();
				String wlbh = JErecord.get("WLBH").toString();		
				int sdypsl = Integer.parseInt(JErecord.get("SDYPSL").toString());
				int sqsl = Integer.parseInt(JErecord.get("SQSL").toString());
				
				mark = sdypsl - sqsl;
				if(mark == 0){
					
					 /** 通过TPM和WLBH从供应商费用支持取对应的POSBH和XH*/
					String str1 = "select * FROM BO_AKL_WXB_XS_POS_BODY where TPM='"+tpm+"' and WLBH='"+wlbh+"'";
					String posbh = DBSql.getString(str1, "POSBH");
					String xh = DBSql.getString(str1, "XH");
					String gysbh = DBSql.getString("select GYSBH from BO_AKL_WXB_XS_POS_HEAD where POSBH='"+posbh+"'", "GYSBH");
					
					/** 通过POSBH和XH从销售订单中取对应的FLZCSL总数及其他字段值*/
					String str2 = "select SUM(FLZCSL) AS FLSL from BO_AKL_WXB_XSDD_BODY where POSID='"+posbh+"' and XH='"+xh+"'";
					int flsl = DBSql.getInt(str2, "FLSL");
					String str3 = "select FLZCD from BO_AKL_WXB_XSDD_BODY where POSID='"+posbh+"' and XH='"+xh+"'";
					double flzcd = DBSql.getDouble(str3, "FLZCD");
					
					
					String a = "POS";
					String b = "未抵扣";
					Hashtable record = new Hashtable();
					double bxje = (sqsl - flsl) * (posprice - flzcd);			
					
					record.put("LX", a);
					record.put("TPM", tpm);
					record.put("FABH", posbh);
					
					/*record.put("YSYJE", value);
					record.put("POSJE", value);*/
					
					record.put("BXSQJE", bxje);
					record.put("YSJE", ysje);
					record.put("SSJE", ssje);
					record.put("GYSBM", posbh);
					record.put("ZT", b);
					
					try {
						BOInstanceAPI.getInstance().createBOData("BO_AKL_POS_MXB", record, bindid, uc.getUID());
					} catch (AWSSDKException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
