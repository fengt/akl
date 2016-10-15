package cn.com.akl.shgl.qhsq.cnt;

public class QHSQCnt {

	/**
	 * 缺货记录状态
	 */
	public static final String zt0 = "076277";//申请中
	public static final String zt1 = "076278";//已申请
	public static final String zt2 = "076349";//已作废
	public static final String zt3 = "076356";//缺货中
	
	/**
	 * 配货方式
	 */
	public static final String bhlx0 = "073263";//单据引发补货
	public static final String bhlx1 = "073264";//特殊申请补货
	public static final String bhlx3 = "073265";//安全库存补货
	
	/**
	 * 其他
	 */
	public static final String low = "0";//低
	public static final String high = "1";//高
	public static final String is = "025000";//是
	public static final String no = "025001";//否
	
	
	public static final String QUERY_QHSQ_P_BHLX = "SELECT BHLX FROM BO_AKL_QHSQ_P WHERE BINDID=?";//补货类型
	public static final String QUERY_QHSQ_P_KFZX = "SELECT KFZX FROM BO_AKL_QHSQ_P WHERE BINDID=?";//客服中心
	public static final String QUERY_KFCKMC = "SELECT KFCKMC FROM BO_AKL_KFCK WHERE KFCKBM=?";//客服名称
	
	
	public static final String QUERY_HIGH = "SELECT 1 FROM BO_AKL_QHSQ_S WHERE BINDID=? AND YXJ='"+high+"'";//优先级：高
	public static final String QUERY_SFZCP = "SELECT 1 FROM BO_AKL_TSSQ_S WHERE BINDID=? AND SFZCP='"+is+"'";//是否主产品：是
	
	public static final String QUERY_QHSQ_S = "SELECT * FROM BO_AKL_QHSQ_S WHERE BINDID=?";//查询缺货申请子表
	public static final String QUERY_TSSQ_S = "SELECT * FROM BO_AKL_TSSQ_S WHERE BINDID=?";//查询特殊申请子表
	
	public static final String UPDATE_QHJL_ZT = "UPDATE BO_AKL_QHJL SET SQSJ=?,SQLY=?,ZT=?,SFJSTH=?,YXJ=? WHERE XMLB=? AND SXDH=? AND WLBH=? AND SX=? AND SXCPHH=? AND JFCPHH=?";//更新缺货记录表状态
	public static final String UPDATE_QHJL_ZT2 = "UPDATE BO_AKL_QHJL SET ZT=? WHERE BINDID=?";//更新(特殊申请)缺货记录状态
	public static final String DELETE_QHJL = "DELETE FROM BO_AKL_QHJL WHERE BINDID=?";//删除(特殊申请)的缺货记录
	
	public static final String UPDATE_QHSQ_P_ZT = "UPDATE BO_AKL_QHSQ_P SET ZT=? WHERE BINDID=?";//更新缺货记录主表状态
	public static final String UPDATE_QHSQ_S_ZT = "UPDATE BO_AKL_QHSQ_S SET ZT=? WHERE BINDID=?";//更新缺货记录子表状态
	public static final String UPDATE_TSSQ_S_ZT = "UPDATE BO_AKL_TSSQ_S SET ZT=? WHERE BINDID=?";//更新特殊申请子表状态
}
