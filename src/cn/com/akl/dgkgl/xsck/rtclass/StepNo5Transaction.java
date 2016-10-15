package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo5Transaction extends WorkFlowStepRTClassA{

	private static final String queryCKDXX = "SELECT CKDH,SHDZ1,KHMC,LXRX1,LXRDH1,KHBH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";

	public StepNo5Transaction() {
		super();
	}

	public StepNo5Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("填充签收单");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		if(!th){
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet reset = null;
			PreparedStatement ckdPs = null;
			ResultSet ckdReset = null;
			Statement stat = null;
			try{
				conn = DBSql.open();
				conn.setAutoCommit(false);
				String xsdh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);

				stat = conn.createStatement();
				String sqlp = "Delete from BO_AKL_QSD_P where bindid ="+bindid;//删除签收单头内容
				stat.executeUpdate(sqlp);
				String sqls = "Delete from BO_AKL_QSD_S where bindid ="+bindid;//删除签收单身内容
				stat.executeUpdate(sqls);

				Hashtable<String, String> qsData = new Hashtable<String, String>();
				Vector<Hashtable<String, String>> qsDatas = new Vector<Hashtable<String,String>>();
				String KHBH = ""; 
				// 出库单填充签收单
				try{
					ckdPs = conn.prepareStatement(queryCKDXX);
					ckdReset = DAOUtil.executeFillArgsAndQuery(conn, ckdPs, bindid);
					if(ckdReset.next()){
						//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
						String sql = "select BM from BO_AKL_YD_P where bindid = "+bindid;
						String BM = DBSql.getString(conn, sql, "BM");
						sql = "SELECT KHCGDH FROM BO_AKL_DGXS_P WHERE XSDDID= '"+xsdh+"'";
						String KHCGDH = DBSql.getString(conn, sql, "KHCGDH");
						sql = "SELECT JDCGDH FROM BO_AKL_CK_YY_S WHERE bindid="+bindid;
						KHCGDH = DBSql.getString(conn, sql, "JDCGDH");
						KHBH = ckdReset.getString("KHBH");
						qsData.put("KHCGDH", KHCGDH);
						qsData.put("BM", BM);
						qsData.put("CKDH", ckdReset.getString("CKDH"));
						qsData.put("SHDZ", ckdReset.getString("SHDZ1"));
						qsData.put("SHDW", ckdReset.getString("KHMC"));
						qsData.put("SHFZR", ckdReset.getString("LXRX1"));
						qsData.put("SHFZRDH", ckdReset.getString("LXRDH1"));
					}
				} finally {
					DBSql.close(ckdPs, ckdReset);
				}

				ps = conn.prepareStatement("SELECT WLMC,XH,WLBH,SFSL,DW FROM BO_BO_AKL_DGCK_S WHERE BINDID=?");
				reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);

				while(reset.next()){
					Hashtable<String, String> qsDatas_s = new Hashtable<String, String>();
					String wlh = reset.getString("WLBH");
					String khspsku = DAOUtil.getStringOrNull(conn, "SELECT KHSPSKU FROM BO_AKL_KHSPBMGL WHERE YKSPSKU=? and KHBM=?", wlh, KHBH);
					khspsku=khspsku==null?"":khspsku;
					qsDatas_s.put("KHSPBH", khspsku);
					qsDatas_s.put("CPMC", reset.getString("WLMC"));
					qsDatas_s.put("XH", reset.getString("XH"));
					qsDatas_s.put("WLH", wlh);
					qsDatas_s.put("YSSL", String.valueOf(reset.getInt("SFSL")));
					qsDatas_s.put("SSSL", String.valueOf(reset.getInt("SFSL")));
					qsDatas_s.put("DW", reset.getString("DW"));
					qsDatas.add(qsDatas_s);
				}

				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_P", qsData, bindid, getUserContext().getUID());
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_S", qsDatas, bindid, getUserContext().getUID());

				conn.commit();
			} catch(Exception e){
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
				return true;
			} finally {
				DBSql.close(conn, ps, reset);
			}
		}
		return true;
	}

}
