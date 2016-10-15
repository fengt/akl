package cn.com.akl.dgkgl.xsck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	//查询单身中的物料信息
	private static final String queryWLXX = "SELECT WLBH,sum(SFSL) sfsl,HWDM,PCH,KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID=? group by HWDM,PCH,KHCGDH,WLBH";
	//更新销售订单单身中的已出库数量
	private static final String updateYCKSL = "UPDATE BO_AKL_DGXS_S SET YCKSL=ISNULL(YCKSL,0)+? WHERE WLBH=? AND DDID=? AND ISNULL(YCKSL,0)+?<=XSSL AND ISNULL(KHCGDH, '')=?";
	//更新代管锁库表中的销售数量
	private static final String updateXSSL = "UPDATE BO_AKL_DGCKSK SET XSSL=ISNULL(XSSL,0)-? WHERE WLBH=? AND XSDH=? AND XSSL>=? AND PCH=? AND HWDM=? AND ISNULL(KHCGDH, '')=?";
	// 查询销售订单是否出完
	private static final String queryXSDDCKSL = "SELECT COUNT(*) FROM BO_AKL_DGXS_P a join BO_AKL_DGXS_S b on a.BINDID=b.BINDID WHERE a.XSDDID=? GROUP BY a.XSDDID HAVING SUM(ISNULL(b.YCKSL, 0))<>SUM(ISNULL(b.XSSL, 0))";
	// 查询销售订单物料的销售数量
	private static final String QUERY_XSDD_WL_XSSL = "SELECT ISNULL(XSSL,0)-ISNULL(YCKSL, 0) xssl FROM BO_AKL_DGXS_S WHERE WLBH=? AND DDID=? AND ISNULL(KHCGDH, '')=?";

	private static final String queryCKDXX = "SELECT * FROM BO_BO_AKL_DGCK_P WHERE BINDID=?";

	// 更新入库序列号状态
	private static final String UPDATE_RKXLH = "UPDATE BO_AKL_CCB_RKD_XLH_S SET ZT='"+XSDDConstant.XLH_ZT_CK+"' WHERE XLH=?";
	// 查询出库序列号
	private static final String QUERY_CKXLH = "SELECT XLH FROM BO_AKL_CCB_CKD_XLH_S WHERE BINDID=?";

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("流程流转后事件: 更新库存");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(PARAMETER_TASK_ID).toInt();

		boolean th = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "退回");
		boolean sf = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "第三方物流");
		boolean zjck = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskId, "直接出库");
		if(!th){
			Connection conn = null;
			Statement stat = null;
			ResultSet rs = null;
			try{
				conn = DAOUtil.openConnectionTransaction();
				stat = conn.createStatement();

				final String xsdh = DAOUtil.getString(conn, "SELECT XSDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
				final String cklx = DAOUtil.getString(conn, "SELECT CKLX FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);

				// 1、解锁库存
				// DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_KC_SPPCSK WHERE DDH=?", xsddh);

				// 1、更新库存明细
				// 查询子表记录，扣减库存
				DAOUtil.executeQueryForParser(conn, "SELECT s.WLBH,s.PCH,sum(s.SFSL) sfsl,s.HWDM,s.SX,p.XSDH, s.KHCGDH FROM BO_BO_AKL_DGCK_S s left join BO_BO_AKL_DGCK_P p on s.bindid = p.bindid WHERE s.BINDID=? group by s.WLBH,s.PCH,s.HWDM,s.SX,p.XSDH,s.KHCGDH", new DAOUtil.ResultPaser() {
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						if(!cklx.equals("054143")&&!cklx.equals("054144")){
							if(0==DAOUtil.executeUpdate(conn, "update BO_AKL_DGKC_KCMX_S set KWSL=ISNULL(KWSL, 0)-? where HWDM=? AND WLBH=? AND PCH=? AND ISNULL(KWSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = ? and HWDM = ? AND PCH = ? AND XSDH<>?), KWSL)>=? AND SX=?", 
									reset.getInt("SFSL"), reset.getString("HWDM"), reset.getString("WLBH"), reset.getString("PCH"), reset.getString("WLBH"), reset.getString("HWDM"), reset.getString("PCH"), reset.getString("XSDH"), reset.getInt("SFSL"), reset.getString("SX"))){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "客户采购单号"+reset.getString("KHCGDH")+"批次号："+reset.getString("PCH")+ " 物料号:"+reset.getString("WLBH")+" 在货位代码："+reset.getString("HWDM")+"可用数量不足"+reset.getInt("SFSL")+"!", true);
								throw new RuntimeException("客户采购单号:"+reset.getString("KHCGDH")+"批次号："+reset.getString("PCH")+ " 物料号:"+reset.getString("WLBH")+" 在货位代码："+reset.getString("HWDM")+"可用数量不足"+reset.getInt("SFSL")+"!");
							}
							if(0==DAOUtil.executeUpdate(conn, "update BO_AKL_DGKC_KCHZ_P set CKSL=ISNULL(CKSL, 0)+?, PCSL=ISNULL(PCSL, 0)-? where WLBH=? AND PCH=? AND ISNULL(PCSL-(SELECT ISNULL(sum(ISNULL(XSSL, 0)), 0) from BO_AKL_DGCKSK WHERE WLBH = ? AND PCH = ? AND XSDH<>?), PCSL)>=? and ZT='042022'", 
									reset.getInt("SFSL"), reset.getInt("SFSL"), reset.getString("WLBH"), reset.getString("PCH"), reset.getString("WLBH"), reset.getString("PCH"), reset.getString("XSDH"), reset.getString("SFSL"))){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "客户采购单号"+reset.getString("KHCGDH")+"物料号:"+reset.getString("WLBH")+" 这批"+reset.getString("PCH")+"物料的出库数量已经达到上限，请检查库存汇总表", true);
								throw new RuntimeException("客户采购单号:"+reset.getString("KHCGDH")+"物料号:"+reset.getString("WLBH")+" 这批"+reset.getString("PCH")+"物料的出库数量已经达到上限，请检查库存汇总表");
							}
						}
						else{
							if(0==DAOUtil.executeUpdate(conn, "update BO_AKL_DGKC_KCMX_S set KWSL=ISNULL(KWSL, 0)-? where HWDM=? AND WLBH=? AND PCH=? AND KWSL>=? AND SX=?", 
									reset.getInt("SFSL"), reset.getString("HWDM"), reset.getString("WLBH"), reset.getString("PCH"), reset.getInt("SFSL"), reset.getString("SX"))){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "批次号："+reset.getString("PCH")+ " 物料号:"+reset.getString("WLBH")+" 在货位代码："+reset.getString("HWDM")+"库存数量不足"+reset.getInt("SFSL")+"!", true);
								throw new RuntimeException("客户采购单号:"+reset.getString("KHCGDH")+"批次号："+reset.getString("PCH")+ " 物料号:"+reset.getString("WLBH")+" 在货位代码："+reset.getString("HWDM")+"库存数量不足"+reset.getInt("SFSL")+"!");
							}
							if(0==DAOUtil.executeUpdate(conn, "update BO_AKL_DGKC_KCHZ_P set CKSL=ISNULL(CKSL, 0)+?, PCSL=ISNULL(PCSL, 0)-? where WLBH=? AND PCH=? AND PCSL>=? and ZT='042022'", 
									reset.getInt("SFSL"), reset.getInt("SFSL"), reset.getString("WLBH"), reset.getString("PCH"), reset.getString("SFSL"))){
								MessageQueue.getInstance().putMessage(getUserContext().getUID(), "物料号:"+reset.getString("WLBH")+" 这批"+reset.getString("PCH")+"物料的出库数量已经达到上限，请检查库存汇总表", true);
								throw new RuntimeException("客户采购单号:"+reset.getString("KHCGDH")+"物料号:"+reset.getString("WLBH")+" 这批"+reset.getString("PCH")+"物料的出库数量已经达到上限，请检查库存汇总表");
							}
						}
						return true;
					}
				}, bindid);

				//2、遍历所有的单身物料信息
				DAOUtil.executeQueryForParser(conn, queryWLXX, new DAOUtil.ResultPaser() {
					@Override
					public boolean parse(Connection conn, ResultSet reset) throws SQLException {
						//2、更新对应的销售订单中的销售数量
						int count = DAOUtil.executeUpdate(conn, updateYCKSL, reset.getInt("SFSL"), reset.getString("WLBH"), xsdh, reset.getInt("SFSL"), reset.getString("KHCGDH")==null?"":reset.getString("KHCGDH"));
						if(count != 1){
							if(count == 0)
								throw new RuntimeException("代管库第一节点销售出库，检测到当前订单号为："+xsdh+",客户采购单号为："+reset.getString("KHCGDH")+" 物料号为:"+reset.getString("WLBH")+", 实发数量为:"+reset.getInt("SFSL") +", 实发数量超过了销售数量"+DAOUtil.getStringOrNull(conn, QUERY_XSDD_WL_XSSL, reset.getString("WLBH"), xsdh, PrintUtil.parseNull(reset.getString("KHCGDH"))));
							if(count >1)
								throw new RuntimeException("代管库第一节点销售出库，检测到当前订单号为：" + xsdh +",客户采购单号为："+reset.getString("KHCGDH")+"中有多条相同的物料编号为："+reset.getString("WLBH")+"，单位为"+reset.getString("DW")+"的信息");
						}
						else{
							DAOUtil.executeUpdate(conn, updateXSSL, reset.getInt("SFSL"), reset.getString("WLBH"), xsdh, reset.getInt("SFSL"), reset.getString("PCH"), reset.getString("HWDM"), PrintUtil.parseNull(reset.getString("KHCGDH")));
						}
						return true;
					}
				}, bindid);
				//更新销售订单状态
				// 1、查询数量是否出完 

				//	DAOUtil.executeUpdate(conn, "Update BO_AKL_DGXS_P Set ZT=(CASE WHEN CKSL=? THEN ? ELSE ? END) WHERE WLBH=? AND DDID=?", reset.getInt("SFSL"), "已出库", "部分出库", reset.getString("WLBH"), reset.getString("DDH"));
				String message = DAOUtil.getStringOrNull(conn, queryXSDDCKSL, xsdh);
				if(message==null||"0".equals(message)){
					DAOUtil.executeUpdate(conn, "Update BO_AKL_DGXS_P Set ZT=? WHERE XSDDID=?", "已出库", xsdh);
					DAOUtil.executeUpdate(conn, "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?", "已出库", bindid);
					//删除锁库表里的数据
					String sqlD = "delete from BO_AKL_DGCKSK where XSDH = '"+xsdh+"'";
					stat.executeUpdate(sqlD);
				}	else {
					DAOUtil.executeUpdate(conn, "Update BO_AKL_DGXS_P Set ZT=? WHERE XSDDID=?", "部分出库", xsdh);
					DAOUtil.executeUpdate(conn, "Update BO_BO_AKL_DGCK_P Set ZT=? WHERE bindid=?", "已出库", bindid);
				}
				if(!zjck){
					String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
					if("否".equals(sfyy)||XSDDConstant.NO.equals(sfyy)){
						// 非预约 填充运单
						fillWLD(conn, xsdh, stat, rs, sf);
						fillQSD(conn, xsdh, bindid, stat);
					} else {
						fillYYD(conn, bindid, xsdh, stat, rs);
						fillQSD(conn, xsdh, bindid, stat);
					}
				}


				// 更新入库序列表状态
				ArrayList<String> collection = DAOUtil.getStringCollection(conn, QUERY_CKXLH, bindid);
				for(String XLH : collection){
					DAOUtil.executeUpdate(conn, UPDATE_RKXLH, XLH);
				}
				conn.commit();
				return true;
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
			}
		}
		else
			return true;
	}

	/**
	 * 填充预约单
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	private void fillYYD(Connection conn,int bindid, String xsdh, Statement stat, ResultSet rs) throws SQLException,
	AWSSDKException {

		//		String sqlp = "Delete from BO_BO_AKL_CK_YY_P where bindid ="+bindid;//删除预约单单头内容
		//		stat.executeUpdate(sqlp);
		String sqls = "Delete from BO_AKL_CK_YY_S where bindid ="+bindid;//删除预约单单身内容
		stat.executeUpdate(sqls);
		StringBuffer khcgdh = new StringBuffer();
		sqls = "select KHCGDH from BO_AKL_DGXS_S where DDID = '"+xsdh+"' group by KHCGDH";
		rs = stat.executeQuery(sqls);
		while(rs.next()){
			khcgdh.append(PrintUtil.parseNull(rs.getString("KHCGDH"))+"、");
		}
		String ckdh = DAOUtil.getString(conn, "SELECT CKDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);
		//		String khcgdh = DAOUtil.getString(conn, "SELECT KHCGDH FROM BO_AKL_DGXS_P WHERE XSDDID=?", xsdh);
		Integer yyzl  = DAOUtil.getIntOrNull(conn, "SELECT SUM(ISNULL(SFSL, 0)) FROM BO_BO_AKL_DGCK_S WHERE BINDID=?", bindid);
		Integer ddsl  = DAOUtil.getIntOrNull(conn, "SELECT SUM(ISNULL(YFSL, 0)) FROM BO_BO_AKL_DGCK_S WHERE BINDID=?", bindid);
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		hashtable.put("CKDH",ckdh==null?"":ckdh);


		Hashtable<String, String> hashtable2 = new Hashtable<String, String>();
		hashtable2.put("CKDH", ckdh==null?"":ckdh);
		hashtable2.put("DDSL", ddsl.toString());
		//hashtable.put("JDCGDH", "");
		hashtable2.put("YYSHL", yyzl.toString());
		hashtable2.put("JDCGDH", khcgdh.substring(0, khcgdh.lastIndexOf("、")));
		FillBiz fillBiz = new FillBiz();
		fillBiz.insertOrUpdateBOData(conn, bindid, getUserContext().getUID(), "BO_BO_AKL_CK_YY_P", hashtable);

		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CK_YY_S", hashtable2,  bindid, getUserContext().getUID());
	}

	/**
	 * 填充物流单或运单
	 * @param conn
	 * @throws AWSSDKException
	 * @throws SQLException 
	 */
	private void fillWLD(Connection conn, String xsdh, Statement stat, ResultSet rs, boolean sf) throws AWSSDKException, SQLException{
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String ckdh = DAOUtil.getString(conn, "SELECT CKDH FROM BO_BO_AKL_DGCK_P WHERE BINDID=?", bindid);

		//		String sqlp = "Delete from BO_AKL_YD_P where bindid ="+bindid;//删除物流单头或运单单头内容
		//		stat.executeUpdate(sqlp);

		//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
		//		String message = DAOUtil.getString(conn, "SELECT XSDDID FROM BO_AKL_DGXS_P WHERE BINDID=?", bindid);
		//		
		//		Hashtable<String, String> qsData = new Hashtable<String, String>();
		//		qsData.put("DDH", message);

		Hashtable<String, String> qsData = new Hashtable<String, String>();

		String sql = "select HZBH from BO_AKL_DGXS_P where XSDDID = '"+xsdh+"'";
		String hzbh = DBSql.getString(conn, sql, "HZBH");
		qsData.put("HZBM", hzbh);
		//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
		sql = "select a.DDH, sum(b.TJ*a.SFSL) as TJ,sum(b.ZL*a.SFSL) ZL, sum(a.SFSL) as SL from BO_BO_AKL_DGCK_S a, BO_AKL_WLXX b where a.WLBH = b.WLBH and a.bindid = "+bindid+" group by a.DDH";
		rs = stat.executeQuery(sql);
		if(rs.next()) {
			qsData.put("CKDH", ckdh==null?"":ckdh);
			qsData.put("DDH", xsdh==null?"":xsdh);
			qsData.put("TJ", rs.getString(2)==null?"":rs.getString(2));
			qsData.put("ZL", rs.getString(3)==null?"":rs.getString(3));
			qsData.put("SL", String.valueOf(rs.getInt(4))==null?"":String.valueOf(rs.getInt(4)));
		}
		sql = "select KHMC, LXRX1, BM, LXRDH1, SHDZ1, KHBH, YFJSFS, YSFS, FHRQ,LXRX2,LXRDH2,SHDZ2 from BO_BO_AKL_DGCK_P where bindid = "+bindid;
		rs = stat.executeQuery(sql);
		String JSFS = null;
		String DZ = null;
		if(rs.next()){
			qsData.put("KHMC", rs.getString(1)==null?"":rs.getString(1));
			qsData.put("KHBH",rs.getString(6)==null?"":rs.getString(6));
			sql = "select LBID from BO_AKL_KH_P where KHID = '"+rs.getString(6)+"'";
			String lbid = DBSql.getString(conn, sql, "LBID")==null?"":DBSql.getString(conn, sql, "LBID");
			qsData.put("KHLX", lbid);
			qsData.put("SHR", rs.getString(2)==null?"":rs.getString(2));
			qsData.put("BM", rs.getString(3)==null?"":rs.getString(3));
			qsData.put("SHRDH", rs.getString(4)==null?"":rs.getString(4));
			qsData.put("SHDW", rs.getString(5)==null?"":rs.getString(5));
			qsData.put("YSFS", rs.getString(8)==null?"":rs.getString(8));
			qsData.put("RQ", rs.getString(9)==null?"":rs.getString(9));
			qsData.put("YFJSFS", rs.getString(7)==null?"":rs.getString(7));
			qsData.put("LXRX2", rs.getString(10)==null?"":rs.getString(10));
			qsData.put("LXRDH2", rs.getString(11)==null?"":rs.getString(11));
			qsData.put("SHDZ2", rs.getString(12)==null?"":rs.getString(12));
			JSFS = rs.getString(7)==null?"":rs.getString(7);
			DZ = rs.getString(5)==null?"":rs.getString(5);
		}
		//获取仓库代码
		String CKDM = null;
		sql = "select top 1 CKDM from BO_BO_AKL_DGCK_S where bindid="+bindid+"group by CKDM";
		rs = stat.executeQuery(sql);
		if(rs.next()){
			CKDM = rs.getString("CKDM");
		}
		//获取仓库地址
		String CKDZ = null;
		sql = "SELECT CKWLDZ from BO_AKL_CK WHERE CKDM = '"+CKDM+"'";
		rs = stat.executeQuery(sql);
		if(rs.next()){
			CKDZ = rs.getString("CKWLDZ")==null?"":rs.getString("CKWLDZ");
		}
		//始发站
		if(CKDZ.length()>=2)
			CKDZ = CKDZ.substring(0, 2);
		qsData.put("SFZ", CKDZ);
		//到站
		if(DZ.length()>=2){
			DZ = DZ.substring(0, 2);
		}
		qsData.put("DZ", CKDZ);

		// 物料、型号、应收、产品名称、客户产品编号（自行关联）
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

	/**
	 * 填充签收单
	 * @param conn
	 * @throws AWSSDKException
	 * @throws SQLException 
	 */
	private void fillQSD(Connection conn, String xsdh, int bindid, Statement stat) throws AWSSDKException, SQLException{
		PreparedStatement ps = null;
		ResultSet reset = null;
		PreparedStatement ckdPs = null;
		ResultSet ckdReset = null;
		try{
			//			String sqlp = "Delete from BO_AKL_QSD_P where bindid ="+bindid;//删除签收单头内容
			//			stat.executeUpdate(sqlp);
			String sqls = "Delete from BO_AKL_QSD_S where bindid ="+bindid;//删除签收单身内容
			stat.executeUpdate(sqls);
			String qsdh = RuleAPI.getInstance().executeRuleScript("SI@replace(@date,-)@formatZero(3,@sequencefordateandkey(BO_AKL_QSD_P))");
			Hashtable<String, String> qsData = new Hashtable<String, String>();
			Vector<Hashtable<String, String>> qsDatas = new Vector<Hashtable<String,String>>();
			String KHBH = ""; 
			// 出库单填充签收单
			try{
				ckdPs = conn.prepareStatement(queryCKDXX);
				ckdReset = DAOUtil.executeFillArgsAndQuery(conn, ckdPs, bindid);
				if(ckdReset.next()){
					//出库单号、客户采购单号、应收合计（销售订单）、仓库、交货地址、仓库联系人、仓库联系人电话、仓库联系人手机、仓库联系人邮箱
					String sql = "select BM from BO_BO_AKL_DGCK_P where bindid = "+bindid;
					String BM = DBSql.getString(conn, sql, "BM");
					sql = "SELECT KHCGDH FROM BO_AKL_DGXS_P WHERE XSDDID= '"+xsdh+"'";
					String KHCGDH = DBSql.getString(conn, sql, "KHCGDH");
					sql = "select HZMC from BO_AKL_DGXS_P where XSDDID = '"+xsdh+"'";
					String TYDH  = DBSql.getString(conn, sql, "HZMC");

					KHBH = ckdReset.getString("KHBH");
					qsData.put("QSDH", qsdh==null?"":qsdh);
					qsData.put("KHCGDH", KHCGDH==null?"":KHCGDH);
					qsData.put("BM", BM);
					qsData.put("TYDH", TYDH);
					qsData.put("CKDH", ckdReset.getString("CKDH")==null?"":ckdReset.getString("CKDH"));
					qsData.put("SHDZ", ckdReset.getString("SHDZ1")==null?"":ckdReset.getString("SHDZ1"));
					qsData.put("SHDW", ckdReset.getString("KHMC")==null?"":ckdReset.getString("KHMC"));
					qsData.put("SHFZR", ckdReset.getString("LXRX1")==null?"":ckdReset.getString("LXRX1"));
					qsData.put("SHFZRDH", ckdReset.getString("LXRDH1")==null?"":ckdReset.getString("LXRDH1"));
					qsData.put("BZ", ckdReset.getString("ZY")==null?"":ckdReset.getString("ZY"));
					qsData.put("SHRQ", ckdReset.getString("FHRQ")==null?"":ckdReset.getString("FHRQ"));
                    qsData.put("YSFS", ckdReset.getString("YSFS")==null?"":ckdReset.getString("YSFS"));
                    qsData.put("YFJSFS", ckdReset.getString("YFJSFS")==null?"":ckdReset.getString("YFJSFS"));
                    qsData.put("LXRX2", ckdReset.getString("LXRX2")==null?"":ckdReset.getString("LXRX2"));
                    qsData.put("LXRDH2", ckdReset.getString("LXRDH2")==null?"":ckdReset.getString("LXRDH2"));
                    qsData.put("SHDZ2", ckdReset.getString("SHDZ2")==null?"":ckdReset.getString("SHDZ2"));
                   
				}
			} finally {
				DBSql.close(ckdPs, ckdReset);
			}

			ps = conn.prepareStatement("SELECT KHCGDH, WLMC,XH,WLBH,SUM(SFSL) SFSL,DW FROM BO_BO_AKL_DGCK_S WHERE BINDID=? group by WLMC,XH,WLBH,DW,KHCGDH");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);

			while(reset.next()){
				Hashtable<String, String> qsDatas_s = new Hashtable<String, String>();
				String wlh = reset.getString("WLBH");
				String khspsku = DAOUtil.getStringOrNull(conn, "SELECT KHSPSKU FROM BO_AKL_KHSPBMGL WHERE YKSPSKU=? and KHBM=?", wlh, KHBH);
				khspsku=khspsku==null?"":khspsku;
				qsDatas_s.put("KHCGDH", reset.getString("KHCGDH")==null?"":reset.getString("KHCGDH"));
				qsDatas_s.put("KHSPBH", khspsku==null?"":khspsku);
				qsDatas_s.put("CPMC", reset.getString("WLMC")==null?"":reset.getString("WLMC"));
				qsDatas_s.put("XH", reset.getString("XH")==null?"":reset.getString("XH"));
				qsDatas_s.put("WLH", wlh==null?"":wlh);
				qsDatas_s.put("YSSL", String.valueOf(reset.getInt("SFSL")));
				qsDatas_s.put("SSSL", String.valueOf(reset.getInt("SFSL")));
				qsDatas_s.put("DW", reset.getString("DW")==null?"":reset.getString("DW"));
				qsDatas.add(qsDatas_s);
			}
			FillBiz fillBiz = new FillBiz();
			fillBiz.insertOrUpdateBOData(conn, bindid, getUserContext().getUID(), "BO_AKL_QSD_P", qsData);
			//			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_P", qsData, bindid, getUserContext().getUID());
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_S", qsDatas, bindid, getUserContext().getUID());
		} 
		finally{
			DBSql.close(ps, reset);
		}
	}
}
