package cn.com.akl.rmash.rtclass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import cn.com.akl.util.DAOUtil;
import cn.com.akl.util.WorkFlowUtil;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;

public class RMA_SH_viladate extends WorkFlowStepRTClassA {

	public RMA_SH_viladate(UserContext uc) {
		super(uc);
		setVersion("RMA收货流程v1.0");
		setProvider("刘松");
		setDescription("用于验证商品是否存在");
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		String uid = getUserContext().getUID();
		Connection conn = null;
		try {
			conn = DBSql.open();
			int stepNo = WorkFlowUtil.getProcessInstanceStepNo(conn, bindid);
			switch (stepNo) {
				case 1:
					validateStep1(conn, bindid, uid);
					break;
				case 5:
					validateStep5(conn, bindid, uid);
					break;
			}
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			MessageQueue.getInstance().putMessage(uid, "后台出现错误，请联系系统管理员!");
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	public void validateStep1(Connection conn, int bindid, String uid) throws SQLException {
		Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);

		if (v == null || v.size() == 0) {
			throw new RuntimeException("单身不允许为空！！！");
		}
		if (v != null) {
			int row = 0;
			for (Hashtable<String, String> h : v) {
				// 查询本单单身中备件条码号是否重复.
				int iD = DAOUtil.getIntOrNull(conn, "select count(ID) ID from BO_AKL_WXB_XS_RMASH_S where BJTM = ? and bindid = ?", h.get("BJTM"),
						bindid);
				if (iD > 1) {
					MessageQueue.getInstance().putMessage(getUserContext().getUID(), "单身第" + row + "行，备件条码：" + h.get("BJTM") + "重复，请检查！");
				}

				// 查询型号是否为空.
				if (h.get("XH").toString().equals("")) {
					throw new RuntimeException("单身第" + row + "行，备件条码：" + h.get("BJTM") + " 没有对应的亚昆物料型号！请检查！");
				}

				// 查询物料表中是否有此物料的物料信息.
				int id = DAOUtil.getIntOrNull(conn, "select count(ID) ID from BO_AKL_WLXX where WLBH = ?", h.get("YKSKU"));
				if (id == 0) {
					throw new RuntimeException("单身第" + row + "行，备件条码：" + h.get("BJTM") + " 物料信息表中未出现过在此商品！请检查是否已经维护了此物料！");
				}
				row++;
			}
		}
	}

	public void validateStep5(Connection conn, int bindid, String uid) throws SQLException {
		Hashtable<String, String> ha = BOInstanceAPI.getInstance().getBOData("BO_AKL_WXB_XS_RMASH_P", bindid);
		String pp = ha.get("PP");

		if ("罗技".equals(pp) || "006006".equals(pp)) {
			Vector<Hashtable<String, String>> v = BOInstanceAPI.getInstance().getBODatas("BO_AKL_WXB_XS_RMASH_S", bindid);
			for (Hashtable<String, String> hashtable : v) {
				String bjtm = hashtable.get("BJTM");
				String pjsfqq = hashtable.get("PJSFQQ");
				String jcjg = hashtable.get("JCJG");
				String cllx = hashtable.get("CLLX");
				String xiangh = hashtable.get("XIANGH");
				if (pjsfqq == null || "".equals(pjsfqq)) {
					throw new RuntimeException("备件条码：" + bjtm + "，配件是否齐全未填写！");
				}
				if (jcjg == null || "".equals(jcjg)) {
					throw new RuntimeException("备件条码：" + bjtm + "，故障原因未填写！");
				}
				if (cllx == null || "".equals(cllx)) {
					throw new RuntimeException("备件条码：" + bjtm + "，处理结果未填写！");
				}
				if (xiangh == null || "".equals(xiangh)) {
					throw new RuntimeException("备件条码：" + bjtm + "，箱号未填写！");
				}
			}
		}
	}

}
