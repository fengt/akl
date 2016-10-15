package cn.com.akl.shgl.sxtz.rtclass;

import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.sxtz.biz.SXTZConstant;
import cn.com.akl.util.DAOUtil;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by huangming on 2015/5/7.
 */
public class StepNo1Transaction extends WorkFlowStepRTClassA {

    public StepNo1Transaction() {
    }

    public StepNo1Transaction(UserContext arg0) {
        super(arg0);
        setProvider("huangming");
        setDescription("单据调整更新数据.");
    }

    @Override
    public boolean execute() {
        return true;
    }


}

