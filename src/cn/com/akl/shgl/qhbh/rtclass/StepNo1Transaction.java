package cn.com.akl.shgl.qhbh.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.jf.biz.ReplacementRuleBiz;
import cn.com.akl.shgl.qhbh.biz.LockBiz;
import cn.com.akl.shgl.qhbh.biz.SecurityBiz;
import cn.com.akl.shgl.qhbh.cnt.QHBHCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;

public class StepNo1Transaction extends WorkFlowStepRTClassA {
	
	private UserContext uc;
	private Connection conn;
	private SecurityBiz securityBiz = new SecurityBiz();
	public StepNo1Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("�Զ�������������⡣");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			securityBiz.securityAllocationAndLockMaterial(conn, bindid, uid);
			
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
	 * �Զ����������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	@Deprecated
	public void autoAllocationAndLockMaterial(Connection conn, final int bindid, final String uid) throws SQLException{
		final String ydh = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_QHBHDH, bindid));//��������
		final String bhck = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_FHCKBM, bindid));//�����ֿ�
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_P_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, QHBHCnt.QUERY_S,
				new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("YCPWLBH"));//�����Ʒ���ϱ��
//				String wlmc = StrUtil.returnStr(rs.getString("YCPZWMC"));//�����Ʒ��������
				String xh = StrUtil.returnStr(rs.getString("YCPPN"));//�����ƷPN
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String sfjsth = StrUtil.returnStr(rs.getString("SFJSTH"));//�Ƿ�����滻
				String sxdh = StrUtil.returnStr(rs.getString("SXDH"));//���޵���
				String jfkfbm = StrUtil.returnStr(rs.getString("JFKFBM"));//�����ⷿ����
				int sl = rs.getInt("SL");//����
				int id = rs.getInt("ID");//ID
				
				//����������ϵĿ�棬�����治�㣬������Ƿ�����滻�������������������ٴβ��㣬�����ʧ�ܣ� 
				//���flag=true�����ʧ��
				boolean flag = setMaterialHander(conn, bindid, uid, ydh, xmlb, wlbh, sx, bhck, sl, id);
				if(sfjsth.equals(QHBHCnt.is) && flag){
					//�����滻����������
					flag = setReplaceMaterialHander(conn, bindid, uid, ydh, xmlb, wlbh, sx, bhck, sl, id);
				}
				if(flag) throw new RuntimeException("�ò�Ʒ��"+xh+"���޿�棬���޷������");
				
				//����ȱ����¼��״̬
				DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_QHJL_ZT, QHBHCnt.qhzt0, xmlb, sxdh, wlbh, sx, jfkfbm);
				
				return true;
			}
		}, bindid);
	}
	
	
	/**
	 * �����滻�������������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param sqcpsl
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public boolean setReplaceMaterialHander(Connection conn, int bindid, String uid, String ydh, String xmlb, String wlbh, String sx, String ckdm, int sl, int id)
			throws SQLException{
		boolean mark = true;
		ReplacementRuleBiz ruleBiz = new ReplacementRuleBiz();
		List<String> replaceWlbhList = ruleBiz.replaceMaterial(conn, xmlb, wlbh, sx);

		if (replaceWlbhList.size() != 0) {
			// ƴ�Ӵ���������.
			StringBuilder replaceWlbhSb = new StringBuilder(50);
			replaceWlbhSb.append(replaceWlbhList.get(0));
			for (int i = 1; i < replaceWlbhList.size(); i++) {
				replaceWlbhSb.append(",");
				replaceWlbhSb.append(replaceWlbhList.get(i));
			}
		} else {
			throw new RuntimeException("�����ϡ�"+wlbh+"��û�п��滻��Ϣ��");
		}

		// �ҵ��п�������.
		for (String reWlbh : replaceWlbhList) {
			boolean flag = setMaterialHander(conn, bindid, uid, ydh, xmlb, reWlbh, sx, ckdm, sl, id);//���flag=true�����ʧ��
			if(!flag) return mark = false;
		}
		return mark;
	}
	
	
	/**
	 * ���滻�������������
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param ydh
	 * @param xmlb
	 * @param wlbh
	 * @param sx
	 * @param sqcpsl
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public boolean setMaterialHander(Connection conn, int bindid, String uid, String ydh, String xmlb, String wlbh, String sx, String ckdm, int sl, int id) 
			throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean flag = true;//��ʶ����Ƿ�ɹ�
		String wlmc = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_WLMC, wlbh));//��������
		String pn = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, QHBHCnt.QUERY_PN8L, wlbh));//�ͺ�
		try {
			ps = conn.prepareStatement(QHBHCnt.QUERY_KCMX_PCHAndHWDM);
			rs = DAOUtil.executeFillArgsAndQuery(conn, ps, xmlb, wlbh, sx, ckdm);
			while(flag && rs.next()){
				String pch = rs.getString("PCH");
				String hwdm = rs.getString("HWDM");
				int kwsl = rs.getInt("KWSL");
				int lockNum = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.QUERY_SK_SUM, xmlb, wlbh, pch, hwdm, sx));
				if(kwsl > lockNum){
					int useNum = kwsl - lockNum;
					if(useNum >= sl){
						
						/**a���������κͻ�λ*/
						DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_S_PCAndHWDM, wlbh, pn, wlmc, sl, pch, hwdm, QHBHCnt.qhzt0, id);
						
						/**b��������������*/
						int isLockExsit = LockBiz.nullParse(DAOUtil.getIntOrNull(conn, QHBHCnt.isLockExsit, ydh, xmlb, wlbh, pch, hwdm, sx));
						if(isLockExsit >= 1){
							int n = DAOUtil.executeUpdate(conn, QHBHCnt.UPDATE_LockNum, sl, ydh, xmlb, wlbh, pch, hwdm, sx);
							if(n != 1) throw new RuntimeException("�������ʧ�ܣ�");
						}else{
							try {
								LockBiz.insertSK(conn, bindid, uid, ydh, xmlb, pn, wlbh, pch, ckdm, hwdm, sx, sl);
							} catch (AWSSDKException e) {
								throw new RuntimeException(e);
							}
						}
						flag = false;
					}
				}
			}
		} finally{
			DBSql.close(null, ps, rs);
		}
		return flag;
	}
	
}
