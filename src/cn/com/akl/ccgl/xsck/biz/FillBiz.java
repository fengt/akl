package cn.com.akl.ccgl.xsck.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;

/**
 * ��Ҫ������������еĸ��ֵ������.
 * 
 * @author huangming
 *
 */
public class FillBiz {

	/**
	 * ��ѯ�ͻ������.
	 */
	private final String QUERY_KH_LBID = "SELECT LBID FROM BO_AKL_KH_P WHERE KHID=?";
	/**
	 * ��ѯ���ⵥ����������.
	 */
	private static final String QUERY_CKD_WLSL = "SELECT WLH, WLMC, XH, JLDW, KHCPBM, SUM(ISNULL(SJSL, 0)) as YSSL FROM BO_AKL_CKD_BODY WHERE BINDID=? GROUP BY WLH, WLMC, XH, JLDW, KHCPBM";
	/**
	 * ��ѯ���ⵥ�ĵ�ͷ.
	 */
	private static final String QUERY_CKD_HEAD = "SELECT BZ, FHRQ,JHDZ,CKDH,KHCGDH,KHMC,CXFZR,CXDH,CK,YSHJ,KFLXR,CKLXRDH,CKLXRSJ,CKLXREMAIL FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯ���˵�λ.
	 */
	private static final String QUERY_YD_TYDW = "SELECT CYS FROM BO_AKL_YD_P WHERE BINDID=?";
	/**
	 * ��ѯǩ�յ���.
	 */
	private static final String QUERY_QSD_DH = "SELECT QSDH FROM BO_AKL_QSD_P WHERE BINDID=?";
	/**
	 * ��ѯ���ⵥӳ�䵽���䵥����Ϣ.
	 */
	private static final String QUERY_CKD_TO_YSD = "SELECT BZ, SJTJ, SJZL, SHFS,CKLXRDH,RMAFXDH, JHDZ, KFLXR, JS, YSHJ, KH, KHMC,FHRQ, CKDH,XSDDH,TJ,ZL FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/**
	 * ��ѯԤԼ��ӳ�䵽���䵥����Ϣ.
	 */
	private static final String QUERY_YYD_TO_YSD = "SELECT CYS,CYSDH,CYSDZ,CYSLXR FROM BO_BO_AKL_CK_YY_P WHERE BINDID=?";
	/**
	 * ��ѯǩ�յ��м�¼��.
	 */
	private static final String QUERY_QSD_COUNT = "SELECT COUNT(*) FROM BO_AKL_QSD_P WHERE BINDID=?";
	/**
	 * ��ѯ��������������Ϣ.
	 */
	private static final String QUERY_All_WLXX = "SELECT a.DDID,b.WLBH,b.WLMC,b.WLGG,b.XH,b.JLDW,b.CKID,b.CKMC,b.PCH,b.DDSL,b.KHSPBH,b.TJ,b.ZL, b.KC,b.YSJE,SL,b.XSDJ FROM BO_AKL_WXB_XSDD_HEAD a, BO_AKL_WXB_XSDD_BODY b WHERE a.BINDID=b.BINDID AND a.DDID=?";
	/**
	 * ��ѯ�ֿ�Ŀ���������Ϣ.
	 */
	private static final String QUERY_KY_WLXX = "SELECT s.ID, p.DJ, s.WLBH, s.PCH, s.KWSL, s.HWDM, s.CKDM, s.CKMC, s.QDM, s.DDM, s.KWDM, s.SX FROM BO_AKL_KC_KCHZ_P p, BO_AKL_KC_KCMX_S s WHERE s.WLBH=? AND s.PCH=? AND p.ZT=? AND CKDM=? AND p.WLBH=s.WLBH AND p.PCH=s.PCH ORDER BY p.PCH,s.KWSL";

	/**
	 * ���ǩ�յ���ͷ.
	 * 
	 * @param bindid
	 * @param conn
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fillQSDHead(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
		Hashtable<String, String> qsData = new Hashtable<String, String>();

		// ����ǩ�յ���.
		Integer count = DAOUtil.getIntOrNull(conn, QUERY_QSD_COUNT, bindid);
		if (count == null || count == 0) {
			String qsdh = RuleAPI.getInstance().executeRuleScript("SI@replace(@date,-)@formatZero(3,@sequencefordateandkey(BO_AKL_QSD_P))");
			qsData.put("QSDH", qsdh);
		} else {
			String qsdh = DAOUtil.getStringOrNull(conn, QUERY_QSD_DH, bindid);
			if (qsdh == null) {
				qsdh = RuleAPI.getInstance().executeRuleScript("SI@replace(@date,-)@formatZero(3,@sequencefordateandkey(BO_AKL_QSD_P))");
				qsData.put("QSDH", qsdh);
			}
		}

		// ��ѯ���˵�λ
		String tydw = DAOUtil.getStringOrNull(conn, QUERY_YD_TYDW, bindid);
		qsData.put("TYDH", parseNull(tydw));

		// ���ⵥ���ǩ�յ�
		PreparedStatement ckPs = null;
		ResultSet ckReset = null;
		try {
			ckPs = conn.prepareStatement(QUERY_CKD_HEAD);
			ckReset = DAOUtil.executeFillArgsAndQuery(conn, ckPs, bindid);
			if (ckReset.next()) {
				qsData.put("SHDZ", parseNull(ckReset.getString("JHDZ")));
				qsData.put("CKDH", parseNull(ckReset.getString("CKDH")));
				qsData.put("KHCGDH", parseNull(ckReset.getString("KHCGDH")));
				qsData.put("SHDW", parseNull(ckReset.getString("KHMC")));
				qsData.put("SHFZR", parseNull(ckReset.getString("CXFZR")));
				qsData.put("SHFZRDH", parseNull(ckReset.getString("CXDH")));
				qsData.put("SHKF", parseNull(ckReset.getString("CK")));
				qsData.put("YSSLHJ", parseNull(ckReset.getString("YSHJ")));
				qsData.put("SHFZR", parseNull(ckReset.getString("KFLXR")));
				qsData.put("SHFZRDH", parseNull(ckReset.getString("CKLXRDH")));
				qsData.put("SHRQ", parseNull(ckReset.getString("FHRQ")));
				qsData.put("BZ", parseNull(ckReset.getString("BZ")));
			}
		} finally {
			DBSql.close(ckPs, ckReset);
		}

		insertOrUpdateBOData(conn, bindid, uid, "BO_AKL_QSD_P", qsData);
	}

	/**
	 * ���ǩ�յ�.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fillQSDBody(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();
		try {
			ps = conn.prepareStatement(QUERY_CKD_WLSL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				String wlbh = reset.getString("WLH");
				String wlmc = reset.getString("WLMC");
				String xh = reset.getString("XH");
				String jldw = reset.getString("JLDW");
				String khcpbm = reset.getString("KHCPBM");
				String wlsl = reset.getString("YSSL");
				hashtable.put("WLH", parseNull(wlbh));
				hashtable.put("CPMC", parseNull(wlmc));
				hashtable.put("XH", parseNull(xh));
				hashtable.put("DW", parseNull(jldw));
				hashtable.put("KHSPBH", parseNull(khcpbm));
				hashtable.put("YSSL", parseNull(wlsl));
				hashtable.put("SSSL", parseNull(wlsl));
				vector.add(hashtable);
			}
		} finally {
			DBSql.close(ps, reset);
		}
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_QSD_S", bindid);
		// ����
		if (vector.size() > 0) {
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_QSD_S", vector, bindid, uid);
		}
	}

	/**
	 * ���ԤԼ��
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fillYYD(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {

		String ckdh = DAOUtil.getStringOrNull(conn, "SELECT CKDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
		String khcgdh = DAOUtil.getStringOrNull(conn, "SELECT KHCGDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
		String xsddh = DAOUtil.getStringOrNull(conn, "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
		Integer yyzl = DAOUtil.getIntOrNull(conn, "SELECT SUM(ISNULL(SJSL, 0)) FROM BO_AKL_CKD_BODY WHERE BINDID=?", bindid);
		Integer ddsl = DAOUtil.getIntOrNull(conn, "SELECT SUM(ISNULL(SL, 0)) FROM BO_AKL_CKD_BODY WHERE BINDID=?", bindid);

		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		hashtable.put("CKDH", ckdh);
		hashtable.put("DDH", xsddh);

		if (khcgdh == null) {
			khcgdh = "";
		}
		if (yyzl == null) {
			yyzl = 0;
		}
		if (ddsl == null) {
			ddsl = 0;
		}

		Hashtable<String, String> hashtable2 = new Hashtable<String, String>();
		hashtable2.put("CKDH", parseNull(ckdh));
		hashtable2.put("DDSL", ddsl.toString());
		hashtable2.put("JDCGDH", parseNull(khcgdh));
		hashtable2.put("YYSHL", yyzl.toString());

		insertOrUpdateBOData(conn, bindid, uid, "BO_BO_AKL_CK_YY_P", hashtable);
		BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CK_YY_S", bindid);
		BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CK_YY_S", hashtable2, bindid, uid);
	}

	/**
	 * ԤԼ��+���ⵥ����˵�
	 * 
	 * @param conn
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fillYD(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {

		Hashtable<String, String> qsData = new Hashtable<String, String>();
		// ���ⵥ�š��ͻ��ɹ����š�Ӧ�պϼƣ����۶��������ֿ⡢������ַ���ֿ���ϵ�ˡ��ֿ���ϵ�˵绰���ֿ���ϵ���ֻ����ֿ���ϵ������
		PreparedStatement ckPs = null;
		ResultSet ckReset = null;
		try {
			ckPs = conn.prepareStatement(QUERY_CKD_TO_YSD);
			ckReset = DAOUtil.executeFillArgsAndQuery(conn, ckPs, bindid);
			if (ckReset.next()) {
				qsData.put("CKDH", parseNull(ckReset.getString("CKDH")));
				String xsddh = ckReset.getString("XSDDH");
				if (xsddh == null || xsddh.trim().equals("")) {
					xsddh = ckReset.getString("RMAFXDH");
				}
				qsData.put("DDH", parseNull(xsddh));
				qsData.put("TJ", parseNull(ckReset.getString("SJTJ")));
				qsData.put("ZL", parseNull(ckReset.getString("SJZL")));
				qsData.put("KHBH", parseNull(ckReset.getString("KH")));
				qsData.put("KHMC", parseNull(ckReset.getString("KHMC")));
				qsData.put("SL", parseNull(ckReset.getString("YSHJ")));
				qsData.put("JS", parseNull(ckReset.getString("JS")));
				qsData.put("SHR", parseNull(ckReset.getString("KFLXR")));
				qsData.put("SHDW", parseNull(ckReset.getString("JHDZ")));
				qsData.put("SHRDH", parseNull(ckReset.getString("CKLXRDH")));
				qsData.put("YSFS", parseNull(ckReset.getString("SHFS")));
				qsData.put("RQ", parseNull(ckReset.getString("FHRQ")));
				qsData.put("BZ", parseNull(ckReset.getString("BZ")));
				qsData.put("HZBM", "01065");
				// �ͻ�����
				String lbid = DAOUtil.getStringOrNull(conn, QUERY_KH_LBID, ckReset.getString("KH"));
				qsData.put("KHLX", parseNull(lbid));
			}
		} finally {
			DBSql.close(ckPs, ckReset);
		}

		PreparedStatement yydPs = null;
		ResultSet yydReset = null;
		try {
			yydPs = conn.prepareStatement(QUERY_YYD_TO_YSD);
			yydReset = DAOUtil.executeFillArgsAndQuery(conn, yydPs, bindid);
			if (yydReset.next()) {
				qsData.put("CYS", parseNull(yydReset.getString("CYS")));
				qsData.put("LXFS", parseNull(yydReset.getString("CYSDH")));
				qsData.put("LXR", parseNull(yydReset.getString("CYSLXR")));
			}
		} finally {
			DBSql.close(yydPs, yydReset);
		}

		// ������������
		insertOrUpdateBOData(conn, bindid, uid, "BO_AKL_YD_P", qsData);
	}

	/**
	 * ��ѯ������������ϣ����ҴӲֿ�ץȡ����.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void queryAllWlxx(Connection conn, int bindid, String uid, String xsddh) throws SQLException, AWSSDKException {

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_All_WLXX);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddh);

			while (reset.next()) {
				// ��ѯ��λ������ʱ������
				String wlbh = reset.getString("WLBH");
				String ckid = reset.getString("CKID");
				String pch = reset.getString("PCH");
				String wlmc = reset.getString("WLMC");
				String wlgg = reset.getString("WLGG");
				String xh = reset.getString("XH");
				String jldw = reset.getString("JLDW");
				String ddid = reset.getString("DDID");
				String khspbh = reset.getString("KHSPBH");
				BigDecimal tj = reset.getBigDecimal("TJ");
				BigDecimal zl = reset.getBigDecimal("ZL");
				BigDecimal ysje = reset.getBigDecimal("YSJE");
				BigDecimal sl = reset.getBigDecimal("SL");
				BigDecimal dj = reset.getBigDecimal("XSDJ");
				int ddslBackup = reset.getInt("DDSL");
				int ddsl = reset.getInt("DDSL");

				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset = null;

				try {
					kywlxxPs = conn.prepareStatement(QUERY_KY_WLXX);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh, pch, XSCKConstant.PC_ZT_ZC, ckid);
					// �ֻ�������� falseΪ�ѽ���
					boolean overFlag = true;
					while (overFlag && kywlxxReset.next()) {

						int haveSl = kywlxxReset.getInt("KWSL");
						String hwdm = kywlxxReset.getString("HWDM");

						if (haveSl != 0) {
							ddsl -= haveSl;

							// Ԥ��ת����ⵥ����
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("XSDDH", parseNull(ddid));
							hashtable.put("WLH", wlbh);
							hashtable.put("XH", xh);
							hashtable.put("FHKFBH", kywlxxReset.getString("CKDM"));
							hashtable.put("FHKFMC", kywlxxReset.getString("CKMC"));
							hashtable.put("GG", wlgg);
							hashtable.put("KWBH", hwdm);
							hashtable.put("WLMC", wlmc);
							hashtable.put("JLDW", jldw);
							hashtable.put("PC", pch);
							hashtable.put("TJ", tj.toString());
							hashtable.put("ZL", zl.toString());
							hashtable.put("KHCPBM", parseNull(khspbh));
							hashtable.put("KCSL", String.valueOf(haveSl));
							hashtable.put("DJ", ysje.divide(new BigDecimal(ddslBackup), XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE).toString());
							hashtable.put("CBDJ", kywlxxReset.getString("DJ"));
							hashtable.put("SHUIL", sl.toString());

							// Ʒ�ơ���ƷP/N������
							String ppid = DAOUtil.getStringOrNull(conn, "SELECT PPID FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);
							String pp = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE   DLBM = 006 AND XLBM=?", ppid);
							String sx = kywlxxReset.getString("SX");

							hashtable.put("PP", pp == null ? ppid : pp);
							hashtable.put("SX", sx == null ? "" : sx);

							if (ddsl <= 0) {
								hashtable.put("SL", String.valueOf(haveSl + ddsl));
								hashtable.put("SJSL", String.valueOf(haveSl + ddsl));
								hashtable.put("SJTJ", tj.toString());
								hashtable.put("SJZL", zl.toString());
								// ��������
								BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hashtable, bindid, uid);
								overFlag = false;
							} else {
								hashtable.put("SL", String.valueOf(haveSl));
								hashtable.put("SJSL", String.valueOf(haveSl));
								hashtable.put("SJTJ", tj.multiply(new BigDecimal(haveSl)).toString());
								hashtable.put("SJZL", zl.multiply(new BigDecimal(haveSl)).toString());
								// ��������
								BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hashtable, bindid, uid);
							}
						}
					}
					if (ddsl > 0) {
						throw new RuntimeException("���۶�����" + ddid + "�����ϱ��Ϊ" + wlbh + "�����ϣ����κ�Ϊ��" + pch + "�����������㣬���ܴ����ϻ�����;������");
					}
				} finally {
					DBSql.close(kywlxxPs, kywlxxReset);
				}
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ����Ӧ��.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertYS(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {

		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			// �Ƿ�Ԥ��Ϊ����Ӧ�ձ��в������ݡ�
			// String sfys = DAOUtil.getStringOrNull(conn,
			// "SELECT SFYS FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
			// if (sfys == null || "��".equals(sfys) || XSDDConstant.NO.equals(sfys) ||
			// "".equals(sfys.trim())) {
			// ��ѯ�ͻ����롢���۵��š����ⵥ�š�Ӧ�ս��
			Hashtable<String, String> hashtable = new Hashtable<String, String>();
			ps = conn.prepareStatement("SELECT KH,KHMC,CKDH,XSDDH,YSHJ,KHCGDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?");
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			if (reset.next()) {
				hashtable.put("KHBM", reset.getString("KH"));
				hashtable.put("KHMC", reset.getString("KHMC"));
				hashtable.put("CKDH", reset.getString("CKDH"));
				hashtable.put("XSDH", reset.getString("XSDDH"));
				hashtable.put("KHCGDH", reset.getString("KHCGDH"));
				BigDecimal ysje = DAOUtil.getBigDecimalOrNull(conn, "SELECT ZDJSHJ FROM BO_AKL_WXB_XSDD_HEAD WHERE DDID=?", reset.getString("XSDDH"));
				hashtable.put("YSJE", ysje.toString());
				hashtable.put("ZT", XSCKConstant.YS_WS);// ״̬
				hashtable.put("LB", XSCKConstant.KH);
			}
			// ����Ӧ�ձ�
			BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_YS", hashtable, bindid, uid);
		} finally {
			DBSql.close(ps, reset);
		}
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

	/**
	 * ������߸�������
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @param tableName
	 * @param qsData
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertOrUpdateBOData(Connection conn, int bindid, String uid, String tableName, Hashtable<String, String> qsData)
			throws SQLException, AWSSDKException {
		StringBuilder findIdSqlSb = new StringBuilder(30);
		findIdSqlSb.append("SELECT ID ");
		findIdSqlSb.append(" FROM ").append(tableName);
		findIdSqlSb.append(" WHERE BINDID=?");

		StringBuilder findCountSqlSb = new StringBuilder(30);
		findCountSqlSb.append("SELECT COUNT(*) ");
		findCountSqlSb.append(" FROM ").append(tableName);
		findCountSqlSb.append(" WHERE BINDID=?");

		Integer count = DAOUtil.getIntOrNull(conn, findCountSqlSb.toString(), bindid);
		if (count == null || count == 0) {
			// ����
			BOInstanceAPI.getInstance().createBOData(conn, tableName, qsData, bindid, uid);
		} else {
			// ����
			Integer boid = DAOUtil.getIntOrNull(conn, findIdSqlSb.toString(), bindid);
			BOInstanceAPI.getInstance().updateBOData(conn, tableName, qsData, boid);
		}
	}

	/**
	 * ��ȡ��������ʷ�ڵ����˲˵�
	 * 
	 * @param conn
	 * @param wfid
	 * @param stepNo
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public String getAuditMenu(Connection conn, int wfid, int stepNo, int bindid) throws SQLException {
		Integer wfsid = DAOUtil.getIntOrNull(conn, "SELECT ID FROM SYSFLOWSTEP WHERE STEPNO=?", stepNo);
		if (wfsid == null) {
			return null;
		}
		Integer taskid = DAOUtil.getIntOrNull(conn, "SELECT TOP 1 ID FROM WF_TASK_LOG WHERE WFID=? AND WFSID=? AND BIND_ID=? ORDER BY ENDTIME DESC",
				wfid, wfsid, bindid);
		if (taskid == null) {
			return null;
		}
		return DAOUtil.getStringOrNull(conn, "SELECT AUDIT_NAME FROM WF_MESSAGEAUDIT WHERE TASK_ID=?", bindid);
	}

	/**
	 * ��ȡ��һ����Ľڵ��
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Integer getPreviousStepNo(Connection conn, int bindid) throws SQLException {
		Integer wfsid = DAOUtil.getIntOrNull(conn, "SELECT TOP 1 WFSID FROM WF_TASK_LOG WHERE BIND_ID=? ORDER BY ENDTIME DESC ", bindid);
		if (wfsid == null) {
			return null;
		}
		return DAOUtil.getIntOrNull(conn, "SELECT STEPNO FROM SYSFLOWSTEP WHERE ID=?", wfsid);
	}

	/**
	 * ��ȡ�ɻ��˵Ľڵ�.
	 * 
	 * @param conn
	 * @param bindid
	 * @return
	 * @throws SQLException
	 */
	public Integer getPreviousStepNo(Connection conn, int bindid, int currentStepNo) throws SQLException {
		Integer wfsid = DAOUtil
				.getIntOrNull(
						conn,
						"SELECT TOP 1 WFSID FROM WF_TASK_LOG WHERE BIND_ID=? AND WFSID IN (SELECT ID FROM SYSFLOWSTEP WHERE STEPNO<?) ORDER BY ENDTIME DESC ",
						bindid, currentStepNo);
		if (wfsid == null) {
			return null;
		}
		return DAOUtil.getIntOrNull(conn, "SELECT STEPNO FROM SYSFLOWSTEP WHERE ID=?", wfsid);
	}

}
