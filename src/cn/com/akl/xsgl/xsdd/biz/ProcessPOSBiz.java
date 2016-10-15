package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;

public class ProcessPOSBiz {

	/**
	 * ��ѯ����POS��¼.
	 */
	private static final String QUERY_DSPOS = "SELECT 	ID, WLBH, PCH, DDID, CKID, DDSL, DFSL, POSID, POSFALX, POSJE, POSZCSL, FLFAH, FLFAMC, FLFALX, FLFS, FLZCJ, FLZCD, FLHJ FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=?";
	/**
	 * ��ѯ����POS.
	 */
	private static final String QUERY_POSGQ = "SELECT a.POSID cc FROM (SELECT POSID, POSFALX FROM BO_AKL_WXB_XSDD_BODY WHERE bindid=? AND POSID IS NOT NULL AND POSID<>''  GROUP BY POSID, POSFALX) as a, BO_AKL_WXB_XS_POS_HEAD as b, BO_AKL_WXB_XS_POS_BODY as c WHERE a.POSID=b.POSBH AND b.BINDID=c.BINDID AND a.POSFALX=? AND (JSSJ<GETDATE() OR KSSJ>GETDATE())";
	/**
	 * ����POS���.
	 */
	private static final String UPDATE_ROLLBACK_POSJE = "UPDATE BO_AKL_POS_MXB SET YSYJE=ISNULL(YSYJE, 0)-? WHERE FABH=?";
	/**
	 * ����POS����.
	 */
	private static final String UPDATE_ROLLBACK_POSSL = "UPDATE BO_AKL_WXB_XS_POS_BODY SET YSYSL=ISNULL(YSYSL, 0)-? WHERE BINDID=? AND WLBH=?";
	/**
	 * ����POS�ʽ�ص�Ӧ�ս��.
	 */
	private static final String UPDATE_POSZJCYSJE = "UPDATE BO_AKL_POS_MXB SET YSYJE=ISNULL(YSYJE, 0)+? WHERE FABH=? AND YSYJE+?<=POSJE";
	/**
	 * ����POS��������.
	 */
	private static final String UPDATE_POSFASL = "UPDATE BO_AKL_WXB_XS_POS_BODY SET YSYSL=ISNULL(YSYSL, 0)+? WHERE BINDID=? AND WLBH=? AND ISNULL(YSYSL, 0)+?<=POSSL";
	/**
	 * ��ѯPOS��bindid.
	 */
	private static final String QUERY_POS_BINDID = "SELECT BINDID FROM BO_AKL_WXB_XS_POS_HEAD WHERE POSBH=?";
	/**
	 * ��POSID�����ѯÿ���������.
	 */
	private static final String QUERY_XSDD_DS_POS_FA = "SELECT POSID, WLBH, SUM(ISNULL(POSZCSL, 0)) as zcsl FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND POSFALX=? GROUP BY POSID, WLBH";
	/**
	 * ��ѯPOS��������.
	 */
	private static final String QUERY_VALIDATE_POS_FA_SL = "SELECT a.POSBH+','+b.XH FROM BO_AKL_WXB_XS_POS_HEAD a,BO_AKL_WXB_XS_POS_BODY b WHERE a.POSBH=? AND a.BINDID=b.BINDID AND b.WLBH=? AND ISNULL(b.POSSL, 0)-ISNULL(b.YSYSL, 0)<?";
	/**
	 * ��ѯ���۶�����ÿ��POS�Ļ��ܽ��.
	 */
	private static final String QUERY_XSDD_DS_POS_ZJC = "SELECT POSID, SUM(ISNULL(POSJE, 0)) as posje FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND POSFALX=? GROUP BY POSID";
	/**
	 * ��ѯ���������.
	 */
	private static final String QUERY_VALIDATE_POS_ZJC_JE = "SELECT FABH FROM BO_AKL_POS_MXB WHERE FABH=? AND ISNULL(POSJE, 0)-ISNULL(YSYJE, 0)>=?";
	/**
	 * ��ѯPOSԭ�۸�.
	 */
	private static final String QUERY_VALIDATE_POS_YJG = "SELECT YJG FROM BO_AKL_WXB_XS_POS_HEAD h, BO_AKL_WXB_XS_POS_BODY b WHERE h.bindid=b.bindid AND h.POSBH=? AND WLBH=?";
	/**
	 * ��ѯ�����Ƿ���POS����.
	 */
	private static final String QUERY_VALIDATE_HAVE_POS = "SELECT COUNT(*) FROM BO_AKL_WXB_XS_POS_HEAD h, BO_AKL_WXB_XS_POS_BODY b WHERE  h.bindid=b.bindid AND POSZT='030003' AND (KHBM=? OR KHBM='' OR KHBM IS NULL) AND WLBH=? AND POSSL-ISNULL(YSYSL, 0)>0";

	/**
	 * У��POS�����Ƿ�������������.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOSFAEqualsFA(Connection conn, int bindid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_XSDD_DS_POS_FA);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, XSDDConstant.POS_FALX_FA);
			while (reset.next()) {
				String posid = reset.getString("POSID");
				if (posid == null || "".equals(posid.trim()))
					continue;
				String wlbh = reset.getString("WLBH");
				int zcsl = reset.getInt("zcsl");
				String message = DAOUtil.getStringOrNull(conn, QUERY_VALIDATE_POS_FA_SL, posid, wlbh, zcsl);
				if (message != null)
					throw new RuntimeException("��POS����֧���������࣬POS��֧�ֵ��ͺŷֱ��ǣ�" + message);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * У��POS�ʽ���Ƿ��н�����.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOSFAEqualsZJC(Connection conn, int bindid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_XSDD_DS_POS_ZJC);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid, XSDDConstant.POS_FALX_ZJC);
			while (reset.next()) {
				String posid = reset.getString("POSID");
				if (posid == null || "".equals(posid.trim()))
					continue;
				BigDecimal posje = reset.getBigDecimal("posje");
				String message = DAOUtil.getStringOrNull(conn, QUERY_VALIDATE_POS_ZJC_JE, posid, posje);
				if (message != null)
					throw new RuntimeException("POS�ʽ�ط������ֳ��POS������Ϊ��" + message);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * ��֤POS�Ƿ����.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOSTimeOut(Connection conn, int bindid) throws SQLException {
		String message = DAOUtil.getStringOrNull(conn, QUERY_POSGQ, bindid, XSDDConstant.POS_FALX_FA);
		if (message != null)
			throw new RuntimeException("��POS����" + message + "�ѹ��ڡ����̲����°���");
	}

	/**
	 * ����POS.
	 * 
	 * @param conn
	 * @param posid
	 * @param posfalx
	 * @param posje
	 * @param possl
	 * @param wlbh
	 * @throws SQLException
	 */
	public void rollBackPOS(Connection conn, String posid, String posfalx, BigDecimal posje, int possl, String wlbh) throws SQLException {

		// ��˲˵�ѡ��ͬ��ʱ������POS�������е�ǰPOS������POS��POS����=POS����+������
		// ��˲˵�ѡ��ͬ��ʱ������ݴ������붩�������Ĳ����POS�������е�ǰPOS������POS������POS����=POS����+��������-����������
		// POS ���淽��������POS�����С�ʣ��֧�������������㶩��������е�POS���=��������*POS֧�ֵ��ۣ���¼��ϸ��
		// POS
		// �ʽ�ط������Զ�����POS�����ġ�POS֧�ֵ��ۡ�=��POS���/����������������POS�ʽ�ػ��ܱ��еġ���ʹ�ý�������������е�POS����ֹ�¼��
		// 1���������POS
		// 2������POS֧������ѡ��ۼ���λ��
		if (XSDDConstant.POS_FALX_ZJC.equals(posfalx)) {
			// ���tyFlag=true �������°�����ͨ���ۼ���ѯ�����
			// ���� posje
			// POS�ʽ�� ���½��
			if (DAOUtil.executeUpdate(conn, UPDATE_ROLLBACK_POSJE, posje, posid) == 0)
				throw new RuntimeException("����POS�ʽ�س��ִ��� ������:" + posid);
		}

		if (XSDDConstant.POS_FALX_FA.equals(posfalx)) {
			// POS ���� ��������
			// ���� ddsl
			String posbindid = DAOUtil.getString(conn, QUERY_POS_BINDID, posid);
			if (DAOUtil.executeUpdate(conn, UPDATE_ROLLBACK_POSSL, possl, posbindid, wlbh) == 0)
				throw new RuntimeException("����POS�������ִ��� ������:" + posid);
		}
	}

	/**
	 * ����POS.
	 * 
	 * @param conn
	 * @param posid
	 * @param posfalx
	 * @param posje
	 * @param possl
	 * @param wlbh
	 * @throws SQLException
	 */
	public void updatePOS(Connection conn, String posid, String posfalx, BigDecimal posje, int possl, String wlbh) throws SQLException {
		// ��˲˵�ѡ��ͬ��ʱ������POS�������е�ǰPOS������POS��POS����=POS����+������
		// ��˲˵�ѡ��ͬ��ʱ������ݴ������붩�������Ĳ����POS�������е�ǰPOS������POS������POS����=POS����+��������-����������
		// POS ���淽��������POS�����С�ʣ��֧�������������㶩��������е�POS���=��������*POS֧�ֵ��ۣ���¼��ϸ��
		// POS
		// �ʽ�ط������Զ�����POS�����ġ�POS֧�ֵ��ۡ�=��POS���/����������������POS�ʽ�ػ��ܱ��еġ���ʹ�ý�������������е�POS����ֹ�¼��
		// 1���������POS
		// 2������POS֧������ѡ��ۼ���λ��
		if (XSDDConstant.POS_FALX_ZJC.equals(posfalx)) {
			// ���tyFlag=true �������°�����ͨ���ۼ���ѯ�����
			// POS�ʽ�� ���½��
			if (DAOUtil.executeUpdate(conn, UPDATE_POSZJCYSJE, posje, posid, posje) == 0)
				throw new RuntimeException("����POS�ʽ�س��ִ��󣬿�����POS֧�ֽ������ʽ�ؽ�������:" + posid);
		}

		if (XSDDConstant.POS_FALX_FA.equals(posfalx)) {
			// POS ���� ��������
			String posbindid = DAOUtil.getString(conn, QUERY_POS_BINDID, posid);
			if (DAOUtil.executeUpdate(conn, UPDATE_POSFASL, possl, posbindid, wlbh, possl) == 0)
				throw new RuntimeException("����POS�������ִ��󣬿����Ƕ���POS֧�����������˿�֧�ֵ�������������:" + posid);
		}
	}

	/**
	 * �����������д����̵�POS.
	 * 
	 * @param conn
	 * @param bindId
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void rollBackPOSFromBindIdForZF(Connection conn, int bindId) throws SQLException, AWSSDKException {
		PreparedStatement state = null;
		ResultSet reset = null;

		try {
			// ��ѯ�����������еļ�¼
			state = conn.prepareStatement(QUERY_DSPOS);
			reset = DAOUtil.executeFillArgsAndQuery(conn, state, bindId);
			while (reset.next()) {
				String posfalx = reset.getString("POSFALX");
				String posid = reset.getString("POSID");
				BigDecimal posje = reset.getBigDecimal("POSJE");
				int dfsl = reset.getInt("POSZCSL");
				String wlbh = reset.getString("WLBH");
				// ����POS
				rollBackPOS(conn, posid, posfalx, posje, dfsl, wlbh);
			}
		} finally {
			DBSql.close(state, reset);
		}
	}

	/**
	 * У��POSԭ�۸�.
	 * ����򷵻�TRUE
	 * 
	 * @param posid
	 * @param wlbh
	 * @param nowPrice
	 * @return
	 * @throws SQLException
	 */
	public boolean validatePOSSalaPrice(Connection conn, String posid, String wlbh, BigDecimal nowPrice) throws SQLException {
		BigDecimal oldPrice = DAOUtil.getBigDecimalOrNull(conn, QUERY_VALIDATE_POS_YJG, posid, wlbh);
		return oldPrice.doubleValue() == nowPrice.doubleValue();
	}

	/**
	 * ��֤�����Ƿ��п���POS.
	 * 
	 * @param conn
	 * @param wlbh
	 * @return
	 * @throws SQLException
	 */
	public boolean validateIsHavePOS(Connection conn, String khid, String wlbh) throws SQLException {
		Integer count = DAOUtil.getIntOrNull(conn, QUERY_VALIDATE_HAVE_POS, khid, wlbh);
		return count == null || count == 0;
	}

}
