package cn.com.akl.ccgl.cgrk.rtclass;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.biz.CreatePCHBiz;
import cn.com.akl.ccgl.cgrk.biz.RKDBiz;
import cn.com.akl.ccgl.cgrk.biz.ZCXXBiz;
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

public class StepNo1Transaction extends WorkFlowStepRTClassA{
	
	private static final String UPDATE_PCH = "UPDATE BO_AKL_CCB_RKD_BODY SET PCH=? WHERE id=?";
	private static final String UPDATE_CGDD_P_ZT = "UPDATE BO_AKL_CGDD_HEAD SET DDZT =? WHERE DDID =?";
	private static final String UPDATE_CGDD_S_ZT = "UPDATE BO_AKL_CGDD_BODY SET ZT =?,ZTSL=ISNULL(ZTSL,0)+? WHERE DDID =? AND XH =?";
	
	private static final String QUERY_KHDM = "SELECT TOP 1 KHDM FROM BO_AKL_CCB_RKD_ZCXX WHERE BINDID =?";
	
	
	private Connection conn = null;
	private UserContext uc;
	
	public StepNo1Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("������Ϻ󣬸��²ɹ������Ķ���״̬Ϊ����ת�֡���������ܼ���ϸ");
	}
	
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//��ⵥͷ����
		Vector<Hashtable<String, String>> sVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);//��ⵥ����
		String rkdb = pTable.get("RKDB").toString();//��ⵥ��
		String rkdh = pTable.get("RKDH").toString();//��ⵥ��
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());//ת������
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			
			/**
			 * ������ⵥ���ж��Ƿ�ת��
			 */
			if(rkdb.equals(CgrkCnt.rkdb0)){
				dealDatas(conn,uc,bindid);//��ת����Ϣ��������ⵥ��
				//sVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);
				sVector = RKDBiz.getRKDVector(conn, bindid);
				updateZcWlbh(conn,sVector,bindid);//����ⵥ�����ݵ����ϱ���ٷ�д��ת����ϸ��
			}else{//����ת����Ϣ����⣬�������κ�
				if(sVector != null){
					for(int i=0; i < sVector.size(); i++){
						Hashtable<String, String> record = sVector.get(i);
						int id = Integer.parseInt(record.get("ID").toString());
						String pch = CreatePCHBiz.createPCH(zcrq);
						int n = DAOUtil.executeUpdate(conn, UPDATE_PCH, pch, id);
						if(n != 1) throw new RuntimeException("ϵͳδ�ܲ����õ����κţ�����ϵ����Ա��");
					}
				}
			}
			
			/**
			 * ���ݿͻ������ж��Ƿ�Կ����ܡ���ϸ�Ͳɹ���Ϣ����
			 * 1������ⵥͷ���������������Ϣ�����������ܱ������ϸ��
			 * 2������ת�ֱ��еĿͻ������ţ����²ɹ���������״̬�� 
			 */
			String khdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QUERY_KHDM, bindid));//��ȡ�ͻ�����
			if("".equals(khdm) || khdm.equals(CgrkCnt.khdm0)){
				if(!CgrkCnt.rkdb3.equals(rkdb) && !CgrkCnt.rkdb5.equals(rkdb)){//��������⡢����ͨ��⣬��Ϊ(���ϲɹ���⣬�ز���⣬�����ɹ���⣬BG�ɹ����)
					UpdateCgddZT(conn, bindid);//���²ɹ���״̬�Ͳɹ���;����
				}
				dealDatas(conn, uc, pTable, sVector,rkdb);
			}else{
				/*����ܿ����ܱ��������*/
				Vector<Hashtable<String, String>> tmpPvector = RKDBiz.RKDPackageV1(conn, pTable);
				Vector<Hashtable<String, String>> rePvector = RKDBiz.getPvectorV2(pTable, tmpPvector,rkdb);
				BOInstanceAPI.getInstance().createBOData(conn,CgrkCnt.tableName12, rePvector, bindid, getUserContext().getUID());
				conn.commit();
			}
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨");
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * ���²ɹ�����״̬
	 * @param bindid
	 */
	public void UpdateCgddZT(Connection conn, int bindid) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select cgddh,xh,sssl from " + CgrkCnt.tableName1 + " where bindid = " + bindid;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs!=null){
				while(rs.next()){
					String aklOrderId = StrUtil.returnStr(rs.getString("cgddh"));
					String lh = StrUtil.returnStr(rs.getString("xh"));
					int sssl = rs.getInt("sssl");
					if(StrUtil.isNotNull(aklOrderId)){
						String zt0 = judge(conn,aklOrderId,bindid);
						String zt1 = judge(conn,aklOrderId,lh,sssl);
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_P_ZT, zt0, aklOrderId);//���µ�ͷ״̬
						DAOUtil.executeUpdate(conn, UPDATE_CGDD_S_ZT, zt1, sssl, aklOrderId, lh);//���µ���״̬����;����
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
	public String judge(Connection conn, String khddh, int bindid) throws SQLException{
		String str0 = "SELECT SUM(CGSL)AS CGSL,SUM(YRKSL)AS YRKSL FROM BO_AKL_CGDD_BODY WHERE DDID='"+khddh+"'";
		String str1 = "SELECT SUM(SSSL)AS SSSL FROM BO_AKL_CCB_RKD_BODY WHERE CGDDH='"+khddh+"' AND BINDID="+bindid;
		int cgsl = DBSql.getInt(conn, str0, "CGSL");//�ɹ�������
		int yrksl = DBSql.getInt(conn, str0, "YRKSL");//�����������
		int sssl = DBSql.getInt(conn, str1, "SSSL");//��������ʵ��������
		if(cgsl-yrksl == sssl ){
			return CgrkCnt.ddzt0;//��ת��
		}else{
			return CgrkCnt.ddzt1;//����ת��
		}
	}
	
	/**
	 * �ж��Ƿ���ת�֡����ǲ���ת��(��Ե���״̬)
	 * @param conn
	 * @param khddh
	 * @param lh
	 * @param chsl
	 * @return
	 */
	public String judge(Connection conn,String khddh,String xh,int sssl) throws SQLException{
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
					int yrksl = rs.getInt("YRKSL");
					wrksl = cgsl - yrksl;
					if(wrksl==sssl){
						return CgrkCnt.ddzt0;//��ת��
					}else if(sssl<wrksl){
						return CgrkCnt.ddzt1;//����ת��
					}
				}
			}
		}finally{
			DBSql.close(ps, rs);
		}
		return CgrkCnt.ddzt3;//ת�������쳣
	}
	
	
	public void dealDatas(Connection conn, UserContext uc, int bindid) throws Exception{
		/**��һ������ת����ϸ��������ⵥ��**/
		Vector<Hashtable<String, String>> zcVector = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName2, bindid);
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);
		ZCXXBiz zxUtil = new ZCXXBiz();
		Date zcrq = Date.valueOf(pTable.get("ZCRQ").toString());
		/**�ڶ�������װ��ⵥ�����ݲ�д��**/
		Vector<Hashtable<String, String>> reZcVector = zxUtil.getZcxx(conn,pTable,zcVector,zcrq);
		BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName1, reZcVector, bindid, uc.getUID());
	}
	
	/**
	 * ����ⵥ�����ݵ����ϱ���ٷ�д��ת����ϸ��
	 * @param sVector
	 * @param bindid
	 */
	public void updateZcWlbh(Connection conn,Vector sVector,int bindid) throws Exception{
		String str = "update " +CgrkCnt.tableName2+ " set wlbh=? where lh=? and khddh=? and bindid =?";
		if(sVector != null){
			for (int i = 0; i < sVector.size(); i++) {
				Hashtable rec = (Hashtable)sVector.get(i);
				String wlbh = rec.get("WLBH").toString();
				String xh = rec.get("XH").toString();
				String cgddh = rec.get("CGDDH").toString();
				DAOUtil.executeUpdate(conn, str, wlbh,xh,cgddh,bindid);
			}
		}
	}
	
	public void dealDatas(Connection conn, UserContext uc, 
			Hashtable<String,String> pTable,Vector<Hashtable<String,String>> sVector,String rkdb) throws SQLException,AWSSDKException{
		int processInstanceId = 0;
		//д������ܱ�����
		Vector<Hashtable<String,String>> rePvector = new Vector<Hashtable<String,String>>();
		Vector<Hashtable<String,String>> tmpPvector = new Vector<Hashtable<String,String>>();
		tmpPvector = RKDBiz.RKDPackageV1(conn, pTable);
		rePvector = RKDBiz.getPvectorV2(pTable,tmpPvector,rkdb);
		//д������ϸ�ӱ�����
		Vector<Hashtable<String,String>> reSvector = new Vector<Hashtable<String,String>>();
		Vector<Hashtable<String,String>> tmpSvector = new Vector<Hashtable<String,String>>();
		tmpSvector = RKDBiz.RKDPackageV2(conn, pTable);
		reSvector = RKDBiz.getSvectorV2(tmpSvector);
		processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance(CgrkCnt.uuid, uc.getUID(), "���ά��");
		int[] processTaskInstanceIds = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uc.getUID(), processInstanceId,  1, uc.getUID(), "���ά��" ); 
		int[] boIds = BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName3, rePvector, processInstanceId, uc.getUID());
		int[] boIds2 = BOInstanceAPI.getInstance().createBOData(conn, CgrkCnt.tableName4, reSvector, processInstanceId, uc.getUID()); 
		conn.commit();
		WorkflowInstanceAPI.getInstance().closeProcessInstance(uc.getUID(), processInstanceId, processTaskInstanceIds[0]);
	}
	
	
}
