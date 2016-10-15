package cn.com.akl.shgl.fjfj.cnt;

public class FJFJCnt {

	/**
	 * 物流、订单的状态
	 */
	public static final String wlzt = "071254";//已签收
	public static final String wlzt1 = "071252";//待处理
	public static final String zt1 = "069239";//待交付(单头)
	public static final String zt2 = "069243";//已检测(单身)
	
	public static final String djlx = "调拨";//单据类型（待发货）
	
	/**
	 * 记录标识：用于待发货两次插入记录时区分数据
	 */
	public static final String jlbz0 = "0";//（客服--检测）
	public static final String jlbz1 = "1";//（检测--客服）
	
	public static final String is = "025000";//是
	
	/**
	 * 物料属性
	 */
	public static final String sx0 = "066354";//坏品
	public static final String sx1 = "066358";//待取品
	
	/**
	 * 库存、序列号明细状态
	 */
	public static final String zt3 = "070245";//在途
	public static final String zt4 = "070247";//在库
	
	/**
	 * 复检返京单据状态（5、6节点状态暂无更新代码，待需要时添加）
	 */
	public static final String djzt0 = "069237";//待发货
	public static final String djzt1 = "069255";//待返货
	public static final String djzt2 = "069256";//已收货
	public static final String djzt3 = "069259";//已返货
//	public static final String djzt4 = "069257";//已二检
//	public static final String djzt5 = "069258";//已三检
	
	/**
	 * 检测处理结果（二检结论&&三检结论）
	 */
	public static final String jcjg0 = "067319";//有故障
	public static final String jcjg1 = "067320";//无故障
	public static final String jcjg2 = "067322";//工厂检测
	public static final String jcjg3 = "088343";//有故障收入
	public static final String jcjg4 = "088344";//无故障收入
	public static final String jcjg5 = "088345";//无故障退回
	
	/**
	 * 送修处理方式
	 */
	public static final String clfs0 = "064221";//复检换新
	public static final String clfs1 = "064222";//复检退回
	
	/**
	 * 基础信息
	 */
	public static final String QUERY_XMLB = "SELECT SSXM FROM BO_AKL_FJFJ_P WHERE BINDID=?";//项目类别
	public static final String QUERY_SRKF = "SELECT SRKF FROM BO_AKL_FJFJ_P WHERE BINDID=?";//收入库房
	public static final String QUERY_JCKF = "SELECT JCKF FROM BO_AKL_FJFJ_P WHERE BINDID=?";//寄出库房
	public static final String QUERY_SX_BINDID = "SELECT a.BINDID FROM BO_AKL_SX_P a,BO_AKL_FJFJ_P b WHERE a.SXDH=b.SXDH AND b.BINDID=?";//送修BINDID
	public static final String QUERY_WLZT = "SELECT WLZT FROM BO_AKL_DFH_P a,BO_AKL_FJFJ_P b WHERE a.DH=b.HPJCDH AND b.BINDID=? AND a.JLBZ=? AND b.ZT=?";//物流状态
	
	public static final String QUERY_PCH = "SELECT PCH FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND HWDM=? AND SX=? AND ZT=?";//库存批次号
	
	public static final String QUERY_WGZ = "SELECT COUNT(1) N FROM BO_AKL_FJFJ_S WHERE BINDID=? AND EJJL='"+jcjg1+"'";//无故障
	public static final String QUERY_GCJC = "SELECT COUNT(1) N FROM BO_AKL_FJFJ_S WHERE BINDID=? AND EJJL='"+jcjg2+"'";//工厂检测

	public static final String QUERY_HWXX = "SELECT CKDM,CKMC,QDM,DDM,KWDM FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//查询货位信息
	public static final String QUERY_FJFJ_P = "SELECT * FROM BO_AKL_FJFJ_P WHERE BINDID=?";//查询主表信息
	public static final String QUERY_FJFJ = "SELECT * FROM BO_AKL_FJFJ_S WHERE BINDID=?";//查询子表信息
	
	public static final String QUERY_isExistKCMX = "SELECT COUNT(1) n FROM BO_AKL_SHKC_S WHERE WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//查询是否已存在库存
	
	/**
	 * 复检单据
	 */
	public static final String UPDATE_FJFJ_P_ZT = "UPDATE BO_AKL_FJFJ_P SET ZT=? WHERE BINDID=?";//更新复检主表状态
	public static final String UPDATE_FJFJ_S_ZT = "UPDATE BO_AKL_FJFJ_S SET ZT=? WHERE BINDID=?";//更新复检子表状态
	public static final String UPDATE_FJFJ_S_HWDM2 = "UPDATE BO_AKL_FJFJ_S SET HWDM2=? WHERE BINDID=? AND CPLH=? AND KFGZDM=?";//更新返京复检子表(检测货位代码)
	public static final String UPDATE_FJFJ_S_SFFH = "UPDATE BO_AKL_FJFJ_S SET SFFH='"+is+"' WHERE BINDID=? AND CPLH=? AND KFGZDM=? AND SX=?";//更新返京复检子表(是否返货)
	
	/**
	 * 库存明细
	 */
	public static final String UPDATE_KCMX_DE = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)-? WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//减库存明细
	public static final String UPDATE_KCMX_IN = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+?,ZT='"+zt3+"' WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//加库存明细
	public static final String UPDATE_KCMX_ZT = "UPDATE BO_AKL_SHKC_S SET ZT='"+zt4+"' WHERE XMLB=? AND WLBH=? AND SX=? AND PCH=? AND HWDM=?";//更新库存明细状态
	
	public static final String UPDATE_KCMX = "UPDATE BO_AKL_SHKC_S SET KWSL=ISNULL(KWSL,0)+? WHERE WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//更新库存信息
	public static final String DELETE_KCMX = "DELETE FROM BO_AKL_SHKC_S WHERE XMLB=? AND WLBH=? AND SX=? AND HWDM=? AND PCH=? AND ZT=?";//删除在途物料
	
	/**
	 * 故障明细
	 */
	public static final String UPDATE_GZMX_ZT = "UPDATE BO_AKL_SHKC_XLH_S SET ZT=? WHERE XMLB=? AND WLBH=? AND PCH=? AND GZTM=?";//更新故障明细状态
	public static final String UPDATE_GZMX = "UPDATE BO_AKL_SHKC_XLH_S SET CKDM=?,CKMC=?,QDM=?,DDM=?,KWDM=?,HWDM=?,ZT='"+zt3+"' WHERE XMLB=? AND WLBH=? AND PCH=? AND GZTM=?";//更新故障明细仓库和状态
	
	/**
	 * 送修单
	 */
	public static final String UPDATE_SX_CLFS = "UPDATE BO_AKL_SX_S SET CLFS=?,SX=? WHERE BINDID=? AND WLBH=? AND GZTM=? AND PCH=? AND HWDM=? AND SX=?";//更新送修子表(处理方式)
	
	/**
	 * 库存明细、返京复检子表、序列号：属性更新
	 */
	public static final String UPDATE_KCMX_SX = "UPDATE BO_AKL_SHKC_S SET SX=? WHERE XMLB=? AND WLBH=? AND PCH=? AND HWDM=? AND SX=?";
	public static final String UPDATE_FJFJ_SX = "UPDATE BO_AKL_FJFJ_S SET SX=? WHERE ID=?";
	public static final String UPDATE_GZMX_SX = "UPDATE BO_AKL_SHKC_XLH_S SET SX=? WHERE XMLB=? AND WLBH=? AND PCH=? AND GZTM=? AND SX=?";
	
}
