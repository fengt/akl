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
        setDescription("У�����޵��Ƿ���Խ����޸�.");
    }

    @Override
    public boolean execute() {
        // ��֤���޵������µ�ʱ���.
        int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
        String uid = getUserContext().getUID();

        Connection conn = null;
        try {
            conn = DBSql.open();
            Date nowtime = DAOUtil.getDateOrNull(conn, SXTZConstant.QUERY_FORM_LASTTIME, bindid);
            String sxdh = DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_FORM_SXDH, bindid);
            Date lastTime = DAOUtil.getDateOrNull(conn, SXTZConstant.QUEYR_OLD_FORM_LASTTIME, sxdh);
            String yywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_YYWLX, bindid));//ԭҵ������
            String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_YWLX, bindid));//ҵ������
            
            if(SXCnt.ywlx0.equals(yywlx) || SXCnt.ywlx1.equals(yywlx)){//ԭҵ�����ͣ����ۻ�����
            	if(!yywlx.equals(ywlx)){
            		throw new RuntimeException("���ۻ����͵ġ�ҵ�����͡��ֶ��ݲ�֧�ֵ�����");
            	}
        	}
            
            /**
             *  ��֤�Ƿ񽻸����������ѱ����ã���������ɾ���������ٵ���
             */
            ArrayList<String> jfdList = DAOUtil.getStringCollection(conn, SXTZConstant.QUERY_isExsitJFD, 0, sxdh);
            ArrayList<String> jfdListEnd = DAOUtil.getStringCollection(conn, SXTZConstant.QUERY_isExsitJFD, 1, sxdh);
            if(jfdList.size() > 0){
            	throw new RuntimeException("��ɾ�������޵���Ӧ�Ľ�����"+jfdList.toString()+"���ٽ��е�����");
            }else if(jfdListEnd.size() > 0){
            	throw new RuntimeException("�����޵��ѽ��������޷�������");
            }
            
            // �Ա��������.
            if (nowtime != null && lastTime != null && nowtime.equals(lastTime)) {
                // �Ƚ��ӱ�ʱ�����.
                return validateSub(conn, bindid);
            } else {
                throw new RuntimeException("�˵������޸ģ���ǰ�޸ĵĵ����Ѿ����������ݵ������̵����ˣ��������������޵���");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    /**
     * �Ա��ӱ�ʱ�����.
     *
     * @param conn
     * @param bindid
     * @return
     */
    public boolean validateSub(Connection conn, int bindid) throws SQLException {
        // ���ӱ��¼���������޵���.
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(SXTZConstant.QUERY_NEW_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                // ��֤�ӱ�����.
                Timestamp newdate = reset.getTimestamp("LASTTIME");
                int parentid = reset.getInt("PARENTID");
                Date olddate = DAOUtil.getDateOrNull(conn, SXTZConstant.QUERY_OLD_FORM_LASTTIME, parentid);
                if (olddate == null) {
                    throw new RuntimeException("ԭ�ӱ��¼�޸�ʱ���޷��ҵ�!");
                }
                if (olddate.getTime() != newdate.getTime()) {
                    throw new RuntimeException("�ӱ��¼�Ѿ����޸���!");
                }
            }
            return true;
        } finally {
            DBSql.close(ps, reset);
        }
    }
}
