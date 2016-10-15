package cn.com.akl.zsj.kh.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.u8.senddata.SendCustomerData;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class No1TransactionAfter extends WorkFlowStepRTClassA {

	private Connection conn = null;
	@SuppressWarnings("unused")
	private UserContext uc;

	public No1TransactionAfter(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("wjj");
		setDescription("½Ó¿Ú´«Êä");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector<Hashtable<String, String>> head = BOInstanceAPI.getInstance()
				.getBODatas("BO_AKL_KH_P", bindid);

		for (Hashtable<String, String> ht : head) {
			try {
				if ("01065".equals(ht.get("SSKHBH"))) {
					SendCustomerData scd = new SendCustomerData();
					scd.sendCustomer(ht);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}

}
