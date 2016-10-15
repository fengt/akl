package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.ExcelDownFilterRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMA_SH_SPBM_EX extends ExcelDownFilterRTClassA {

	public RMA_SH_SPBM_EX(UserContext arg0) {
		super(arg0);
		setVersion("RMA收货流程v1.0");
		setProvider("刘松");
		setDescription("模板上传后处理，用亚昆SKU匹配系统物料，并带出对应的物料信息。");
	}

	@Override
	public HSSFWorkbook fixExcel(HSSFWorkbook hs) {
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;
		try {
			conn = DBSql.open();
			service(conn, bindid, uid);
			conn.commit();
		} catch (RuntimeException e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
		} catch (Exception e) {
			DAOUtil.connectRollBack(conn);
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
		} finally {
			DBSql.close(conn, null, null);
		}

		return null;
	}

	/**
	 * 遍历单身信息.
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 */
	public void service(Connection conn, int bindid, String uid) throws SQLException {
		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);

		if (v == null || v.size() == 0) {
			throw new RuntimeException("单身不允许为空！！！");
		}

		conn.setAutoCommit(false);
		for (Hashtable<String, String> h : v) {
			// 通过客户商品编码和客户编号查询亚昆商品编号.
			updateMaterialInfo(conn, bindid, h.get("XH"), ha.get("DJBH"), h.get("BJTM"), h.get("KHSPBH"), ha.get("KHBH"));
		}
	}

	/**
	 * 查找并更新物料单身对应的物料信息.
	 * 
	 * @param conn
	 * @param bindid
	 * @param djbh
	 * @param bjtm
	 * @param khspbh
	 * @param khbh
	 * @throws SQLException
	 */
	public void updateMaterialInfo(Connection conn, int bindid, String xh, String djbh, String bjtm, String khspbh, String khbh) throws SQLException {
		PreparedStatement state = null;
		ResultSet rs = null;

		try {
			/**
			 * 匹配方案：<br/>
			 * 1、如果客户商品编号为null，则采用型号对应物料编号. <br/>
			 * 2、如果客户商品编号不为null，则采用客户商品编号对应物料编号.<br/>
			 * 3、更新单身物料信息时,如果客户商品编号不为null因为XH未知所以未上传,需要更新XH字段.qjc.2015-02-16 
			 */
			String wlbh = null;
			if (khspbh == null || khspbh.equals("")) {
				wlbh = DAOUtil.getStringOrNull(conn, "SELECT WLBH FROM BO_AKL_WLXX WHERE XH=? AND HZBM=?", xh, "01065");
				if (wlbh == null) {
					throw new RuntimeException("备件条码：" + bjtm + " 型号：" + xh + " 对应的亚昆物料编号不存在！请检查！");
				}
			} else {
				wlbh = DAOUtil.getStringOrNull(conn, "SELECT YKSPSKU FROM BO_AKL_KHSPBMGL where KHSPSKU =? AND KHBM =?", khspbh, khbh);
				if (wlbh == null) {
					throw new RuntimeException("备件条码：" + bjtm + " 客户编号：" + khbh + " 对应的亚昆物料编号不存在！请检查！");
				}
			}

			state = conn.prepareStatement("SELECT WLBH, WLMC, XH FROM BO_AKL_WLXX WHERE WLBH=?");
			rs = DAOUtil.executeFillArgsAndQuery(conn, state, wlbh);
			if (rs.next()) {
				// 更新单身物料信息.
				DAOUtil.executeUpdate(conn,
						"UPDATE BO_AKL_WXB_XS_RMASH_S SET DJBH = ?, YKSKU = ?, SHID = ?, SPMC = ?,XH=? WHERE BJTM = ? AND bindid =? AND XH=?", djbh,
						rs.getString("WLBH"), khbh, rs.getString("WLMC"),rs.getString("XH"), bjtm, bindid, xh);
			} else {
				throw new RuntimeException("备件条码：" + bjtm + " 物料编号：" + wlbh + " 对应的亚昆物料信息不存在！请检查！");
			}
		} finally {
			DBSql.close(state, rs);
		}
	}
}
