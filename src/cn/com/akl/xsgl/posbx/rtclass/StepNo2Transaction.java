package cn.com.akl.xsgl.posbx.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	//POS类型之正常方案
	private static final String POSLX_FA = "035001";
	
	private static final float SL = 0.17f;
	
	//POS报销状态
	private static final String POS_BXZT0 = "056185";//未报销
	private static final String POS_BXZT2 = "056187";//已报销
	
	
	//获取POS编号
	private static final String QUERY_POSBH = "SELECT POSBH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//获取TPM号
	private static final String QUERY_TPMH = "SELECT TPMH FROM BO_AKL_POSBX_P WHERE BINDID=?";
	
	//获取总返利支持数量
	//private static final String QUERY_FLSL = "SELECT SUM(ISNULL(FLZCSL,0)) AS FLSL FROM BO_AKL_WXB_XSDD_BODY WHERE POSID=? AND XH=?";
	
	//查询返利总金额
	private static final String QUERY_PROCESS_FLJE = 
			"SELECT SUM(ISNULL(FLZCJ,0)) as FLJE FROM BO_AKL_WXB_XSDD_HEAD head,BO_AKL_WXB_XSDD_BODY body "
			+ "WHERE head.ISEND=1 AND head.bindid=body.bindid AND body.POSFALX=? AND body.POSID=? GROUP BY body.POSID";
	
	//查询POS报销实批总金额
	private static final String QUERY_POS_SPZJE = "SELECT SUM(ISNULL(SPPOSZE,0)) AS SPZJE FROM BO_AKL_POSBX_S WHERE BINDID=?";
	
	//查询POS报销预批总金额
	private static final String QUERY_POS_YPZJE = "SELECT SUM(ISNULL(YPPOSZE,0)) AS YPZJE FROM BO_AKL_POSBX_S WHERE BINDID=?";
	
	//查询供应商编号和名称
	private static final String QUERY_GYSBH = "SELECT GYSBH FROM BO_AKL_WXB_XS_POS_HEAD where POSBH=?";
	private static final String QUERY_GYSMC = "SELECT GYSMC FROM BO_AKL_WXB_XS_POS_HEAD where POSBH=?";	

	//更新POS报销状态
	private static final String UPDATE_POS_ZT = "UPDATE BO_AKL_POSBX_S  SET ZT=? WHERE BINDID=?";
	
	//更新供应商费用支出TPMH状态
	private static final String UPDATE_TMPH_BXZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET BXZT=? WHERE POSBH=? AND TPM=?";
	
	
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo2Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo2Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("向POS资金池明细表中插入TPM号的POS报销金额、应收金额。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "同意");
//		Vector<Hashtable<String, String>> vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_POSBX_S", bindid);
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			String posbh = DAOUtil.getString(conn, QUERY_POSBH, bindid);//POS编号
			String tpm = DAOUtil.getString(conn, QUERY_TPMH, bindid);//TPM号
			
			if(yes){
				//POS资金池注入
				DataHander(conn, uc, bindid, posbh, tpm);
				//更新POS状态
				updateZT(conn, POS_BXZT2, bindid, posbh, tpm);
			}else{
				updateZT(conn, POS_BXZT0, bindid, posbh, tpm);
			}
			
			conn.commit();
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现错误，请检查控制台。");
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
	}
	
	
	/**
	 * POS更新资金池操作
	 * @param vector
	 * @param bindid
	 */
	public void DataHander(Connection conn, UserContext uc,
			int bindid, String posbh, String tpm) throws SQLException, AWSSDKException{
		
		String gysbh = DAOUtil.getString(conn, QUERY_GYSBH, posbh);//供应商编号
		String gysmc = DAOUtil.getString(conn, QUERY_GYSMC, posbh);//供应商名称
		BigDecimal ypzje = DAOUtil.getBigDecimal(conn, QUERY_POS_YPZJE, bindid);//预批POS总额
		BigDecimal spzje = DAOUtil.getBigDecimal(conn, QUERY_POS_SPZJE, bindid);//实批POS总额
		BigDecimal flje = DAOUtil.getBigDecimal(conn, QUERY_PROCESS_FLJE, POSLX_FA, posbh);//返利总金额
		
		BigDecimal sl = new BigDecimal(1);
		sl = sl.add(new BigDecimal(SL)).setScale(4, 4);
		BigDecimal ypzje2 = ypzje.multiply(sl);//实批POS总额(含税)
		BigDecimal spzje2 = spzje.multiply(sl);//实批POS总额(含税)
		BigDecimal syje = spzje2.subtract(flje);//剩余金额(pos金额)
		
		/**
		 * POS资金池明细表封装
		 */
		String a = "043028";//POS
		String b = "1";//未抵扣
		Hashtable<String, String> record = new Hashtable<String, String>();
		record.put("LX", a);
		record.put("TPM", tpm);
		record.put("FABH", posbh);
		record.put("YSYJE", "0");//已使用金额
		record.put("POSJE", syje.toString());//POS金额
		record.put("BXSQJE", ypzje2.toString());//报销申请金额
		record.put("YSJE", ypzje2.toString());//应收金额
		record.put("SSJE", spzje2.toString());//实收金额
		record.put("GYSBH", gysbh);//供应商编码
		record.put("ZT", b);
		
		// 如果剩余金额为负，或者为零
		if(syje != null && syje.doubleValue() > 0){
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_MXB", record, bindid, uc.getUID());
		}
		
		/**
		 * POS资金池汇总表封装
		 */
		String query_zjc = "SELECT COUNT(1)N FROM BO_AKL_POS_HZB WHERE GYSBH=?";
		Integer n = DAOUtil.getIntOrNull(conn, query_zjc, gysbh);
		if(n != null && n > 0){
			String update_zjc = "UPDATE BO_AKL_POS_HZB SET POSZJE=ISNULL(POSZJE,0) + ?, SPZJE=ISNULL(SPZJE,0) + ?, SSZJE=ISNULL(SSZJE,0) + ? WHERE GYSBH=?";
			int updateCount = DAOUtil.executeUpdate(conn, update_zjc, syje.toString(), ypzje2.toString(), spzje2.toString(), gysbh);
			if(updateCount != 1) throw new RuntimeException("POS资金池更新失败，请联系管理员！");
		}else{
			Hashtable<String, String> Precord = new Hashtable<String, String>();
			Precord.put("GYSBH", gysbh);//供应商编码
			Precord.put("GYSMC", gysmc);//供应商名称
			Precord.put("YSYZJE", "0");//已使用总金额
			Precord.put("POSZJE", syje.toString());//POS总金额
			Precord.put("SPZJE", ypzje2.toString());//应收总金额
			Precord.put("SSZJE", spzje2.toString());//实收总金额
			
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_HZB", Precord, bindid, uc.getUID());
		}
	}
	
	public static void updateZT(Connection conn, String zt, int bindid, String posbh, String tpm) throws SQLException{
		DAOUtil.executeUpdate(conn, UPDATE_POS_ZT, zt, bindid);//更新报销状态
		DAOUtil.executeUpdate(conn, UPDATE_TMPH_BXZT, zt, posbh, tpm);//更新供应商费用支持TMP号专题
	}
	
}
