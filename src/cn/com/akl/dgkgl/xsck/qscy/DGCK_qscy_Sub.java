package cn.com.akl.dgkgl.xsck.qscy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SubWorkflowEventClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCK_qscy_Sub extends SubWorkflowEventClassA{
	//查询签收数量录入单身
	private static final String QUERY_DGQSD_S = "select a.WLH, a.YSSL, a.SSSL, a.XH, b.TJ from BO_AKL_DGCK_QSSL_S a left join BO_AKL_WLXX b on a.WLH = b.WLBH where a.bindid=? AND a.YSSL<>a.SSSL";
	
	public DGCK_qscy_Sub(UserContext uc){
		super(uc);
		setProvider("liusong");
		setVersion("1.0.0");
		setDescription("子流程启动后向子流程表单插入数据");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_PARENT_PROCESS_INSTANCE_ID).toInt();// 父类bindid
		Hashtable hbindid = getParameter(PARAMETER_SUB_PROCESS_INSTANCE_ID).toHashtable();
		int subBindid =  Integer.parseInt(hbindid.get(0).toString());// 子类bindid
		Connection conn = null;
		Statement stat = null;
		ResultSet rs = null;
		PreparedStatement qsbodyPs = null;
		ResultSet qsbobyReset = null;
		String sql = null;
		try {
			conn = DBSql.open();
			conn.setAutoCommit(false);
			stat = conn.createStatement();
			Hashtable<String, String> hashtablep = new Hashtable<String, String>();
			// 查询差异单的出库单号
			sql = "select top 1 CKDH from BO_AKL_DGCK_QSSL_S where bindid="+bindid;
			String CKDH = DBSql.getString(sql, "CKDH");// 出库单号
			// 查询运单单头获取签收差异单单头信息
			sql = "SELECT SHDW, SHR, SHRDH, SFZ, DZ, CYS, LXR, LXFS, YSFS, DDH, CKDH FROM BO_AKL_YD_P WHERE CKDH ='"+CKDH+"'";
			rs = stat.executeQuery(sql);
			if(rs.next()){
				CKDH = rs.getString("CKDH") == null?"":rs.getString("CKDH");// 出库单号
				String DDH = rs.getString("DDH") == null?"":rs.getString("DDH");// 销售订单号
				String SHDW = rs.getString("SHDW") == null?"":rs.getString("SHDW");// 收货单位
				String SHR = rs.getString("SHR") == null?"":rs.getString("SHR");// 收货负责人
				String SHRLXFS = rs.getString("SHRDH") == null?"":rs.getString("SHRDH");// 收货人电话
				String SFZ = rs.getString("SFZ") == null?"":rs.getString("SFZ");// 始发站
				String MDZ = rs.getString("DZ") == null?"":rs.getString("DZ");// 目的站
				String CYS = rs.getString("CYS") == null?"":rs.getString("CYS");// 承运商
				String CYSLXR = rs.getString("LXR") == null?"":rs.getString("LXR");// 承运商联系人
				String CYSLXFS = rs.getString("LXFS") == null?"":rs.getString("LXFS");// 承运商联系方式
				String YSFS = rs.getString("YSFS") == null?"":rs.getString("YSFS");// 运输方式
				
				hashtablep.put("CKDH", CKDH);
				hashtablep.put("DDH", DDH);
				hashtablep.put("SHDW", SHDW);
				hashtablep.put("SHR", SHR);
				hashtablep.put("SHRLXFS", SHRLXFS);
				hashtablep.put("SFZ", SFZ);
				hashtablep.put("MDZ", MDZ);
				hashtablep.put("CYS", CYS);
				hashtablep.put("CYSLXR", CYSLXR);
				hashtablep.put("CYSLXFS", CYSLXFS);
				hashtablep.put("YSFS", YSFS);
			}
			else{
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "此出库单无运单信息", true);
				return false;
			}
			// 从签收单查询发货日期
			sql = "select SHRQ from BO_AKL_QSD_P where CKDH = '"+CKDH+"'";
			String FHRQ = DBSql.getString(sql, "SHRQ")==null?"":DBSql.getString(sql, "SHRQ");// 发货日期
			hashtablep.put("FHRQ", FHRQ);
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_DGCK_QSCY_P", subBindid);
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCK_QSCY_P", hashtablep, subBindid, getUserContext().getUID());
			
			//插入单身信息
			qsbodyPs = conn.prepareStatement(QUERY_DGQSD_S);
			qsbobyReset = DAOUtil.executeFillArgsAndQuery(conn, qsbodyPs, bindid);
			while(qsbobyReset.next()){
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("CYXH", PrintUtil.parseNull(qsbobyReset.getString("XH")));
				hashtable.put("WLH", PrintUtil.parseNull(qsbobyReset.getString("WLH")));
				hashtable.put("SL", String.valueOf(qsbobyReset.getInt("YSSL")));
				hashtable.put("QSSL", String.valueOf(qsbobyReset.getInt("SSSL")));
				hashtable.put("TJ", String.valueOf(qsbobyReset.getInt("TJ")));
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCK_QSCY_S", hashtable, subBindid, getUserContext().getUID());
			}
			
			
			conn.commit();
		} catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch(Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，无法出库，请检查控制台", true);
			return false;
		} finally {
			DBSql.close(conn, stat, rs);
			DBSql.close(null, qsbodyPs, qsbobyReset);
		}
		
		return true;
	}

}
