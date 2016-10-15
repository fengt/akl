package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("填充运单");
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();
		boolean sf = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "第三方物流");
		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		if(!th){
			Connection conn = null;
			Statement stat = null;
			String sql = null;
			try{
				conn = DAOUtil.openConnectionTransaction();			
				// 4-5 出库单+预约单填充运单
				// 4-6 出库单+预约单填充运单（物流单）
				stat = conn.createStatement();
				sql = "SELECT JDCGDH FROM BO_AKL_CK_YY_S WHERE bindid="+bindid;
				String KHCGDH = DBSql.getString(conn, sql, "JDCGDH");
				sql = "update BO_AKL_QSD_P set KHCGDH = '"+KHCGDH+"' where bindid ="+bindid;
				stat.executeUpdate(sql);
				fillYD(conn, bindid, sf);
				
				conn.commit();
				return true;
			} catch(Exception e){
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台", true);
				return true;
			} finally {
				DBSql.close(conn, stat, null);
			}
		}
		else
			return true;
	}

	/**
	 * 预约单+出库单填充运单
	 * @param conn
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	private void fillYD(Connection conn, int bindid, boolean sf) throws SQLException, AWSSDKException{
		Statement stat = conn.createStatement();
		stat = conn.createStatement();
		String ckdh = DAOUtil.getString(conn, "SELECT CKDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);

//		String sqlp = "Delete from BO_AKL_YD_P where bindid ="+bindid;//删除运单单头内容
//		stat.executeUpdate(sqlp);

		Hashtable<String, String> qsData = new Hashtable<String, String>();
		//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
		String ckMessage = DAOUtil.getStringOrNull(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
//		String yydMessage = DAOUtil.getStringOrNull(conn, "SELECT CYS+'{}'+CYSDH+'{}'+CYSDZ+'{}'+CYSLXR FROM BO_BO_AKL_CK_YY_P WHERE BINDID=?", bindid);


		qsData.put("DDH",ckMessage);

//		if(yydMessage!=null){
//			String[] yydSplit = yydMessage.split("\\{\\}");
//			if(yydSplit.length == 4){
//				qsData.put("CYS", yydSplit[0]);
//				qsData.put("LXR", yydSplit[3]);
//				qsData.put("LXFS", yydSplit[1]);
//			}
//		}
		String sql = "select HZBH from BO_AKL_DGXS_P where XSDDID = '"+ckMessage+"'";
		String hzbh = DBSql.getString(conn, sql, "HZBH");
		qsData.put("HZBM", hzbh);
		sql = "select a.DDH, sum(b.TJ*a.SFSL) as TJ,sum(b.ZL*a.SFSL) ZL, sum(a.SFSL) as SL from BO_BO_AKL_DGCK_S a, BO_AKL_WLXX b where a.WLBH = b.WLBH and a.bindid = "+bindid+" group by a.DDH";

		ResultSet rs = stat.executeQuery(sql);
		if(rs.next()) {
			qsData.put("CKDH", ckdh==null?"":ckdh);
			qsData.put("TJ", rs.getString(2)==null?"":rs.getString(2));
			qsData.put("ZL", rs.getString(3)==null?"":rs.getString(3));
			qsData.put("SL", String.valueOf(rs.getInt(4))==null?"":String.valueOf(rs.getInt(4)));
		}
		sql = "select KHMC, LXRX1, BM, LXRDH1, SHDZ1, KHBH, YFJSFS, YSFS, FHRQ from BO_BO_AKL_DGCK_P where bindid = "+bindid;
		rs = stat.executeQuery(sql);
		String JSFS = null;
		if(rs.next()){
			qsData.put("KHMC", rs.getString(1)==null?"":rs.getString(1));
			qsData.put("KHBH",rs.getString(6)==null?"":rs.getString(6));
			sql = "select LBID from BO_AKL_KH_P where KHID = '"+rs.getString(6)+"'";
			String lbid = DBSql.getString(conn, sql, "LBID");
			qsData.put("KHLX", lbid);
			qsData.put("SHR", rs.getString(2)==null?"":rs.getString(2));
			qsData.put("BM", rs.getString(3)==null?"":rs.getString(3));
			qsData.put("SHRDH", rs.getString(4)==null?"":rs.getString(4));
			qsData.put("SHDW", rs.getString(5)==null?"":rs.getString(5));
			qsData.put("YSFS", rs.getString(8)==null?"":rs.getString(8));
			qsData.put("RQ", rs.getString(9)==null?"":rs.getString(9));
			JSFS = rs.getString(7)==null?"":rs.getString(7);
		}
		// 物料、型号、应收、产品名称、客户产品编号（自行关联）
		
		sql = "select CYS, CYSDH, CYSLXR from BO_BO_AKL_CK_YY_P WHERE bindid ="+bindid;
		rs = stat.executeQuery(sql);
		if(rs.next()){
			qsData.put("CYS", rs.getString(1)==null?"":rs.getString(1));
			qsData.put("LXR", rs.getString(3)==null?"":rs.getString(3));
			qsData.put("LXFS", rs.getString(2)==null?"":rs.getString(2));
		}
		FillBiz fillBiz = new FillBiz();
		fillBiz.insertOrUpdateBOData(conn, bindid, getUserContext().getUID(), "BO_AKL_YD_P", qsData);
//		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YD_P", qsData, bindid, getUserContext().getUID());
		//填充委外单身
		Hashtable<String, String> qsDatas_s = new Hashtable<String, String>();
		if(sf){
			qsDatas_s.put("JSFS", JSFS);
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_YDWW_S", bindid);
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YDWW_S", qsDatas_s, bindid, getUserContext().getUID());
		}
	}

}
