package cn.com.akl.cggl.cgdd.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.cggl.cgdd.biz.CalImportDatasBiz;
import cn.com.akl.cggl.cgdd.constant.CgddConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1AfterSave extends WorkFlowStepRTClassA{

	private static final String UPDATE_CGDD_P_ZT = "UPDATE BO_AKL_CGDD_HEAD SET DDZT =? WHERE DDID =?";
	private static final String UPDATE_CGDD_S_ZT = "UPDATE BO_AKL_CGDD_BODY SET ZT=? WHERE DDID=? AND WLBH=? ";
	
	private UserContext uc;
	public StepNo1AfterSave(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("������水ť�������˰�ϼ�!");
	}
	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		String tablename = this.getParameter(PARAMETER_TABLE_NAME).toString();
		Hashtable hash = BOInstanceAPI.getInstance().getBOData(CgddConstant.tableName0, bindid);
		Vector vector = BOInstanceAPI.getInstance().getBODatas(CgddConstant.tableName1, bindid);
		
		if(CgddConstant.tableName0.equals(tablename)){
			String ddid = hash.get("DDID").toString();//�ɹ�������
			//���м�˰�ϼ�
			CalImportDatasBiz calUtil = new CalImportDatasBiz();
			calUtil.calDatasForAccount(vector,bindid);
			//״̬����
			if(taskid == 0){//��ͼģʽ
				updateZT(vector,ddid);
			}
		}
		
		return true;
	}

	
	/**
	 * �ɹ���״̬����(��ͼģʽ)
	 * @param vector
	 * @param ddid
	 */
	public void updateZT(Vector vector, String ddid){
		Connection conn = null;
		try{
			conn = DBSql.open();
			for (int i = 0; i < vector.size(); i++) {
				Hashtable rec = (Hashtable)vector.get(i);
				String wlbh = rec.get("WLBH").toString();//���ϱ��
				int cgsl = Integer.parseInt(rec.get("CGSL").toString());//�ɹ�����
				int yrksl = Integer.parseInt(rec.get("YRKSL").toString());//���������
				if(cgsl == yrksl){
					String zt = CgddConstant.zt1;//�����
					DAOUtil.executeUpdate(conn, UPDATE_CGDD_S_ZT,zt,ddid,wlbh);//���µ���״̬
				}
			}
			
			String str = "SELECT SUM(CGSL)AS CGSL,SUM(YRKSL)AS YRKSL FROM BO_AKL_CGDD_BODY WHERE DDID='"+ddid+"'";
			int totalCgsl = DBSql.getInt(conn, str, "CGSL");//�ɹ�������
			int totalYrksl = DBSql.getInt(conn, str, "YRKSL");//�����������
			if(totalCgsl == totalYrksl){
				String ddzt = CgddConstant.zt1;//�����
				DAOUtil.executeUpdate(conn, UPDATE_CGDD_P_ZT, ddzt, ddid);//���µ�ͷ״̬
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DBSql.close(conn, null, null);
		}
			
	}
}
