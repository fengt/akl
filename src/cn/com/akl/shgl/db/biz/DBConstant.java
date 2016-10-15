package cn.com.akl.shgl.db.biz;

public class DBConstant {

	/** 查询调拨单头信息. */
	public static final String QUERY_DB_FORM_HEAD = "SELECT * FROM BO_AKL_DB_P WHERE BINDID=?";
	/** 查询单身序列号信息. */
	public static final String QUERY_DB_FORM_XLH = "SELECT * FROM BO_AKL_DB_XLH_S WHERE BINDID=?";
	/** 查询调拨单身信息 */
	public static final String QUERY_DB_FORM_BODY = "SELECT * FROM BO_AKL_DB_S WHERE BINDID=?";
	/** 查询调拨单身汇总信息 */
	public static final String QUERY_DB_FORM_HZ_BODY = "SELECT * FROM BO_AKL_DB_HZ_S WHERE BINDID=?";
	/** 查询调拨表单的项目类型 */
	public static final String QUERY_DB_FORM_XMLX = "SELECT XMLX FROM BO_AKL_DB_P WHERE BINDID=?";
	/** 查询调拨表单的收货仓库编码 */
	public static final String QUERY_DB_FORM_SHCKBM = "SELECT SHKFCKBM FROM BO_AKL_DB_P WHERE BINDID=?";
	/** 查询调拨表单的发货仓库编码 */
	public static final String QUERY_DB_FORM_FHCKBM = "SELECT FHKFCKBM FROM BO_AKL_DB_P WHERE BINDID=?";
	/** 查询调拨单号. */
	public static final String QUERY_DB_FORM_DBDH = "SELECT DBDH FROM BO_AKL_DB_P WHERE BINDID=?";
    /** 查询调拨类型. */
    public static final String QUERY_DB_FORM_DBLX = "SELECT DBLX FROM BO_AKL_DB_P WHERE BINDID=?";
	/** 查询调拨装箱方式. */
	public static final String QUERY_DB_FORM_SFGJPCHZX = "SELECT SFGJPCHZX FROM BO_AKL_DB_P WHERE BINDID=?";
	/** 查询调拨单身，用于插入装箱单.*/
	public static final String QUERY_DB_FORM_BODY_GROUP_ZXD = "SELECT WLBH,WLMC CPMC,CPSX,'' as PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY WLBH,WLMC,CPSX,CPLX";
	/** 查询调拨单身，用于插入装箱单.（多个PCH）*/
	public static final String QUERY_DB_FORM_BODY_GROUP_ZXD_PCH = "SELECT WLBH,WLMC CPMC,CPSX,PCH,SUM(SJCKSL) SL,CPLX,SUM(SJCKSL) ZXSL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY PCH,WLBH,WLMC,CPSX,CPLX";
	/** 根据物料号和批次号进行分组. */
	public static final String QUERY_ZXD_GROUP_WLHPCH = "SELECT WLBH, CPMC, PCH, SUM(ISNULL(ZXSL, 0)) SL FROM BO_AKL_ZXD_S WHERE BINDID=? GROUP BY WLBH,PCH,CPMC";
	/** 根据物料号和批次号对装箱单进行分组. */
	public static final String QUERY_DB_FORM_BODY_GROUP_WLH = "SELECT WLBH, WLMC CPMC, '' AS PCH, SUM(SJCKSL) SL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY WLBH,WLMC";
	/** 根据物料号对装箱单进行分组. */
	public static final String QUERY_DB_FORM_BODY_GROUP_WLHPCH = "SELECT WLBH, WLMC CPMC, PCH, SUM(SJCKSL) SL FROM BO_AKL_DB_S WHERE BINDID=? GROUP BY PCH,WLBH,WLMC";

	/** 更新调拨表单单身的收货仓库编码 */
	public static final String UPDATE_DB_FORM_SHCKBM_AND_RKSL = "UPDATE BO_AKL_DB_S SET RKCKDM=?, RKCKMC=?, RKHWDM=?, RKSL=?, SJRKSL=? WHERE ID=?";

	/** 调拨类型，季度返京. */
	public static final String DBLX_JDFJ = "季度返京";

}
