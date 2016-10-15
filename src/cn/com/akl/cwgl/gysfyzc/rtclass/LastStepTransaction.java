package cn.com.akl.cwgl.gysfyzc.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class LastStepTransaction extends WorkFlowStepRTClassA {

	/**
	 * 查询开始日期
	 */
	private static final String QUERY_KSSJ = "SELECT KSSJ FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?";

	/**
	 * 更新POS状态
	 */
	private static final String UPDATE_ZT = "UPDATE BO_AKL_WXB_XS_POS_HEAD SET POSZT=? WHERE BINDID=?";

	/**
	 * 状态，执行中
	 */
	private static final String POSZT_ZXZ = "030003";

	/**
	 * 类别_ POS.
	 */
	private static final String LB_POS = "043028";
	/**
	 * POS类别_高价保.
	 */
	private static final String LB_PP = "043026";
	/**
	 * 类别_高容量.
	 */
	private static final String LB_DD = "043027";
	/**
	 * 类别_市场费用.
	 */
	private static final String LB_MDF = "043025";
	/**
	 * POS资金池中状态字段- 未抵扣.
	 */
	private static final String POSZJC_ZT_WDK = "1";

	public LastStepTransaction() {
		super();
	}

	public LastStepTransaction(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("最后一个节点激活事件");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();

			String lb = DAOUtil.getStringOrNull(conn, "SELECT LB FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);

			if (lb.equals(LB_POS)) {
				Date kssj = DAOUtil.getDate(conn, QUERY_KSSJ, bindid);
				Date now = Calendar.getInstance().getTime();
				if (kssj.getTime() < now.getTime()) {
					// 更新状态
					DAOUtil.executeUpdate(conn, UPDATE_ZT, POSZT_ZXZ, bindid);
				}
			} else {
				BigDecimal poszje = DAOUtil.getBigDecimalOrNull(conn, "SELECT SUM(POSSL*POSDJ) FROM BO_AKL_WXB_XS_POS_BODY WHERE BINDID=?", bindid);
				String posbh = DAOUtil.getStringOrNull(conn, "SELECT POSBH FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);
				String tpm = DAOUtil.getStringOrNull(conn, "SELECT TPM FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);
				String gysbh = DAOUtil.getStringOrNull(conn, "SELECT GYSBH FROM BO_AKL_WXB_XS_POS_HEAD WHERE BINDID=?", bindid);

				Hashtable<String, String> hashtable = new Hashtable<String, String>();
				hashtable.put("LX", lb);
				hashtable.put("FABH", posbh);
				hashtable.put("TPM", tpm);
				hashtable.put("GYSBH", gysbh);
				hashtable.put("ZT", POSZJC_ZT_WDK);
				hashtable.put("YSYJE", "0");
				hashtable.put("BXSQJE", "0");
				hashtable.put("YSJE", poszje.toString());
				hashtable.put("SSJE", "0");
				hashtable.put("POSJE", poszje.toString());

				// 插入资金池
				BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_POS_MXB", hashtable, bindid, getUserContext().getUID());
			}

			conn.commit();
			return true;
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现错误，请联系管理员");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

}
