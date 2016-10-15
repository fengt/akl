package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGCK_YDTC extends WorkFlowStepRTClassA{
	
	public DGCK_YDTC() {
		super();
	}

	public DGCK_YDTC(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�������˵�");
	}
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			// 4-5 ���ⵥ+ԤԼ������˵�
			// 4-6 ���ⵥ+ԤԼ������˵�����������
			
			fillYD(conn, bindid);
			/*
			Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
			Vector<Hashtable<String, String>> boDatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CKD_BODY", bindid);
			
			Hashtable<String, String> qsData = new Hashtable<String, String>();
			//���ⵥ�š��ͻ��ɹ����š�Ӧ�պϼƣ����۶��������ֿ⡢������ַ���ֿ���ϵ�ˡ��ֿ���ϵ�˵绰���ֿ���ϵ���ֻ����ֿ���ϵ������
			
			qsData.put("DDH", boData.get("CKDH"));
			qsData.put("TJ", boData.get("TJ"));
			qsData.put("ZL", boData.get("SJZL"));
			// ���ϡ��ͺš�Ӧ�ա���Ʒ���ơ��ͻ���Ʒ��ţ����й�����
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YD_P", qsData, bindid, getUserContext().getUID());
			*/
			
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
	private void fillYD(Connection conn, int bindid) throws SQLException, AWSSDKException{
		Hashtable<String, String> qsData = new Hashtable<String, String>();
		//���ⵥ�š��ͻ��ɹ����š�Ӧ�պϼƣ����۶��������ֿ⡢������ַ���ֿ���ϵ�ˡ��ֿ���ϵ�˵绰���ֿ���ϵ���ֻ����ֿ���ϵ������
		String sql = "select a.DDH, sum(b.TJ) as TJ,sum(b.ZL) ZL from BO_BO_AKL_DGCK_S a, BO_AKL_WLXX b where a.WLBH = b.WLBH group by a.DDH";
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(sql);
		while(rs.next()){
			qsData.put("DDH", rs.getString(1));
			qsData.put("TJ", rs.getString(2));
			qsData.put("ZL", rs.getString(3));
		}
		
		String yydMessage = DAOUtil.getString(conn, "SELECT CYS+'{}'+CYSDH+'{}'+CYSDZ+'{}'+CYSLXR FROM BO_BO_AKL_CK_YY_P WHERE BINDID=?", bindid);

		String[] yydSplit = yydMessage.split("\\{\\}");
		qsData.put("CYS", yydSplit[0]);
		qsData.put("LXR", yydSplit[3]);
		qsData.put("LXFS", yydSplit[1]);
		
		// ���ϡ��ͺš�Ӧ�ա���Ʒ���ơ��ͻ���Ʒ��ţ����й�����
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YD_P", qsData, bindid, getUserContext().getUID());
	}

}
