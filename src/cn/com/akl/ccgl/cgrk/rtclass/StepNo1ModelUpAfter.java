package cn.com.akl.ccgl.cgrk.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1ModelUpAfter extends ExcelDownFilterRTClassA {

	private UserContext uc;
	public StepNo1ModelUpAfter(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("校验Excel导入前是否已点击暂存。");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook hs) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);
		String rkdh = pTable.get("RKDH").toString();//入库单号
		String rkdb = pTable.get("RKDB").toString();//入库单别
		if(pTable.isEmpty()){
			try {
				String sql = "delete from " + CgrkCnt.tableName2 + " where bindid = " + bindid;
				DBSql.executeUpdate(sql);
				MessageQueue.getInstance().putMessage(uc.getUID(), "请在导入数据前先点击暂存，否则无法导入数据！",true);
			} catch (Exception e) {
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(uc.getUID(), "后台出现错误，请联系管理员!", true);
			}
		}else{
			if(CgrkCnt.rkdb3.equals(rkdb)||CgrkCnt.rkdb5.equals(rkdb)){//其他入库
				fillBackByXh(body,rkdh);
			}
		}
		return hs;
	}
	
	
	public void fillBackByXh(Vector<Hashtable<String, String>> vector, String rkdh){
		Connection conn = null;
		try{
			conn = DBSql.open();
			for (int i = 0; i < vector.size(); i++) {
				Hashtable<String, String> rec = vector.get(i);
				String xh = rec.get("XH").toString();
				int sssl = Integer.parseInt(rec.get("SSSL").toString());
				int id = Integer.parseInt(rec.get("ID").toString());
				String query_wlxx = "SELECT WLBH,WLMC,DW,SL FROM BO_AKL_WLXX WHERE XH='"+xh+"' AND HZBM='01065'";
				String query_kwxx = "SELECT b.CKDM,b.QDM,b.DDM,b.KWDM,b.KWBH FROM BO_AKL_WLKWGXB b,(SELECT MAX(ID)AS ID,XH,SSKHBM FROM BO_AKL_WLKWGXB WHERE SFYX='是' AND XH='"+xh+"' AND SSKHBM='01065' GROUP BY XH,SSKHBM)c WHERE b.ID=c.ID";
				String wlbh = StrUtil.returnStr(DBSql.getString(query_wlxx, "WLBH"));//物料编号
				String wlmc = StrUtil.returnStr(DBSql.getString(query_wlxx, "WLMC"));//物料名称
				String dw = StrUtil.returnStr(DBSql.getString(query_wlxx, "DW"));//单位
				double sl = DBSql.getDouble(query_wlxx, "SL");//税率
				
				String ckdm = StrUtil.returnStr(DBSql.getString(query_kwxx, "CKDM"));//仓库代码
				String qdm = StrUtil.returnStr(DBSql.getString(query_kwxx, "QDM"));//区代码
				String ddm = StrUtil.returnStr(DBSql.getString(query_kwxx, "DDM"));//道代码
				String kwdm = StrUtil.returnStr(DBSql.getString(query_kwxx, "KWDM"));//库位代码
				String kwbh = StrUtil.returnStr(DBSql.getString(query_kwxx, "KWBH"));//货位编号
				
				String updatePerId = "UPDATE BO_AKL_CCB_RKD_BODY SET WLBH=?,CPMC=?,DW=?,SL=?,CKBM=?,KFQBM=?,KFDBM=?,KFKWDM=?,KWBH=?,YSSL=?,RKDH=? WHERE ID=?";
				DAOUtil.executeUpdate(conn, updatePerId, wlbh,wlmc,dw,sl,ckdm,qdm,ddm,kwdm,kwbh,sssl,rkdh,id);
			}
		} catch(SQLException e){
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "数据导入失败，请重新导入！");
		}finally{
			DBSql.close(conn, null, null);
		}
	}

}
