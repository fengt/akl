package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class FXSLYZ extends WorkFlowStepRTClassA {

	/**
	 * 查询物料编号和型号.
	 */
	private final static String QUERY_BODY_MATERIAL_INFO = "SELECT SPBH WLBH, XH FROM BO_AKL_WXB_RMAFXZX_S WHERE BINDID=?";
	private final static String QUERY_BODY_MATERIAL_INFO2 = "SELECT FXWLBH AS WBH,FXXH AS XH FROM BO_AKL_WXB_RMAFX_S WHERE BINDID=?";
	/**
	 * 查询物料信息中物料编号对应的型号.
	 */
	private final static String QUERY_MATERIAL_INFO = "SELECT XH FROM BO_AKL_WLXX WHERE WLBH=?";

	public FXSLYZ() {
	}

	public FXSLYZ(UserContext uc) {
		super(uc);
		setVersion("RMA返新退货流程v1.0");
		setProvider("刘松");
		setDescription("用于验证退回或返新不能为零或为空");
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		// 取得RMA返新退货申请表单数据
		Hashtable<String, String> h = null;
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		Hashtable<String, String> hft = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_RMAFX_P", bindid);
		Vector<Hashtable<String, String>> vft = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_RMAFX_S", bindid);
		int i = 1;
		if (vft != null) {
			Iterator<Hashtable<String, String>> it = vft.iterator();
			while (it.hasNext()) {
				h = (Hashtable<String, String>) it.next();
				String THXH = h.get("THXH") == null ? "" : h.get("THXH").toString();
				String FXXH = h.get("FXXH") == null ? "" : h.get("FXXH").toString();
				String FXSL = h.get("FXSL") == null ? "0" : h.get("FXSL").toString();
				String XSDJ = h.get("XSDJ") == null ? "0" : h.get("XSDJ").toString();
				if (h.get("LX").toString().equals("坏品返新") && Double.parseDouble(XSDJ) <= 0) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(),
							"单身第" + i + "行，退回件编码为 " + h.get("THJBM") + " 型号为 " + THXH + " 的返新商品无销售价格！请通知财务并维护！！！");
					return false;
				} else if (h.get("LX").toString().equals("坏品返新") && Double.parseDouble(h.get("TKJE").toString()) <= 0) {
					if (Integer.parseInt(FXSL) <= 0 || FXXH.equals("")) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(),
								"单身第" + i + "行，退回件编码为 " + h.get("THJBM") + " 型号为 " + THXH + " 的返新商品数量有为'0'或为空数据或无返新商品，且无退款金额！请检查！！！");
						return false;
					}
				} else if (h.get("LX").toString().equals("坏品返新") && Double.parseDouble(h.get("TKJE").toString()) > 0) {
					if (Integer.parseInt(FXSL) > 0) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(),
								"单身第" + i + "行，退回件编码为 " + h.get("THJBM") + " 型号为 " + THXH + " 的返新商品数量大于'0'且有'退款'！请检查！！！");
						return true;
					}
				} else if (h.get("LX").toString().equals("退回")) {
					if (Integer.parseInt(FXSL) > 0 || Double.parseDouble(h.get("TKJE").toString()) > 0) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(),
								"单身第" + i + "行，退回件编码为 " + h.get("THJBM") + " 型号为 " + THXH + " 的退回商品数量大于'0'或有'退款'！请检查！！！");
						return false;
					}
				}
				i++;
			}
		}

		return validateMaterialInfo(bindid);
	}

	/**
	 * 校验物料编号和型号是否对应.
	 * 
	 * @param bindid
	 * @return
	 */
	public boolean validateMaterialInfo(int bindid) {
		Connection conn = null;
		try {
			conn = DBSql.open();
			DAOUtil.executeQueryForParser(conn, QUERY_BODY_MATERIAL_INFO, validateMaterialInfoPaser, bindid);
			DAOUtil.executeQueryForParser(conn, QUERY_BODY_MATERIAL_INFO2, validateMaterialInfoPaser, bindid);
			DAOUtil.executeQueryForParser(conn, "SELECT THJBM,KHDH FROM BO_AKL_WXB_RMAFX_S WHERE BINDID=?", new DAOUtil.ResultPaser() {
				@Override
				public boolean parse(Connection conn, ResultSet reset) throws SQLException {
					String thjbm = reset.getString("THJBM");
					String khdh = reset.getString("KHDH");
					Integer count = DAOUtil.getIntOrNull(conn, "SELECT COUNT(*) FROM BO_AKL_WXB_RMAFX_S WHERE THJBM=? AND KHDH=?", thjbm, khdh);
					if (count != null && count > 1) {
						MessageQueue.getInstance().putMessage(getUserContext().getUID(), "条码号：" + thjbm + "，客户单号：" + khdh + " 存在重复，请检查！");
					}
					return true;
				}
			}, bindid);
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage());
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常请联系管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	private DAOUtil.ResultPaser validateMaterialInfoPaser = new DAOUtil.ResultPaser() {
		@Override
		public boolean parse(Connection conn, ResultSet reset) throws SQLException {
			String wlbh = reset.getString(1);
			String xh = reset.getString(2);

			/** 如果物料不需要进行返新. */
			if ((wlbh == null || "".equals(wlbh)) && (xh == null || "".equals(xh))) {
				return true;
			}

			String rXh = DAOUtil.getStringOrNull(conn, QUERY_MATERIAL_INFO, wlbh);
			if (!xh.equals(rXh)) {
				throw new RuntimeException("物料编号：" + wlbh + " 对应的型号：" + xh + " 错误， 应对应型号：" + rXh + " !");
			}
			return true;
		}

	};

}
