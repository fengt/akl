package cn.com.akl.shgl.fjfj.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.SheetFilterRTClassA;
import com.actionsoft.loader.core.SheetRowLookAndFeel;

public class StepNo6SheetFilter extends SheetFilterRTClassA {

	public StepNo6SheetFilter(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("过滤子表不工厂检测的产品信息。");
	}

	@Override
	public SheetRowLookAndFeel acceptRowData(Hashtable metaDataMapList, Vector rowData) {
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		
		if(tablename.equals("BO_AKL_FJFJ_S")){
			SheetRowLookAndFeel displayRow = new SheetRowLookAndFeel();
			String ejjl = this.getFieldValue(metaDataMapList, rowData, "EJJL");//二检结论
			if(!FJFJCnt.jcjg2.equals(ejjl)){
				displayRow.setDisplay(false);
			}
			return displayRow;
		}else{
			return null;
		}
		
	}

	@Override
	public String orderByStatement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[][] rowDataFilter() {
		
		return null;
	}

}
