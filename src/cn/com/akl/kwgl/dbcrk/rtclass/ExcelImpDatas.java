package cn.com.akl.kwgl.dbcrk.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class ExcelImpDatas extends ExcelDownFilterRTClassA {

	public ExcelImpDatas(UserContext arg0) {
		super(arg0);
		setProvider("wjh");
		setDescription("调拨出入库Excel导入");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable dbHead=BOInstanceAPI.getInstance().getBOData("BO_AKL_DBCRK_P", bindid);
		Vector vectorBody = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DBCRK_S", bindid);
		
	
		if(dbHead.isEmpty()){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "请在导入数据前先点击暂存，否则无法导入数据！");
			String sql = "delete from BO_AKL_DBCRK_S where bindid = " + bindid;
			DBSql.executeUpdate(sql);
		}else{
			
			/**数据导入回填字段**/
				ImportDataFillback(vectorBody, dbHead,bindid);
			
		}
		return null;
	}
	/**数据导入回填字段**/
	public void ImportDataFillback(Vector vector, Hashtable pTable, int bindid){
		String wlbh=null;
		String wlmc=null;
		String xh2=null;
		String pch=null;
		String kwsl=null;
		String hwdm=null;
		String sx=null;
		for (int i = 0; i < vector.size(); i++) {
			Hashtable rec = (Hashtable)vector.get(i);
			String sqlHead = "select * from BO_AKL_DBCRK_P where bindid="+bindid;
			String xh = rec.get("WLXH").toString();//型号
			String zcck = DBSql.getString(sqlHead, "ZCCK");//转出仓库
			String str="SELECT SFDGK	FROM BO_AKL_CK	WHERE CKDM = '"+zcck+"'";
			String strCK=DBSql.getString(str, "SFDGK");
			if(strCK.equals("025001")){//自营仓库
				String zySql="SELECT  WLBH, WLMC, XH, PCH, JLDW, CKDM, KWSL, HWDM,SX  FROM BO_AKL_KC_KCMX_S WHERE CKDM = '"+zcck+"' AND KWSL > 0 AND PCH IN ( SELECT PCH FROM BO_AKL_KC_KCHZ_P WHERE ZT = '042022' GROUP BY PCH ) AND '025001' = '"+strCK+"' and XH='"+xh+"' ORDER BY PCH";
				wlbh=DBSql.getString(zySql, "WLBH");
				wlmc=DBSql.getString(zySql, "WLMC");
				xh2=DBSql.getString(zySql, "XH");
				pch=DBSql.getString(zySql, "PCH");
				kwsl=DBSql.getString(zySql, "KWSL");
				hwdm=DBSql.getString(zySql, "HWDM");
				sx=DBSql.getString(zySql,"SX");
			}else if(strCK.equals("025000")){//是代管库
				String dgSql="SELECT  WLBH, WLMC, XH, PCH, JLDW, CKDM, KWSL, HWDM,SX  FROM BO_AKL_DGKC_KCMX_S WHERE CKDM = '"+zcck+"' AND KWSL > 0 AND PCH IN ( SELECT PCH FROM BO_AKL_DGKC_KCHZ_P WHERE ZT = '042022' GROUP BY PCH ) AND '025000' ='"+strCK+"' and XH='"+xh+"' ORDER BY PCH";
				wlbh=DBSql.getString(dgSql, "WLBH");
				wlmc=DBSql.getString(dgSql, "WLMC");
				xh2=DBSql.getString(dgSql, "XH");
				pch=DBSql.getString(dgSql, "PCH");
				kwsl=DBSql.getString(dgSql, "KWSL");
				hwdm=DBSql.getString(dgSql, "HWDM");
				sx=DBSql.getString(dgSql, "SX");
			}
			//
			String strUpdate="update BO_AKL_DBCRK_S set WLBH='"+wlbh+"',WLMC='"+wlmc+"',WLXH='"+xh2+"',TZQPC='"+pch+"',TZHPC='"+pch+"',TZQSL='"+kwsl+"',TZQKW='"+hwdm+"',SX='"+sx+"' WHERE WLXH='"+xh+"' and BINDID='"+bindid+"' ";
			
			int cnt = DBSql.executeUpdate(strUpdate);
			
			
		}
	}

}
