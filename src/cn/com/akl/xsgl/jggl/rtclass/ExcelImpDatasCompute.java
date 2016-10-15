package cn.com.akl.xsgl.jggl.rtclass;

import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;


import cn.com.akl.xsgl.jggl.biz.ExcelImpDatasComputeImp;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class ExcelImpDatasCompute extends ExcelDownFilterRTClassA {

	public ExcelImpDatasCompute(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("�۸���Ϣ����󣬼������۹����ۺͼ۸�ë����");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_JGGL", bindid);
		ExcelImpDatasComputeImp excelCompute = new ExcelImpDatasComputeImp();
		excelCompute.PriceCompute(vector, bindid);
		return null;
	}

}
