package cn.com.akl.kwgl.dbcrk.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.kwgl.constant.KwglConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	
	private UserContext uc;
	private Connection conn = null;
	public StepNo3Transaction() {
	}

	public StepNo3Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
		setProvider("fengtao");
		setDescription("���������ʱ��������ϸ����»����������ݡ�");
	}

	@Override
	public boolean execute() {
		int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		int taskid = this.getParameter(PARAMETER_TASK_ID).toInt();
		boolean yes = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "ͬ��");
		Vector vector = BOInstanceAPI.getInstance().getBODatas(KwglConstant.table7, bindid);
		
		
		try{
			conn = DAOUtil.openConnectionTransaction();
			if(yes){
				DbcrkUtil(conn, uc, vector, bindid);
			}
			conn.commit();
			return true;
		}catch(RuntimeException e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), e.getMessage());
			return false;
		}catch(Exception e){
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uc.getUID(), "��̨���ִ����������̨��");
			return false;
		}finally{
			DBSql.close(conn, null, null);
		}
	}
	
	
	/**
	 * ���������������ݲ�������
	 * @param vector
	 * @param bindid
	 */
	public void DbcrkUtil(Connection conn, UserContext uc, Vector vector, int bindid)throws SQLException, AWSSDKException{
		String str0 = "select * from " +KwglConstant.table6+ " where bindid="+bindid;
		String zrck = DBSql.getString(conn, str0, "ZRCK");
		String str1 = "SELECT * FROM " +KwglConstant.table4+ " WHERE CKDM='"+zrck+"'";
		String ckmc = DBSql.getString(conn, str1, "CKMC");
		String sfdgk = DBSql.getString(conn, str1, "SFDGK");
		
		int total = 0;
		if(vector != null){
			for (int i = 0; i < vector.size(); i++) {
				Hashtable records = (Hashtable)vector.get(i);
				String wlbh = records.get("WLBH").toString();
				String wlmc = records.get("WLMC").toString();
				String xh = records.get("WLXH").toString();
				String dw = records.get("ZJLDW").toString();
				String pch = records.get("TZQPC").toString();
				int tzqsl = Integer.parseInt(records.get("TZQSL").toString());
				int tzhsl = Integer.parseInt(records.get("TZHSL").toString());
				total += tzqsl - tzhsl;
				String tzqkw = records.get("TZQKW").toString();
				String tzhq = records.get("TZHQ").toString();
				String tzhd = records.get("TZHD").toString();
				String tzhkw = records.get("TZHKW").toString();
				String tzhhw = records.get("TZHHW").toString();
				String sx = StrUtil.returnStr(records.get("SX").toString());
				
				Hashtable hashtable = new Hashtable();
				hashtable.put("WLBH", wlbh);
				hashtable.put("WLMC", wlmc);
				hashtable.put("XH", xh);
				hashtable.put("JLDW", StrUtil.returnStr(dw));
				hashtable.put("SX", sx);
				hashtable.put("PCH", pch);
				hashtable.put("KWSL", tzhsl);
				
				hashtable.put("CKDM", zrck);
				hashtable.put("CKMC", ckmc);
				hashtable.put("QDM", tzhq);
				hashtable.put("DDM", tzhd);
				hashtable.put("KWDM", tzhkw);
				hashtable.put("HWDM", tzhhw);
				
				
				if(sfdgk.equals(KwglConstant.isProxies0)){
					String sql = "SELECT * FROM " +KwglConstant.table3+ " WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND HWDM='"+tzqkw+"' AND SX='"+sx+"'";
					String zjm = DBSql.getString(conn, sql, "ZJM");
					String fzsx = DBSql.getString(conn, sql, "FZSX");
					int bzq = DBSql.getInt(conn, sql, "BZQ");
					String scrq = DBSql.getString(conn, sql, "SCRQ");
					String gg = DBSql.getString(conn, sql, "GG");
					int kwsl = DBSql.getInt(conn,sql, "KWSL");//��ʱ�Ŀ������
					
					hashtable.put("ZJM", StrUtil.returnStr(zjm));
					hashtable.put("FZSX", StrUtil.returnStr(fzsx));
					hashtable.put("BZQ", bzq);
					hashtable.put("SCRQ", StrUtil.returnStr(scrq));
					hashtable.put("GG", StrUtil.returnStr(gg));
					
					//a.����ǰ�����Ϳ�������Ƿ�һ��
					if(tzqsl != kwsl){
						throw new RuntimeException("���������Ρ�"+wlbh+","+pch+","+tzqkw+"���ĵ���ǰ����δ��ʱ���£����˻غ��������룡");
					}
					
					//b.���ܿ����ϸ���»����
					KCMXUpdate(conn, bindid,uc,wlbh,pch,tzhhw,tzqkw,tzhsl,KwglConstant.table3,sx,hashtable);
				}else{
					String sql = "SELECT * FROM " +KwglConstant.table2+ " WHERE WLBH='"+wlbh+"' AND PCH='"+pch+"' AND HWDM='"+tzqkw+"' AND SX='"+sx+"'";
					String zjm = DBSql.getString(conn, sql, "ZJM");
					String fzsx = DBSql.getString(conn, sql, "FZSX");
					int bzq = DBSql.getInt(conn, sql, "BZQ");
					String scrq = DBSql.getString(conn, sql, "SCRQ");
					String gg = DBSql.getString(conn, sql, "GG");
					int kwsl = DBSql.getInt(conn,sql, "KWSL");//��ʱ�Ŀ������
					
					hashtable.put("ZJM", StrUtil.returnStr(zjm));
					hashtable.put("FZSX", StrUtil.returnStr(fzsx));
					hashtable.put("BZQ", bzq);
					hashtable.put("SCRQ", StrUtil.returnStr(scrq));
					hashtable.put("GG", StrUtil.returnStr(gg));
					
					//a.����ǰ�����Ϳ�������Ƿ�һ��
					if(tzqsl != kwsl){
						throw new RuntimeException("���������Ρ�"+wlbh+","+pch+","+tzqkw+"���ĵ���ǰ����δ��ʱ���£����˻غ��������룡");
					}
					
					//b�����ϸ���»����
					KCMXUpdate(conn, bindid,uc,wlbh,pch,tzhhw,tzqkw,tzhsl,KwglConstant.table2,sx,hashtable);
				}
			}
			/*���µ�����ͷ�ִ���*/
			String str4 = "UPDATE " +KwglConstant.table6+ " SET XCL="+total+" WHERE bindid="+bindid;
			DBSql.executeUpdate(conn, str4);
		}
	}
	
	/**
	 * �����ϸ���»����(���������ޱ仯)
	 * @param wlbh
	 * @param pch
	 * @param tzhhw
	 * @param tzqkw
	 * @param tzhsl
	 * @param table
	 * @param hashtable
	 */
	public static void KCMXUpdate(
			Connection conn, int bindid, UserContext uc, String wlbh, String pch, String tzhhw, String tzqkw, int tzhsl, String table, String sx, Hashtable hashtable)
	throws SQLException, AWSSDKException{
		
		/*��ѯ�Ƿ��Ѵ��ڵ������λ��Ϣ*/
		String str = "SELECT count(*) cnt from " +table+ " WHERE WLBH = '"+wlbh+"' AND PCH = '"+pch+"' AND HWDM = '"+tzhhw+"' AND SX='"+sx+"'";
		int count = DBSql.getInt(conn, str, "cnt");
		if(count <= 0){
			BOInstanceAPI.getInstance().createBOData(conn, table, hashtable, bindid, uc.getUID());
		}else{
			String str2 = "UPDATE " +table+ " SET KWSL=ISNULL(KWSL,0)+ "+tzhsl+" WHERE HWDM='"+tzhhw+"' AND WLBH ='"+wlbh+"' AND PCH = '"+pch+"' AND SX='"+sx+"'";
			int updateCount1 = DBSql.executeUpdate(conn, str2);
			if(updateCount1 != 1) throw new RuntimeException("����������ʧ�ܡ�"+wlbh+","+pch+"��������ϵ����Ա��");
		}
		String str3 = "UPDATE " +table+ " SET KWSL=ISNULL(KWSL,0)- "+tzhsl+" WHERE HWDM ='"+tzqkw+"' AND WLBH ='"+wlbh+"' AND PCH = '"+pch+"' AND SX='"+sx+"'";
		int updateCount2 = DBSql.executeUpdate(conn, str3);
		if(updateCount2 != 1) throw new RuntimeException("����������ʧ�ܡ�"+wlbh+","+pch+"��������ϵ����Ա��");
	}
	

}
