package cn.com.akl.shgl.fjjh.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.fjjh.biz.DBBiz;
import cn.com.akl.shgl.fjjh.biz.FJJHBiz;
import cn.com.akl.shgl.fjjh.cnt.FJJHCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo9SubAfterLoad extends SubWorkflowEventClassA {

	private Connection conn = null;
	private UserContext uc;
	private FJJHBiz fjjhBiz = new FJJHBiz();
	private DBBiz dbBiz = new DBBiz();
	public StepNo9SubAfterLoad() {
	}

	public StepNo9SubAfterLoad(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("加载数据到调拨单。");
	}

	@Override
	public boolean execute() {
		int parent_bindid = this.getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();//父流程bindid
		Hashtable process = getParameter(this.PARAMETER_SUB_PROCESS_INSTANCE_ID).toHashtable();
		String processid = process.get(0) == null?"":process.get(0).toString();
		String uid = uc.getUID();
		if(processid.equals("")){
			return true;
		}
		int sub_bindid = Integer.parseInt(processid);//子流程bindid
		
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**加载数据到调拨单.*/
			String jcck = DAOUtil.getStringOrNull(conn, FJJHCnt.QUERY_FJJH_P_JCCKBM, parent_bindid);//检测库
			Vector<Hashtable<String, String>> vector = fjjhBiz.queryByWlbh(conn, parent_bindid, jcck);
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DB_HZ_S", vector, sub_bindid, uc.getUID());//加载子表数据
			dbBiz.insertDBHead(conn, parent_bindid, sub_bindid, uid, FJJHCnt.direction1);//加载主表数据
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		} catch (Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现异常，请检查控制台");
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}
	

}
