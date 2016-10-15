package cn.com.akl.xsgl.jggl.rtclass;

import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;


import cn.com.akl.xsgl.jggl.biz.ExcelImpDatasComputeImp;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class kh_ExcelImpDatasCompute extends ExcelDownFilterRTClassA {

	public kh_ExcelImpDatasCompute(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("�ͻ��۸���Ϣ����󣬴����������ƺ��ͺš�");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook arg0) {
		int bindid = this.getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_KH_JGGL_S", bindid);
		ExcelImpDatasComputeImp excelCompute = new ExcelImpDatasComputeImp();
		excelCompute.DataFillBack(vector, bindid);
		return null;
	}

}
