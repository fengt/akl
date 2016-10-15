package cn.com.akl.ccgl.thck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	private UserContext uc;
	public StepNo1BeforeSave() {
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("根据选择的源单号带出单身信息。");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable<String, String> formHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
		String thdh= formHead.get("THDH") == null ? "" : formHead.get("THDH").toString();//退货单号（表单）
		
		/*String str = "SELECT YDH FROM BO_AKL_CKD_HEAD WHERE BINDID="+bindid+"";
		String ydh1 = DBSql.getString(str, "YDH");//源单号（数据库）
		if(ydh.equals(ydh1)){
			return true;
		}*/
		
		if(tablename.equals("BO_AKL_CKD_HEAD")){
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
			Hashtable<String, String> rec = null;
			
			try {
				conn = DBSql.open();
				
				if(thdh == null || thdh.trim().length() == 0){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CKD_BODY", bindid);
					return true;
				}
				
//				String queryByYDH = "SELECT * FROM BO_AKL_CGDD_BODY WHERE DDID='"+ydh+"'";
				String queryByYDH = "SELECT WLBH,CPMC,XH,PCH,SUM(SSSL)SSSL,SL,WSJG,HSJG,CKBM,KWBH,RKDH FROM BO_AKL_CCB_RKD_BODY WHERE CGDDH='"+thdh+"'"
						+ " GROUP BY WLBH,CPMC,XH,PCH,SL,WSJG,HSJG,CKBM,KWBH,RKDH";
				String queryCKMC = "SELECT CKMC FROM BO_AKL_CK WHERE CKDM=?";
				ps = conn.prepareStatement(queryByYDH);
				rs = ps.executeQuery();
				while(rs.next()){
					String rkdh = StrUtil.returnStr(rs.getString("RKDH"));//入库单号
					String wlbh = StrUtil.returnStr(rs.getString("WLBH"));//物料编号
					String wlmc = StrUtil.returnStr(rs.getString("CPMC"));//物料名称
//					String gg = StrUtil.returnStr(rs.getString("GG"));//规格
					String xh = StrUtil.returnStr(rs.getString("XH"));//型号
					String pch = StrUtil.returnStr(rs.getString("PCH"));//批次号
					String ckbm = StrUtil.returnStr(rs.getString("CKBM"));//仓库编号
					String ckmc = StrUtil.returnStr(DAOUtil.getString(conn, queryCKMC, ckbm));//仓库名称
					String hwbm = StrUtil.returnStr(rs.getString("KWBH"));//货位编号
					double dj = rs.getDouble("HSJG");//单价
					double cbdj = rs.getDouble("WSJG");//成本单价
					double sl = rs.getDouble("SL");//税率
					int sssl = rs.getInt("SSSL");//实收数量
//					int yrksl = rs.getInt("YRKSL");//已入库数量
					
					if(sl == 0.00) sl = 0.17;
					
					rec = new Hashtable<String, String>();
					rec.put("YDH", rkdh);//入库单号，扣减应付金额时用
					rec.put("WLH", wlbh);
					rec.put("WLMC", wlmc);
//					rec.put("GG", gg);
					rec.put("XH", xh);
					rec.put("PC", pch);
					rec.put("FHKFBH", ckbm);
					rec.put("FHKFMC", ckmc);
					rec.put("KWBH", hwbm);
					rec.put("DJ", String.valueOf(dj));
					rec.put("CBDJ", String.valueOf(cbdj));
					rec.put("SHUIL", String.valueOf(sl));
					rec.put("SL", String.valueOf(sssl));
					rec.put("SJSL", String.valueOf(sssl));
					vector.add(rec);
				}
				
				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CKD_BODY", bindid);
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", vector, bindid,  uc.getUID());
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally{
				DBSql.close(conn, ps, rs);
			}
		}
		return true;
	}

}
