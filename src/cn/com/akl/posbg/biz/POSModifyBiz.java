package cn.com.akl.posbg.biz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;

public class POSModifyBiz {

	/**
	 * ��ѯPOS��BINDID.
	 */
	private static final String QUERY_POS_BINDID = "SELECT BINDID FROM BO_AKL_WXB_XS_POS_HEAD WHERE POSBH=?";
	/**
	 * ����pos���뵥ͷ.
	 */
	private static final String UPDATE_POS_DT = "update BO_AKL_WXB_XS_POS_HEAD set KSSJ=?, JSSJ=?, POSMC=? where POSBH=?";
	/**
	 * �������۶����е�POS�Լ������ļ���..
	 */
	private static final String UPDATE_XSDD_POS = "UPDATE BO_AKL_WXB_XSDD_BODY SET POSZCDJ =?, POSJE =?*POSZCSL, CBZE = ( PCCBJ * DDSL - ISNULL(POSZCSL, 0) * ? ) * ( CASE WHEN SL IS NULL THEN 1.17 ELSE 1 + (SL / 100) END ), JJMLL = ( 1 - ( PCCBJ * DDSL - ISNULL(POSZCSL, 0) * ? ) * ( CASE WHEN SL IS NULL THEN 1.17 ELSE 1 + (SL / 100) END ) / JJZE ) * 100 WHERE WLBH=? AND POSID=?";
	/**
	 * ����POS��������.
	 */
	private static final String UPDATE_POS_DS = "UPDATE BO_AKL_WXB_XS_POS_BODY SET POSDJ=?, POSSL=? WHERE BINDID=? AND WLBH=?";

	/**
	 * �������۶����и�POS�ĵ��ۣ�֧�ֵ��ۣ�����ë����.
	 * 
	 * @return
	 */
	public ResultPaserAbs getUpdateSalerOrder() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String posbh = reset.getString("POSBH");
				String wlbh = reset.getString("WLBH");
				Double posdj = reset.getDouble("POSDJ");
				DAOUtil.executeUpdate(conn, UPDATE_XSDD_POS, posdj, posdj, posdj, posdj, wlbh, posbh);
				return true;
			}
		};
	}

	/**
	 * ����POS�еļ�¼��Ϣ.
	 * 
	 * @param posid
	 * @return
	 */
	public ResultPaserAbs getUpdatePOSBody(final String posid) {
		return new ResultPaserAbs() {
			private int bindid;

			@Override
			public void init(Connection conn) throws SQLException {
				bindid = DAOUtil.getIntOrNull(conn, QUERY_POS_BINDID, posid);
			}

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");
				Double posdj = reset.getDouble("POSDJ");
				int possl = reset.getInt("POSSL");
				DAOUtil.executeUpdate(conn, UPDATE_POS_DS, posdj, possl, bindid, wlbh);
				return true;
			}
		};
	}

	/**
	 * ����POS��ͷ����Ϣ.
	 * 
	 * @return
	 */
	public ResultPaserAbs getUpdatePOSHead() {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				DAOUtil.executeUpdate(conn, UPDATE_POS_DT, reset.getTimestamp("KSSJ"), reset.getTimestamp("JSSJ"), reset.getString("POSMC"),
						reset.getString("POSBH"));
				return false;
			}
		};
	}

	/**
	 * ��֤POS�޸ĺ��������POS����.
	 * 
	 * @param conn
	 * @param bindid
	 * @throws SQLException
	 */
	public void validatePOS(Connection conn, int bindid) throws SQLException {
		String message = DAOUtil.getStringOrNull(conn, "SELECT WLBH FROM BO_AKL_POS_BG_S WHERE POSSL<YSYSL AND BINDID=?", bindid);
		if (message != null) {
			throw new RuntimeException("���ϱ�ţ�" + message + "����ǰ������������ʹ��������Ӧ��С����ʹ��������");
		}
	}

}
