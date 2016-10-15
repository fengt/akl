package cn.com.akl.shgl.fjjh.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo7Validate extends WorkFlowStepRTClassA {

	private static final String QUERY_FJ_S_SUM = "SELECT COUNT(1)SL FROM BO_AKL_FJ_S WHERE BINDID=? AND KFCKBH=? AND CPLH=? AND CPSX=?";
	
	private static final String QUERY_FJ_S_GATHER = "SELECT KFCKBH,CPLH,CPSX,PN,COUNT(1)SL FROM BO_AKL_FJ_S WHERE BINDID=? GROUP BY KFCKBH,CPLH,CPSX,PN";
	
	private static final String QUERY_FJ_isExsit = "SELECT COUNT(1)N FROM BO_AKL_FJJH_S WHERE BINDID=? AND KFZX=? AND WLBH=? AND SX=?";
	
	private static final String QUERY_FJJH_S = "SELECT * FROM BO_AKL_FJJH_S WHERE BINDID=?";
	
	private UserContext uc;
	private Connection conn;
	public StepNo7Validate() {
	}

	public StepNo7Validate(UserContext arg0) {
		super(arg0);
		this.uc = arg0;
		setProvider("fengtao");
		setDescription("У���Ƿ������¼����Ϣ��");
	}

	@Override
	public boolean execute() {
		final int bindid = this.getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = uc.getUID();
		try {
			conn = DBSql.open();
			/**
			 * 1��������Ϣ�Ƿ�������Ϣ���
			 */
			DAOUtil.executeQueryForParser(conn, QUERY_FJJH_S, new ResultPaserAbs(){
				public boolean parse(Connection conn, ResultSet rs) throws SQLException{
					validate1(conn, rs, bindid);
					return true;
				}
			}, bindid);
			
			/**
			 * 2���Ƿ񲻴��ڵĸ�����Ϣ
			 */
			DAOUtil.executeQueryForParser(conn, QUERY_FJ_S_GATHER, new ResultPaserAbs(){
				public boolean parse(Connection conn, ResultSet rs) throws SQLException{
					validate2(conn, rs, bindid);
					return true;
				}
			}, bindid);
			
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨��������ϵ����Ա��");
			return false;
		} finally{
			DBSql.close(conn, null, null);
		}
		return true;
	}
	
	public void validate1(Connection conn, ResultSet rs, int bindid) throws SQLException{
		String kfzx = StrUtil.returnStr(rs.getString("KFZX"));
		String kfbm = StrUtil.returnStr(rs.getString("KFBM"));
		String wlbh = StrUtil.returnStr(rs.getString("WLBH"));
		String xh = StrUtil.returnStr(rs.getString("XH"));
		String sx = StrUtil.returnStr(rs.getString("SX"));
		int fjsl = rs.getInt("FJJHSL");
		int num = DAOUtil.getInt(conn, QUERY_FJ_S_SUM, bindid, kfzx, wlbh, sx);
		if(num == 0){
			throw new RuntimeException("�ÿͷ���"+kfbm+"�����ͺ�Ϊ��"+xh+"���ĸ�����Ϣ�����ڣ����飡");
		}else if(num < fjsl){
			throw new RuntimeException("�ÿͷ���"+kfbm+"�����ͺ�Ϊ��"+xh+"���ĸ�������"+num+"С�ڸ���ƻ�����"+fjsl+"�����飡");
		}else if(num > fjsl){
			throw new RuntimeException("�ÿͷ���"+kfbm+"�����ͺ�Ϊ��"+xh+"���ĸ�������"+num+"���ڸ���ƻ�����"+fjsl+"�����飡");
		}
	}
	
	public void validate2(Connection conn, ResultSet rs, int bindid) throws SQLException{
		String kfbm = StrUtil.returnStr(rs.getString("KFCKBH"));
		String wlbh = StrUtil.returnStr(rs.getString("CPLH"));
		String sx = StrUtil.returnStr(rs.getString("CPSX"));
		String xh = StrUtil.returnStr(rs.getString("PN"));
		int n = DAOUtil.getInt(conn, QUERY_FJ_isExsit, bindid, kfbm, wlbh, sx);
		if(n == 0){
			throw new RuntimeException("������Ϣ�и��ͺš�"+xh+"�����ڸ���ƻ��У����飡");
		}
	}
	

}
