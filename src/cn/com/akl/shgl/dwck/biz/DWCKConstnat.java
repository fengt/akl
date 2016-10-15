package cn.com.akl.shgl.dwck.biz;

public class DWCKConstnat {

	/** 查询项目类型. */
	public static final String QUERY_DWCK_XMLX = "SELECT XMLX FROM BO_AKL_SH_DWCK_P WHERE BINDID=?";
	/** 查询是否根据批次号装箱. */
	public static final String QUERY_DWCK_SFGJPCHZX = "SELECT SFGJPCHZX FROM BO_AKL_SH_DWCK_P WHERE BINDID=?";
	/** 对外出库单头. */
	public static final String QUERY_DWCK_HEAD = "SELECT * FROM BO_AKL_SH_DWCK_P WHERE BINDID=?";
	/** 对外出库单身. */
	public static final String QUERY_DWCK_BODY = "SELECT * FROM BO_AKL_SH_DWCK_S WHERE BINDID=?";
	/** 对外出库单身汇总. */
	public static final String QUERY_DWCK_HZ_BODY = "SELECT * FROM BO_AKL_SH_DWCK_HZ_S WHERE BINDID=?";
	/** 汇总出库单身，填充装箱单. */
	public static final String QUERY_DWCK_BODY_GROUP_ZXD_PCH = "SELECT WLBH,WLMC CPMC,CPSX,PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,WLMC,CPSX,CPLX,PCH";
	/** 汇总出库单身，填充装箱单.(多个批次号.) */
	public static final String QUERY_DWCK_BODY_GROUP_ZXD = "SELECT WLBH,WLMC CPMC,CPSX,'' AS PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,WLMC,CPSX,CPLX";

	/** 查询对外出库单的物料号和批次号汇总. */
	public static final String QUERY_DWCK_GROUP_WLH = "SELECT WLBH, WLMC CPMC, '' AS PCH, SUM(SJCKSL) SL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,WLMC";
	/** 查询对外出库单的物料号汇总. */
	public static final String QUERY_DWCK_GROUP_WLHPCH = "SELECT WLBH, PCH, WLMC CPMC, SUM(SJCKSL) SL FROM BO_AKL_SH_DWCK_S WHERE BINDID=? GROUP BY WLBH,PCH,WLMC";
	/** 查询装箱单的物料号和批次号汇总. */
	public static final String QUERY_ZXD_GROUP_WLHPCH = "SELECT WLBH, CPMC, PCH, SUM(ISNULL(ZXSL, 0)) SL FROM BO_AKL_ZXD_S WHERE BINDID=? GROUP BY WLBH,PCH,CPMC";
}
