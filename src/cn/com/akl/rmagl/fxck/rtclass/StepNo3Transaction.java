package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.biz.FillBiz;
import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {
	/**
	 * ��ѯ���ⵥ����.
	 */
	private final String QUERY_CKD_BODY = "SELECT KWBH, SL, WLH, PC FROM BO_AKL_CKD_BODY WHERE BINDID=?";
	/**
	 * ��������.
	 */
	private KCBiz kcbiz = new KCBiz();

	public StepNo3Transaction() {
		super();
	}
	
	public StepNo3Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("������ת���¼�: ���¿��");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		try{
			conn = DAOUtil.openConnectionTransaction();
			String xsddh = DAOUtil.getString(conn, "SELECT RMAFXDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			
			// 1���������
			DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_KC_SPPCSK WHERE DDH=?", xsddh);
			
			// 2�����¿����ϸ
						// ��ѯ�ӱ��¼���ۼ����
						DAOUtil.executeQueryForParser(conn, QUERY_CKD_BODY, new DAOUtil.ResultPaser() {
							public boolean parse(Connection conn, ResultSet reset) throws SQLException {
								kcbiz.outOfWarehouseHZ(conn, reset.getString("WLH"), reset.getString("PC"), reset.getString("KWBH"), reset.getInt("SL"));
								kcbiz.outOfWarehouseMX(conn, reset.getString("WLH"), reset.getString("PC"), reset.getInt("SL"));
								return true;
							}
						}, bindid);
			
			// �������۶���״̬
			DAOUtil.executeUpdate(conn, "Update BO_AKL_WXB_RMAFX_P Set DDZT=3 WHERE FHDH=?", xsddh);
			
			String sfyy = DAOUtil.getString(conn, "SELECT SFYY FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			FillBiz fillBiz = new FillBiz();

			if("��".equals(sfyy)||XSDDConstant.NO.equals(sfyy)){
				// ��ԤԼ ����˵�
				fillBiz.fillYD(conn, bindid, getUserContext().getUID());
			} else {
				// ���ԤԼ��
				fillBiz.fillYYD(conn, bindid, getUserContext().getUID());
			}
			
			conn.commit();
			return true;
		} catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch(Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
}
