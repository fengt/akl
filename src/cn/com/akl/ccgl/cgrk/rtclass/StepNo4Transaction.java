package cn.com.akl.ccgl.cgrk.rtclass;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.biz.RKDBiz;
import cn.com.akl.ccgl.cgrk.biz.YFBiz;
import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA{

	private static final String UPDATE_CGDD_P_ZT = "UPDATE BO_AKL_CGDD_HEAD SET DDZT =? WHERE DDID =?";
	private static final String UPDATE_CGDD_S_ZT = "UPDATE BO_AKL_CGDD_BODY SET ZT=?,YRKSL=ISNULL(YRKSL,0)+?,ZTSL=ISNULL(ZTSL,0)-? WHERE DDID=? AND XH=? ";
	private static final String UPDATE_RKRQ = "UPDATE "+CgrkCnt.tableName0+" SET RKRQ=? WHERE BINDID=?";
	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo4Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("������Ϻ󣬸��²ɹ������Ķ���״̬Ϊ�������/�������!");
	}
	
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//��ⵥͷ����
		Vector<Hashtable<String, String>> sVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);
		String rkdb = pTable.get("RKDB").toString();//��ⵥ��
		
		Date date = Calendar.getInstance().getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String now = df.format(date);//��ȡ��ǰʱ��
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			DAOUtil.executeUpdate(conn,UPDATE_RKRQ,now,bindid);//�����������Ϊ��ǰʱ��
			
			/**
			 * ���ݿͻ������ж��Ƿ�Կ����ܡ���ϸ�Ͳɹ���Ϣ����
			 */
			String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//��ȡ�ͻ�����
			if("".equals(khdm) || khdm.equals(CgrkCnt.khdm0)){
				
				dealStatusForKCHZ(conn,sVector,bindid,CgrkCnt.tableName3);//�������ϱ�ź����κŸ��¿����ܱ��"�������"��״̬
				if(!CgrkCnt.rkdb3.equals(rkdb) && !CgrkCnt.rkdb5.equals(rkdb)){//��������⡢����ͨ��⣬��Ϊ(���ϲɹ���⣬�ز���⣬�����ɹ���⣬BG�ɹ����)
					UpdateCgdd(conn,bindid);//���²ɹ�����״̬���������������;����
					YFBiz.insertInfoToYf(conn,bindid,uc,pTable,sVector);//��Ӧ����Ϣ���в���Ӧ����¼
				}			
				deleteAndInsertUtil(conn,uc,pTable,bindid);//ɾ�������ϸ��ת��ʱ���ɵ����ݺͲ������ʱ�µ�����
				
			}else{
				dealStatusForKCHZ(conn,sVector,bindid,CgrkCnt.tableName12);//�������ϱ�ź����κŸ��´��ܿ����ܱ��"�������"��״̬
				Vector<Hashtable<String, String>> tmpSvector = RKDBiz.RKDPackageV2(conn, pTable);
				Vector<Hashtable<String, String>> reSvector = RKDBiz.getSvectorV2(tmpSvector);
				BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName13, reSvector, bindid, getUserContext().getUID());//����ܿ����ϸ���в�������
				conn.commit();
			}
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}
	
	
	
	
	/**
	 * ɾ��ת��ʱ�˴���⣨ͬһ���Σ�����ϸ�����������ݺ�����ϸ��������ʱ�µ�����
	 * @param vector
	 * @param bindid
	 */
	private void deleteAndInsertUtil(Connection conn, UserContext uc, Hashtable<String, String> pTable,int bindid) throws SQLException,AWSSDKException{
		String str = "select pch from " + CgrkCnt.tableName1 + " where bindid = " + bindid;
		String pch1 = DBSql.getString(str, "pch");
		String str2 = "delete from " + CgrkCnt.tableName4 + " where pch = '"+pch1+"'";
		DBSql.executeUpdate(conn, str2);
		
		Vector<Hashtable<String, String>> tmpSvector = RKDBiz.RKDPackageV2(conn, pTable);
		Vector<Hashtable<String, String>> recordDatas = RKDBiz.getSvectorV2(tmpSvector);
		
		int processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance(CgrkCnt.uuid, uc.getUID(), "���ά��");
		int[] processTaskInstanceId = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uc.getUID(), processInstanceId, 1, uc.getUID(), "���ά��");
		BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName4, recordDatas, processInstanceId, uc.getUID());
		conn.commit();
		WorkflowInstanceAPI.getInstance().closeProcessInstance(uc.getUID(), processInstanceId, processTaskInstanceId[0]);
	}
	
	
	/**
	 * ���¿����ܱ��е�״̬Ϊ"����"��"�������"
	 * @param xh
	 * @param pch
	 */
	private void dealStatusForKCHZ(Connection conn, Vector<Hashtable<String, String>> sVector,int bindid,String tablename) throws SQLException{
		for (int i = 0; i < sVector.size(); i++) {
			Hashtable<String, String> rec = sVector.get(i);
			String wlbh = rec.get("WLBH").toString();
			String pch = rec.get("PCH").toString();
			String xh = rec.get("XH").toString();
			String str3 = "select sum(sssl) sssl from " + CgrkCnt.tableName1 + " where wlbh = '"+wlbh+"' and pch = '"+pch+"' and bindid = " + bindid;
			int sssl = DBSql.getInt(str3, "sssl");
			String sql = "update " + tablename +" set rksl = "+sssl+" , pcsl = "+sssl+", zt = '" + CgrkCnt.kczt2 + "' where wlbh = '"+wlbh+"' and PCH = '" + pch +"' and xh = '" + xh + "'";
			int updateCount = DBSql.executeUpdate(conn, sql);
			if(updateCount != 1) throw new RuntimeException("������״̬����ʧ�ܣ�����ϵ����Ա��");
		}
	}
	
	
	/**
	 * ���²ɹ�������״̬�����������
	 * @param bindid
	 */
	private void UpdateCgdd(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select cgddh,xh,sssl,pch from " + CgrkCnt.tableName1 + " where bindid = " + bindid;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					String aklOrderId = StrUtil.returnStr(rs.getString("cgddh"));
					String xh = StrUtil.returnStr(rs.getString("xh"));
					String pch = StrUtil.returnStr(rs.getString("pch"));
					int sssl = rs.getInt("sssl");
					if(StrUtil.isNotNull(aklOrderId) && StrUtil.isNotNull(xh)){
						String zt1 = judege(conn, aklOrderId, xh, sssl);
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_S_ZT,zt1,sssl,sssl,aklOrderId,xh);//���µ���״̬����;����
						String zt0 = judge(conn, aklOrderId);
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_P_ZT, zt0, aklOrderId);//���µ�ͷ״̬
					}
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
	}
	
	/**
	 * �ж��Ƿ���ת�֡����ǲ���ת��(��Ե�ͷ״̬)
	 * @param conn
	 * @param khddh
	 * @return
	 * @throws SQLException
	 */
	public String judge(Connection conn, String khddh) throws SQLException{
		String str = "SELECT SUM(CGSL)AS CGSL,SUM(YRKSL)AS YRKSL FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"'";
		int cgsl = DBSql.getInt(conn, str, "CGSL");//�ɹ�������
		int yrksl = DBSql.getInt(conn, str, "YRKSL");//�����������
		if(cgsl == yrksl){
			return CgrkCnt.ddzt4;//�����
		}else{
			return CgrkCnt.ddzt5;//�������
		}
	}
	
	/**
	 * �ж��Ƿ�����⡢���ǲ������(��Ե���״̬)
	 * @param conn
	 * @param khddh
	 * @param lh
	 * @param chsl
	 * @return
	 */
	private String judege(Connection conn,String khddh,String xh,int sssl) throws SQLException{
		String sql = "select * from " + CgrkCnt.tableName7 + " where ddid = '" + khddh + "' and xh = '" + xh + "'";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			int wrksl = 0;
			if(rs!=null){
				while(rs.next()){
					int cgsl = rs.getInt("cgsl");
					int yrksl = rs.getInt("yrksl");
					wrksl = cgsl - yrksl;
					if(wrksl==sssl){
						return CgrkCnt.ddzt4;//�����
					}else if(sssl<wrksl){
						return CgrkCnt.ddzt5;//�������
					}else{
						return CgrkCnt.ddzt6;//��������쳣
					}
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return CgrkCnt.ddzt6;//��������쳣
	}
	
}
