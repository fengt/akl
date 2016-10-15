package cn.com.akl.rmagl.fxck.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.ccgl.xsck.constant.XSCKConstant;
import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class StepNo1BeforeSave extends WorkFlowStepRTClassA {

	// 查询单身所有物料信息
	private static final String QUERY_All_WLXX = "SELECT CHCK,KHDH,FXKHSPBM,FXWLBH,FXXH,FXSL,XSDJ,ZDCBWS FROM BO_AKL_WXB_RMAFX_P a INNER JOIN BO_AKL_WXB_RMAFX_S b ON a.BINDID=b.BINDID AND b.LX='坏品返新' AND a.FHDH=?";
	// 查询仓库的可用物料信息
	private static final String QUERY_KY_WLXX = "SELECT s.ID, s.WLBH, s.GG, s.JLDW, s.WLMC, s.PCH, s.KWSL, s.HWDM, s.CKDM, s.CKMC, s.QDM, s.DDM, s.KWDM, s.SX FROM BO_AKL_KC_KCHZ_P p, BO_AKL_KC_KCMX_S s WHERE s.WLBH=? AND s.CKMC=? AND p.ZT=? AND p.WLBH=s.WLBH AND p.PCH=s.PCH ORDER BY p.RKRQ";

	public StepNo1BeforeSave() {
		super();
	}

	public StepNo1BeforeSave(UserContext arg0) {
		super(arg0);
		setVersion("1.0.0");
		setDescription("表单保存后事件：拆分库存");

		/**
		 * 存在问题：如果此流程拆分过后，未点办理，另一个出库流程将此库存拿走，就存在了冲突。
		 */
	}

	@Override
	public boolean execute() {
		String tablename = getParameter(PARAMETER_TABLE_NAME).toString();
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Hashtable<String, String> hashtable = getParameter(PARAMETER_FORM_DATA).toHashtable();

		if ("BO_AKL_CKD_HEAD".equals(tablename)) {

			Connection conn = null;
			try {
				conn = DAOUtil.openConnectionTransaction();

				String rmafxdh = hashtable.get("RMAFXDH");

				if (rmafxdh == null || rmafxdh.trim().length() == 0) {
					BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CKD_BODY", bindid);
					conn.commit();
					return true;
				}

				String rmafxdh2 = DAOUtil.getStringOrNull(conn, "SELECT RMAFXDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?", bindid);
				if (rmafxdh.equals(rmafxdh2)) {
					return true;
				}

				BOInstanceAPI.getInstance().removeProcessInstanceBOData(conn, "BO_AKL_CKD_BODY", bindid);
				queryAllWlxx(conn, bindid, uid, rmafxdh);

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
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "拆分出现问题，请联系管理员！", true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}

	/**
	 * 查询单身的所有物料，并且从仓库抓取物料
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void queryAllWlxx(Connection conn, int bindid, String uid, String rmafxdh) throws SQLException, AWSSDKException {

		PreparedStatement ps = conn.prepareStatement(QUERY_All_WLXX);
		ResultSet reset = DAOUtil.executeFillArgsAndQuery(conn, ps, rmafxdh);

		while (reset.next()) {
			// 查询库位，根据时间排序
			// String wlbh = reset.getString("WLBH");
			String wlbh = reset.getString("FXWLBH");
			String ckid = reset.getString("CHCK");
			// String pch = reset.getString("PCH");
			// String wlmc = reset.getString("WLMC");
			// String wlgg = reset.getString("WLGG");
			// String xh = reset.getString("XH");
			String xh = reset.getString("FXXH");
			// String jldw = reset.getString("JLDW");
			// String khspbh = reset.getString("KHSPBH");
			String khspbh = reset.getString("FXKHSPBM");
			// BigDecimal tj = reset.getBigDecimal("TJ");
			// BigDecimal zl = reset.getBigDecimal("ZL");
			// String kcsl = reset.getString("KC");
			BigDecimal tj = DAOUtil.getBigDecimalOrNull(conn, "SELECT TJ FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);
			BigDecimal zl = DAOUtil.getBigDecimalOrNull(conn, "SELECT ZL FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);

			// int sl = reset.getInt("DDSL");
			int sl = reset.getInt("FXSL");

			PreparedStatement kywlxxPs = conn.prepareStatement(QUERY_KY_WLXX);
			ResultSet kywlxxReset = DAOUtil.executeFillArgsAndQuery(conn, kywlxxPs, wlbh, ckid, XSCKConstant.PC_ZT_ZC);

			// 分货结束标记 false为已结束
			boolean overFlag = true;
			while (overFlag && kywlxxReset.next()) {

				int haveSl = kywlxxReset.getInt("KWSL");
				String hwdm = kywlxxReset.getString("HWDM");

				if (haveSl != 0) {
					sl -= haveSl;

					// 预备转存入库单身中
					Hashtable<String, String> hashtable = new Hashtable<String, String>();
					hashtable.put("RMAFXDH", rmafxdh);
					hashtable.put("WLH", wlbh);
					hashtable.put("XH", xh);
					hashtable.put("FHKFBH", kywlxxReset.getString("CKDM"));
					hashtable.put("FHKFMC", kywlxxReset.getString("CKMC"));
					hashtable.put("GG", kywlxxReset.getString("GG"));
					hashtable.put("KWBH", hwdm);
					hashtable.put("WLMC", kywlxxReset.getString("WLMC"));
					hashtable.put("JLDW", kywlxxReset.getString("JLDW"));
					hashtable.put("PC", kywlxxReset.getString("PCH"));
					hashtable.put("TJ", tj.toString());
					hashtable.put("ZL", zl.toString());
					hashtable.put("KHCPBM", khspbh);
					hashtable.put("KCSL", kywlxxReset.getString("KWSL"));

					// 品牌、产品P/N、属性
					String ppid = DAOUtil.getStringOrNull(conn, "SELECT PPID FROM BO_AKL_WLXX WHERE WLBH=?", wlbh);
					String pp = DAOUtil.getStringOrNull(conn, "SELECT XLMC FROM BO_AKL_DATA_DICT_S WHERE   DLBM = 006 AND XLBM=?", ppid);
					String sx = kywlxxReset.getString("SX");

					hashtable.put("PP", pp == null ? ppid : pp);
					hashtable.put("SX", sx == null ? "" : sx);

					if (sl <= 0) {
						hashtable.put("SL", String.valueOf(haveSl + sl));
						hashtable.put("SJSL", String.valueOf(haveSl + sl));
						hashtable.put("SJTJ", tj.multiply(new BigDecimal(haveSl + sl)).toString());
						hashtable.put("SJZL", zl.multiply(new BigDecimal(haveSl + sl)).toString());
						// 插入数据
						BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hashtable, bindid, uid);
						overFlag = false;
					} else {
						hashtable.put("SL", String.valueOf(haveSl));
						hashtable.put("SJSL", String.valueOf(haveSl));
						hashtable.put("SJTJ", tj.multiply(new BigDecimal(haveSl)).toString());
						hashtable.put("SJZL", zl.multiply(new BigDecimal(haveSl)).toString());
						// 插入数据
						BOInstanceAPI.getInstance().createBOData(conn, "BO_AKL_CKD_BODY", hashtable, bindid, uid);
					}
				}
			}
			if (sl > 0) {
				throw new RuntimeException("返新单：" + rmafxdh + "中物料编号为" + wlbh + "的物料数量不足，可能此物料还有在途数量。");
			}
		}
	}

}
