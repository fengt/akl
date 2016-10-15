package cn.com.akl.cggl.cgdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * 
 * @author 鲁祥宇
 *
 */

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	// 空参构造器
	public StepNo1BeforeSave() {
	}

	// 带参构造器
	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setProvider("鲁祥宇");
		setDescription("采购量及现有量可销售天数");
	}

	@Override
	public boolean execute() {
		// 获得流程实例id
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		// 获得表名
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		// 获取表单字段值
		Hashtable<String, String> frmHead = this.getParameter(PARAMETER_FORM_DATA).toHashtable();

		// 参考录入时触发主表行为
		if (CgrkCnt.tableName6.equals(tablename)) {
			Connection conn = null;
			try {
				conn = DBSql.open();
				// 数据库数据
				Vector<Hashtable<String, String>> vector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName7, bindid);
				String wlbh = "";// 物料编号
				int cgsl = 0;// 采购数量
				int kcsl = 0;// 库存数量
				int rjxl = 0;// 日均销量
				int yjxsts = 0; // 预计销售天数
				if (vector != null) {
					for (Hashtable<String, String> a : vector) {
						wlbh = a.get("WLBH").toString();
						cgsl = Integer.parseInt(a.get("CGSL").toString());
						kcsl = Integer.parseInt(a.get("KCSL").toString());
						System.out.println("这个不是空的吧"+kcsl);
					}
				}
				// 物料初始销售时间
				Date scxssj = DAOUtil.getDateOrNull(conn,"SELECT TOP 1 CREATEDATE FROM BO_AKL_WXB_XSDD_BODY WHERE WLBH =? ORDER BY createdate",wlbh);
				if (scxssj != null) {
					Date xtsj = new Date();
					// 已销售天数
					int s = (int) ((xtsj.getTime() - scxssj.getTime()) / (1000 * 60 * 60 * 24));
					// 判断销售是否满30天
					if (s < 30) {
						// 日销量
						int xl;
						xl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE wlbh=?",wlbh);
						rjxl = xl / s;
					} else {
						// 日销量
						int yxl;
						yxl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE CREATEDATE BETWEEN DATEADD(day,-30,getdate()) AND getdate() and wlbh=?",wlbh);
						rjxl = yxl / 30;
					}
					if (rjxl != 0) {
						// 预计销售天数
						yjxsts = (cgsl + kcsl) / rjxl;
						
					}
				}
				// 更新日均销量 和 预计销售天数
				DAOUtil.executeUpdate(conn,"UPDATE BO_AKL_CGDD_BODY SET RJXL=? WHERE WLBH=?",rjxl, wlbh);
				DAOUtil.executeUpdate(conn,"UPDATE BO_AKL_CGDD_BODY SET YJXSTS=? WHERE WLBH=?",yjxsts, wlbh);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, null, null);
			}
		}
		//导入本地数据时触发 采购订单单身表行为
		if (CgrkCnt.tableName7.equals(tablename)) {
			// 采购数量
			int cgsl = Integer.parseInt(frmHead.get("CGSL"));
			//型号
			String xh = frmHead.get("XH");
			// 物料编号 
			String str = "select * from " + CgddConstant.tableName3 + " where xh = '"+xh+"' and hzbm = '"+CgddConstant.hzbm0+"'";
			String wlbh = DBSql.getString(str, "wlbh");
			// 库存数量
			String str4 = "SELECT SUM(PCSL) AS KCSL FROM " + CgddConstant.tableName6 + " WHERE WLBH='"+wlbh+"'";
			int kcsl = DBSql.getInt(str4, "KCSL");
			Connection conn = null;
			int rjxl =0;//日均销量
			int yjxsts =0;//预计销售天数
			try{
				conn = DBSql.open();
				// 物料初始销售时间
				Date scxssj = DAOUtil.getDateOrNull(conn,"SELECT TOP 1 CREATEDATE FROM BO_AKL_WXB_XSDD_BODY WHERE WLBH =? ORDER BY createdate",wlbh);
				if (scxssj != null) {
					Date xtsj = new Date();
					// 已销售天数
					int s = (int) ((xtsj.getTime() - scxssj.getTime()) / (1000 * 60 * 60 * 24));
					// 判断销售是否满30天
					if (s < 30) {
						// 日销量
						int xl;
						xl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE wlbh=?",wlbh);
						rjxl = xl / s;
					} else {
						// 日销量
						int yxl;
						yxl = DAOUtil.getInt(conn,"SELECT SUM(ddsl) FROM BO_AKL_WXB_XSDD_BODY WHERE CREATEDATE BETWEEN DATEADD(day,-30,getdate()) AND getdate() and wlbh=?",wlbh);
						rjxl = yxl / 30;
					}
					if (rjxl != 0) {
						// 预计销售天数
						yjxsts = (cgsl + kcsl) / rjxl;
						
					}
				}
			}catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, null, null);
			}
			frmHead.put("RJXL", String.valueOf(rjxl));
			frmHead.put("YJXSTS", String.valueOf(yjxsts));
		}

		return true;
	}

}
