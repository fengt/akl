package cn.com.akl.shgl.fpkp.rtclass;

public class FpkpConstant {

	/** 发票状态：申请开票. */
	public static final String FPZT_SQKP = "078288";
	/** 发票状态：未开票. */
	public static final String FPZT_WKP = "078289";
	/** 发票状态：已开票. */
	public static final String FPZT_YKP = "078290";
	/** 发票状态：已签收. */
	public static final String FPZT_YQS = "078291";
	/** 发票状态：丢失. */
	public static final String FPZT_DS = "078292";
	/** 发票状态：在途. */
	public static final String FPZT_ZT = "078293";

	/** 发票签收状态：已签收 */
	public static final String QSZT_QS = "已签收";
	/** 发票签收状态：已丢失 */
	public static final String QSZT_DS = "丢失";

	/** 发票返回方式：内部物流至客服 */
	public static final String FPFHFS_NBWL = "079293";
	/** 发票返回方式：平邮至客户 */
	public static final String FPFHFS_PY = "079294";
	/** 发票返回方式：快递至客户 */
	public static final String FPFHFS_KD = "079295";

	/** 查询发票开票的单身. */
	public static final String QUERY_FPKP_FORM_BODY = "SELECT * FROM BO_AKL_FPKP_S WHERE BINDID=?";
	/** 查询单身中含有此发票申请单号的数量. */
	public static final String QUERY_FPKP_FORM_BODY_FPSQDH = "SELECT BINDID FROM BO_AKL_FPKP_S WHERE FPSQDH=? AND ISEND=0 AND BINDID<>?";
	/** 发票状态. */
	public static final String QUERY_FPSQ_FPZT = "SELECT FPZT FROM BO_AKL_FPSQ WHERE FPSQDH=?";
	/** 更新送修单开票状态. */
	public static final String UPDATE_SXD_KPZT = "UPDATE BO_AKL_SX_P SET SFKP=? WHERE SXDH=?";
	/** 更新开票申请的状态. */
	public static final String UPDATE_KPSQ_KPZT = "UPDATE BO_AKL_FPSQ SET FPZT=? WHERE FPSQDH=?";

}
