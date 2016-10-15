package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class Insert_KC_YJ extends WorkFlowStepRTClassA {
	
	public Insert_KC_YJ(UserContext uc) {
		super(uc);
		setVersion("RMA收货流程v1.0");
		setProvider("刘松");
		setDescription("用于检测后修改库存明细表");
	}

	@Override
	public boolean execute() {
		// 取得RMA收货单检测结果
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int taskid = getParameter(this.PARAMETER_TASK_ID).toInt();
		String uid = getUserContext().getUID();

		Connection conn = null;

		boolean readyFlag = WorkflowTaskInstanceAPI.getInstance().checkCurrentAuditMenu(bindid, taskid, "检测完毕");

		if (readyFlag) {
			try {
				conn = DAOUtil.openConnectionTransaction();
				service(conn, bindid, uid);
				conn.commit();
			} catch (SQLException e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
				return false;
			} catch (Exception e) {
				DAOUtil.connectRollBack(conn);
				e.printStackTrace();
				MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请联系管理员!", true);
				return false;
			} finally {
				DBSql.close(conn, null, null);
			}
		}

		return true;
	}

	/**
	 * 更新库存明细表返货库房信息，坏品写入二捡坏品库，退货写入二捡退货库，并将返货空房商品数量更
	 * 
	 * @param conn
	 * @param bindid
	 * @param uid
	 * @throws SQLException
	 * @throws AWSSDKException
	 */
	public void service(Connection conn, int bindid, String uid) throws SQLException, AWSSDKException {

		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);

		if (v == null || v.size() == 0) {
			throw new RuntimeException("单身不允许为空！！！");
		}

		// 检测当前特批状态.
		if (ha.get("TP").toString().equals("等待特批")) {
			throw new RuntimeException("等待特批中不允许继续办理");
		}

		String pp = ha.get("PP");

		for (Hashtable<String, String> h : v) {
			// 获得检测结果判断是否入到返货库
			String JCJG = h.get("JCJG").toString();

			// 处理结果
			String cljg = h.get("CLLX");
			String tp = ha.get("TP");
			String tph = h.get("TPH");
			String bjtm = h.get("BJTM");
			String djbh = h.get("DJBH");

			/**
			 * 根据处理结果更新库存中的处理类型<br/>
			 * 1.根据处理结果 退回->退回，其他->坏品返新<br/>
			 * 2.检测结果为空 坏品返新<br/>
			 * 3.有特批，坏品返新<br/>
			 * */
			/** 非闪迪产品更新仓库 */
			if ("闪迪".equals(pp) || "006001".equals(pp)||"卓棒".equals(pp)||"006440".equals(pp)) {//添加品牌卓棒
				// 闪迪处理方式
				sdDealWay(conn, JCJG, cljg, tp, tph, bjtm, djbh);
			} else if ("罗技".equals(pp) || "006006".equals(pp)) {
				// 罗技处理方式
				ljDealWay(conn, JCJG, cljg, tp, tph, bjtm, djbh);
			} else {
				// 默认采用罗技处理方式
				ljDealWay(conn, JCJG, cljg, tp, tph, bjtm, djbh);
			}
		}
	}

	/**
	 * 罗技处理方式.
	 * 
	 * @param conn
	 * @param JCJG
	 * @param cljg
	 * @param tp
	 * @param tph
	 * @param bjtm
	 * @param djbh
	 * @throws SQLException
	 */
	private void ljDealWay(Connection conn, String JCJG, String cljg, String tp, String tph, String bjtm, String djbh) throws SQLException {
		String lx = "";

		if ("".equals(cljg)) {
			throw new RuntimeException("备件条码:" + bjtm + "，未填写处理结果!");
		}

		if ("更换".equals(cljg) || "坏品返新".equals(cljg)) {
			lx = "坏品返新";
		} else if ("退回".equals(cljg)) {
			lx = "退回";
		} else {
			throw new RuntimeException("备件条码:" + bjtm + "，处理结果：" + cljg + " 不可识别!");
		}

		// 获取返货库仓库代码
		String CKDM = "FH01";
		String CKMC = "售后返货库";
		DAOUtil.executeUpdate(conn, "update BO_AKL_RMA_KCMX set LX=?, CKDM=?, CKMC=? where  ZJM = ? and DDH = ?", lx, CKDM, CKMC, bjtm, djbh);
	}

	/**
	 * 闪迪处理方式.
	 * 
	 * @param conn
	 * @param JCJG
	 * @param cljg
	 * @param tp
	 * @param tph
	 * @param bjtm
	 * @param djbh
	 * @throws SQLException
	 */
	private void sdDealWay(Connection conn, String JCJG, String cljg, String tp, String tph, String bjtm, String djbh) throws SQLException {
		String jljccy = DAOUtil.getStringOrNull(conn, "select XLMC from BO_AKL_DATA_DICT_S where DLBM = ? AND XLMC=?", "046", JCJG);
		if (jljccy != null) {
			if (!JCJG.equals("Z02非罗技正品") && !JCJG.equals("NPF检测无故障")) {
				JCJG = "";
			}
		}

		// 闪迪处理方式.
		String lx = "";
		if ("退回".equals(cljg)) {
			lx = "退回";
		} else {
			lx = "坏品返新";
		}

		// 如果检测结果为空，则为 坏品返新
		if ("".equals(JCJG)) {
			lx = "坏品返新";
		}

		// 如果为特批，则为 坏品返新
		if (!"".equals(tph) || "整单特批".equals(tp)) {
			lx = "坏品返新";
		}

		// 更新处理方式
		DAOUtil.executeUpdate(conn, "update BO_AKL_RMA_KCMX set LX=? where ZJM = ? and DDH = ?", lx, bjtm, djbh);
	}

}
