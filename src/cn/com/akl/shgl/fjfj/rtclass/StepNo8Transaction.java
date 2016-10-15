package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo8Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	public StepNo8Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo8Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("���¿ͷ����Ŀ����Ϣ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**1�����¿��״̬*/
			String wlzt = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_WLZT, bindid, FJFJCnt.jlbz1, FJFJCnt.djzt1));//����״̬
			if(!wlzt.equals(FJFJCnt.wlzt)){
				throw new RuntimeException("�õ���δ��ɷ��������޷�����");
			}else{
				setKCMXStatue(conn, bindid);
			}
			
			/**2�����µ���״̬*/
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_P_ZT, FJFJCnt.djzt3, bindid);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_ZT, FJFJCnt.djzt3, bindid);
			
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
	
	/**
	 * �ͷ���漰���к�״̬���£���;-->�ڿ⣩
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setKCMXStatue(Connection conn, final int bindid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));//���ϱ��
				String sx = StrUtil.returnStr(rs.getString("SX"));//����
				String pch = StrUtil.returnStr(rs.getString("PCH"));//���κ�
				String hwdm = StrUtil.returnStr(rs.getString("HWDM"));//��λ����
				String gztm = StrUtil.returnStr(rs.getString("KFGZDM"));//��������
				
				int updateCount1 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_KCMX_ZT, xmlb, wlbh, sx, pch, hwdm);
				int updateCount2 = DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_GZMX_ZT, FJFJCnt.zt4, xmlb, wlbh, pch, gztm);
				if(updateCount1 != 1 || updateCount2 != 1) throw new RuntimeException("������кŸ���ʧ�ܣ�");
				return true;
			}
		}, bindid);
	}
}



