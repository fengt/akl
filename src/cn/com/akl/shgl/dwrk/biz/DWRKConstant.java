package cn.com.akl.shgl.dwrk.biz;

public class DWRKConstant {

	/** 查询项目类型. */
	public static final String QUERY_DWRK_XMLX = "SELECT XMLX FROM BO_AKL_SH_DWRK_P WHERE BINDID=?";
	/** 收货仓库代码. */
	public static final String QUERY_DWRK_SHKFCKBM = "SELECT SHKFCKBM FROM BO_AKL_SH_DWRK_P WHERE BINDID=?";
	/** 对外入库单头. */
	public static final String QUERY_DWRK_HEAD = "SELECT * FROM BO_AKL_SH_DWRK_P WHERE BINDID=?";
	/** 对外入库单身. */
	public static final String QUERY_DWRK_BODY = "SELECT * FROM BO_AKL_SH_DWRK_S WHERE BINDID=?";

	/** 更新调拨表单单身的收货仓库编码 */
	public static final String UPDATE_DB_FORM_SHCKBM_AND_RKSL = "UPDATE BO_AKL_SH_DWRK_S SET RKHWDM=?, RKCKDM=?, RKCKMC=?, PCH=?, RKSL=?, SJRKSL=? WHERE ID=?";
}
