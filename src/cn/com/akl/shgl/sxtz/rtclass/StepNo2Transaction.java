package cn.com.akl.shgl.sxtz.rtclass;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.dgkgl.xsdd.constant.XSDDConstant;
import cn.com.akl.shgl.sx.biz.ShipmentsBiz;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.shgl.sxtz.biz.SXTZBiz;
import cn.com.akl.shgl.sxtz.biz.SXTZConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by huangming on 2015/5/7.
 */
public class StepNo2Transaction extends WorkFlowStepRTClassA {

	SXTZBiz sxtzBiz = new SXTZBiz();
    public StepNo2Transaction() {
    }

    public StepNo2Transaction(UserContext arg0) {
        super(arg0);
        setProvider("huangming");
        setDescription("���ݵ�����������.");
    }

    @Override
    public boolean execute() {
        int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
        int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
        Hashtable<String, String> hashtable = BOInstanceAPI.getInstance().getBOData(SXTZConstant.NEW_TABLE_MAIN, bindid);// ��ȡ���޸ĵ�����.
        String parentBindid = hashtable.get("PARENTBINDID");
        boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ͬ��");
        String uid = getUserContext().getUID();
        Connection conn = null;
        try {
            conn = DAOUtil.openConnectionTransaction();
            String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_XMLB, bindid));//��Ŀ���
            String ywlx = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_YWLX, bindid));//ҵ������
            String sxdh = DAOUtil.getStringOrNull(conn, SXTZConstant.QUERY_FORM_SXDH, bindid);
            ArrayList<String> jfdList = DAOUtil.getStringCollection(conn, SXTZConstant.QUERY_isExsitJFD, 1, sxdh);
            
            if(yes){
            	/**
            	 * 1�����޿�����
            	 */
            	if(!SXCnt.ywlx0.equals(ywlx) && !SXCnt.ywlx1.equals(ywlx)){//��ҵ�����ͣ����ۺ�����
            		sxtzBiz.repositoryHandle(conn, bindid, uid, parentBindid, xmlb);
            	}
            	
            	/**
            	 * 2������������
            	 */
            	if(jfdList.size() > 0){
            		for(String jfdh : jfdList){
//            			sxtzBiz.jfdHandle(conn, bindid, jfdh, xmlb);
            		}
            	}
            	
            	/**
            	 * 4�����޵��ݸ���
            	 */
            	fillBackData(conn, bindid, uid, hashtable, parentBindid, sxdh);
            }
            
            conn.commit();
            return true;
        } catch (RuntimeException e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, e.getMessage(), true);
            return false;
        } catch (Exception e) {
            DAOUtil.connectRollBack(conn);
            e.printStackTrace();
            MessageQueue.getInstance().putMessage(uid, "��̨�����쳣���������̨", true);
            return false;
        } finally {
            DBSql.close(conn, null, null);
        }
    }

    public void fillBackData(Connection conn, int bindid, String uid, 
    		Hashtable<String, String> hashtable, String parentBindid, String sxdh) throws AWSSDKException, SQLException {
        String ysxfs = DAOUtil.getString(conn, SXTZConstant.QUERY_YSXFS, bindid);
        String sxfs = DAOUtil.getString(conn, SXTZConstant.QUERY_SXFS, bindid);
        String parentid = hashtable.get("PARENTID");
        if (parentid == null || parentid.equals("") || parentBindid == null || parentBindid.equals("")) {
            throw new RuntimeException("��ѡ����Ҫ�޸ĵĵ���!");
        }

        // �Ա�ԭ�ӱ��¼ʱ��������ӱ��¼ʱ���Ƿ�һ�£���һ�����ܽ��и��¡�

        /** 
         * �ȸ��µ������ݵ�״̬��������&&�Ѽ�⣩
         * �ٸ���ԭ����������Ϣ
         */
        hashtable.put("ZT", SXCnt.zt1);
        hashtable.put("ISEND", String.valueOf(1));//����ԭ���޵�ISENDΪ1
        BOInstanceAPI.getInstance().updateBOData(conn, SXTZConstant.OLD_TABLE_MAIN, hashtable, Integer.parseInt(parentid));

        // ɾ��ԭ���ӱ��¼��
        BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, SXTZConstant.OLD_TABLE_SUB, Integer.parseInt(parentBindid));

        // ���ӱ��¼���������޵���.
        PreparedStatement ps = null;
        ResultSet reset = null;
        try {
            ps = conn.prepareStatement(SXTZConstant.QUERY_NEW_FORM_BODY);
            reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
            while (reset.next()) {
                Hashtable<String, String> newHashtable = new Hashtable<String, String>();
                // ���ӱ���д����������ԭ���޵�.
                newHashtable.put("WLBH", PrintUtil.parseNull(reset.getString("WLBH")));
                newHashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
                newHashtable.put("XH", PrintUtil.parseNull(reset.getString("XH")));
                newHashtable.put("JG", PrintUtil.parseNull(reset.getString("JG")));
                newHashtable.put("ZT", SXCnt.zt2);//����Ʒ
                newHashtable.put("YKTMH", PrintUtil.parseNull(reset.getString("YKTMH")));
                newHashtable.put("GZYY", PrintUtil.parseNull(reset.getString("GZYY")));
                newHashtable.put("SFSJ", PrintUtil.parseNull(reset.getString("SFSJ")));
                newHashtable.put("SJLX", PrintUtil.parseNull(reset.getString("SJLX")));
                newHashtable.put("SJMS", PrintUtil.parseNull(reset.getString("SJMS")));
                newHashtable.put("PFJG", PrintUtil.parseNull(reset.getString("PFJG")));
                newHashtable.put("SFTP", PrintUtil.parseNull(reset.getString("SFTP")));
                newHashtable.put("TPH", PrintUtil.parseNull(reset.getString("TPH")));
                newHashtable.put("PFMS", PrintUtil.parseNull(reset.getString("PFMS")));
                newHashtable.put("CLYJ", PrintUtil.parseNull(reset.getString("CLYJ")));
                newHashtable.put("SN", PrintUtil.parseNull(reset.getString("SN")));
                newHashtable.put("GMRQ", PrintUtil.parseNull(reset.getString("GMRQ")));
                newHashtable.put("ZBJZRQ", PrintUtil.parseNull(reset.getString("ZBJZRQ")));
                newHashtable.put("ZZJBRQ", PrintUtil.parseNull(reset.getString("ZZJBRQ")));
                newHashtable.put("JBLX", PrintUtil.parseNull(reset.getString("JBLX")));
                newHashtable.put("ZBYY", PrintUtil.parseNull(reset.getString("ZBYY")));
                newHashtable.put("CLFS", PrintUtil.parseNull(reset.getString("CLFS")));
                newHashtable.put("FJ", PrintUtil.parseNull(reset.getString("FJ")));
                newHashtable.put("GZTM", PrintUtil.parseNull(reset.getString("GZTM")));
                newHashtable.put("SL", PrintUtil.parseNull(reset.getString("SL")));
                newHashtable.put("ZBLX", PrintUtil.parseNull(reset.getString("ZBLX")));
                newHashtable.put("GZYYBZ", PrintUtil.parseNull(reset.getString("GZYYBZ")));
                newHashtable.put("SXCPHH", PrintUtil.parseNull(reset.getString("SXCPHH")));
                newHashtable.put("SYRLX", PrintUtil.parseNull(reset.getString("SYRLX")));
                newHashtable.put("SFSCZB", PrintUtil.parseNull(reset.getString("SFSCZB")));
                newHashtable.put("RBLH", PrintUtil.parseNull(reset.getString("RBLH")));
                newHashtable.put("CCPN", PrintUtil.parseNull(reset.getString("CCPN")));
                newHashtable.put("SFDC", PrintUtil.parseNull(reset.getString("SFDC")));
                newHashtable.put("SX", PrintUtil.parseNull(reset.getString("SX")));
                newHashtable.put("HWDM", PrintUtil.parseNull(reset.getString("HWDM")));
                newHashtable.put("PCH", PrintUtil.parseNull(reset.getString("PCH")));
                newHashtable.put("SCSXSN", PrintUtil.parseNull(reset.getString("SCSXSN")));
                newHashtable.put("SJJG", PrintUtil.parseNull(reset.getString("SJJG")));
                newHashtable.put("SJMS2", PrintUtil.parseNull(reset.getString("SJMS2")));
                newHashtable.put("SFZCSJ", PrintUtil.parseNull(reset.getString("SFZCSJ")));
                BOInstanceAPI.getInstance().createBOData(conn, SXTZConstant.OLD_TABLE_SUB, newHashtable, Integer.parseInt(parentBindid), uid);
            }
        } finally {
            DBSql.close(ps, reset);
        }

        /** ����ISEND. */
        DAOUtil.executeUpdate(conn, "UPDATE " + SXTZConstant.OLD_TABLE_SUB + " SET ISEND=1 WHERE BINDID=?", Integer.parseInt(parentBindid));
        DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_SXTZ_P_ZT, bindid);
        DAOUtil.executeUpdate(conn, SXTZConstant.UPDATE_SXTZ_S_ZT, bindid);
        
		/**
    	 * 3�������ʼļ�����״̬����
    	 */
    	if(!ysxfs.equals(sxfs) && SXCnt.sxfs.equals(sxfs)){
    		ShipmentsBiz.insertShipments(conn, Integer.parseInt(parentBindid), uid);//�ʼ����ޣ������������¼
    	}else if(!ysxfs.equals(sxfs) && SXCnt.sxfs.equals(ysxfs)){
    		DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_SX_DFH_P, sxdh);
    		DAOUtil.executeUpdate(conn, SXTZConstant.DELETE_SX_DFH_S, sxdh);
    	}
        
    }

}

