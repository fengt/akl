package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.biz.FJFJBiz;
import cn.com.akl.shgl.fjfj.biz.ShipmentsBiz;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo7Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	private ShipmentsBiz shipBiz = new ShipmentsBiz();
	public StepNo7Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo7Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("�ۼ��ܲ����ⷿ�Ŀ�档");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		int stepNo = 7;//����ýڵ��
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**1���ۼ��ܲ������*/
			FJFJBiz.decreaseKCXX(conn, bindid, stepNo);
			
			/**2�����¿��Ͳ����������¼*/
			increaseKCXXAndShipments(conn, bindid, uid);
			
			/**3���������޵�����ʽ�ͼ���ӱ�"�Ƿ񷵻�"ֵ*/
			setSXDealAndIsBack(conn, bindid);
			
			/**4�����µ���״̬*/
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_P_ZT, FJFJCnt.djzt1, bindid);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_ZT, FJFJCnt.djzt1, bindid);
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * �ӿ��Ͳ����������¼���ͷ���
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void increaseKCXXAndShipments(Connection conn, final int bindid, final String uid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//��Ŀ���
		final String ckdm = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_JCKF, bindid));//�ĳ��ⷿ
		
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//���ϱ��
				String wlmc = StrUtil.returnStr(rs.getString("CPZWMC"));//��������
				String xh = StrUtil.returnStr(rs.getString("PN"));//�ͺ�
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//��������
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
				String ejjl = StrUtil.returnStr(rs.getString("EJJL"));//�������
				String sjjl = StrUtil.returnStr(rs.getString("SJJL"));//�������
				int sl = rs.getInt("SL");//����
				
//				String pch = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_PCH, xmlb, wlbh, hwdm, sx, FJFJCnt.zt4));//��ȡ�����Ͽ�����κ�
				
				/**1���й��ϻ����챻����ģ���ò�Ʒ�����ظ��ͷ�*/
				if(ejjl.equals(FJFJCnt.jcjg0)){
					return true;
				}else if(ejjl.equals(FJFJCnt.jcjg2) && !sjjl.equals(FJFJCnt.jcjg5)){
					return true;
				}
				
				/**2�����¹�����ϸ��Ϣ(�ⷿ��״̬)*/
				Hashtable<String, String> rec = getKFHWXX(conn, xmlb, wlbh, sx, pch, hwdm);
				int updateCount;
				if(rec == null){
					throw new RuntimeException("�ͷ�������ϸ��Ϣ����ʧ�ܣ�����ϵ����Ա��");
				}else{
					updateCount = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX,
							rec.get("CKDM"), rec.get("CKMC"), rec.get("QDM"), rec.get("DDM"), rec.get("KWDM"), hwdm, xmlb, wlbh, pch, gztm);
				}
				
				/**3�����¿����Ϣ*/
				int n = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_IN, sl, xmlb, wlbh, sx, pch, hwdm);
				if(n != 1 || updateCount != 1){
					throw new RuntimeException("������ʧ�ܣ�����ϵ����Ա��");
				}
				
				/**4�������������¼���ӱ�*/
				Hashtable<String, String> body = new Hashtable<String, String>();
				body.put("WLBH", wlbh);
				body.put("WLMC", wlmc);
				body.put("XH", xh);
				body.put("SX", sx);
				body.put("PCH", pch);
				body.put("SL", String.valueOf(sl));
				body.put("QSSL", String.valueOf(sl));
				body.put("HWDM", hwdm);
				body.put("CKDM", ckdm);
				body.put("JLBZ", FJFJCnt.jlbz1);//��¼��ʶ
				
				try {
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DFH_S", body, bindid, uid);
				} catch (AWSSDKException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		}, bindid);
		
		
		/**5�����������¼*/
		shipBiz.insertHead(conn, bindid, uid, FJFJCnt.jlbz1);
	}
	
	
	/**
	 * ��ȡ�ͷ���λ��Ϣ
	 * @param conn
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param hwdm
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> getKFHWXX(Connection conn, String xmlb, String wlbh, String sx, String pch, String hwdm) throws SQLException{
		Hashtable<String, String> rec = new Hashtable<String, String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(FJFJCnt.QUERY_HWXX);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, sx, pch, hwdm);
			while(rs.next()){
				rec.put("CKDM", StrUtil.returnStr(rs.getString(1)));
				rec.put("CKMC", StrUtil.returnStr(rs.getString(2)));
				rec.put("QDM", StrUtil.returnStr(rs.getString(3)));
				rec.put("DDM", StrUtil.returnStr(rs.getString(4)));
				rec.put("KWDM", StrUtil.returnStr(rs.getString(5)));
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return rec;
	}
	
	
	/**
	 * �������޴���ʽ������
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setSXDealAndIsBack(Connection conn, final int bindid) throws SQLException{
		final int sxBindid = DAOUtil.getInt(conn, FJFJCnt.QUERY_SX_BINDID, bindid);//����BINDID
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//���ϱ��
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//��������
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
				String hwdm2 = StrUtil.returnStr(rs.getString("HWDM2"));//����λ����
				String ejjl = StrUtil.returnStr(rs.getString("EJJL"));//�������
				String sjjl = StrUtil.returnStr(rs.getString("SJJL"));//�������
				int id = rs.getInt("ID");//�ӱ�ID
				
				String clfs = "";
				String sx2 = "";//�����Ӱ������
				if(ejjl.equals(FJFJCnt.jcjg0)){//�й���
					clfs = FJFJCnt.clfs0;
					sx2 = FJFJCnt.sx0;
				}else if(ejjl.equals(FJFJCnt.jcjg1)){//�޹���
					clfs = FJFJCnt.clfs1;
					sx2 = FJFJCnt.sx1;
				}else if(ejjl.equals(FJFJCnt.jcjg2) && sjjl.equals(FJFJCnt.jcjg5)){//������� && �޹����˻�(�й������� || �޹�������)
					clfs = FJFJCnt.clfs1;
					sx2 = FJFJCnt.sx1;
				}else if(ejjl.equals(FJFJCnt.jcjg2) && !sjjl.equals(FJFJCnt.jcjg5)){//������� && (�й������� || �޹�������)
					clfs = FJFJCnt.clfs0;
					sx2 = FJFJCnt.sx0;
				}
				
				/**1�����¼���ӱ��Ƿ񷵻���.*/
				if(clfs.equals(FJFJCnt.clfs1)){
					int m = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_SFFH, bindid, wlbh, gztm, sx);
					if(m != 1) throw new RuntimeException("����ӱ��Ƿ񷵻�������ʧ�ܣ�");
				}
				
				FJFJBiz.setAttribute(conn, sxBindid, clfs, sx, sx2, xmlb, wlbh, pch, hwdm, hwdm2, gztm, id, false);
				return true;
			}
		}, bindid);
	}
}



