package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("流程流转后事件: 更新库存");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		Connection conn = null;
		
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
			if(th)
				return true;
			
			String dh = DAOUtil.getString(conn, "SELECT JHHDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			
			
			// 1、解锁库存
			DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_KC_SPPCSK WHERE DDH=?", dh);
			
			// 2、更新库存明细 根据相应的单据来源
			//TODO 借货出库 获取相应的单身信息，还货出库获取相应的单身信息
			//获得单头
			Hashtable<String, String> ckdtht = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
			int jhhdb = Integer.parseInt(ckdtht.get("JHHDB"));
			//0:借货出库|1:还货出库
			/**Vector<Hashtable<String, String>> wlxxs = null;
			if(0==jhhdb){
				// 获取订单物料的信息
				wlxxs = BOInstanceAPI.getInstance().getBODatasBySQL("BO_AKL_JHDD_BODY", "WHERE JHDH='"+dh+"'");
			}else if(1==jhhdb){
				// 获取订单物料的信息
				wlxxs = BOInstanceAPI.getInstance().getBODatasBySQL("BO_AKL_HHDD_BODY", "WHERE JHDH='"+dh+"'");
			}else{
				return false;
			}*/
			Vector<Hashtable<String, String>> ckDatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CKD_BODY", bindid);
			for(Hashtable<String, String> ht:ckDatas){
				// 将库存减掉
				int rei = DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_KC_KCHZ_P SET CKSL= ISNULL(CKSL, 0) + ?,PCSL = ISNULL(PCSL, 0) - ? WHERE WLBH = ? AND PCH = ?",ht.get("SJSL"),ht.get("SJSL") ,ht.get("WLH"),ht.get("PC"));
				int rej = DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_KC_KCMX_S SET KWSL=KWSL - ? WHERE WLBH = ? AND PCH = ? AND HWDM = ?",ht.get("SJSL") ,ht.get("WLH"),ht.get("PC"),ht.get("KWBH"));
				if(rei != 1 || rej !=1){
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "库存执行错误！",true);
					throw new Exception("库存执行错误！");
					//return false;
				}
			}
			//更新单据状态
			if(0==jhhdb){
				DAOUtil.executeUpdate(conn, "update BO_AKL_JHDD_HEAD set JHZT = 3,CKSJ = GETDATE() where JHDH = ? ",dh);
			}else if(1==jhhdb){
				DAOUtil.executeUpdate(conn, "update BO_AKL_HHDD_HEAD set ZT = 3,CRKSJ = GETDATE() where HHDH = ? ",dh);
			}
			conn.commit();
			return true;
		} catch(Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
