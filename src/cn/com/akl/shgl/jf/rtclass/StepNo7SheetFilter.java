package cn.com.akl.shgl.jf.rtclass;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.qscy.biz.QSCYBiz;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SheetFilterRTClassA;
import com.actionsoft.loader.core.SheetRowLookAndFeel;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

public class StepNo7SheetFilter extends SheetFilterRTClassA {

    public StepNo7SheetFilter(UserContext arg0) {
        super(arg0);
    }

    @Override
    public SheetRowLookAndFeel acceptRowData(Hashtable hashtable, Vector vector) {
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
        if (tableName.equals("BO_AKL_WXJF_S")) {
            SheetRowLookAndFeel sheetRowLookAndFeel = new SheetRowLookAndFeel();
            String sfjf = getFieldValue(hashtable, vector, "SFJF");
            if (XSDDConstant.NO.equals(sfjf)) {
                //sheetRowLookAndFeel.setRemove(false);
                    sheetRowLookAndFeel.setDisplay(false);
                return sheetRowLookAndFeel;
            }
        }

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
