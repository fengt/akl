package cn.com.akl.shgl.fpkp.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.shgl.qscy.biz.QSCYBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.SheetFilterRTClassA;
import com.actionsoft.loader.core.SheetRowLookAndFeel;

public class StepNo5SheetFilter extends SheetFilterRTClassA {

    public StepNo5SheetFilter(UserContext arg0) {
        super(arg0);
    }

    @Override
    public SheetRowLookAndFeel acceptRowData(Hashtable hashtable, Vector vector) {
        String tableName = getParameter(PARAMETER_TABLE_NAME).toString();
        if (tableName.equals("BO_AKL_FPKP_S")) {
            SheetRowLookAndFeel sheetRowLookAndFeel = new SheetRowLookAndFeel();
            String kfckdm = getFieldValue(hashtable, vector, "KFCKBM");
            String uid = getUserContext().getUID();
            if (uid.equals("admin")) {
                return null;
            }

            Connection conn = null;
            try {
                conn = DBSql.open();
                String processUid = QSCYBiz.getProcessUid(conn, kfckdm);
                if (processUid == null || processUid.equals("")) {
                    System.err.println("客服仓库" + kfckdm + "没有维护对应部门!");
                } else {
                    if (processUid.indexOf(uid) != -1) {
                        sheetRowLookAndFeel.setRemove(true);
                        sheetRowLookAndFeel.setDisplay(true);
                    } else {
                        sheetRowLookAndFeel.setRemove(false);
                        sheetRowLookAndFeel.setDisplay(false);
                    }
                }
            } finally {
                DBSql.close(conn, null, null);
            }
            return sheetRowLookAndFeel;
        } else {
            return null;
        }
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
