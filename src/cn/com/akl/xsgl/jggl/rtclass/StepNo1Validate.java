package cn.com.akl.xsgl.jggl.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public static final String QUERY_WLBH = "SELECT COUNT(*)AS m FROM BO_AKL_WLXX WHERE WLBH=?";
	public static final String QUERY_GYSBH = "SELECT COUNT(*)AS n FROM BO_AKL_GYS_P WHERE GYSBH=?";
	
	private Connection conn = null;
	private UserContext uc;
	public StepNo1Validate() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Validate(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("У�����ۼ۸�������ϱ�ź͹�Ӧ�̱���Ƿ���ȷ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas("BO_AKL_JGGL", bindid);
		conn = DBSql.open();
		
		try {
			if(body != null){
				for (int i = 0; i < body.size(); i++) {
					Hashtable<String, String> rec = body.get(i);
					String wlbh = rec.get("WLBH").toString();//���ϱ��
					String gysbh = rec.get("GYSBH").toString();//��Ӧ�̱��
					int m = DAOUtil.getInt(conn, QUERY_WLBH, wlbh);
					int n = DAOUtil.getInt(conn, QUERY_GYSBH, gysbh);
					if(m<=0){
						MessageQueue.getInstance().putMessage(uc.getUID(), "���ϱ�š�"+wlbh+"�������ڣ���˲飡");
						return false;
					}else if(n<=0){
						MessageQueue.getInstance().putMessage(uc.getUID(), "��Ӧ�̱�š�"+gysbh+"�������ڣ���˲飡");
						return false;
					}
				}
			}else{
				MessageQueue.getInstance().putMessage(uc.getUID(), "�۸���ϢΪ�գ������µ��룡");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DBSql.close(conn, null, null);
		}
		return true;
	}

}
