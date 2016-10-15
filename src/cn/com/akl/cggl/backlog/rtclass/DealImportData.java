package cn.com.akl.cggl.backlog.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.cggl.cgdd.constant.CgddConstant;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DealImportData extends WorkFlowStepRTClassA {

	public DealImportData() {
	}

	public DealImportData(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("通过BACKLOG临时表导入的数据，更新或插入backlog");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CGDD_BACKLOG_LS", bindid);
		
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String n1 = rec.get("PONUMBER").toString();//n1
				String n2 = rec.get("PODT").toString();
				String n3 = rec.get("SALESORDER").toString();
				String n4 = rec.get("LNITEM").toString();//n2
				String n5 = rec.get("SOLDTOPARTY").toString();
				String n6 = rec.get("SOLDTONAME").toString();
				String n7 = rec.get("MATERIAL").toString();
				String n8 = rec.get("CONF").toString();
				int n9 = Integer.parseInt(rec.get("ORDEREDQUANTITY").toString());
				double n10 = Double.parseDouble(rec.get("UNITPRICE").toString());
				String n11 = rec.get("REQUIREDDELIVERYDATE").toString();
				String n12 = rec.get("PINDPGIDT").toString();
				String n13 = rec.get("REVPGIDT").toString();
				String n14 = rec.get("LINESTATUSES").toString();
				String n15 = rec.get("COUNTRY").toString();
				double n16 = Double.parseDouble(rec.get("DAYSOF").toString());
				
				
				Hashtable store = new Hashtable();
				store.put("PONUMBER", n1);
				store.put("PODT", n2);
				store.put("SALESORDER", n3);
				store.put("LNITEM", n4);
				store.put("SOLDTOPARTY", n5);
				store.put("SOLDTONAME", n6);
				store.put("MATERIAL", n7);
				store.put("CONF", n8);
				store.put("ORDEREDQUANTITY", n9);
				store.put("UNITPRICE", n10);
				store.put("REQUIREDDELIVERYDATE", n11);
				store.put("PINDPGIDT", n12);
				store.put("REVPGIDT", n13);
				store.put("LINESTATUSES", n14);
				store.put("COUNTRY", n15);
				store.put("DAYSOF", n16);
				
				
				String str = "select count(*) n from BO_AKL_CGDD_BACKLOG where PONUMBER='"+n1+"' and LNITEM='"+n4+"'";
				int n = DBSql.getInt(str, "n");
				if(n <= 0){
					try {
						BOInstanceAPI.getInstance().createBOData("BO_AKL_CGDD_BACKLOG", store, bindid, getUserContext().getUID());
					} catch (AWSSDKException e) {
						e.printStackTrace();
					}
				}else{
					String str2 = "update BO_AKL_CGDD_BACKLOG set ORDEREDQUANTITY="+n9+",REQUIREDDELIVERYDATE='"+n11+"', PINDPGIDT='"+n12+"', REVPGIDT='"+n13+"' where PONUMBER='"+n1+"' and LNITEM='"+n4+"'";
					int cnt  = DBSql.executeUpdate(str2);
					System.out.println(cnt);
				}
				
				
			}
		}
		return false;
	}
	
	/*public int judgeIsExists(String ponumber, String material){
		String sql = "select count(*) n from " + CgddConstant.tableName5 + " where ponumber = '"+ponumber+"' and material = '"+material+"'";
		int cnt = DBSql.executeUpdate(sql);
		return cnt;
	}*/
	
	

}
