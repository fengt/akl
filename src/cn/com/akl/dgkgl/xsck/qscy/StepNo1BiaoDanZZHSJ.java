package cn.com.akl.dgkgl.xsck.qscy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BiaoDanZZHSJ extends WorkFlowStepRTClassA{

	//查询签收单身
	private static final String QUERY_DGQSD_S = "select a.WLH, a.YSSL, a.SSSL, a.XH, b.TJ, c.CKDH from BO_AKL_QSD_S a left join BO_AKL_WLXX b on a.WLH = b.WLBH left join BO_AKL_QSD_P c on a.bindid = c.bindid where c.bindid=? AND a.YSSL<>a.SSSL";
	public StepNo1BiaoDanZZHSJ() {
		super();
	}

	public StepNo1BiaoDanZZHSJ(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("表单装载后：插入单身数据");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int parentbindid = getParameter(PARAMETER_PARENT_WORKFLOW_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable ckData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGCK_QSCY_P", bindid);//数据库读取单头信息
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DGCK_QSCY_S", bindid);

		String DDH = ckData.get("DDH") == null?"":ckData.get("DDH").toString();
		String CYDDH = ckData.get("CYDDH") == null?"":ckData.get("CYDDH").toString();
		
		if(CYDDH.equals("")&&v==null&&!DDH.equals("")){
			Connection conn = null;
			Statement stat = null;
			PreparedStatement qsbodyPs = null;
			ResultSet qsbobyReset = null;
			try {
				conn = DAOUtil.openConnectionTransaction();
				stat = conn.createStatement();
				qsbodyPs = conn.prepareStatement(QUERY_DGQSD_S);
				qsbobyReset = DAOUtil.executeFillArgsAndQuery(conn, qsbodyPs, parentbindid);
				String CKDH = "";
				while(qsbobyReset.next()){
					Hashtable<String, String> hashtable = new Hashtable<String, String>();
					hashtable.put("CYXH", PrintUtil.parseNull(qsbobyReset.getString("XH")));
					hashtable.put("WLH", PrintUtil.parseNull(qsbobyReset.getString("WLH")));
					hashtable.put("SL", String.valueOf(qsbobyReset.getInt("YSSL")));
					hashtable.put("QSSL", String.valueOf(qsbobyReset.getInt("SSSL")));
					hashtable.put("TJ", String.valueOf(qsbobyReset.getInt("TJ")));
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCK_QSCY_S", hashtable, bindid, uid);
					CKDH = PrintUtil.parseNull(qsbobyReset.getString("CKDH"));
				}
				String sql = "update BO_AKL_DGCK_QSCY_P set CKDH = '"+CKDH+"' where bindid="+bindid;
				stat.executeUpdate(sql);
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
				DBSql.close(conn, qsbodyPs, qsbobyReset);
				DBSql.close(null, stat, null);
			}

		}
		return true;
	}

}
