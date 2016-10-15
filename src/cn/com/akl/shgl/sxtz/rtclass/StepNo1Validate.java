package cn.com.akl.shgl.sxtz.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.shgl.sxtz.biz.SXTZConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

/**
 * Created by huangming on 2015/5/7.
 */
public class StepNo1Validate extends WorkFlowStepRTClassA {

    public StepNo1Validate(UserContext arg0) {
        super(arg0);
        setProvider("huangming");
        setDescription("校验送修单是否可以进行修改.");
    }

    @Override
    public boolean execute() {
        // 验证送修单最后更新的时间戳.
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DBSql.open();
            Date nowtime = DAOUtil.getDateOrNull(conn, SXTZConstant.QUERY_FORM_LASTTIME, bindid);
            String sxdh = DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_FORM_SXDH, bindid);
            Date lastTime = DAOUtil.getDateOrNull(conn, SXTZConstant.QUEYR_OLD_FORM_LASTTIME, sxdh);
            String yywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_YYWLX, bindid));//原业务类型
            String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_YWLX, bindid));//业务类型
            
            if(SXCnt.ywlx0.equals(yywlx) || SXCnt.ywlx1.equals(yywlx)){//原业务类型：销售或赠送
            	if(!yywlx.equals(ywlx)){
            		throw new RuntimeException("销售或赠送的【业务类型】字段暂不支持调整！");
            	}
        	}
            
            /**
             *  验证是否交付，若数据已被引用，则需提醒删除交付单再调整
             */
            ArrayList<String> jfdList = DAOUtil.getStringCollection(conn, SXTZConstant.QUERY_isExsitJFD, 0, sxdh);
            ArrayList<String> jfdListEnd = DAOUtil.getStringCollection(conn, SXTZConstant.QUERY_isExsitJFD, 1, sxdh);
            if(jfdList.size() > 0){
            	throw new RuntimeException("请删除该送修单对应的交付单"+jfdList.toString()+"后再进行调整！");
            }else if(jfdListEnd.size() > 0){
            	throw new RuntimeException("该送修单已交付，暂无法调整！");
            }
            
            // 对比主表差异.
            if (nowtime != null && lastTime != null && nowtime.equals(lastTime)) {
                // 比较子表时间差异.
                return validateSub(conn, bindid);
            } else {
                throw new RuntimeException("此单不可修改，当前修改的单据已经被其他单据调整流程调整了，请重新引入送修单！");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * 对比子表时间差异.
     *
     * @param conn
     * @param bindid
     * @return
     */
    public boolean validateSub(Connection conn, int bindid) throws SQLException {
        // 将子表记录更新至送修单中.
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(SXTZConstant.QUERY_NEW_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                // 验证子表数据.
                Timestamp newdate = reset.getTimestamp("LASTTIME");
                int parentid = reset.getInt("PARENTID");
                Date olddate = DAOUtil.getDateOrNull(conn, SXTZConstant.QUERY_OLD_FORM_LASTTIME, parentid);
                if (olddate == null) {
                    throw new RuntimeException("原子表记录修改时间无法找到!");
                }
                if (olddate.getTime() != newdate.getTime()) {
                    throw new RuntimeException("子表记录已经被修改了!");
                }
            }
            return true;
        } finally {
            DBSql.close(ps, reset);
        }
    }
}
