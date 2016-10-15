package cn.com.akl.posbg.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA{
	//查询pos编号对应的单身信息
	private static final String queryPOSDH= "select * from BO_AKL_WXB_XS_POS_BODY where POSBH = ?";
	public StepNo1BeforeSave(UserContext uc){
		super(uc);
		setVersion("1.0.0");
		setDescription("保存前事件：选择pos编号后自动引入单身数据");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		Integer bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		String uid = getUserContext().getUID();
		Hashtable<String,String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		Hashtable<String,String> data = BOInstanceAPI.getInstance().getBOData("BO_AKL_POS_BG_P", bindid);//数据库读取单头信息


		if(tablename.equals("BO_AKL_POS_BG_P")){
			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();
				String posbh = hashtable.get("POSBH");
				String posbhdata = data.get("POSBH");
				data.get("");
				if(!posbh.equals(posbhdata)){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_POS_BG_S", bindid);
					query_insertDS(conn, bindid, uid, posbh);
				}
				conn.commit();
			}catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	public void query_insertDS(Connection conn, Integer bindid, String uid, String posbh) throws Exception{
		PreparedStatement ps = conn.prepareStatement(queryPOSDH);
		ResultSet rs = DAOUtil.executeFillArgsAndQuery(conn, ps, posbh);
		try{
			while(rs.next()){
				posbh = parseStringorNull(posbh);// POS编号
				String tpm = parseStringorNull(rs.getString("TPM"));// 供应商POS编号(TPM号)
				String wlbh = parseStringorNull(rs.getString("WLBH"));// 物料编号
				String wlmc = parseStringorNull(rs.getString("WLMC"));// 物料名称
				String xh = parseStringorNull(rs.getString("XH"));// 型号
				String gg = parseStringorNull(rs.getString("GG"));// 规格
				Integer possl = rs.getInt("POSSL");// POS数量
				String currency = parseStringorNull(rs.getString("CURRENCY"));// 币种
				Double posdj = rs.getDouble("POSDJ");// POS单价
				Double yjg = rs.getDouble("YJG");// 原价格
				Double zchjjbhs = rs.getDouble("ZCHJJBHS");// 支持后净价(未税)
				Double zchjjhs = rs.getDouble("ZCHJJHS");// 支持后净价(含税)
				Double sl = rs.getDouble("SL");// 税率
				String zt = parseStringorNull(rs.getString("ZT"));// 状态
				Integer ysysl = rs.getInt("YSYSL");// 已使用数量
				
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("POSBH",posbh);
				hashtable.put("TPM",tpm);
				hashtable.put("WLBH",wlbh);
				hashtable.put("WLMC",wlmc);
				hashtable.put("XH",xh);
				hashtable.put("GG",gg);
				hashtable.put("POSSL",String.valueOf(possl));
				hashtable.put("CURRENCY",currency);
				hashtable.put("POSDJ",String.valueOf(posdj));
				hashtable.put("YJG",String.valueOf(yjg));
				hashtable.put("ZCHJJBHS",String.valueOf(zchjjbhs));
				hashtable.put("ZCHJJHS",String.valueOf(zchjjhs));
				hashtable.put("SL",String.valueOf(sl));
				hashtable.put("ZT",gg);
				hashtable.put("YSYSL",String.valueOf(ysysl));
				
				// 原POS单价
				hashtable.put("YPOSDJ",String.valueOf(posdj));
				// 原已使用数量
				hashtable.put("YYSYSL",String.valueOf(ysysl));
				// 原数量
				hashtable.put("YPOSSL", String.valueOf(possl));
				
				// 插入数据
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_BG_S", hashtable, bindid, uid);
				
				
			}
		}finally{
			DBSql.close(null, ps, rs);
		}
	}
	public String parseStringorNull(String parse){
		return parse==null?"":parse;
	}
}
