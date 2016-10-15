package cn.com.akl.shgl.sx.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.sx.cnt.SXCnt;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.SheetFilterRTClassA;
import com.actionsoft.loader.core.SheetRowLookAndFeel;

public class StepNo8SheetFilter extends SheetFilterRTClassA {

	private Connection conn;
	private UserContext uc;
	public StepNo8SheetFilter(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("过滤子表未升级的产品信息。");
	}

	@Override
	public SheetRowLookAndFeel acceptRowData(Hashtable metaDataMapList, Vector rowData) {
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		
		if(tablename.equals("BO_AKL_SX_S")){
			SheetRowLookAndFeel displayRow = new SheetRowLookAndFeel();
			String sfsj = this.getFieldValue(metaDataMapList, rowData, "SFZCSJ");
			if(sfsj.equals(SXCnt.no)){
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
		// TODO Auto-generated method stub
		return null;
	}

}
