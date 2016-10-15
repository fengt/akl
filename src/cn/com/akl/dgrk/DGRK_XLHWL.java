package cn.com.akl.dgrk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGRK_XLHWL extends ExcelDownFilterRTClassA {

	public DGRK_XLHWL(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("匹配物料号，提示相关序列号出库信息");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tableName = this.getParameter(PARAMETER_TABLE_NAME).toString();
		//读取入库单头信息
		Hashtable rkdtData = BOInstanceAPI.getInstance().getBOData("BO_AKL_DGRK_P", bindid);
		String rklx = rkdtData.get("RKLX") == null ?"":rkdtData.get("RKLX").toString();//入库类型
		if(tableName.equals("BO_AKL_CCB_RKD_XLH_S")){
			//序列号表
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			String str = "SELECT XH,XLH FROM BO_AKL_CCB_RKD_XLH_S WHERE BINDID="+bindid;
			try {
				conn = DBSql.open();
				ps = conn.prepareStatement(str);
				rs = ps.executeQuery();
				while(rs.next()){
					//读取序列号单身数据
					String xh = parseNull(rs.getString("XH"));//型号
					String xlh = parseNull(rs.getString("XLH"));//序列号
					String wlsql = "select WLBH from BO_AKL_WLXX where XH='"+xh+"'";
					String wlh = DBSql.getString(wlsql, "WLBH");
					//更新物料号
					String updatewl = "update BO_AKL_CCB_RKD_XLH_S set WLBH='"+wlh+"',ZT='在库' where bindid = '"+bindid+"' and XH='"+xh+"'";
					DBSql.executeUpdate(updatewl);
					//如果是退货入库，提示相关序列号出库信息
					if(rklx.equals("退货入库")){
						String ckxxsql = "select b.ckdh,b.fhrq,b.lxrx1,b.shdz1,c.qsrq from (select max(ID) as id,bindid from BO_AKL_CCB_CKD_XLH_S where XLH='"+xlh+"' and ISEND=1 GROUP BY bindid) a LEFT JOIN BO_BO_AKL_DGCK_P b on a.bindid=b.bindid LEFT JOIN BO_AKL_QSD_P c on b.bindid=c.bindid ";
						String ckdh = parseNull(DBSql.getString(ckxxsql, "CKDH"));//出库单号
						String fhrq = parseNull(DBSql.getString(ckxxsql, "FHRQ"));//发货日期
						String shr = parseNull(DBSql.getString(ckxxsql, "LXRX1"));//收货人
						String shdz = parseNull(DBSql.getString(ckxxsql, "SHDZ1"));//收货地址
						String qsrq = parseNull(DBSql.getString(ckxxsql, "QSRQ"));//签收日期
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "型号"+xh+",序列号"+xlh+"的出库单号"+ckdh+",发货日期"+fhrq+",收货人"+shr+",收货地址"+shdz+",签收日期"+qsrq+"");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				DBSql.close(conn, ps, rs);
			}
		}
		return null;
	}
	public String parseNull(String str){
		return str == null?"":str;
	}
}
