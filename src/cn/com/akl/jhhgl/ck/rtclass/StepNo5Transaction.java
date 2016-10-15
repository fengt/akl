package cn.com.akl.jhhgl.ck.rtclass;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo5Transaction extends WorkFlowStepRTClassA {

	public StepNo5Transaction() {
		super();
	}

	public StepNo5Transaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("���ǩ�յ�");
	}

	@Override
	public boolean execute() {
		
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		
		Connection conn = null;
		
		try{
			conn = DBSql.open();
			conn.setAutoCommit(false);
			
			Hashtable<String, String> boData = BOInstanceAPI.getInstance().getBOData("BO_AKL_CKD_HEAD", bindid);
			Vector<Hashtable<String, String>> boDatas = BOInstanceAPI.getInstance().getBODatas("BO_AKL_CKD_BODY", bindid);
			
			Hashtable<String, String> data = new Hashtable<String, String>();
			//���ⵥ�š��ͻ��ɹ����š�Ӧ�պϼƣ����۶��������ֿ⡢������ַ���ֿ���ϵ�ˡ��ֿ���ϵ�˵绰���ֿ���ϵ���ֻ����ֿ���ϵ������
			data.put("CKDH", boData.get("CKDH"));
			data.put("KHCGDH", boData.get("KHCGDH"));
			data.put("CK", boData.get("SHKF"));
			data.put("JHDZ", boData.get("SHDZ"));
			data.put("KFLXR", boData.get("SHFZR"));
			data.put("CKLXRDH", boData.get("SHFZRDH"));
			
			Vector<Hashtable<String, String>> datas = new Vector<Hashtable<String,String>>();
			// ���ϡ��ͺš�Ӧ�ա���Ʒ���ơ��ͻ���Ʒ��ţ����й�����
			for (Hashtable<String, String> hashtable : boDatas) {
				Hashtable<String, String> qsDatas_s = new Hashtable<String, String>();
				qsDatas_s.put("KHSPBH", hashtable.get(""));
				qsDatas_s.put("CPMC", hashtable.get("WLMC"));
				qsDatas_s.put("XH", hashtable.get("XH"));
				qsDatas_s.put("WLH", hashtable.get("WLH"));
				qsDatas_s.put("YSSL", hashtable.get("SL"));
				qsDatas_s.put("DW", hashtable.get("JLDW"));
				datas.add(qsDatas_s);
			}
			
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_P", data, bindid, getUserContext().getUID());
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_S", datas, bindid, getUserContext().getUID());
			
			conn.commit();
			return true;
		} catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
