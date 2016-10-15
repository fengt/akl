package cn.com.akl.jhhgl.ck.rtclass;
/**
 * 废弃
 * 2014/10/09 15:53 
 * hzy
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1FormAfterSave_copy extends WorkFlowStepRTClassA{

	public StepNo1FormAfterSave_copy() {
		super();
	}

	public StepNo1FormAfterSave_copy(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("借还货申请单身保存后事件: 重构单身信息");
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		Connection conn = null;
		try {
			conn = DBSql.open();
			//获取出库单身数据
			Vector<Hashtable<String, String>> ckds = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CKD_BODY", bindid);
			//判断如果库位信息不为null return false;
			for(Hashtable<String, String> ht : ckds){
				if(!ht.get("KWBH").isEmpty()){
					return false;
				}
			}
			//获取库位信息数据
			Vector<Hashtable<String, String>> ckdsDatas = new Vector<Hashtable<String,String>>();
			for(Hashtable<String, String> ht : ckds){
				Vector<Hashtable<String, String>> kws = null;
					kws = BOInstanceAPI.getInstance().getBODatasBySQL("BO_AKL_KC_KCMX_S", "where pch = "+ht.get("PCH")+"order by KWSL");
					int sjsl = Integer.parseInt(ht.get("SJSL").toString());
					int n = sjsl;
					for(int i = 0;i<kws.size();i++){
						// 判断 需要几个库位的货
						if (n - sjsl <= 0) {
							ht.remove("KWBH");
							String dm = kws.get(i).get("CKDM")+kws.get(i).get("QDM")+kws.get(i).get("DDM")+kws.get(i).get("KWDM");
							ht.put("KWBH", dm);
							ht.remove("SJSL");
							ht.put("SJSL",n+"");
							ckdsDatas.add(ht);
							break;
						} else {
							n = n - sjsl;
							ht.remove("KWBH");
							String dm = kws.get(i).get("CKDM")+kws.get(i).get("QDM")+kws.get(i).get("DDM")+kws.get(i).get("KWDM");
							ht.put("KWBH", dm);
							ht.remove("SJSL");
							ht.put("SJSL",kws.get(i).get("KWSL"));
							ckdsDatas.add(ht);
						}
					}
					conn.setAutoCommit(false);
					//先删除，在重新存储
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn,"BO_AKL_CKD_BODY", bindid);
					BOInstanceAPI.getInstance().createBOData(conn,"BO_AKL_CKD_BODY", ckdsDatas, this.getUserContext().getUID());
					conn.commit();
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}finally 
		{
			DBSql.close(conn, null, null);
		}
	}

}
