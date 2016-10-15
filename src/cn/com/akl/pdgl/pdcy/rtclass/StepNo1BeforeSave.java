package cn.com.akl.pdgl.pdcy.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.dict.util.DictionaryUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	public StepNo1BeforeSave() {
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setProvider("zhangtiesong");
		setDescription("V1.0");
		setDescription("�����̵㵥�Ŵ�����쵥������");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable pdcydtData = this.getParameter(PARAMETER_FORM_DATA).toHashtable();
		Vector v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_PDCYCL_P", bindid);
		//��ȡ��ͷ��Ϣ
		String cydh = pdcydtData.get("CYDH") == null ?"":pdcydtData.get("CYDH").toString();//���쵥��
		String pddh = pdcydtData.get("PDDH") == null ?"":pdcydtData.get("PDDH").toString();//�̵㵥��
		if(tablename.equals("BO_AKL_PDCYCL_P")){
			Vector vc =new Vector();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Connection conn = DBSql.open();
			try {
				//�̵㵥��Ϊ��
				if(pddh == null || pddh.trim().length()==0){
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_PDCYCL_S", bindid); 
					return true;
				}
				//�������ݵ��̵㵥��
				String pddhsql = "SELECT distinct PDDH FROM BO_AKL_PDCYCL_S WHERE BINDID="+bindid+"";
				String pddh1 = DBSql.getString(pddhsql, "PDDH");
				if(pddh.equals(pddh1)){
					return true;
				}
				//��ѯ�̵㵥ͷƷ�Ʊ���
				String ppsql = "select PPBH from BO_AKL_KCPD_P where PDDH='"+pddh+"'";
				String ppbh_tmp = DBSql.getString(conn, ppsql, "PPBH");
				String ppbh = DictionaryUtil.parsePPToName(ppbh_tmp);
				//��ȡ�̵㵥��������Ϣ
				String sql = "select * from BO_AKL_KCPD_FK_S where PDDH='"+pddh+"'";
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				if(rs != null){
					while(rs.next()){
						String wlbh = rs.getString("WLBH") == null ?"":rs.getString("WLBH").toString();//���ϱ��
						String wlmc = rs.getString("WLMC") == null ?"":rs.getString("WLMC").toString();//��������
						String xh = rs.getString("XH") == null ?"":rs.getString("XH").toString();//�ͺ�
						String sx = rs.getString("SX") == null ?"":rs.getString("SX").toString();//����
						String pc = rs.getString("PC") == null ?"":rs.getString("PC").toString();//����
						int kwsl = rs.getInt("KWSL");//��λ����
						int pksjsl = rs.getInt("PKSJSL");//�̿�ʵ������
						String cyyy = rs.getString("CYYY") == null ?"":rs.getString("CYYY").toString();//����ԭ��
						String hwdm = rs.getString("HWDM") == null ?"":rs.getString("HWDM").toString();//��λ����
						//������ⵥ���
						Hashtable recordData = new Hashtable();
						recordData.put("PDDH", pddh);//�̵㵥��
						recordData.put("CYDH", cydh);//���쵥��
						recordData.put("WLBH", wlbh);//���ϱ��
						recordData.put("WLMC", wlmc);//��������
						recordData.put("PPBH", ppbh);//Ʒ�Ʊ��
						recordData.put("XH", xh);//�ͺ�
						recordData.put("SX", sx);//����
						recordData.put("PC", pc);//����
						recordData.put("KWSL", kwsl);//��λ����
						recordData.put("PKSJSL", pksjsl);//�̿�ʵ������
						recordData.put("CYSL", pksjsl-kwsl);//��������
						recordData.put("CYYY", cyyy);//����ԭ��
						recordData.put("KWBM", hwdm);//��λ����
						if(pksjsl-kwsl != 0){
							vc.add(recordData);
						}
					}
					//ɾ������
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_PDCYCL_S", bindid); 
					//�������ݿ�
					BOInstanceAPI.getInstance().createBOData("BO_AKL_PDCYCL_S", vc, this.getParameter(PARAMETER_INSTANCE_ID).toInt(),  this.getUserContext().getUID());
				}else{
					MessageQueue.getInstance().putMessage(this.getUserContext().getUID(), "��ǰ�̵㵥������Ϊ�գ�����!");
					return false;
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBSql.close(conn, ps, rs);
			}
		}
		return true;
	}

}
