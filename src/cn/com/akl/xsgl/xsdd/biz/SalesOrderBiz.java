package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.regex.Pattern;

import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.IMAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

/**
 * ���۶���������.
 * 
 * @author huangming
 *
 */
public class SalesOrderBiz {

	/**
	 * ��ѯ�ͻ��ɹ������Ƿ����ظ���.
	 */
	private static final String QUERY_KHCGDD_COUNT = "SELECT COUNT(*)  FROM BO_AKL_WXB_XSDD_HEAD a WHERE a.bindid<>? and a.KHCGDH=?";
	/**
	 * ��ѯ���۶���������.
	 */
	private static final String QUERY_XSDD_DDID = "SELECT DDID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯǩ�����ڳ�ʱ����.
	 */
	private static final String QUERY_KH_QSRQ_TIMEOUT = "SELECT COUNT (*) FROM BO_AKL_CKD_HEAD a LEFT JOIN BO_AKL_QSD_P b ON a.bindid = b.bindid AND a.KH = ? JOIN BO_AKL_WXB_XSDD_HEAD c ON a.XSDDH = c.DDID WHERE b.QSRQ IS NOT NULL AND b.QSRQ + ? < getdate() AND c.DDZT = ?";
	/**
	 * ���۶�����ѯ �ͻ����ơ��ͻ��ɹ����š��ͻ��ֿ�. ��ʽ���ͻ�����,�ͻ��ɹ�����,�ͻ��ֿ�
	 */
	private static final String QUERY_XSDD_MESSAGE_KKK = "SELECT ISNULL(KHMC, ' ')+','+ISNULL(KHCGDH, ' ')+','+ISNULL(KHCK, '') FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ���۶�����ͷ.
	 */
	private static final String QUERY_XSDD_DT = "SELECT DDID, KHID, XDRQ,KHMC, JHRQ, XSR, ZQ, JSFS, GSTJ, GSZL, KHCGDH, XSBM, CGFZR, ZDJSHJ, CXFZRDH, CXFZRSJ, CXFZRYX, WFFZR, WFFZERDH, WFFZRSJ, WFFZRYX, KHCK, CKLXR, CKBYLXR, CKLXRDH, CKBYLXRDH, JHDZ, SHFS, ZJEHJ, SFYY, SFYS, BZ FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ���۶����Ĳֿ����.
	 */
	private static final String QUERY_XSDD_CKDM = "SELECT CKDM FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?";

	private static ProcessRebateBiz flBiz = new ProcessRebateBiz();
	private static ProcessPOSBiz posBiz = new ProcessPOSBiz();
	private static ProcessMaterialBiz skBiz = new ProcessMaterialBiz();

	private static Pattern _name_regex = Pattern.compile("<[^><]*>");

	/**
	 * �����ⵥ.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, String> fillCKD(Connection conn, int bindid) throws SQLException {
		Hashtable<String, String> hashtable = new Hashtable<String, String>();

		PreparedStatement ps = null;
		ResultSet reset = null;

		try {
			ps = conn.prepareStatement(QUERY_XSDD_DT);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			if (reset.next()) {
				hashtable.put("KH", parseNull(reset.getString("KHID")));
				hashtable.put("KHMC", parseNull(reset.getString("KHMC")));
				hashtable.put("TJ", parseNull(reset.getString("GSTJ")));
				hashtable.put("ZL", parseNull(reset.getString("GSZL")));
				hashtable.put("XSDDH", parseNull(reset.getString("DDID")));
				hashtable.put("KHCGDH", parseNull(reset.getString("KHCGDH")));
				hashtable.put("CXFZR", parseNull(reset.getString("CGFZR")));
				hashtable.put("CXDH", parseNull(reset.getString("CXFZRDH")));
				hashtable.put("CXSJ", parseNull(reset.getString("CXFZRSJ")));
				hashtable.put("CXEMAIL", parseNull(reset.getString("CXFZRYX")));
				hashtable.put("WFFZR", parseNull(reset.getString("WFFZR")));
				hashtable.put("CXSJ", parseNull(reset.getString("WFFZERDH")));
				hashtable.put("WFDH", parseNull(reset.getString("CXFZRSJ")));
				hashtable.put("WFSJ", parseNull(reset.getString("WFFZRSJ")));
				hashtable.put("WFEMAIL", parseNull(reset.getString("WFFZRYX")));
				hashtable.put("CK", parseNull(reset.getString("KHCK")));
				hashtable.put("KFLXR", parseNull(reset.getString("CKLXR")));
				hashtable.put("CKLXRDH", parseNull(reset.getString("CKLXRDH")));
				hashtable.put("YSHJ", parseNull(reset.getString("ZJEHJ")));
				hashtable.put("SFYS", parseNull(reset.getString("SFYS")));
				hashtable.put("SFYY", parseNull(reset.getString("SFYY")));
				hashtable.put("XDRQ", parseNull(reset.getString("XDRQ")));
				hashtable.put("JHDZ", parseNull(reset.getString("JHDZ")));
				hashtable.put("ZDJSHJ", parseNull(reset.getString("ZDJSHJ")));
				hashtable.put("BZ", parseNull(reset.getString("BZ")));
			} else {
				throw new RuntimeException("û���ҵ���Ӧ�����۵�!");
			}
		} finally {
			DBSql.close(ps, reset);
		}

		return hashtable;
	}

	/**
	 * ��ȡ�ͻ��ɹ������ظ��Ĵ���.
	 * 
	 * @param conn
	 * @param bindid
	 * @param khcgdh
	 * @return
	 * @throws SQLException
	 */
	public int getKHCGDDHRepeatCount(Connection conn, int bindid, String khcgdh) throws SQLException {
		return DAOUtil.getInt(conn, QUERY_KHCGDD_COUNT, bindid, khcgdh);
	}

	/**
	 * ��ȡ���۳��������.
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public String getSalesManager(Connection conn, String ckdm) throws SQLException {
		return DAOUtil.getStringOrNull(conn, "SELECT BLRBM FROM BO_AKL_CK_CKRYDYGX WHERE CKDM=?", ckdm);
	}

	/**
	 * ������������.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws Exception
	 */
	public void startCKDProcess(Connection conn, int bindid, String uid) throws Exception {

		cn.com.akl.ccgl.xsck.biz.FillBiz ckFillBiz = new cn.com.akl.ccgl.xsck.biz.FillBiz();

		String ddid = DAOUtil.getStringOrNull(conn, QUERY_XSDD_DDID, bindid);
		String ckdm = DAOUtil.getStringOrNull(conn, QUERY_XSDD_CKDM, bindid);
		String startUids = getSalesManager(conn, ckdm);
		startUids = _name_regex.matcher(startUids).replaceAll("");

		DAOUtil.executeQueryForParser(conn, "SELECT PCH, WLBH FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?", new DAOUtil.ResultPaser() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String zt = DAOUtil.getStringOrNull(conn, "SELECT ZT FROM BO_AKL_KC_KCHZ_P WHERE PCH=? AND WLBH=?", reset.getString("PCH"),
						reset.getString("WLBH"));
				if (XSCKConstant.PC_ZT_ZT.equals(zt)) {
					throw new RuntimeException("�˶�������;���ϲ�����������!");
				}
				return true;
			}
		}, bindid);

		if (startUids == null || "".equals(startUids.trim())) {
			throw new RuntimeException("�ֿ��ţ�" + ckdm + " δά����Ӧ�İ����ˣ� �������������ɹ���");
		}

		// ����������
		int ckBindid = 0;
		try {
			Hashtable<String, String> hashtable = fillCKD(conn, bindid);

			/** ƴ�ӱ��� */
			StringBuilder titleSb = new StringBuilder();
			titleSb.append("���۳���");
			titleSb.append(" ").append(hashtable.get("KHMC"));
			titleSb.append("--").append(hashtable.get("KHCGDH"));
			titleSb.append("--").append(hashtable.get("CK"));

			/** �������� */
			ckBindid = WorkflowInstanceAPI.getInstance().createProcessInstance("1d67285fab7d0a0731d579105b172546", uid, titleSb.toString());
			int n = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(uid, ckBindid, 0);
			WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(uid, ckBindid, n, startUids, titleSb.toString());
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_HEAD", hashtable, ckBindid, uid);
			ckFillBiz.queryAllWlxx(conn, ckBindid, uid, ddid);

		} catch (RuntimeException e) {
			if (ckBindid != 0) {
				WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
			}

			String message = DAOUtil.getStringOrNull(conn, QUERY_XSDD_MESSAGE_KKK, bindid);
			String[] split = message.split("\\,");

			StringBuilder titleSb = new StringBuilder();
			titleSb.append("���۶�����:");
			titleSb.append(ddid);
			titleSb.append("--");
			titleSb.append(split[0]);
			titleSb.append("--");
			titleSb.append(split[1]);
			titleSb.append("--");
			titleSb.append(split[2]);
			titleSb.append("--���������������ɹ�");

			StringBuilder contentSb = new StringBuilder();
			contentSb.append("����:<br/>&nbsp;&nbsp;&nbsp;&nbsp;");
			contentSb.append("���۶��� ");
			contentSb.append(ddid);
			contentSb.append(" �ĳ�����������δ�ɹ�����ɳ��������������ɹ���ԭ��<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			contentSb.append(e.getMessage());
			contentSb.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;��������Ҫ���½��������̣��ֶ��������۵��š�");

			// �����ʼ�
			IMAPI.getInstance().sendMail(uid, startUids, titleSb.toString(), contentSb.toString());
			throw e;
		} catch (Exception e) {
			// �������ʧ�ܣ��򴴽����������̡�
			try {
				if (ckBindid != 0) {
					WorkflowInstanceAPI.getInstance().removeProcessInstance(ckBindid);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			throw e;
		}
	}

	/**
	 * У�����ϵ�ë�����Ƿ�С��ά����ë����.
	 * 
	 * @param conn
	 * @param wlbh
	 * @param gmr
	 * @return
	 * @throws SQLException
	 */
	public boolean validateMaterialGrossMarginRate(Connection conn, String wlbh, BigDecimal gmr) throws SQLException {
		/** ��ȡ����Ĭ��ë���� */
		BigDecimal mll = DAOUtil.getBigDecimalOrNull(conn, "SELECT MLL FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);
		/** ��������Ĭ��ë�����򷵻�true */
		if (mll == null || mll.doubleValue() == 0) {
			return true;
		} else {
			/** ����������ë����С��Ĭ��ë���ʣ��򷵻�true. */
			return mll.doubleValue() <= gmr.doubleValue();
		}
	}

	/**
	 * ��֤���۶����������ݵ�����ë����.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateSalesOrderFormBodyGrossMarginRate(Connection conn, int bindid) throws SQLException {
		DAOUtil.executeQueryForParser(conn, "SELECT WLBH, XH, JJMLL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?", new DAOUtil.ResultPaser() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				if (!validateMaterialGrossMarginRate(conn, reset.getString("WLBH"), reset.getBigDecimal("JJMLL"))) {
					throw new RuntimeException("���ϣ�" + reset.getString("XH") + "��ë����С��Ĭ��ë����!");
				}
				return true;
			}
		}, bindid);
	}

	/**
	 * ����δ�����ë�������۶����µ��۸�������ָ���۲�����POS�ʽ��ѡ�񡢳����Ŷ������жϣ�û�з���true���з���false.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public void otherCaseValidate(Connection conn, int bindid) throws SQLException {
		String khid = DAOUtil.getString(conn, "SELECT KHID FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?", bindid);

		// ����δ����
		Integer zq = DAOUtil.getIntOrNull(conn, "SELECT ZQTS FROM BO_AKL_KH_P WHERE KHID=?", khid);
		if (zq == null||zq==0) {
			throw new RuntimeException("�˿ͻ�û��ά������!");
			// �˿ͻ�û��ά������
		} else {
			Integer yqwfkCount;
			String zqjsfs = DAOUtil.getStringOrNull(conn, "SELECT ZQJSFS FROM BO_AKL_KH_P WHERE KHID=?", khid);
			if (zqjsfs == null || zqjsfs.trim().equals("") || zqjsfs.trim().equals(XSDDConstant.ZQJSFS_QSRQ)) {
				yqwfkCount = DAOUtil.getIntOrNull(conn, QUERY_KH_QSRQ_TIMEOUT, khid, zq, XSDDConstant.XSDD_DDZT_YQS);
			} else if (zqjsfs.trim().equals(XSDDConstant.ZQJSFS_FPRQ)) {
				yqwfkCount = DAOUtil.getIntOrNull(conn, QUERY_KH_QSRQ_TIMEOUT, khid, zq, XSDDConstant.XSDD_DDZT_YQS);
			} else {
				yqwfkCount = DAOUtil.getIntOrNull(conn, QUERY_KH_QSRQ_TIMEOUT, khid, zq, XSDDConstant.XSDD_DDZT_YQS);
			}

			if (yqwfkCount != null && yqwfkCount > 0) {
				throw new RuntimeException("�˿ͻ�������δ����!");
			}

		}

		// ��ë��
		Integer zqjeCount = DAOUtil.getInt(conn, "select count(*) from BO_AKL_WXB_XSDD_BODY where bindid=? AND JJMLL<0", bindid);
		if (zqjeCount > 0) {
			throw new RuntimeException("�˶����и�ë��!");
		}

		// �ж����۵���Ϊ�������..
		Integer zeroCount = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND XSDJ=0", bindid);
		if (zeroCount != null && zeroCount > 0) {
			throw new RuntimeException("�˶��������۵���Ϊ0�����ϣ���Ҫ����������!");
		}

		// ���۶����µ��۸�������ָ���۲���
		Integer bfCount = DAOUtil.getInt(conn, "select count(*) from BO_AKL_WXB_XSDD_BODY where bindid=? and XSDJ<>XSZDJ", bindid);
		if (bfCount > 0) {
			throw new RuntimeException("�˶��������۶����µ��۸�������ָ���۲���!");
		}

		// POS�ʽ��ѡ��
		Integer posCount = DAOUtil.getInt(conn, "select count(*) from BO_AKL_WXB_XSDD_BODY where bindid=? and POSFALX='0'", bindid);
		if (posCount > 0) {
			throw new RuntimeException("��ѡ��POS�ʽ��!");
		}

		// �����Ŷ��
		// 1���˿ͻ������˶��ٽ��
		BigDecimal zdjshj = DAOUtil.getBigDecimalOrNull(conn, "SELECT ZDJSHJ FROM BO_AKL_WXB_XSDD_HEAD WHERE BINDID=?", bindid);
		// 2����ѯ���ö��
		BigDecimal xyed = DAOUtil.getBigDecimalOrNull(conn, "SELECT XYJE FROM BO_AKL_KH_P WHERE KHID=?", khid);
		if (xyed == null) {
			xyed = new BigDecimal(0);
		}
		if (zdjshj.compareTo(xyed) == 1) {
			throw new RuntimeException("�˿ͻ������ö��!");
		}
	}

	/**
	 * ���������¼.
	 * 
	 * @param bindid
	 * @param uid
	 * @return
	 */
	public ResultPaserAbs getInsertLockResultPaser(final int bindid, final String uid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				// �����ж�
				String sfsk = reset.getString("SDZT");
				if (sfsk != null && ("��".equals(sfsk.trim()) || XSDDConstant.YES.equals(sfsk))) {
					String ddh = reset.getString("DDID");
					String pch = reset.getString("PCH");
					String wlbh = reset.getString("WLBH");
					String ckdm = reset.getString("CKID");
					int ddsl = reset.getInt("DDSL");
					// ��������
					try {
						skBiz.insertSK(conn, bindid, uid, ddh, pch, wlbh, ckdm, ddsl);
					} catch (AWSSDKException e) {
						throw new RuntimeException(e);
					}
				}
				return true;
			}
		};
	}

	/**
	 * ������ע�뷵���ʽ��.
	 * 
	 * @param bindid
	 * @param uid
	 * @param khid
	 * @return
	 */
	public ResultPaserAbs getUpdateHFLResultPaser(final int bindid, final String uid, final String khid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah)) {
					String flfs = reset.getString("FLFS");
					BigDecimal jjze = reset.getBigDecimal("JJZE");
					BigDecimal flzcj = reset.getBigDecimal("FLZCJ");

					// �������.
					try {
						flBiz.processHFL(conn, bindid, uid, khid, flfs, flfah, jjze, flzcj);
					} catch (AWSSDKException e) {
						throw new RuntimeException(e);
					}
				}
				return true;
			}
		};
	}

	/**
	 * ���·�����POS.
	 * 
	 * @return
	 */
	public ResultPaserAbs getUpdateFLAndPOSPaser() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");
				// ����POS
				String posid = reset.getString("POSID");
				if (posid != null && !"".equals(posid.trim())) {
					String posfalx = reset.getString("POSFALX");
					BigDecimal posje = reset.getBigDecimal("POSJE");
					int poszcsl = reset.getInt("POSZCSL");
					posBiz.updatePOS(conn, posid, posfalx, posje, poszcsl, wlbh);
				}

				// ���·���
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah.trim())) {
					int flsl = reset.getInt("FLSL");
					flBiz.updateFL(conn, flfah, flsl, wlbh);
				}
				return true;
			}
		};
	}

	/**
	 * �ع�������POS.
	 * 
	 * @return
	 */
	public ResultPaserAbs getRollbackFLAndPOSPaser() {
		return new ResultPaserAbs() {
			private ProcessPOSBiz posBiz = new ProcessPOSBiz();

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");

				String posid = reset.getString("POSID");
				if (posid != null && !"".equals(posid.trim())) {
					String posfalx = reset.getString("POSFALX");
					BigDecimal posje = reset.getBigDecimal("POSJE");
					int poszcsl = reset.getInt("POSZCSL");
					// ����POS
					posBiz.rollBackPOS(conn, posid, posfalx, posje, poszcsl, wlbh);
				}
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah.trim())) {
					int flsl = reset.getInt("FLSL");
					// ���˷���
					flBiz.rollbackFL(conn, flfah, flsl, wlbh);
				}
				return true;
			}
		};
	}

	/**
	 * �ع�����.
	 * 
	 * @param bindid
	 * @param uid
	 * @param khid
	 * @return
	 */
	public ResultPaserAbs getRollbackHFL(final int bindid, final String uid, final String khid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah)) {
					String flfs = reset.getString("FLFS");
					BigDecimal ddzje = reset.getBigDecimal("DDZJE");
					BigDecimal flzcj = reset.getBigDecimal("FLZCJ");

					// ���˷���
					try {
						flBiz.rollbackHFL(conn, khid, flfs, ddzje, flzcj);
					} catch (AWSSDKException e) {
						throw new RuntimeException(e);
					}
				}
				return true;
			}
		};
	}

	/**
	 * ת��NULL.
	 * 
	 * @param str
	 * @return
	 */
	public String parseNull(String str) {
		return str == null ? "" : str;
	}
}
