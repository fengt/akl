package cn.com.akl.rmagl.fxck.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.SheetFilterRTClassA;
import com.actionsoft.loader.core.SheetRowLookAndFeel;

public class StepNo3SheetFilter extends SheetFilterRTClassA {

	public StepNo3SheetFilter(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("¹ýÂË²Ö¿âµÄÏÔÊ¾");
	}

	@Override
	public SheetRowLookAndFeel acceptRowData(Hashtable arg0, Vector arg1) {
		return null;
	}

	@Override
	public String orderByStatement() {
		return null;
	}

	@Override
	public String[][] rowDataFilter() {
		return null;
	}

}
