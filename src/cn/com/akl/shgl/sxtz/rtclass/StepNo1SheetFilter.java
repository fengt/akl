package cn.com.akl.shgl.sxtz.rtclass;

import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;
import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.SheetFilterRTClassA;
import com.actionsoft.loader.core.SheetRowLookAndFeel;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Created by huangming on 2015/5/8.
 */
public class StepNo1SheetFilter extends SheetFilterRTClassA {

    public StepNo1SheetFilter(UserContext arg0) {
        super(arg0);
        setVersion("1.0.0");
        setDescription("过滤掉原数据");
    }

    @Override
    public SheetRowLookAndFeel acceptRowData(Hashtable hashtable, Vector vector) {
        Set<Map.Entry<Integer, MetaDataMapModel>> set = hashtable.entrySet();
        int indexAt = -1;
        for (Map.Entry<Integer, MetaDataMapModel> entry : set) {
            MetaDataMapModel model = entry.getValue();
            if(model.getFieldName().equals("ISOLD")){
                indexAt = entry.getKey();
            }
        }

        Object value = vector.get(indexAt);
        if (value != null) {
            String isold = value.toString();
            if (isold.equals(XSDDConstant.YES)) {
                SheetRowLookAndFeel sheetRowLookAndFeel = new SheetRowLookAndFeel();
                sheetRowLookAndFeel.setDisplay(false);
                return sheetRowLookAndFeel;
            }
        }
        return null;
    }

    @Override
    public String[][] rowDataFilter() {
        return new String[0][];
    }

    @Override
    public String orderByStatement() {
        return null;
    }
}
