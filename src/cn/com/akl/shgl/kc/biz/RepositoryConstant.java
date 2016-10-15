package cn.com.akl.shgl.kc.biz;

public class RepositoryConstant {

	/** 故障条码_状态_在途 */
	public static final String GZTM_ZT_ZT = "070245";
	/** 故障条码_状态_丢失 */
	public static final String GZTM_ZT_DS = "070246";
	/** 故障条码_状态_在库 */
	public static final String GZTM_ZT_ZK = "070247";
	/** 故障条码_状态_已出 */
	public static final String GZTM_ZT_YC = "070248";
	/** 物料_状态_在途 */
	public static final String WL_ZT_ZT = "070245";// "070249";
	/** 物料_状态_在库 */
	public static final String WL_ZT_ZK = "070247";// "070250";

	/** 更新物料的剩余数量. */
	public static final String UPDATE_MATERIAL_REMAINING_NUMBER = "UPDATE BO_AKL_SHKC_S SET KWSL=KWSL+? WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND ZT=? AND ISNULL(KWSL,0)+?>=0";
	/** 查询物料在货位的数量. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER = "SELECT SUM(KWSL) FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND ZT=?";
	/** 查询物料在货位的锁定数量. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_LOCK = "SELECT SUM(SDSL) FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";
	/** 查询物料在客服仓库的剩余数量. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_INKF = "SELECT SUM(KWSL) FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND CKDM=? AND SX=? AND ZT=?";
	/** 查询物料在客服仓库的锁定数量. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_INKF_LOCK = "SELECT SUM(SDSL) FROM BO_AKL_SH_KCSK WHERE XMLB=? AND WLBH=? AND CKDM=? AND SX=?";
	/** 查询物料的所有数量. */
	public static final String QUERY_MATERIAL_REMAINING_NUMBER_ALL = "SELECT KWSL FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND HWDM=? AND SX=?";
	/** 查询是否有重复记录. */
	public static final String QUERY_RECORD_COUNT = "SELECT COUNT(*) FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=? AND ZT=?";
	/** 查询序列号. */
	public static final String QUERY_XLH_COUNT = "SELECT COUNT(*) FROM BO_AKL_SHKC_XLH_S WHERE XMLB=? AND WLBH=? AND PCH=? AND CKDM=? AND SX=? AND ZT=? AND GZTM=?";
	/** 查询序列号通过型号. */
	public static final String QUERY_XLH_COUNT_XH = "SELECT COUNT(*) FROM BO_AKL_SHKC_XLH_S WHERE XMLB=? AND XH=? AND GZTM=?";

	public static final String QUERY_KYWL = "SELECT * FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND CKDM=? AND SX=? AND ZT=?";
}
