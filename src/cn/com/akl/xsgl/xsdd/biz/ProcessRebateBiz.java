package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

/**
 * ����������.
 * 
 * @author huangming
 *
 */
public class ProcessRebateBiz {

	/**
	 * ���·�������.
	 */
	private static final String UPDATE_FLSL = "UPDATE BO_AKL_WXB_XS_FL_BODY SET YSYSL=ISNULL(YSYSL, 0)+? WHERE FLBH=? AND WLBH=? AND ISNULL(YSYSL, 0)+?<=FLSL";
	/**
	 * �ع���������.
	 */
	private static final String UPDATE_ROLLBACK_FLSL = "UPDATE BO_AKL_WXB_XS_FL_BODY SET YSYSL=ISNULL(YSYSL, 0)-? WHERE FLBH=? AND WLBH=? AND ISNULL(YSYSL, 0)-?>=0";
	/**
	 * ����Ӧ�ս��.
	 */
	private static final String UPDATE_YSJE = "UPDATE BO_AKL_WXB_XSDD_BODY SET YSJE=? WHERE ID=?";
	/**
	 * ������ʹ�ý��.
	 */
	private static final String UPDATE_ADD_YSYJE = "UPDATE BO_AKL_FL_MXB SET YSYJE=ISNULL(YSYJE,0)+? WHERE KHBM=?";
	/**
	 * ��ѯ�����Ƿ���ʱ�䷶Χ��.
	 */
	private static final String QUERY_FL_INRANGE_DATE = "SELECT b.FLFAH cc FROM BO_AKL_WXB_XS_FL_HEAD a RIGHT JOIN BO_AKL_WXB_XSDD_BODY b ON b.FLFAH=a.FLFABH AND FLFAH IS NOT NULL AND FLFAH<>'' WHERE (JSSJ<GETDATE() OR KSSJ>GETDATE()) AND b.BINDID=?";
	/**
	 * ��ѯ�����ʽ���д˿ͻ��������ļ�¼����.
	 */
	private static final String QUERY_YS_RECORDCOUNT = "SELECT COUNT(*) FROM BO_AKL_FL_MXB WHERE KHBM=?";
	/**
	 * ��ѯ������������.
	 */
	private static final String QUERY_FL_FLFAH_SL = "SELECT FLFAH, WLBH, SUM(ISNULL(FLSL, 0)) as FLZCSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND FLFAH<>'' AND FLFAH is not null GROUP BY FLFAH, WLBH";
	/**
	 * У�鷵����������.
	 */
	private static final String QUERY_VALIDATE_FL_FLFAH_SL = "SELECT FLFABH+','+b.XH FROM BO_AKL_WXB_XS_FL_HEAD a, BO_AKL_WXB_XS_FL_BODY b WHERE a.BINDID=b.BINDID AND FLFABH=? AND WLBH=? AND FLSL-YSYSL<?";

	/**
	 * ���·���.
	 * 
	 * @param conn
	 * @param reset
	 * @param ddsl
	 * @throws SQLException
	 */
	public void updateFL(Connection conn, String flfah, int flsl, String wlbh) throws SQLException {
		// ��˲˵�ѡ��ͬ��ʱ�����·����������е�ǰ���������ķ�������������=��������+������
		// ��˲˵�ѡ��ͬ��ʱ������ݴ������붩�������Ĳ���·����������е�ǰ���������ķ�����������������=��������+��������-����������
		// ���� �۸�֧�ַ��������ݿ�ʼ����ʱ�䣬���ݶ������������·���֧�ַ������з��������з���������
		// ���� �ʽ�ط��������ݶ�������ķ���֧�ֽ����·����ʽ�ر��е���ʹ�ý�
		// ����������������֧�ֵ��ۡ�*��������д�뷵���ʽ���У��ɱ��������з�����ϸ��}
		// ��������
		// ����������
		// if (XSDDConstant.FL_FALX_JGZC.equals(flfalx)) {
		// ������������
		// ���� -ddsl
		if (DAOUtil.executeUpdate(conn, UPDATE_FLSL, flsl, flfah, wlbh, flsl) == 0)
			throw new RuntimeException("������������ʧ��");
		// }
		// �ȷ��� Ӧ�ս��=�����ܶ���� Ӧ�ս��=�����ܶ�
		/*
		 * BigDecimal yfje = ("0".equals(flfs)) ? jjze : ddzje; if (DAOUtil.executeUpdate(conn,
		 * updateYSJE, yfje, id) == 0) throw new RuntimeException("����Ӧ������ʧ��");
		 */
	}

	/**
	 * У�鷵������.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateFLSL(Connection conn, int bindid) throws SQLException {
		// ��������
		PreparedStatement ps = null;
		ResultSet reset = null;
		try {
			ps = conn.prepareStatement(QUERY_FL_FLFAH_SL);
			reset = DAOUtil.executeFillArgsAndQuery(conn, ps, bindid);
			while (reset.next()) {
				String flfah = reset.getString("FLFAH");
				if (flfah == null || "".equals(flfah.trim()))
					continue;

				String wlbh = reset.getString("WLBH");
				int flzcsl = reset.getInt("FLZCSL");
				String message = DAOUtil.getStringOrNull(conn, QUERY_VALIDATE_FL_FLFAH_SL, flfah, wlbh, flzcsl);
				if (message != null)
					throw new RuntimeException("���������������㣬���������ź����Ϻ�Ϊ��" + message);
			}
		} finally {
			DBSql.close(ps, reset);
		}
	}

	/**
	 * У�鷵�������Ƿ�ʱ.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validateFLTimeOut(Connection conn, int bindid) throws SQLException {
		// ��������Ƿ�ʱ�ж�
		String message = DAOUtil.getStringOrNull(conn, QUERY_FL_INRANGE_DATE, bindid);
		if (message != null) {
			throw new RuntimeException("����������" + message + " ����");
		}
	}

	/**
	 * ���˷���.
	 * 
	 * @param conn
	 * @param flfah
	 * @param flsl
	 * @param wlbh
	 * @throws SQLException
	 */
	public void rollbackFL(Connection conn, String flfah, int flsl, String wlbh) throws SQLException {
		// ��˲˵�ѡ��ͬ��ʱ�����·����������е�ǰ���������ķ�������������=��������+������
		// ��˲˵�ѡ��ͬ��ʱ������ݴ������붩�������Ĳ���·����������е�ǰ���������ķ�����������������=��������+��������-����������
		// ���� �۸�֧�ַ��������ݿ�ʼ����ʱ�䣬���ݶ������������·���֧�ַ������з��������з���������
		// ���� �ʽ�ط��������ݶ�������ķ���֧�ֽ����·����ʽ�ر��е���ʹ�ý�
		// ����������������֧�ֵ��ۡ�*��������д�뷵���ʽ���У��ɱ��������з�����ϸ��}
		// ��������
		// ����������
		// if (XSDDConstant.FL_FALX_JGZC.equals(flfalx)) {
		// ������������
		// ���� -ddsl
		if (DAOUtil.executeUpdate(conn, UPDATE_ROLLBACK_FLSL, flsl, flfah, wlbh, flsl) == 0)
			throw new RuntimeException("������������ʧ��");
		// }
	}

	/**
	 * �������.
	 * 
	 * @param conn
	 * @param bindId
	 * @param uid
	 * @param khid
	 * @param flfs
	 * @param flfah
	 * @param ddzje
	 * @param jjze
	 * @param flzcj
	 * @param id
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void processHFL(Connection conn, int bindId, String uid, String khid, String flfs, String flfah, BigDecimal jjze,
			BigDecimal flzcj) throws SQLException, AWSSDKException {
		if (XSDDConstant.FL_FLFS_HFL.equals(flfs)) {
			// ��
			// ע�뷵���ʽ��
			// TODO ÿ�� ��������+�ͻ� = ����
			int count = DAOUtil.getInt(conn, QUERY_YS_RECORDCOUNT, khid);
			if (count == 0) {
				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("FABH", flfah);
				hashtable.put("KHBM", khid);
				hashtable.put("LX", "0");
				hashtable.put("YSYJE", String.valueOf(flzcj.floatValue()));
				hashtable.put("FLJE", String.valueOf(flzcj.floatValue()));
				hashtable.put("BXSQJE", "0");
				hashtable.put("YSJE", "0");
				hashtable.put("SSJE", "0");
				hashtable.put("ZT", "0");
				BOInstanceAPI.getInstance().createBOData("BO_AKL_FL_MXB", hashtable, bindId, uid);
			} else {
				DAOUtil.executeUpdate(conn, UPDATE_ADD_YSYJE, flzcj, khid);
			}
		}
	}
	
	/**
	 * ���˺���.
	 * 
	 * @param conn
	 * @param khid
	 * @param flfs
	 * @param ddzje
	 * @param flzcj
	 * @param id
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void rollbackHFL(Connection conn, String khid, String flfs, BigDecimal ddzje,
			BigDecimal flzcj) throws SQLException, AWSSDKException {
		if (XSDDConstant.FL_FLFS_HFL.equals(flfs)) {
			// ��
			// �������
			// ע�뷵���ʽ��
			// TODO ÿ�� ��������+�ͻ� = ����
			int count = DAOUtil.getInt(conn, QUERY_YS_RECORDCOUNT, khid);
			if (count == 0) {
				throw new RuntimeException("�Ҳ����˿ͻ��ķ����ʽ�ؼ�¼���޷�����! �ͻ�ID:" + khid);
			} else {
				DAOUtil.executeUpdate(conn, UPDATE_ADD_YSYJE, -flzcj.doubleValue(), khid);
			}
		}
	}

	/**
	 * ��������ʽ������Ӧ��
	 * 
	 * @param conn
	 * @param bindId
	 * @param uid
	 * @param khid
	 * @param flfs
	 * @param flfah
	 * @param ddzje
	 * @param jjze
	 * @param flzcj
	 * @param id
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void processFLFS(Connection conn, String flfs, BigDecimal ddzje, BigDecimal jjze, BigDecimal flzcj, int id) throws SQLException{
		if (XSDDConstant.FL_FLFS_XFL.equals(flfs)) {
			// �ȷ�
			DAOUtil.executeUpdate(conn, UPDATE_YSJE, jjze, id);
		}

		if (XSDDConstant.FL_FLFS_HFL.equals(flfs)) {
			// ��
			DAOUtil.executeUpdate(conn, UPDATE_YSJE, ddzje, id);
		}

		if (XSDDConstant.FL_FLFS_BFL.equals(flfs) || flfs == null || "".equals(flfs.trim())) {
			// ����
			DAOUtil.executeUpdate(conn, UPDATE_YSJE, ddzje, id);
		}
	}
}
