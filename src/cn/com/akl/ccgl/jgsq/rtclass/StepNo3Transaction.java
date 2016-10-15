package cn.com.akl.ccgl.jgsq.rtclass;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.cgrk.cnt.CgrkCnt;
import cn.com.akl.ccgl.jgsq.biz.ProductInfoBiz;
import cn.com.akl.ccgl.xsck.biz.KCBiz;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.StrUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessMaterialBiz;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo3Transaction extends WorkFlowStepRTClassA {

	public static final String table_JG = "BO_AKL_JGSQ_CP_S";// �ӹ������Ʒ�ӱ�

	public static final String table_MX = "BO_AKL_KC_KCMX_S";// �����ϸ��
	public static final String table_HZ = "BO_AKL_KC_KCHZ_P";// �����ܱ�

	/**
	 * ��ѯ������ϸ�ĵ�����Ϣ.
	 */
	private static final String QUERY_JGSQ_WLMX = "SELECT HWDM, PCH, ZDCB, JGSL, WLBH FROM BO_AKL_JGSQ_WLMX_S WHERE BINDID=?";
	/**
	 * �������κ�.
	 */
	private static final String UPDATE_JGSQ_CP_PCH = "UPDATE BO_AKL_JGSQ_CP_S SET PCH=? WHERE BINDID=?";
	private String pch = null;

	private UserContext uc;
	private KCBiz kcBiz = new KCBiz();
	private ProductInfoBiz productInfoBiz = new ProductInfoBiz();
	private ProcessMaterialBiz processSKBiz = new ProcessMaterialBiz();

	public StepNo3Transaction() {
		super();
	}

	public StepNo3Transaction(UserContext uc) {
		super(uc);
		this.uc = uc;
	}

	@Override
	public boolean execute() {

		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		Connection conn = null;
		// ��ȡ��Ʒ������Ϣ (�����)
		Vector<Hashtable<String, String>> body = BOInstanceAPI.getInstance().getBODatas(table_JG, bindid);
		// ��ȡ��ϸ������Ϣ (�����)
		try {
			conn = DAOUtil.openConnectionTransaction();

			// �������
			processSKBiz.deleteSK(conn, bindid);

			// �������
			DAOUtil.executeQueryForParser(conn, QUERY_JGSQ_WLMX, new DAOUtil.ResultPaser() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					kcBiz.outOfWarehouseMX(conn, reset.getString("WLBH"), reset.getString("PCH"), reset.getInt("JGSL"));
					kcBiz.outOfWarehouseHZ(conn, reset.getString("WLBH"), reset.getString("PCH"), reset.getString("HWDM"), reset.getInt("JGSL"));
					return false;
				}
			}, bindid);
			// �������κ�
			pch = ProductInfoBiz.createPCH(conn, new Date(new java.util.Date().getTime()));
			DAOUtil.executeUpdate(conn, UPDATE_JGSQ_CP_PCH, pch, bindid);

			// ��������
			/** ���ӿ�� **/
			Vector<Hashtable<String, String>> hzVector_tmp = BodyHZ(conn, bindid);
			Vector<Hashtable<String, String>> hzVector = getHZVector(hzVector_tmp);// ��������
			Vector<Hashtable<String, String>> mxVector = getMXVector(body);// ��ϸ����

			BOInstanceAPI.getInstance().createBOData(conn, table_HZ, hzVector, bindid, uc.getUID());
			BOInstanceAPI.getInstance().createBOData(conn, table_MX, mxVector, bindid, uc.getUID());
			
			conn.commit();
			return true;
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "��̨�����쳣���������̨", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * ��װ�����ϸ������.
	 * 
	 * @param pTable
	 * @param vector
	 * @return
	 */
	public Vector<Hashtable<String, String>> getMXVector(Vector<Hashtable<String, String>> vector) {
		Vector<Hashtable<String, String>> receiveVector = new Vector<Hashtable<String, String>>();
		for (int i = 0; i < vector.size(); i++) {
			Hashtable<String, String> receiveHash = new Hashtable<String, String>();
			Hashtable<String, String> rec = vector.get(i);
			receiveHash.put("WLBH", rec.get("WLBH"));// ���ϱ��
			receiveHash.put("WLMC", rec.get("WLMC"));// ��������
			receiveHash.put("PCH", pch);// ���κ�
			receiveHash.put("XH", rec.get("XH"));// �ͺ�
			receiveHash.put("GG", rec.get("XH"));// ���

			receiveHash.put("JLDW", rec.get("DW"));// ������λ
			receiveHash.put("KWSL", rec.get("SL"));// ��λ����

			String ckbm = rec.get("CKDM").toString();
			receiveHash.put("CKDM", ckbm);// �ֿ����
			String ckmc = DBSql.getString("select CKMC from BO_AKL_CK where CKDM='" + ckbm + "'", "ckmc");
			receiveHash.put("CKMC", ckmc);// �ֿ�����

			receiveHash.put("QDM", rec.get("QDM"));// ������
			receiveHash.put("DDM", rec.get("DDM"));// ������
			receiveHash.put("KWDM", rec.get("KWDM"));// ��λ����
			receiveHash.put("HWDM", rec.get("HWDM"));// ��λ����
			receiveHash.put("SX", CgrkCnt.sx0);
			receiveVector.add(receiveHash);
		}
		return receiveVector;
	}

	/**
	 * ��װ�����ܱ�����.
	 * 
	 * @param pTable
	 * @param vector
	 * @return
	 */
	public Vector<Hashtable<String, String>> getHZVector(Vector<Hashtable<String, String>> vector) {
		Vector<Hashtable<String, String>> receiveVector = new Vector<Hashtable<String, String>>();
		for (int i = 0; i < vector.size(); i++) {
			Hashtable<String, String> receiveHash = new Hashtable<String, String>();
			Hashtable<String, String> recordData = vector.get(i);
			receiveHash.put("WLBH", recordData.get("wlbh").toString());
			receiveHash.put("WLMC", recordData.get("wlmc").toString());
			receiveHash.put("XH", recordData.get("xh").toString());
			receiveHash.put("PCH", pch);
			receiveHash.put("DW", recordData.get("dw").toString());
			receiveHash.put("RKSL", recordData.get("sssl").toString());
			receiveHash.put("PCSL", recordData.get("sssl").toString());
			receiveHash.put("DJ", recordData.get("zdcb").toString());
			receiveHash.put("ZT", CgrkCnt.kczt2);
			receiveVector.add(receiveHash);
		}
		return receiveVector;
	}

	/**
	 * ���ܵ�������(ͬһ����,ͬһ����).
	 * 
	 * @param conn
	 * @param pTable
	 * @return
	 */
	public Vector<Hashtable<String, String>> BodyHZ(Connection conn, int bindid) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> rec = null;
		String sql = "SELECT wlbh,wlmc,xh,pch,ISNULL(zdcb,0)as dj,SUM(sl)as sssl,dw FROM " + table_JG + " WHERE bindid=" + bindid
				+ " GROUP BY wlbh,wlmc,xh,pch,zdcb,dw";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					rec = new Hashtable<String, String>();
					rec.put("wlbh", rs.getString("wlbh"));
					rec.put("wlmc", StrUtil.returnStr(rs.getString("wlmc")));
					rec.put("xh", rs.getString("xh"));
					rec.put("pch", pch);
					rec.put("zdcb", rs.getString("dj"));
					rec.put("sssl", rs.getString("sssl"));
					rec.put("dw", parseNull(rs.getString("DW")));
					vector.add(rec);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, ps, rs);
		}
		return vector;
	}

	public String parseNull(String value) {
		if (value == null) {
			return "";
		} else {
			return value;
		}
	}

}
