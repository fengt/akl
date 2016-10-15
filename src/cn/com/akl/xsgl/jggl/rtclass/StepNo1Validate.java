package cn.com.akl.xsgl.jggl.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public static final String QUERY_WLBH = "SELECT COUNT(*)AS m FROM BO_AKL_WLXX WHERE WLBH=?";
	public static final String QUERY_GYSBH = "SELECT COUNT(*)AS n FROM BO_AKL_GYS_P WHERE GYSBH=?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo1Validate() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("校验销售价格导入的物料编号和供应商编号是否正确。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_JGGL", bindid);
		conn = DBSql.open();
		
		try {
			if(body != null){
				for (int i = 0; i < body.size(); i++) {
					Hashtable<String, String> rec = body.get(i);
					String wlbh = rec.get("WLBH").toString();//物料编号
					String gysbh = rec.get("GYSBH").toString();//供应商编号
					int m = DAOUtil.getInt(conn, QUERY_WLBH, wlbh);
					int n = DAOUtil.getInt(conn, QUERY_GYSBH, gysbh);
					if(m<=0){
						MessageQueue.getInstance().putMessage(uc.getUID(), "物料编号【"+wlbh+"】不存在，请核查！");
						return false;
					}else if(n<=0){
						MessageQueue.getInstance().putMessage(uc.getUID(), "供应商编号【"+gysbh+"】不存在，请核查！");
						return false;
					}
				}
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "价格信息为空，请重新导入！");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DBSql.close(conn, null, null);
		}
		return true;
	}

}
