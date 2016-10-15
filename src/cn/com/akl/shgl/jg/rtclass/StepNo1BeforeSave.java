package cn.com.akl.shgl.jg.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.shgl.jg.biz.JGBiz;
import cn.com.akl.shgl.jg.biz.JGConstant;
import cn.com.akl.shgl.kc.biz.RepositoryBiz;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	private RepositoryBiz repositoryBiz = new RepositoryBiz();
	private JGBiz jgBiz = new JGBiz();
	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("�ӹ������Զ���������.�������еļӹ��������Զ�������Ӧ�����ϡ�");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		String tableName = getParameter(PARAMETER_TABLE_NAME).toString();

		Connection conn = null;
		try {
			conn = DBSql.open();

			if ("BO_AKL_SH_JG_P".equals(tableName)) {
				dealHead(conn, bindid);
			}

			if ("BO_AKL_SH_JG_GZ_S".equals(tableName)) {
				dealBody(conn, bindid);
			}

			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "��̨���ִ�������ϵϵͳ����Ա!");
			return true;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ����ͷ.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void dealHead(Connection conn, int bindid) throws SQLException, AWSSDKException {
		/**
		 * ����ӹ��ļ�¼.<br/>
		 * ##�����ӹ�����ʽ��<br/>
		 * 1����ȡ��Ҫ�ӹ�������.<br/>
		 * 2����ȡ�ӱ��Ѽӹ�����.<br/>
		 * 3�������ӱ�δ�ӹ�����.<br/>
		 * ##�������ӹ�����ʽ��<br/>
		 * 1����ȡ�ӱ��Ƿ�������.<br/>
		 * 2�������ݣ���ȡ��Ҫ�ӹ�����.<br/>
		 * 3������ӹ�����.<br/>
		 */

		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();
		Vector<Hashtable<String, String>> vector = BOInstanceAPI.getInstance().getBODatas("BO_AKL_SH_DJG_S", bindid);

		String jggzjh = DAOUtil.getStringOrNull(conn, "SELECT JGGZJH FROM BO_AKL_SH_JG_P WHERE BINDID=?", bindid);
		String newGzbh = DAOUtil.getStringOrNull(conn,
				"SELECT GZBH+'-'+CONVERT(varchar(16),JGCS) AS 'X' FROM BO_AKL_SH_JG_GZ_S WHERE BINDID=? FOR XML PATH('')", bindid);
		if (newGzbh == null) {
			newGzbh = "";
		}

		if (vector == null) {
			vector = new Vector<Hashtable<String, String>>();
		}

		if (jggzjh == null || jggzjh.equals("") || !jggzjh.equals(newGzbh)) {
			repositoryBiz.removeLock(conn, bindid);
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_DJG_S", bindid);
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_JGWC_S", bindid);
			BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_JG_PJXH_S", bindid);
		}

		if (!newGzbh.equals("") && !newGzbh.equals(jggzjh)) {
			fetchAndInsert(conn, bindid, hashtable, vector);
		}

		DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_SH_JG_P SET JGGZJH=? WHERE BINDID=?", newGzbh, bindid);
	}

	/**
	 * ��ȡ�ӱ����ݼ�¼��.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 */
	public int countRecord(Connection conn, int bindid) {
		return 0;
	}

	/**
	 * 1�����������Ҫ��ԭ��. <br/>
	 * (1)����ȡ��Ҫ��ԭ��.<br/>
	 * (2)��ץȡ��������. <br/>
	 * (3)�������¼.<br/>
	 * 2���������������Ķ���.<br/>
	 * (1)��ץȡ���������Ķ���.<br/>
	 * (2)������. <br/>
	 * 3���������<br/>
	 * (1)����ȡ�����Ϣ.<br/>
	 * (2)��ץȡ�����������.<br/>
	 * (3)������.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fetchAndInsert(Connection conn, int bindid, Hashtable<String, String> hashtable, Vector<Hashtable<String, String>> vector)
			throws SQLException, AWSSDKException {
		String uid = getUserContext().getUID();

		repositoryBiz.removeLock(conn, bindid);

		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_DJG_S", bindid);
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_JGWC_S", bindid);
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_SH_JG_PJXH_S", bindid);

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_JGGZ);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String gzbm = reset.getString("GZBH");
				int jgcs = reset.getInt("JGCS");
				dealGz(conn, bindid, uid, hashtable, gzbm, jgcs, vector);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ����ӹ�����.
	 * 
	 * @param conn
	 * @param bindid
	 * @param gzbm
	 * @param jgcs
	 * @param vector
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void dealGz(Connection conn, int bindid, String uid, Hashtable<String, String> hashtable, String gzbm, int jgcs,
			Vector<Hashtable<String, String>> vector) throws SQLException, AWSSDKException {
		dealGzXh(conn, bindid, uid, hashtable, gzbm, jgcs, vector);
		dealGzWc(conn, bindid, uid, hashtable, gzbm, jgcs, vector);
		dealGzPj(conn, bindid, uid, hashtable, gzbm, jgcs, vector);
	}

	/**
	 * ����ӹ�����.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param jgcs
	 * @param vector
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	private void dealGzXh(Connection conn, int bindid, String uid, Hashtable<String, String> main, String gzbm, int jgcs,
			Vector<Hashtable<String, String>> vector) throws SQLException, AWSSDKException {

		String ckdm = main.get("CKDM");
		String ckmc = main.get("CKMC");
		String xmlb = main.get("XMLB");
		String jglx = main.get("JGLX");
		String sxdh = main.get("SXDH");

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_JGGZ_XH);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, gzbm);
			while (reset.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String sx = reset.getString("SX");
				int sl = reset.getInt("SL") * jgcs;

				hashtable.put("WLBH", wlbh);
				hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
				hashtable.put("XH", PrintUtil.parseNull(xh));
				hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
//				hashtable.put("SL", String.valueOf(sl));
				hashtable.put("JGSL", String.valueOf(sl));
				hashtable.put("CPSX", sx);
				hashtable.put("GZBH", reset.getString("GZBH"));
				hashtable.put("XMLX", xmlb);
				hashtable.put("CKDM", ckdm);
				hashtable.put("CKMC", ckmc);

				boolean isSuccess = false;
				if(jglx.equals(JGConstant.JGLX_ZCJG)){
					isSuccess = repositoryBiz.autoFetchNoLock(conn, bindid, uid, xmlb, wlbh, xh, ckdm, sx, sl, hashtable);
				}else{
					isSuccess = jgBiz.autoFetchNoLock(conn, bindid, uid, xmlb, wlbh, sxdh, ckdm, sx, sl, hashtable);
				}
				
				if (isSuccess) {
					// ��������
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_DJG_S", hashtable, bindid, uid);
				} else {
					throw new RuntimeException("���ӹ������ͺţ�" + xh + "�����������!");
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ����ӹ����.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param jgcs
	 * @param vector
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	private void dealGzWc(Connection conn, int bindid, String uid, Hashtable<String, String> main, String gzbm, int jgcs,
			Vector<Hashtable<String, String>> vector) throws SQLException, AWSSDKException {

		String ckdm = main.get("CKDM");
		String ckmc = main.get("CKMC");
		String xmlb = main.get("XMLB");

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_JGGZ_WC);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, gzbm);
			while (reset.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String sx = reset.getString("SX");
				int sl = reset.getInt("SL") * jgcs;

				hashtable.put("WLBH", wlbh);
				hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
				hashtable.put("XH", PrintUtil.parseNull(xh));
				hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
				hashtable.put("SL", String.valueOf(sl));
				hashtable.put("CPSX", sx);
				hashtable.put("GZBH", reset.getString("GZBH"));
				hashtable.put("XMLX", xmlb);
				hashtable.put("HWDM", ckdm);
				hashtable.put("CKDM", ckdm);
				hashtable.put("CKMC", ckmc);

				// ��������
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_JGWC_S", hashtable, bindid, uid);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ����ӹ������Ϣ.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param jgcs
	 * @param vector
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	private void dealGzPj(Connection conn, int bindid, String uid, Hashtable<String, String> main, String gzbm, int jgcs,
			Vector<Hashtable<String, String>> vector) throws SQLException, AWSSDKException {
		String ckdm = main.get("CKDM");
		String ckmc = main.get("CKMC");
		String xmlb = main.get("XMLB");

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(JGConstant.QUERY_JGGZ_PJ);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, gzbm);
			while (reset.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				String wlbh = reset.getString("WLBH");
				String xh = reset.getString("XH");
				String sx = reset.getString("SX");
				int sl = reset.getInt("SL") * jgcs;

				hashtable.put("WLBH", wlbh);
				hashtable.put("WLMC", PrintUtil.parseNull(reset.getString("WLMC")));
				hashtable.put("XH", PrintUtil.parseNull(xh));
				hashtable.put("GG", PrintUtil.parseNull(reset.getString("GG")));
				hashtable.put("JYSL", String.valueOf(sl));
				hashtable.put("SJSL", String.valueOf(sl));
				hashtable.put("CPSX", sx);
				hashtable.put("GZBH", reset.getString("GZBH"));
				hashtable.put("XMLX", xmlb);
				hashtable.put("CKDM", ckdm);
				hashtable.put("CKMC", ckmc);

				boolean isSuccess = repositoryBiz.autoFetchNoLock(conn, bindid, uid, xmlb, wlbh, xh, ckdm, sx, sl, hashtable);
				if (isSuccess) {
					// ��������
					BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_SH_JG_PJXH_S", hashtable, bindid, uid);
				} else {
					throw new RuntimeException("����������ͺţ�" + xh + "�����������!");
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ������.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void dealBody(Connection conn, int bindid) throws SQLException {
	}

}
