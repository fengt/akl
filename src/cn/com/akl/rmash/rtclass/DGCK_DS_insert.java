package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCK_DS_insert extends WorkFlowStepRTClassA {
	public DGCK_DS_insert() {
		super();
	}

	public DGCK_DS_insert(UserContext arg0) {
		super(arg0);
		setVersion("代管出库1.0.0");
		setProvider("刘松");
		setDescription("表单保存后事件：拆分库存");

		/**
		 * 存在问题：如果此流程拆分过后，未点办理，另一个出库流程将此库存拿走，就存在了冲突。
		 */
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		if("BO_BO_AKL_DGCK_P".equals(tablename)){
			Connection conn = null;
			Statement state =null;
			try{
				conn = DBSql.open();
				state = conn.createStatement();
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_BO_AKL_DGCK_S", bindid); 
				queryAllWlxx(conn,state, bindid, uid);
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "拆分出现问题，请联系管理员！", true);
			} finally {
				DBSql.close(conn, state, null);
			}
		}
		return false;
	}


	/**
	 * 查询单身的所有物料，并且从仓库抓取物料
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void queryAllWlxx(Connection conn,Statement state, int bindid, String uid) throws SQLException{

		String sql =null;
		int i = 1;
		String XLH = null;
		Hashtable h = null;

		ResultSet rsxs = null;
		ResultSet rshz = null;
		ResultSet rsmx = null;

		int KCSL = 0;
		int XSSL = 0;
		int YFSL = 0;

		int a = 0;
		int b = 0; 
		String CKMC = null;
		String WLBH =null;

		List<Hashtable> lxs = new ArrayList<Hashtable>();
		Hashtable hxs = null;
		List<Hashtable> lhz = new ArrayList<Hashtable>();
		Hashtable hhz = null;

		String xsddh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
		// 查询销售订单身所有物料信息销售数量
		sql="SELECT a.WLBH, a.XSSL, a.CKMC, a.WLMC FROM BO_AKL_DGXS_S a, BO_AKL_DGXS_P b WHERE a.DDID='"+xsddh+"' AND b.ZT = '已签' AND a.DDID = b.XSDDID";
		rsxs = state.executeQuery(sql);
		if(rsxs!=null){
			while(rsxs.next()){
				hxs = new Hashtable();
				hxs.put("WLBH", rsxs.getString(1));
				hxs.put("XSSL", rsxs.getInt(2));
				hxs.put("CKMC", rsxs.getString(3));
				hxs.put("WLMC", rsxs.getString(4));
				lxs.add(hxs);
			}
		}
		for(Hashtable xsl : lxs){
			XSSL = Integer.parseInt(xsl.get("XSSL").toString());
			sql = "select sum(KWSL) KWSL from BO_AKL_DGKC_KCMX_S where CKMC = '"+xsl.get("CKMC")+"' and WLBH = '"+xsl.get("WLBH")+"'";
			KCSL = DBSql.getInt(conn, sql, "KWSL");
			if(KCSL<XSSL){
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "用于发货的"+xsl.get("WLMC")+"商品本库存数量不足,差"+(XSSL-KCSL)+",请其他库房发剩于货物！",true);
			}
			// 查询代管库明细表此物料号的批次信息
			sql="SELECT SUM(KWSL) as KWSL, PCH FROM BO_AKL_DGKC_KCMX_S WHERE WLBH= '"+xsl.get("WLBH")+"' and CKMC = '"+xsl.get("CKMC")+"' group by PCH order by PCH";
			rshz = state.executeQuery(sql);
			if(rshz!=null){
				while(rshz.next()){
					hhz = new Hashtable();
					hhz.put("KWSL", rshz.getInt(1));
					hhz.put("PCH", rshz.getString(2));
					lhz.add(hhz);
				}
			}
			for(Hashtable hzl : lhz){
				if(XSSL<=Integer.parseInt(hzl.get("KWSL").toString())){
					// 查询仓库的可用物料信息
					sql="SELECT WLMC, WLBH, KWSL, HWDM, CKDM, CKMC, QDM, DDM, KWDM, XH, GG, PCH, JLDW  FROM BO_AKL_DGKC_KCMX_S WHERE PCH = '"+hzl.get("PCH")+"' and CKMC = '"+xsl.get("CKMC")+"' and WLBH = '"+xsl.get("WLBH")+"'";
					rsmx = state.executeQuery(sql);
					if(rsmx!=null){
						while(rsmx.next()){
							if(rsmx.getInt("KWSL")>0){
								h = new Hashtable();
								h.put("HH", i);
								h.put("WLBH", rsmx.getString("WLBH"));
								h.put("WLMC", rsmx.getString("WLMC"));
								h.put("GG", rsmx.getString("GG"));
								h.put("XH", rsmx.getString("XH"));
								h.put("PCH", rsmx.getString("PCH"));
								h.put("CKDM", rsmx.getString("CKDM"));
								h.put("CKMC", rsmx.getString("CKMC"));
								h.put("DDH", xsddh);
								if(XSSL<=rsmx.getInt("KWSL")){
									h.put("QDM", rsmx.getString("QDM"));
									h.put("DDM", rsmx.getString("DDM"));
									h.put("KWDM", rsmx.getString("KWDM"));
									h.put("HWDM", rsmx.getString("HWDM"));
									//h.put("DW", rsmx.getString("JLDW"));
									h.put("YFSL", XSSL);
									h.put("SFSL", XSSL);
									try {
										BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", h, bindid, uid);
									} catch (AWSSDKException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									a = 1;
									break;
								}
								else{
									h.put("QDM", rsmx.getString("QDM"));
									h.put("DDM", rsmx.getString("DDM"));
									h.put("KWDM", rsmx.getString("KWDM"));
									h.put("HWDM", rsmx.getString("HWDM"));
									//h.put("DW", rsmx.getString("JLDW"));
									h.put("YFSL", rsmx.getInt("KWSL"));
									h.put("SFSL", rsmx.getInt("KWSL"));
									try {
										BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", h, bindid, uid);
									} catch (AWSSDKException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									XSSL -= rsmx.getInt("KWSL");
									i++;
								}
							}
						}

					}
					if(a == 1){
						break;
					}
				}						
				else{
					// 查询仓库的可用物料信息
					sql="SELECT WLMC, WLBH, KWSL, HWDM, CKDM, CKMC, QDM, DDM, KWDM, XH, GG, PCH, JLDW  FROM BO_AKL_DGKC_KCMX_S WHERE PCH = '"+hzl.get("PCH")+"' and CKMC = '"+xsl.get("CKMC")+"' and WLBH = '"+xsl.get("WLBH")+"'";
					rsmx = state.executeQuery(sql);
					if(rsmx!=null){
						while(rsmx.next()){
							if(rsmx.getInt("KWSL")>0){
								h = new Hashtable();
								h.put("HH", i);
								h.put("WLBH", rsmx.getString("WLBH"));
								h.put("WLMC", rsmx.getString("WLMC"));
								h.put("GG", rsmx.getString("GG"));
								h.put("XH", rsmx.getString("XH"));
								h.put("PCH", rsmx.getString("PCH"));
								h.put("CKDM", rsmx.getString("CKDM"));
								h.put("CKMC", rsmx.getString("CKMC"));
								h.put("QDM", rsmx.getString("QDM"));
								h.put("DDM", rsmx.getString("DDM"));
								h.put("KWDM", rsmx.getString("KWDM"));
								h.put("HWDM", rsmx.getString("HWDM"));
								h.put("DDH", xsddh);
								//h.put("DW", rsmx.getString("JLDW"));
								h.put("YFSL", rsmx.getInt("KWSL"));
								h.put("SFSL", rsmx.getInt("KWSL"));
								try {
									BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", h, bindid, uid);
								} catch (AWSSDKException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								XSSL -= rsmx.getInt("KWSL");
								i++;
							}
						}
					}

				}
			}
		}
	}

}
