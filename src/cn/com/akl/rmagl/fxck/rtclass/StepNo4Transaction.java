package cn.com.akl.rmagl.fxck.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo4Transaction extends WorkFlowStepRTClassA {

	public StepNo4Transaction() {
		super();
	}

	public StepNo4Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("����˵�");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DAOUtil.openConnectionTransaction();			
			// 4-5 ���ⵥ+ԤԼ������˵�
			// 4-6 ���ⵥ+ԤԼ������˵�����������
			
			fillYD(conn, bindid);

			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	/**
	 * ԤԼ��+���ⵥ����˵�
	 * @param conn
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	private void fillYD(Connection conn, int bindid) throws SQLException, AWSSDKException{
		Hashtable<String, String> qsData = new Hashtable<String, String>();
		//���ⵥ�š��ͻ��ɹ����š�Ӧ�պϼƣ����۶��������ֿ⡢������ַ���ֿ���ϵ�ˡ��ֿ���ϵ�˵绰���ֿ���ϵ���ֻ����ֿ���ϵ������
		PreparedStatement ckPs = null;
		ResultSet ckReset = null;
		try{
			ckPs = conn.prepareStatement("SELECT CKDH,RMAFXDH,TJ,ZL FROM BO_AKL_CKD_HEAD WHERE BINDID=?");
			ckReset = DAOUtil.executeFillArgsAndQuery(conn, ckPs, bindid);
			if(ckReset.next()){
				qsData.put("CKDH",ckReset.getString("CKDH"));
				qsData.put("DDH",ckReset.getString("RMAFXDH"));
				qsData.put("TJ",ckReset.getString("TJ"));
				qsData.put("ZL",ckReset.getString("ZL"));
			}
		} finally {
			DBSql.close(ckPs, ckReset);
		}
		
		PreparedStatement yydPs = null;
		ResultSet yydReset = null;
		try{
			yydPs = conn.prepareStatement("SELECT CYS,CYSDH,CYSDZ,CYSLXR FROM BO_BO_AKL_CK_YY_P WHERE BINDID=?");
			yydReset = DAOUtil.executeFillArgsAndQuery(conn, yydPs, bindid);
			if(yydReset.next()){
				qsData.put("CYS",parseNull(ckReset.getString("CYS")));
				qsData.put("LXFS",parseNull(ckReset.getString("CYSDH")));
				qsData.put("LXR",parseNull(ckReset.getString("CYSLXR")));
			}
		} finally {
			DBSql.close(yydPs, yydReset);
		}
		
		if(qsData.size() != 0)
		// ���ϡ��ͺš�Ӧ�ա���Ʒ���ơ��ͻ���Ʒ��ţ����й�����
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YD_P", qsData, bindid, getUserContext().getUID());
	}
	
	public String parseNull(String str){
		return str==null?"":str;
	}
}
