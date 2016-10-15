package cn.com.akl.ccgl.xsck.qscy.rtclass;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.util.DAOUtil;
import cn.com.akl.xsgl.xsdd.biz.ProcessPOSBiz;
import cn.com.akl.xsgl.xsdd.biz.ProcessRebateBiz;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;

public class StepNo2Transaction extends WorkFlowStepRTClassA {

	/** 查询出库单的出库类型 */
	private static final String QUERY_CKD_CKLX = "SELECT CKLX FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/** 查询销售订单单身记录. */
	private static final String QUERY_XSDD_BODY_WLXX_RECORD = "SELECT XSDJ, DDSL FROM BO_AKL_WXB_XSDD_BODY WHERE DDID=? AND WLBH=?";
	/** 查询签收差异的差异类型. */
	private static final String QUERY_QSCY_CYLX = "SELECT CYLX FROM BO_AKL_QSCY_P WHERE BINDID=?";
	/** 查询出库单号. */
	private static final String QUERY_QCCY_CKDH = "SELECT CKDH FROM BO_AKL_QSCY_P WHERE BINDID=?";
	/** 查询出库单号的bindid. */
	private static final String QUERY_QSCY_CKDH_BINDID = "SELECT BINDID FROM BO_AKL_CKD_HEAD WHERE CKDH=?";
	/** 查询出库单对应的销售订单号. */
	private static final String QUERY_CKD_XSDDH_FOR_CKDH = "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE CKDH=?";
	/** 查询BINDID对应的销售单号. */
	private static final String QUERY_CKD_XSDDH_FOR_BINDID = "SELECT XSDDH FROM BO_AKL_CKD_HEAD WHERE BINDID=?";
	/** 查询签收差异单身中差异数量不为0的数据. */
	private static final String QUERY_QSCY_BODY = "SELECT WLH, CYXH, SL, CYSL, QSSL FROM BO_AKL_QSCY_S WHERE BINDID=? AND CYSL<>0";
	/** 查询销售订单单身. */
	private static final String QUERY_XSDD_DS = "SELECT DDSL, WLBH, POSID, POSFALX, POSJE, POSZCSL, FLFAH, FLSL FROM BO_AKL_WXB_XSDD_BODY WHERE BINDID=? AND WLBH=?";
	/** 查询销售订单BINDID. */
	private static final String QUERY_XSDD_BINDID = "SELECT BINDID FROM BO_AKL_WXB_XSDD_BODY WHERE DDID=?";
	/** 签收差异单身汇总. */
	private static final String QUERY_QSCY_BODY_SUM = "SELECT WLH, CYXH, SUM(CYSL) CYSL FROM BO_AKL_QSCY_S WHERE BINDID=? GROUP BY WLH, CYXH";

	public StepNo2Transaction() {
		super();
	}

	public StepNo2Transaction(UserContext arg0) {
		super(arg0);
	}

	@Override
	public boolean execute() {
		int bindid = getParameter(PARAMETER_INSTANCE_ID).toInt();
		Integer parentBindid = getParameter(PARAMETER_PARENT_WORKFLOW_INSTANCE_ID).toInt();

		Connection conn = null;
		try {
			conn = DAOUtil.openConnectionTransaction();
			String cylx = DAOUtil.getStringOrNull(conn, QUERY_QSCY_CYLX, bindid);
			if (cylx == null || cylx.trim().equals("")) {
				throw new RuntimeException("差异类型未填写!");
			}

			int ckbindid;
			String xsddh;
			boolean backFlag = false;

			String[] cylxBack = QSCYConstants.cylxBack;
			// 拒收、破损、取消订单回填库存
			for (String string : cylxBack) {
				if (string.equals(cylx.trim())) {
					backFlag = true;
					break;
				}
			}

			if (!backFlag) {
				if (parentBindid == 0) {
					// 更新签收录入数量
					String ckdh = DAOUtil.getStringOrNull(conn, QUERY_QCCY_CKDH, bindid);
					ckbindid = DAOUtil.getIntOrNull(conn, QUERY_QSCY_CKDH_BINDID, ckdh);
					xsddh = DAOUtil.getStringOrNull(conn, QUERY_CKD_XSDDH_FOR_CKDH, ckdh);

					DAOUtil.executeQueryForParser(conn, QUERY_QSCY_BODY, getLrslPaser(ckbindid), bindid);
				} else {
					ckbindid = parentBindid;
					xsddh = DAOUtil.getStringOrNull(conn, QUERY_CKD_XSDDH_FOR_BINDID, ckbindid);
				}

				/** 检查是否是销售出库. */
				String cklx = DAOUtil.getStringOrNull(conn, QUERY_CKD_CKLX, ckbindid);
				cklx = "1";
				if ("0".equals(cklx) || "销售出库".equals(cklx)) {
					// 签收数量存在差异时，更新数量.
					// 更新应收.
					DAOUtil.executeQueryForParser(conn, QUERY_QSCY_BODY, new DAOUtil.ResultPaser[] { getYsPaser(xsddh) }, bindid);

					// 查询销售订单的BINDID
					String xsddbindid = DAOUtil.getStringOrNull(conn, QUERY_XSDD_BINDID, xsddh);

					// 更新返利和POS
					DAOUtil.executeQueryForParser(conn, QUERY_QSCY_BODY_SUM, getRollBackPosAndFlPaser(xsddbindid), bindid);
				}
			}

			conn.commit();
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), e.getMessage(), true);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			DAOUtil.connectRollBack(conn);
			MessageQueue.getInstance().putMessage(getUserContext().getUID(), "后台出现异常，请检查控制台!", true);
			return false;
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 获取签收录入中的物料并更新录入数量.
	 * 
	 * @param ckbindid
	 * @return
	 */
	public ResultPaserAbs getLrslPaser(final int ckbindid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				int cysl = reset.getInt("CYSL");
				String wlbh = reset.getString("WLH");
				int count = DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_QSD_S SET SSSL=SSSL-? WHERE BINDID=? AND WLH=?", cysl, ckbindid, wlbh);
				if (count == 1) {
					return true;
				} else {
					throw new RuntimeException("更新签收录入数量失败");
				}
			}
		};
	}

	/**
	 * 回退POS和返利数量
	 * 
	 * @return
	 */
	public ResultPaserAbs getRollBackPosAndFlPaser(final String xsddbindid) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				// 当差异数量小于1时，不进行回滚.
				if (reset.getInt("CYSL") < 1) {
					return true;
				} else {
					DAOUtil.executeQueryForParser(conn, QUERY_XSDD_DS, getSalesOrderPaser(reset.getInt("CYSL")), xsddbindid, reset.getString("WLH"));
					return true;
				}
			}
		};
	}

	/**
	 * 回退订单中的POS以及返利.
	 * 
	 * @param sl
	 * @return
	 */
	public ResultPaserAbs getSalesOrderPaser(final int sl) {
		return new ResultPaserAbs() {
			private ProcessPOSBiz posBiz = new ProcessPOSBiz();
			private ProcessRebateBiz flBiz = new ProcessRebateBiz();

			private int cksl;

			@Override
			public void init(Connection conn) throws SQLException {
				cksl = sl;
			}

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLBH");
				String posid = reset.getString("POSID");
				String posfalx = reset.getString("POSFALX");
				int ddsl = reset.getInt("DDSL");

				if (posid != null && !"".equals(posid.trim()) && posfalx.trim().equals(XSDDConstant.POS_FALX_FA)) {
					BigDecimal posje = reset.getBigDecimal("POSJE");
					int poszcsl = reset.getInt("POSZCSL");

					// 如果当前回滚的POS数量大于当前记录中所含的POS数量，那么就找到下一条记录.
					if (ddsl >= cksl) {
						// 回退POS，如果出库当前所欠数量大于POS数量，那么就回滚POS数量，否则回滚当前所欠数量。
						if (cksl > poszcsl) {
							posBiz.rollBackPOS(conn, posid, posfalx, posje, poszcsl, wlbh);
						} else {
							posBiz.rollBackPOS(conn, posid, posfalx, posje, cksl, wlbh);
						}
					} else {
						// 回退POS
						posBiz.rollBackPOS(conn, posid, posfalx, posje, poszcsl, wlbh);
					}
				}

				String flfah = reset.getString("FLFAH");
				if (flfah != null && !"".equals(flfah.trim())) {
					int flsl = reset.getInt("FLSL");
					if (ddsl >= cksl) {
						// 回退返利，如果返利数量大于所欠数量，那么回退所欠数量.
						if (flsl >= cksl) {
							flBiz.rollbackFL(conn, flfah, cksl, wlbh);
						} else {
							flBiz.rollbackFL(conn, flfah, flsl, wlbh);
						}
					} else {
						flBiz.rollbackFL(conn, flfah, flsl, wlbh);
					}
				}

				if (ddsl >= cksl) {
					cksl = 0;
				} else {
					cksl = cksl - ddsl;
				}

				return cksl > 0;
			}

			@Override
			public void destory(Connection conn) throws SQLException {
				if (cksl > 0) {
					throw new RuntimeException("反写销售订单中的POS与返利时，出现差异数量大于订单数量的情况!");
				}
			}
		};
	}

	/**
	 * 获取销售订单中的物料并且更新应收.
	 * 
	 * @param xsddh
	 * @return
	 */
	public ResultPaserAbs getYsPaser(final String xsddh) {
		return new ResultPaserAbs() {
			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String wlbh = reset.getString("WLH");
				String xh = reset.getString("CYXH");
				int sl = reset.getInt("CYSL");
				if (sl < 1) {
					return true;
				}

				BigDecimal priceTotal = new BigDecimal(0);

				PreparedStatement ps = null;
				ResultSet resultSet = null;

				try {
					ps = conn.prepareStatement(QUERY_XSDD_BODY_WLXX_RECORD);
					resultSet = DAOUtil.executeFillArgsAndQuery(conn, ps, xsddh, wlbh);
					while (resultSet.next() && sl > 0) {
						int sjsl = resultSet.getInt("DDSL");
						int updateSl;

						if (sl < sjsl) {
							updateSl = sl;
						} else {
							updateSl = sjsl;
						}
						sl -= sjsl;

						BigDecimal xsdj = resultSet.getBigDecimal("XSDJ");
						BigDecimal xszj = xsdj.multiply(new BigDecimal(updateSl));

						// 计算价格总和
						priceTotal = priceTotal.add(xszj);
					}

					if (sl > 0) {
						throw new RuntimeException("销售订单中此型号：" + xh + " 的物料并没有卖出" + reset.getInt("SL"));
					} else {
						DAOUtil.executeUpdate(conn, "UPDATE BO_AKL_YS SET YSJE=YSJE-? WHERE XSDH=?", priceTotal, xsddh);
					}
					return true;
				} finally {
					DBSql.close(ps, resultSet);
				}
			}
		};
	}

	public String parseNull(String obj) {
		if (obj == null) {
			return "";
		} else {
			return obj;
		}
	}
}
