package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo6Transaction extends WorkFlowStepRTClassA{
	
	public StepNo6Transaction() {
		super();
	}

	public StepNo6Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("填充签收单");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
			Vector<Hashtable<String, String>> boDatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CKD_BODY", bindid);
			Hashtable<String, String> qsData = new Hashtable<String, String>();
			//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
			qsData.put("CKDH", boData.get("CKDH"));
			qsData.put("KHCGDH", boData.get("KHCGDH"));
			//qsData.put("TYDW", boData.get("SHKF"));
			//qsData.put("BM", boData.get("SHDZ"));
			qsData.put("SHDW", boData.get("KHMC"));
			qsData.put("SHFZR", boData.get("KFLXR"));
			qsData.put("SHFZRDH", boData.get("CKLXRDH"));
			//qsData.put("YSSLHJ", boData.get("YSSLHJ"));
			
			Vector<Hashtable<String, String>> qsDatas = new Vector<Hashtable<String,String>>();
			// 物料、型号、应收、产品名称、客户产品编号（自行关联）
			for (Hashtable<String, String> hashtable : boDatas) {
				Hashtable<String, String> qsDatas_s = new Hashtable<String, String>();
				//qsDatas_s.put("KHSPBH", hashtable.get(""));
				qsDatas_s.put("CPMC", hashtable.get("WLMC"));
				qsDatas_s.put("XH", hashtable.get("XH"));
				qsDatas_s.put("WLH", hashtable.get("WLH"));
				qsDatas_s.put("YSSL", hashtable.get("SJSL"));
				//签收数量默认出库数量
				qsDatas_s.put("SSSL", hashtable.get("SJSL"));
				qsDatas_s.put("DW", hashtable.get("JLDW"));
				qsDatas.add(qsDatas_s);
			}
			
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_P", qsData, bindid, getUserContext().getUID());
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_S", qsDatas, bindid, getUserContext().getUID());
			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
