package cn.com.akl.shgl.qhbh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.qhbh.biz.DBBiz;
import cn.com.akl.shgl.qhbh.biz.LockBiz;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo2Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	DBBiz dbBiz = new DBBiz();
	List<Integer> list = null;
	public StepNo2Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("���ͷ��������ݲ���������������̣����������⡣");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "����");
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**
			 * 1��ɾ������
			 */
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_KCSK", bindid);
			
			if(yes){
				/**
				 * 2����������������̲�����
				 */
				subLoadDataAndStartProcess(conn, bindid, uid);
				
				/**
				 * 3������״̬����
				 */
				setStatue(conn, bindid, true);
			}else{
				DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_PCAndHWDM_TH, bindid);
				setStatue(conn, bindid, false);
			}
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			if(list !=null) removeProcess(list);
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			if(list !=null) removeProcess(list);
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * �������ݣ���������������̲���������
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void subLoadDataAndStartProcess(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException{
		Hashtable<String, String> head = null;
		Vector<Hashtable<String, String>> detailBody = null;
		Vector<Hashtable<String, String>> gatherBody = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String fhr = DAOUtil.getString(conn, QHBHCnt.QUERY_USERNAME, uid);//������
		String db_uid = DAOUtil.getString(conn, QHBHCnt.QUERY_USERID, uid);//�������˺�
		String xmlb = DAOUtil.getString(conn, QHBHCnt.QUERY_P_XMLB, bindid);//��Ŀ���
		String bhck = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_FHCKBM, bindid));//�����ֿ�
		
		try{
			ps = conn.prepareStatement(QHBHCnt.QUERY_KFFZR);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while(rs.next()){
				String kfzx = StrUtil.returnStr(rs.getString(1));//�ͷ�����
				String kfmc = StrUtil.returnStr(rs.getString(2));//�ͷ�����
//				String fzr = StrUtil.returnStr(rs.getString(3));//������
//				String db_uid = dbBiz.accountParse(fzr);
				
				/**
				 * 1���������������
				 */
				final String dbdh = RuleAPI.getInstance().executeRuleScript("DB@replace(@date,-)@formatZero(3,@sequencefordateandkey(BO_AKL_DB_P))");//��������
				head = dbBiz.getHead(conn, bindid, dbdh, xmlb, bhck, kfzx, db_uid, fhr);
//				detailBody = dbBiz.getDetailBody(conn, bindid, kfzx);
				gatherBody = dbBiz.getGatherBody(conn, bindid, kfzx, bhck);
				int sub_bindid = dbBiz.startWorkflow(conn, kfmc, db_uid, head, detailBody, gatherBody);
				
				/**
				 * 2��ÿ��������bindid���ϣ������쳣ʱɾ�����е�����
				 */
				list = new ArrayList<Integer>();
				list.add(sub_bindid);
				
				/**
				 * 3��ÿ��������������������
				 */
				DAOUtil.executeQueryForParser(conn, QHBHCnt.QUERY_S_HZ,
						new ResultPaserAbs[]{
						LockBiz.checkInvetoryPaser(),//a����֤���
						LockBiz.insertLockPaser(sub_bindid, uid, dbdh)//b����������
				}, bindid, kfzx);
				
				
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
	}
	
	/**
	 * �쳣����ɾ�������쳣�����ĵ�������
	 * @param list
	 */
	public void removeProcess(List<Integer> list){
		try {
			for(int bindid : list){
				WorkflowInstanceAPI.getInstance().removeProcessInstance(bindid);
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
		}
	}
	
	/**
	 * ����״̬����
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setStatue(Connection conn, final int bindid, final boolean flag) throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, QHBHCnt.QUERY_S,
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//���޵���
				String sxcphh = StrUtil.returnStr(rs.getString("SXCPHH"));//���޲�Ʒ�к�
				String jfcphh = StrUtil.returnStr(rs.getString("JFCPHH"));//������Ʒ�к�
				String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//�Ƿ�����滻
				String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));//ԭ��Ʒ���ϱ��
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//�����ⷿ����
				
				//����ȱ����¼��״̬
				int count = 0;
				if(flag){
					count = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_ZT, QHBHCnt.qhzt1, xmlb, wlbh, sx, jfkfbm, sfjsth, QHBHCnt.qhzt0, sxdh, sxcphh, jfcphh);//����ȱ����¼��״̬
				}else{
					count = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_ZT, QHBHCnt.qhzt2, xmlb, wlbh, sx, jfkfbm, sfjsth, QHBHCnt.qhzt0, sxdh, sxcphh, jfcphh);//����ȱ����¼��״̬
				}
				if(count != 1) throw new RuntimeException("ȱ����¼״̬����ʧ�ܣ�");
				return true;
			}
		}, bindid);
		
		if(flag)
			DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_ZT, QHBHCnt.qhzt1, bindid);//���²����ӱ��״̬
		else
			DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_ZT, QHBHCnt.qhzt0, bindid);//���²����ӱ��״̬
		
	}
	
}
