package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.shgl.fjfj.biz.FJFJBiz;
import cn.com.akl.shgl.fjfj.cnt.FJFJCnt;
import cn.com.akl.shgl.sx.cnt.SXCnt;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo5Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	public StepNo5Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo5Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("���ݲ�Ʒ�������ޣ��ж���������ҵ�������");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		
		try {
			conn = DAOUtil.openConnectionTransaction();
			int wgz = DAOUtil.getInt(conn, FJFJCnt.QUERY_WGZ, bindid);
			int gcjc = DAOUtil.getInt(conn, FJFJCnt.QUERY_GCJC, bindid);
			
			if(wgz == 0 && gcjc == 0){//���޹������޹�����⣬��ȫ��Ϊ���ϲ�Ʒ�����̽���
				setRenewForSX(conn, bindid);//���������ӱ�(����ʽ)
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
	
	/**
	 * �������޴���ʽ��ȫΪ���컻�£�
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void setRenewForSX(Connection conn, int bindid) throws SQLException{
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
				int id = rs.getInt("ID");
				
				String clfs = "";
				String sx2 = "";
				if(ejjl.equals(FJFJCnt.jcjg0)){//�й���
					clfs = FJFJCnt.clfs0;
					sx2 = FJFJCnt.sx0;
				}
				
				FJFJBiz.setAttribute(conn, sxBindid, clfs, sx, sx2, xmlb, wlbh, pch, hwdm, hwdm2, gztm, id, true);
				return true;
			}
		}, bindid);
	}
	
}



