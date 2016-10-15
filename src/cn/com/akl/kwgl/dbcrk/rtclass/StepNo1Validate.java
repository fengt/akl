package cn.com.akl.kwgl.dbcrk.rtclass;

import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.kwgl.constant.KwglConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	public StepNo1Validate() {
		// TODO Auto-generated constructor stub
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("У������������ⲻ����ͬ��");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		Vector vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_DBCRK_S", bindid);
		String sql = "select * from BO_AKL_DBCRK_P where bindid="+bindid;
		String zcck = DBSql.getString(sql, "ZCCK");//ת���ֿ�
		String zrck = DBSql.getString(sql, "ZRCK");//ת��ֿ�
		String sfdgk = DBSql.getString("SELECT SFDGK FROM " +KwglConstant.table4+ " WHERE CKDM='"+zcck+"'", "SFDGK");//�Ƿ���ܿ�
		if(zrck.equals(zcck)){
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��������͵�����ⲻ����ͬһ�ֿ⣬������ѡ��", true);
			return false;
		}
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable records = (Hashtable)vector.get(i);
				String wlbh = records.get("WLBH").toString();//���ϱ��
				String pch = records.get("TZQPC").toString();//����ǰ����
				String tzck = records.get("TZHCK").toString();//�����ֿ�
				String tzqkw = records.get("TZQKW").toString();//����ǰ��λ
				int tzqsl = Integer.parseInt(records.get("TZQSL").toString());//����ǰ����
				int tzhsl = Integer.parseInt(records.get("TZHSL").toString());//����������

				//a.����������Ϊ��У��
				if(tzhsl == 0){
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "�����������������Ϊ�㣬�����������������", true);
					return false;
				}

				//b.�������ε�����У��
				if(sfdgk.equals(KwglConstant.isProxies0)){//���ܿ�
					String QUERY_DGSK = "SELECT SUM(ISNULL(XSSL,0))ZSDSL FROM BO_AKL_DGCKSK WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND HWDM='"+tzqkw+"'";
					int num = DBSql.getInt(QUERY_DGSK, "ZSDSL");//����������
					if(tzqsl - num < tzhsl){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���������Ρ�"+wlbh+","+pch+","+tzqkw+"����治�㣬�ݲ��ܵ�����");
						return false;
					}
				}else{//��Ӫ��
					String QUERY_SK = "SELECT SUM(ISNULL(SDSL,0))ZSDSL FROM BO_AKL_KC_SPPCSK WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND CKDM='"+zcck+"'";
					int num = DBSql.getInt(QUERY_SK, "ZSDSL");//����������
					if(tzqsl - num < tzhsl){
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "���������Ρ�"+wlbh+","+pch+"����治�㣬�ݲ��ܵ�����");
						return false;
					}
				}
			}
		}

		return true;
	}

}
