package cn.com.akl.xsgl.xsdd.biz;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import cn.com.akl.dao.util.abs.ResultPaserAbs;
import cn.com.akl.xsgl.xsdd.constant.XSDDConstant;

public class ComputeBiz {

	/**
	 * 计算成本.
	 * 
	 * @param hashtable
	 * @param xszdj
	 * @param pccbj
	 * @param sl
	 */
	public void computeChengben(Hashtable<String, String> hashtable) {
		BigDecimal ddsl = getBigDecimal(hashtable, "DDSL");
		BigDecimal xsdj = getBigDecimal(hashtable, "XSDJ");
		BigDecimal pccbj = getBigDecimal(hashtable, "PCCBJ");
		BigDecimal flzcdj = getBigDecimal(hashtable, "FLZCD");
		BigDecimal flsl = getBigDecimal(hashtable, "FLSL");
		BigDecimal poszcdj = getBigDecimal(hashtable, "POSZCDJ");
		BigDecimal poszcsl = getBigDecimal(hashtable, "POSZCSL");
		String flfs = hashtable.get("FLFS");
		String flfah = hashtable.get("FLFAH");

		if (ddsl.doubleValue() < 1) {
			// 当数量小于等于0的时候，不予计算
			hashtable.put("FLHJ", "0");
			hashtable.put("CBZE", "0");
			hashtable.put("JJZE", "0");
			hashtable.put("YSJE", "0");
			hashtable.put("DDZJE", "0");
			hashtable.put("JJMLL", "0");
			return;
		}

		// 1-税率 从未税转成含税用
		BigDecimal slv = new BigDecimal(1);
		slv = slv.add(new BigDecimal(XSDDConstant.SL));
		slv = slv.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);

		// 订单总额(含税) = 订单数量*销售单价(含税)
		BigDecimal ddzje = xsdj.multiply(ddsl);
		ddzje = ddzje.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);

		// 返利支持金额 = 返利支持单价(含税)*返利支持数量
		BigDecimal flzcje = flzcdj.multiply(flsl);
		flzcje = flzcje.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
		// 净价总额(含税) = 销售单价(含税)*订单数量 - 返利支持单价(含税)*返利支持数量
		BigDecimal jjze = ddzje.subtract(flzcje);
		jjze = jjze.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);

		BigDecimal flhj = xsdj.subtract(flzcdj);

		// 成本总额(未税) = 批次成本(未税)*订单数量-POS支持单价(未税)*POS支持数量
		BigDecimal cbze = pccbj.multiply(ddsl);
		cbze = cbze.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
		BigDecimal poszcje = poszcsl.multiply(poszcdj);
		poszcje = poszcje.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
		cbze = cbze.subtract(poszcje);
		cbze = cbze.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);

		// 成本总额(含税) = 成本总额(未税)*(1+税率)
		BigDecimal cbzeHs = cbze.multiply(slv);
		cbzeHs = cbzeHs.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);

		// 净价毛利率 = (净价总额(含税)-成本总额(含税))/净价总额(含税)
		//
		BigDecimal jjmll = jjze.subtract(cbzeHs);
		if (jjze.doubleValue() != 0) {
			jjmll = jjmll.divide(jjze, XSDDConstant.FLOAT_SCALE + 2, XSDDConstant.ROUND_MODE);
			jjmll = jjmll.multiply(new BigDecimal(100));
		} else {
			jjmll = new BigDecimal(0);
		}
		jjmll = jjmll.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);

		if (XSDDConstant.FL_FLFS_XFL.equals(flfs) && !"".equals(flfah) && flfah!=null) {
			hashtable.put("YSJE", jjze.toString());
		} else {
			hashtable.put("YSJE", ddzje.toString());
		}

		hashtable.put("FLZCJ", flzcje.toString());
		hashtable.put("FLHJ", flhj.toString());
		hashtable.put("CBZE", cbzeHs.toString());
		hashtable.put("JJZE", jjze.toString());
		hashtable.put("DDZJE", ddzje.toString());
		hashtable.put("JJMLL", jjmll.toString());
	}

	/**
	 * 计算返利.
	 * 
	 * @param hashtable
	 */
	public void computeFL(Hashtable<String, String> hashtable) {

		String flfah = hashtable.get("FLFAH");
		if (flfah == null || flfah.trim().equals(""))
			return;

		BigDecimal flsl = getBigDecimal(hashtable, "FLSL");
		BigDecimal flzcdj = getBigDecimal(hashtable, "FLZCD");

		if (flsl == null || flzcdj == null) {
			return;
		}

		BigDecimal flzcje = flsl.multiply(flzcdj);
		flzcje = flzcje.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
		hashtable.put("FLZCJ", flzcje.toString());
	}

	/**
	 * 计算POS.
	 * 
	 * @param hashtable
	 */
	public void computePOS(Hashtable<String, String> hashtable) {
		// POS方案类型
		String posfalx = hashtable.get("POSFALX");
		String posid = hashtable.get("POSID");
		// POS支持数量
		BigDecimal poszcsl = getBigDecimal(hashtable, "POSZCSL");

		if (posid == null || posid.trim().equals("")) {
			return;
		}

		if (posfalx == null || posfalx.trim().equals("")) {
			return;
		}
		if (poszcsl == null) {
			return;
		}

		if (XSDDConstant.POS_FALX_FA.equals(posfalx)) {
			// POS类型为正常方案
			BigDecimal poszcdj = getBigDecimal(hashtable, "POSZCDJ");
			BigDecimal posje = poszcdj.multiply(poszcsl);
			posje = posje.setScale(XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
			hashtable.put("POSJE", posje.toString());

		} else if (XSDDConstant.POS_FALX_ZJC.equals(posfalx)) {
			// POS类型为资金池方案
			BigDecimal posje = getBigDecimal(hashtable, "POSJE");

			if (poszcsl.doubleValue() == 0) {
				hashtable.put("POSZCDJ", "0");
			} else {
				BigDecimal poszcdj = posje.divide(poszcsl, XSDDConstant.FLOAT_SCALE, XSDDConstant.ROUND_MODE);
				hashtable.put("POSZCDJ", poszcdj.toString());
			}
		}
	}

	/**
	 * 获取BigDecimal值.
	 * 
	 * @param hashtale
	 * @param field
	 * @return
	 */
	public BigDecimal getBigDecimal(Hashtable<String, String> hashtable, String field) {
		return parseNullToZero(hashtable.get(field));
	}

	/**
	 * 转换空成零.
	 * 
	 * @param value
	 * @return
	 */
	public BigDecimal parseNullToZero(String value) {
		if (value == null || value.trim().equals("")) {
			return new BigDecimal(0);
		} else {
			return new BigDecimal(value);
		}
	}

	/**
	 * 转换NULL.
	 * 
	 * @param str
	 * @return
	 */
	public String parseNull(String str) {
		return str == null ? "" : str;
	}

	/**
	 * 应收计算结果集处理.
	 * 
	 * @return
	 */
	public ResultPaserAbs getComputeReceivable() {
		return new ResultPaserAbs() {
			private ProcessRebateBiz rebateBiz = new ProcessRebateBiz();

			@Override
			public boolean parse(Connection conn, ResultSet reset) throws SQLException {
				String flfah = reset.getString("FLFAH");
				if (flfah == null || flfah.trim().equals(""))
					return true;

				// 获取返利方式
				String flfs = reset.getString("FLFS");
				BigDecimal ddzje = reset.getBigDecimal("DDZJE");
				BigDecimal jjze = reset.getBigDecimal("JJZE");
				int id = reset.getInt("ID");
				BigDecimal flzcj = reset.getBigDecimal("FLZCJ");
				// 处理返利
				rebateBiz.processFLFS(conn, flfs, ddzje, jjze, flzcj, id);
				return true;
			}
		};
	}

}
