package cn.com.akl.cwgl.cysfygl.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.xsgl.jggl.biz.ExcelImpDatasComputeImp;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DealExcelImpData extends ExcelDownFilterRTClassA {

	public DealExcelImpData(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("承运商信息导入后，计算每条记录的总额。");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CYSFYGL_S", bindid);
		ZECompute(vector, bindid);
		return null;
	}
	
	public void ZECompute(Vector vector, int bindid){
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable table = (Hashtable)vector.get(i);
				double yf  = Double.parseDouble(table.get("YF").toString());
				double thf = Double.parseDouble(table.get("THF").toString());
				double psf = Double.parseDouble(table.get("PSF").toString());
				double bxf = Double.parseDouble(table.get("BXF").toString());
				double fdf = Double.parseDouble(table.get("FDF").toString());
				double kk = Double.parseDouble(table.get("KK").toString());
				int id = Integer.parseInt((table.get("ID").toString()));
				double ze = (yf+thf+psf+bxf+fdf)-kk ;
				DBSql.executeUpdate("update BO_AKL_CYSFYGL_S set ZE ="+ze+" where id = "+id);
			}
		}
	}

}
