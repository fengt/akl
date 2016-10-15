package cn.com.akl.ccgl.cgrk.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.biz.CheckBiz;
import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA{

	private static final String QUERY_CNT = "SELECT COUNT(*) AS CNT FROM BO_AKL_CCB_RKD_BODY WHERE BINDID=? AND WLBH=? AND CGDDH=?";
	private static final String QUERY_WLBH = "SELECT WLBH FROM BO_AKL_CCB_RKD_BODY WHERE BINDID=? AND WLBH=? AND CGDDH=? GROUP BY WLBH,CGDDH HAVING(WLBH)>1";
	
	private UserContext uc;
	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("zhangran");
		setDescription("V1.0");
		setDescription("�����뵥������Ψһ�Ҳ�Ϊ���Լ���������Ƿ���ת���������!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector<Hashtable<String, String>> vector1 = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName1, bindid);//��ⵥ����
		Vector<Hashtable<String, String>> vector2 = BOInstanceAPI.getInstance().getBODatas(CgrkCnt.tableName2, bindid);//ת����ϸ��
		Hashtable<String, String> pTable = BOInstanceAPI.getInstance().getBOData(CgrkCnt.tableName0, bindid);//�ɹ���ⵥͷ
		String rkdb = pTable.get("RKDB").toString();
		
		
		/**������ⵥ���ж��Ƿ���Ҫת����Ϣ����**/
		if(rkdb.equals(CgrkCnt.rkdb0)){
			/*�ж�ת����Ϣ��Ϊ��*/
			if(vector2 != null && vector1 == null){
				/*a.�ж��Ϻ��Ƿ������Ψһ;b.�ж��Ƿ�ת����ϢУ��*/
				return CheckBiz.XHCheck(uc, vector2, bindid);
			}else if(vector2 == null && vector1 == null){
				MessageQueue.getInstance().putMessage(uc.getUID(), "����ת����Ϣ����Ϊ�գ����������ݣ�");
				return false;
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "��������������ⵥ����Ϣ�������ݣ���ɾ����");
				return false;
			}
		}else{
			/*�ж���ⵥ��Ϊ��*/
			if(vector1 == null || vector2 != null){
				MessageQueue.getInstance().putMessage(uc.getUID(), "������ⵥ��Ϊ�ջ������ת����Ϣ�������ݣ����������룡");
				return false;
			}else{
				return wlbhCheck(uc,vector1,bindid,rkdb);
			}
			
			//ҵ����չʱ����
			/*else if(CgrkCnt.rkdb1.equals(rkdb)){//�ز����
				
			}else if(CgrkCnt.rkdb2.equals(rkdb)){//�����ɹ����
				
			}else{//�������
				
			}*/
		}
	}
	
	/**
	 * У�����ϲ����ظ����Ҽ۸�����
	 * @param uc
	 * @param vector
	 * @param bindid
	 * @return
	 */
	public boolean wlbhCheck(UserContext uc, Vector<Hashtable<String, String>> vector, int bindid, String rkdb){
		Connection conn = null;
		for (int i = 0; i < vector.size(); i++) {
			Hashtable<String, String> rec = vector.get(i);
			String cgddh = rec.get("CGDDH").toString();
			String wlbh = rec.get("WLBH").toString();
			double wsjg = Double.parseDouble(rec.get("WSJG").toString());//δ˰�۸�
			double hsjg = Double.parseDouble(rec.get("HSJG").toString());//��˰�۸�
			int sssl = Integer.parseInt(rec.get("SSSL").toString());//ʵ������
			try {
				conn = DBSql.open();
				//�����ظ�У��
				int n =  DAOUtil.getInt(conn, QUERY_CNT, bindid,wlbh,cgddh);
				if(n !=1){
					ArrayList<String> list = DAOUtil.getStringCollection(conn, QUERY_WLBH, bindid,wlbh,cgddh);
					if(list.size()>0 && list.size()<10){
						MessageQueue.getInstance().putMessage(uc.getUID(), "�����ظ����Ϻš���Ϣ��" + list.toString());
						return false;
					}else{
						MessageQueue.getInstance().putMessage(uc.getUID(), "�����ظ����Ϻš���Ϣ����ȥ�غ����°���");
						return false;
					}
				}
				//ʵ����������Ϊ��
				if(sssl == 0){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+wlbh+"����ʵ����������Ϊ�㣡");
					return false;
				}
				//����ⵥ��Ϊ�������ʱ�����м۸�Ϊ��У��
				if(!rkdb.equals(CgrkCnt.rkdb3)&&(wsjg == 0.0 || hsjg == 0.0)){
					MessageQueue.getInstance().putMessage(uc.getUID(), "�����ϡ�"+wlbh+"����δ˰�۸��˰�۸���Ϊ�㣡");
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally{
				DBSql.close(conn, null, null);
			}
		}
		return true;
	}
	
	
}