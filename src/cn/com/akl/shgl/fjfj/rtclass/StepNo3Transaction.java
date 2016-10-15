package cn.com.akl.shgl.fjfj.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	
	private Connection conn = null;
	private UserContext uc;
	private ShipmentsBiz shipBiz = new ShipmentsBiz();
	public StepNo3Transaction() {
		// TODO Auto-generated constructor stub
	}

	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("�ۼ��ͷ����Ŀ�档");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		int stepNo = 3;//����ýڵ��
		try {
			conn = DAOUtil.openConnectionTransaction();
			
			/**1�������*/
			FJFJBiz.decreaseKCXX(conn, bindid, stepNo);
			
			/**2�������ܲ����ⷿ��¼*/
			insertKCXX(conn, bindid, uid);
			
			/**3�����������¼*/
			shipBiz.insertShipments(conn, bindid, uid, FJFJCnt.jlbz0);
			
			/**3�����µ���״̬*/
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_P_ZT, FJFJCnt.djzt0, bindid);
			DAOUtil.executeUpdate(conn, FJFJCnt.UPDATE_FJFJ_S_ZT, FJFJCnt.djzt0, bindid);
			
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
	 * �ܲ����ⷿ�����¼
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param xmlb
	 * @param ckdm
	 * @throws SQLException
	 */
	public void insertKCXX(Connection conn, final int bindid, final String uid)throws SQLException{
		final String xmlb = StrUtil.returnStr(DAOUtil.getStringOrNull(conn, FJFJCnt.QUERY_XMLB, bindid));//��Ŀ���
		DAOUtil.executeQueryForParser(conn, FJFJCnt.QUERY_FJFJ, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				FJFJBiz.insertXLH(conn, bindid, uid, rs, xmlb);
				return true;
			}
		}, bindid);
	}
	
}



