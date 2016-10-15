package cn.com.akl.dgkgl.xsck.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.web.util.PrintUtil;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class DGOutFillBiz {
	/**
	 * ��ѯ���۶�����BINDID.
	 */
	private static final String QUERY_SALES_BINDID= "SELECT BINDID FROM BO_AKL_DGXS_P WHERE XSDDID=?";
	/**
	 * ��ѯ�������.
	 */
	private static final String QUERY_HZBH = "SELECT HZBH FROM BO_AKL_DGXS_P WHERE XSDDID=?";
	/**
	 * ��ѯ������д����۶���������.
	 */
	private static final String QUERY_LOCK_MATERIAL_NUM = "SELECT a.HWKYSL, a.WLBH, a.XSDH, a.XSSL, a.PCH, a.HWDM, b.GG, b.XH, b.DW, b.WLMC, a.KHCGDH  FROM BO_AKL_DGCKSK a left join BO_AKL_WLXX b on a.WLBH = b.WLBH WHERE a.XSDH=? AND b.HZBM=? AND b.WLZT in (0, 1, 4) order by a.KHCGDH, b.XH";
	/**
	 * ��ѯ�ֿ�Ŀ���������Ϣ.
	 */
	private static final String QUERY_CANUSE_MATERIAL = "SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? and s.PCH=? and s.HWDM=? and a.HZBM=? AND s.SX in ('049088', '049090') AND a.WLZT in (0, 1, 4)";
	/**
	 * ��ѯ��������������Ϣ.
	 */
	private static final String QUERY_SALES_BODY_MATERIAL = "SELECT b.WLBH,b.WLMC,b.GG,b.XH,b.JLDW,b.XSSL,b.KCSL,b.YCKSL,b.KHCGDH  FROM BO_AKL_DGXS_S b WHERE BINDID=?";
	/**
	 * ��ѯ�ֿ�Ŀ���������Ϣ.
	 */
	private static final String QUERY_REPOSITORY_MATERIAL = "SELECT s.ID, s.WLBH, s.PCH, s.KWSL, s.HWDM, s.CKDM, s.CKMC, s.QDM, s.DDM, s.SX, s.KWDM, ISNULL(a.TJ, 0) TJ, ISNULL(a.ZL, 0) ZL FROM BO_AKL_DGKC_KCMX_S s left join BO_AKL_WLXX a on a.WLBH = s.WLBH WHERE s.WLBH=? ORDER BY s.PCH, s.KWSL";
	/**
	 * ��ѯ���ϵ���������.
	 */
	private static final String QUERY_MATERIAL_LOCKNUM = "SELECT SUM(ISNULL(XSSL, 0)) SDSL FROM BO_AKL_DGCKSK WHERE WLBH=? AND PCH=? AND HWDM=?";
	/**
	 * ��ѯ�����۶������ϵ���������.
	 */
	private static final String QUERY_SALES_MATERIAL_LOCKNUM = "SELECT SUM(ISNULL(XSSL, 0)) SDSL FROM BO_AKL_DGCKSK WHERE WLBH=? AND XSDH=?";
	
	/**
	 * ��ѯ������������ϣ����ҴӲֿ�ץȡ����.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fetchLockMaterial(Connection conn, int bindid, String uid, String xsddh) throws SQLException, AWSSDKException {
		String hzbh = DAOUtil.getStringOrNull(conn, QUERY_HZBH, xsddh);
		PreparedStatement ps = conn.prepareStatement(QUERY_LOCK_MATERIAL_NUM);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddh, hzbh);
		try {
			while (reset.next()) {
				// ��ѯ��λ������ʱ������
				int hwkysl = reset.getInt("HWKYSL");
				String wlbh = PrintUtil.parseNull(reset.getString("WLBH"));
				String khcgdh = PrintUtil.parseNull(reset.getString("KHCGDH"));
				String wlmc = PrintUtil.parseNull(reset.getString("WLMC"));
				String wlgg = PrintUtil.parseNull(reset.getString("GG"));
				String xh = PrintUtil.parseNull(reset.getString("XH"));
				String jldw = PrintUtil.parseNull(reset.getString("DW"));
				int xssl = reset.getInt("XSSL");
				String pch = PrintUtil.parseNull(reset.getString("PCH"));
				String hwdm = PrintUtil.parseNull(reset.getString("HWDM"));
				// ��ѯ���۵���ժҪ
				String zy = DAOUtil.getStringOrNull(conn, "SELECT zy from BO_AKL_DGXS_S where DDID=? and WLBH=?", xsddh, wlbh);
				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset = null;
				try {
					kywlxxPs = conn.prepareStatement(QUERY_CANUSE_MATERIAL);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh, pch, hwdm, hzbh);
					while (kywlxxReset.next()) {
						int kwsl = kywlxxReset.getInt("KWSL");
						int haveSl = hwkysl;
						String qdm = PrintUtil.parseNull(kywlxxReset.getString("QDM"));
						String ddm = PrintUtil.parseNull(kywlxxReset.getString("DDM"));
						String kwdm = PrintUtil.parseNull(kywlxxReset.getString("KWDM"));
						String ckdm = PrintUtil.parseNull(kywlxxReset.getString("CKDM"));
						String ckmc = PrintUtil.parseNull(kywlxxReset.getString("CKMC"));
						String sx = PrintUtil.parseNull(kywlxxReset.getString("SX"));
						int TJ = kywlxxReset.getInt("TJ");
						int ZL = kywlxxReset.getInt("ZL");
						if (haveSl >= xssl) {
							// Ԥ��ת����ⵥ����
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("CKDM", ckdm);
							hashtable.put("KHCGDH", khcgdh);
							hashtable.put("CKMC", ckmc);
							hashtable.put("QDM", qdm);
							hashtable.put("DDM", ddm);
							hashtable.put("KWDM", kwdm);
							hashtable.put("HWDM", hwdm);
							hashtable.put("DDH", xsddh);
							hashtable.put("PCH", pch);
							hashtable.put("WLBH", wlbh);
							hashtable.put("XH", xh);
							hashtable.put("GG", wlgg);
							hashtable.put("WLMC", wlmc);
							hashtable.put("DW", jldw);
							hashtable.put("KCSL", String.valueOf(kwsl));
							hashtable.put("SX", sx);
							hashtable.put("TJ", String.valueOf(TJ));
							hashtable.put("ZL", String.valueOf(ZL));
							hashtable.put("BZ", zy);
							hashtable.put("SFSL", String.valueOf(xssl));
							hashtable.put("YFSL", String.valueOf(xssl));
							hashtable.put("HWKYSL", String.valueOf(hwkysl));

							BOInstanceAPI.getInstance().createBOData(conn, "BO_BO_AKL_DGCK_S", hashtable, bindid, uid);
						} else {
							throw new RuntimeException("���۶�����" + xsddh + "�����ϱ��Ϊ" + wlbh + "�ͺ�Ϊ" + xh + "������λΪ" + jldw + "�����Ͽ����������㡣");
						}
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
	 * �����۶�����ץȡ����.<br/>
	 * �������۶���ʣ��δ��������Ͻ�����������.<br/>
	 * ���۶�����������-�ѳ�������-���γ������� = ʣ�µ����� 
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void fetchCanUseMaterial(Connection conn, int bindid, String uid, String xsddh) throws SQLException, AWSSDKException {
		String xsddbindid = DAOUtil.getStringOrNull(conn, QUERY_SALES_BINDID, xsddh);
		PreparedStatement ps = conn.prepareStatement(QUERY_SALES_BODY_MATERIAL);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddbindid);
		try {
			while (reset.next()) {
				// ��ѯ��λ������ʱ������
				String wlbh = PrintUtil.parseNull(reset.getString("WLBH"));
				String xh = PrintUtil.parseNull(reset.getString("XH"));
				String khcgdh = PrintUtil.parseNull(reset.getString("KHCGDH"));
				int xssl = reset.getInt("XSSL");
				int ycksl = reset.getInt("YCKSL");

				// ����������Ҫ��ȥ�ѳ�������.
				int ysksl = DAOUtil.getInt(conn, QUERY_SALES_MATERIAL_LOCKNUM, wlbh, xsddh);
				int sl = xssl - ycksl - ysksl;

				PreparedStatement kywlxxPs = null;
				ResultSet kywlxxReset = null;
				try {
					kywlxxPs = conn.prepareStatement(QUERY_REPOSITORY_MATERIAL);
					kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh);

					// �ֻ�������� falseΪ�ѽ���
					boolean overFlag = true;
					while (overFlag && kywlxxReset.next()) {
						int kwsl = kywlxxReset.getInt("KWSL");
						String hwdm = PrintUtil.parseNull(kywlxxReset.getString("HWDM"));
						String pch = PrintUtil.parseNull(kywlxxReset.getString("PCH"));

						int kysl;

						// ��ѯ�����ϵ��������.
						Integer sdsl = DAOUtil.getIntOrNull(conn, QUERY_MATERIAL_LOCKNUM, wlbh, pch, hwdm);
						if (sdsl == null || sdsl == 0) {
							kysl = kwsl;
						} else {
							kysl = kwsl - sdsl;
						}
						int haveSl = kysl;

						// ���������㹻�������.
						if (haveSl > 0) {
							sl -= haveSl;

							// Ԥ��ת����ⵥ����
							Hashtable<String, String> hashtable = new Hashtable<String, String>();
							hashtable.put("HWDM", hwdm);
							hashtable.put("XSDH", xsddh);
							hashtable.put("PCH", pch);
							hashtable.put("KHCGDH", khcgdh);
							hashtable.put("WLBH", wlbh);
							hashtable.put("HWKYSL", String.valueOf(kysl));
							if (sl <= 0) {
								hashtable.put("XSSL", String.valueOf(haveSl + sl));
								// �ֻ�����
								overFlag = false;
							} else {
								hashtable.put("XSSL", String.valueOf(haveSl));
							}
							// ��������
							BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCKSK", hashtable, bindid, uid);
						}
					}
					if (overFlag == true && sl > 0) {
						throw new RuntimeException("���۶�����" + xsddh + "�����ϱ��Ϊ" + wlbh + "�ͺ�Ϊ" + xh + "�����Ͽ����������㡣");
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
	 * �Ƴ�����.
	 * @param conn
	 * @param xsdh
	 * @throws SQLException
	 */
	public void removeLockMaterial(Connection conn, String xsdh) throws SQLException{
		DAOUtil.executeUpdate(conn, "DELETE FROM BO_AKL_DGCKSK WHERE XSDH=?", xsdh);
	}

	/**
	 * �����ⵥ�ĵ������½�������.
	 * 
	 * @param conn
	 * @param parentBindid
	 * @param bindid
	 * @param uid
	 * @param xsdh
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void insertLockFromBody(Connection conn, int bindid, String uid, String xsdh) throws SQLException, AWSSDKException {
		PreparedStatement ps = null;
		ResultSet result = null;
		try {
			int xsddbindid = DAOUtil.getIntOrNull(conn, "SELECT BINDID FROM BO_AKL_DGXS_P WHERE XSDDID=?", xsdh);

			ps = conn.prepareStatement("SELECT WLBH,HWDM,PCH,HWKYSL,SFSL,KHCGDH FROM BO_BO_AKL_DGCK_S WHERE BINDID=?");
			result = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			// �������еĵ���������Ϣ
			while (result.next()) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("HWDM", parseNullStr(result.getString("HWDM")));
				hashtable.put("KHCGDH", parseNullStr(result.getString("KHCGDH")));
				hashtable.put("PCH", parseNullStr(result.getString("PCH")));
				hashtable.put("HWKYSL", String.valueOf(result.getInt("HWKYSL")));
				hashtable.put("XSDH", xsdh);
				hashtable.put("WLBH", parseNullStr(result.getString("WLBH")));
				hashtable.put("XSSL", String.valueOf(result.getInt("SFSL")));
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_DGCKSK", hashtable, xsddbindid, uid);
			}
		} finally {
			DBSql.close(ps, result);
		}
	}
	
	public String parseNullStr(String str){
		return str==null?"":str;
	}


}
