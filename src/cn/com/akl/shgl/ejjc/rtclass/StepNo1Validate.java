package cn.com.akl.shgl.ejjc.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;

public class StepNo1Validate extends WorkFlowStepRTClassA {

	private static final String QUERY_FJ_S = "SELECT COUNT(1)N FROM BO_AKL_FJ_S WHERE BINDID=?";
	
	private static final String QUERY_FJ_S_GATHER = "SELECT KFCKBH,CPLH,CPSX,PN,KFMC,COUNT(1)SL FROM BO_AKL_FJ_S WHERE BINDID=? GROUP BY KFCKBH,CPLH,CPSX,PN,KFMC";
	
	private static final String QUERY_FJ_S_isGZTMHRepeat = "SELECT GZTMH FROM BO_AKL_FJ_S WHERE BINDID=? GROUP BY GZTMH HAVING COUNT(1)>1";
	
	private static final String QUERY_FJJH_isExsit = "SELECT COUNT(1)N FROM BO_AKL_FJJH_S WHERE BINDID=? AND KFZX=? AND WLBH=? AND SX=?";
	
	private static final String QUERY_FJJH_NUM = "SELECT FJJHSL FROM BO_AKL_FJJH_S WHERE BINDID=? AND KFZX=? AND WLBH=? AND SX=?";
	
	
	private static final String STEP_NAME = "�������������";
	
	public StepNo1Validate() {
		super();
	}

	public StepNo1Validate(UserContext arg0) {
		super(arg0);
		setProvider("fengtao");
		setDescription("��Ʒ��ϢУ�顣");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String, String> head = BOInstanceAPI.getInstance().getBOData("BO_AKL_FJJH_P", bindid);
		String fjjhdh = head.get("FJJHDH").toString();//����ƻ�����
		int fjjh_bindid = DBSql.getInt("SELECT BINDID FROM BO_AKL_FJJH_P WHERE FJDJBH='"+fjjhdh+"'", "BINDID");//����ƻ�BINDID
		Connection conn = null;
		try {
			conn = DBSql.open();
			String stepName = WorkflowInstanceAPI.getInstance().getCurrentStepName(fjjh_bindid).toString();//��ȡ����ƻ���ǰ�Ľڵ�����
			if(stepName.equals(STEP_NAME)){//�������������
				int isEmpty = DAOUtil.getInt(conn, QUERY_FJ_S, bindid);
				ArrayList<String> gztmList = DAOUtil.getStringCollection(conn, QUERY_FJ_S_isGZTMHRepeat, bindid);
				if(isEmpty == 0){
					throw new RuntimeException("������Ϣ����Ϊ�գ�");
				}
				if(gztmList.size() > 0){
					throw new RuntimeException("������Ϣ�д����ظ��������"+gztmList.toString()+"����˲飡");
				}
				validate(conn, bindid, fjjh_bindid);
			}else{
				throw new RuntimeException("����ƻ����������޸�������˲飡");
			}
			
			return true;
		} catch (RuntimeException e) {
 			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}
	
	public void validate(Connection conn, int bindid, final int fjjh_bindid) throws SQLException{
		DAOUtil.executeQueryForParser(conn, QUERY_FJ_S_GATHER, new ResultPaserAbs(){
			public boolean parse(Connection conn, ResultSet rs) throws SQLException{
				String kfbm = StrUtil.returnStr(rs.getString("KFCKBH"));
				String wlbh = StrUtil.returnStr(rs.getString("CPLH"));
				String sx = StrUtil.returnStr(rs.getString("CPSX"));
				String xh = StrUtil.returnStr(rs.getString("PN"));
				String kfmc = StrUtil.returnStr(rs.getString("KFMC"));
				int sl = rs.getInt("SL");//��������
				Integer fjsl1 = DAOUtil.getIntOrNull(conn, QUERY_FJJH_NUM, fjjh_bindid, kfbm, wlbh, sx);//��������
				int fjsl = fjsl1 == null ? 0 : fjsl1.intValue();
				int n = DAOUtil.getInt(conn, QUERY_FJJH_isExsit, fjjh_bindid, kfbm, wlbh, sx);
				if(n == 0){
					throw new RuntimeException("������Ϣ�иÿͷ���"+kfmc+"�����ͺš�"+xh+"�����ڸ���ƻ��У����飡");
				}else if(sl > fjsl){
					throw new RuntimeException("������Ϣ�иÿͷ���"+kfmc+"�����ͺš�"+xh+"���Ѽ��"+sl+"�������ڸ���ƻ�����"+fjsl+"�����飡");
				}
				
				return true;
			}
		}, bindid);
	}
	
}
